/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.cast.ipa.callgraph.AstCallGraph.AstFakeRoot;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dataflow.graph.BitVectorFramework;
import com.ibm.wala.dataflow.graph.BitVectorSolver;
import com.ibm.wala.fixpoint.BitVectorVariable;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.impl.GraphInverter;
import com.ibm.wala.util.graph.traverse.DFS;
import com.ibm.wala.util.intset.BitVector;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableMapping;
import com.ibm.wala.util.intset.OrdinalSetMapping;

import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.VNA00JFixInformation;
import edu.illinois.keshmesh.detector.util.AnalysisUtils;
import edu.illinois.keshmesh.util.Logger;
import edu.illinois.keshmesh.util.Modes;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class VNA00JBugDetector extends BugPatternDetector {

	VNA00JIntermediateResults intermediateResults = new VNA00JIntermediateResults();

	private final OrdinalSetMapping<InstructionInfo> globalValues = MutableMapping.make();

	private final Map<CGNode, CGNodeInfo> cgNodeInfoMap = new HashMap<CGNode, CGNodeInfo>();

	private Collection<IClass> threadSafeClasses;

	@Override
	public IntermediateResults getIntermediateResults() {
		return intermediateResults;
	}

	private boolean isThreadSafe(IClass klass) {
		if (threadSafeClasses == null) {
			populateThreadSafeClasses();
			intermediateResults.setThreadSafeClasses(threadSafeClasses);
		}
		return threadSafeClasses.contains(klass);
	}

	@Override
	public BugInstances performAnalysis(IJavaProject javaProject, BasicAnalysisData basicAnalysisData) {
		this.javaProject = javaProject;
		this.basicAnalysisData = basicAnalysisData;
		Iterator<CGNode> cgNodesIter = basicAnalysisData.callGraph.iterator();
		while (cgNodesIter.hasNext()) {
			CGNode cgNode = cgNodesIter.next();
			Logger.log("CGNode: " + cgNode.getIR());
		}
		collectUnprotectedInstructionsThatMayAccessUnsafelySharedFields();

		BitVectorSolver<CGNode> bitVectorSolver = propagateUnprotectedInstructionThatMayAccessUnsafelySharedFields();
		Collection<InstructionInfo> instructionInfosToReport = getInstructionsToReport(bitVectorSolver);

		return createBugInstances(instructionInfosToReport);
	}

	//TODO: All derived classes of a thread safe class should be considered thread safe as well. 
	private void populateThreadSafeClasses() {
		threadSafeClasses = new HashSet<IClass>();
		Iterator<CGNode> cgNodesIter = basicAnalysisData.callGraph.iterator();
		while (cgNodesIter.hasNext()) {
			CGNode cgNode = cgNodesIter.next();
			if (!isIgnoredClass(cgNode.getMethod().getDeclaringClass()) && belongsToThreadSafeClass(cgNode)) {
				threadSafeClasses.add(cgNode.getMethod().getDeclaringClass());
			}
		}
	}

	private boolean belongsToThreadSafeClass(CGNode cgNode) {
		IClass declaringClass = cgNode.getMethod().getDeclaringClass();
		if (implementsRunnableInterface(declaringClass) || extendsThreadClass(declaringClass)) {
			return true;
		} else {
			return AnalysisUtils.contains(javaProject, cgNode, new InstructionFilter() {
				@Override
				public boolean accept(InstructionInfo instructionInfo) {
					return instructionInfo.getInstruction() instanceof SSAMonitorInstruction;
				}
			});
		}
	}

	private boolean extendsThreadClass(IClass klass) {
		IClass superclass = klass.getSuperclass();
		if (superclass == null) {
			return false;
		}
		if (isThreadClass(superclass)) {
			return true;
		}
		return extendsThreadClass(superclass);
	}

	private boolean implementsRunnableInterface(IClass klass) {
		for (IClass implementedInterface : klass.getAllImplementedInterfaces()) {
			if (isRunnableInterface(implementedInterface)) {
				return true;
			}
		}
		return false;
	}

	private boolean isRunnableInterface(IClass interfaceClass) {
		return AnalysisUtils.getEnclosingNonanonymousClassName(interfaceClass.getName()).equals("java.lang.Runnable");
	}

	private boolean isThreadClass(IClass klass) {
		return AnalysisUtils.getEnclosingNonanonymousClassName(klass.getName()).equals("java.lang.Thread");
	}

	/**
	 * 
	 * This method is based on
	 * LCK06JBugDetector#populateSynchronizedBlocksForNode.
	 * 
	 * @param synchronizedBlocks
	 * @param cgNode
	 * @param synchronizedBlockKind
	 */
	private void populateSynchronizedBlocksForNode(Collection<InstructionInfo> synchronizedBlocks, final CGNode cgNode) {
		AnalysisUtils.collect(javaProject, synchronizedBlocks, cgNode, new InstructionFilter() {

			@Override
			public boolean accept(InstructionInfo instructionInfo) {
				SSAInstruction instruction = instructionInfo.getInstruction();
				if (AnalysisUtils.isMonitorEnter(instruction)) {
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * This method is based on
	 * LCK06JBugDetector#populateUnsafeModifyingStaticFieldsInstructionsMap.
	 * 
	 * For every method of the input program, this method computes the set of
	 * instructions that are not protected by a synchronized block but may
	 * access a field that is shared unsafely. A field that is neither volatile
	 * nor final is shared unsafely.
	 * 
	 * The resulting instructions will be used as initial values of the data
	 * flow problem corresponding to the interprocedural part of the detector.
	 * 
	 * FIXME: This method is doing too much work and needs to be split into
	 * smaller methods.
	 */
	private void collectUnprotectedInstructionsThatMayAccessUnsafelySharedFields() {
		Map<CGNode, Collection<InstructionInfo>> intermediateMapOfUnprotectedInstructions = null;

		if (!Modes.isInProductionMode()) {
			intermediateMapOfUnprotectedInstructions = new HashMap<CGNode, Collection<InstructionInfo>>();
		}

		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			CGNode cgNode = cgNodesIterator.next();
			BitVector bitVector = new BitVector();
			Collection<InstructionInfo> synchronizedBlocks = new HashSet<InstructionInfo>();
			Collection<InstructionInfo> unprotectedInstructionsThatMayAccessUnsafelySharedFields = new HashSet<InstructionInfo>();
			if (canContainUnprotectedInstructions(cgNode.getMethod())) {
				Collection<InstructionInfo> instructionsThatMayAccessUnsafelySharedFields = getInstructionsThatMayAccessUnsafelySharedFields(cgNode);
				populateSynchronizedBlocksForNode(synchronizedBlocks, cgNode);
				for (InstructionInfo instructionThatMayAccessesUnsafelySharedFields : instructionsThatMayAccessUnsafelySharedFields) {
					if (!AnalysisUtils.isProtectedByAnySynchronizedBlock(synchronizedBlocks, instructionThatMayAccessesUnsafelySharedFields)) {
						unprotectedInstructionsThatMayAccessUnsafelySharedFields.add(instructionThatMayAccessesUnsafelySharedFields);
					}
				}
				for (InstructionInfo instructionThatMayAccessesUnsafelySharedFields : instructionsThatMayAccessUnsafelySharedFields) {
					Logger.log("UNSAFE ACCESS: " + instructionThatMayAccessesUnsafelySharedFields);
				}
				for (InstructionInfo unprotectedInstruction : unprotectedInstructionsThatMayAccessUnsafelySharedFields) {
					bitVector.set(globalValues.add(unprotectedInstruction));
					Logger.log("UNPROTECTED INSTRUCTION: " + unprotectedInstruction);
				}
			}
			cgNodeInfoMap.put(cgNode, new CGNodeInfo(synchronizedBlocks, bitVector));

			if (!Modes.isInProductionMode() && intermediateMapOfUnprotectedInstructions != null) {
				intermediateMapOfUnprotectedInstructions.put(cgNode, unprotectedInstructionsThatMayAccessUnsafelySharedFields);
			}
		}

		if (!Modes.isInProductionMode()) {
			intermediateResults.setUnprotectedInstructionsThatMayAccessUnsafelySharedFields(intermediateMapOfUnprotectedInstructions);
		}
	}

	private boolean canContainUnprotectedInstructions(IMethod method) {
		return !method.isSynchronized() && !isIgnoredClass(method.getDeclaringClass()) && !isInitializationMethod(method);
	}

	private boolean isInitializationMethod(IMethod method) {
		String methodName = method.getName().toString();
		return methodName.equals("<init>") || methodName.equals("<clinit>");
	}

	/**
	 * 
	 * See LCK06JBugDetector#isIgnoredClass
	 * 
	 * @param klass
	 * @return
	 */
	private boolean isIgnoredClass(IClass klass) {
		return AnalysisUtils.isJDKClass(klass);
	}

	/**
	 * 
	 * See LCK06JBugDetector#getModifyingStaticFieldsInstructions
	 * 
	 * @param cgNode
	 * @return
	 */
	private Collection<InstructionInfo> getInstructionsThatMayAccessUnsafelySharedFields(CGNode cgNode) {
		Collection<InstructionInfo> instructionsThatMayAccessUnsafelySharedFields = new HashSet<InstructionInfo>();
		AnalysisUtils.collect(javaProject, instructionsThatMayAccessUnsafelySharedFields, cgNode, new InstructionFilter() {

			@Override
			public boolean accept(InstructionInfo instructionInfo) {
				return mayAccessUnsafelySharedFields(instructionInfo);
			}
		});
		return instructionsThatMayAccessUnsafelySharedFields;
	}

	/**
	 * 
	 * See LCK06JBugDetector#canModifyStaticField.
	 * 
	 * @param instructionInfo
	 * @return
	 */
	private boolean mayAccessUnsafelySharedFields(InstructionInfo instructionInfo) {
		SSAInstruction ssaInstruction = instructionInfo.getInstruction();
		if (ssaInstruction instanceof SSAFieldAccessInstruction) {
			SSAFieldAccessInstruction fieldAccessInstruction = (SSAFieldAccessInstruction) ssaInstruction;
			IField accessedField = basicAnalysisData.classHierarchy.resolveField(fieldAccessInstruction.getDeclaredField());
			if (accessedField.isVolatile()) {
				return false;
			}
			if (fieldAccessInstruction.isStatic()) {
				IClass declaringClass = accessedField.getDeclaringClass();
				if (isThreadSafe(declaringClass)) {
					return true;
				}
			} else {
				PointerKey pointerForValueNumber = getPointerForValueNumber(instructionInfo.getCGNode(), fieldAccessInstruction.getRef());
				if (basicAnalysisData.basicHeapGraph.getSuccNodeCount(pointerForValueNumber) != 1) {
					throw new AssertionError("Expected that value number pointer points to a single object: " + instructionInfo);
				}
				InstanceKey pointedInstance = (InstanceKey) basicAnalysisData.basicHeapGraph.getSuccNodes(pointerForValueNumber).next();
				Graph<Object> invertedHeapGraph = GraphInverter.invert(basicAnalysisData.basicHeapGraph);
				Set<Object> reachingHeapGraphNodes = DFS.getReachableNodes(invertedHeapGraph, Arrays.asList(pointedInstance));
				return doesContainExternalPointer(reachingHeapGraphNodes, instructionInfo.getCGNode())
						&& (isThreadSafe(pointedInstance.getConcreteType()) || doesContainThreadSafeFieldPointer(reachingHeapGraphNodes));
			}
		}
		return false;
	}

	private boolean doesContainExternalPointer(Set<Object> reachingHeapGraphNodes, CGNode localCGNode) {
		for (Object node : reachingHeapGraphNodes) {
			if (node instanceof LocalPointerKey && isExternalPointer((LocalPointerKey) node, localCGNode)) {
				return true;
			}
		}
		return false;
	}

	private boolean doesContainThreadSafeFieldPointer(Set<Object> reachingNodes) {
		for (Object node : reachingNodes) {
			if (node instanceof InstanceFieldPointerKey && isThreadSafeFieldPointer((InstanceFieldPointerKey) node)) {
				return true;
			}
		}
		return false;
	}

	private boolean isExternalPointer(LocalPointerKey pointerKey, CGNode cgNode) {
		CGNode pointerNode = pointerKey.getNode();
		return !isInitializationMethod(pointerNode.getMethod()) && pointerNode != cgNode;
	}

	private boolean isThreadSafeFieldPointer(InstanceFieldPointerKey pointerKey) {
		return isThreadSafe(pointerKey.getInstanceKey().getConcreteType());
	}

	/**
	 * This method is based on
	 * LCK06JBugDetector#propagateUnsafeModifyingStaticFieldsInstructions. The
	 * only difference is in the first line when the actual transfer function is
	 * instantiated.
	 * 
	 * @return
	 */
	private BitVectorSolver<CGNode> propagateUnprotectedInstructionThatMayAccessUnsafelySharedFields() {
		VNA00JTransferFunctionProvider transferFunctions = new VNA00JTransferFunctionProvider(javaProject, basicAnalysisData.callGraph, cgNodeInfoMap, basicAnalysisData.classHierarchy);

		BitVectorFramework<CGNode, InstructionInfo> bitVectorFramework = new BitVectorFramework<CGNode, InstructionInfo>(GraphInverter.invert(basicAnalysisData.callGraph), transferFunctions,
				globalValues);

		BitVectorSolver<CGNode> bitVectorSolver = new BitVectorSolver<CGNode>(bitVectorFramework) {
			@Override
			protected BitVectorVariable makeNodeVariable(CGNode cgNode, boolean IN) {
				BitVectorVariable nodeBitVectorVariable = new BitVectorVariable();
				nodeBitVectorVariable.addAll(cgNodeInfoMap.get(cgNode).getBitVector());
				return nodeBitVectorVariable;
			}
		};
		try {
			bitVectorSolver.solve(null);
		} catch (CancelException ex) {
			throw new RuntimeException("Bitvector solver was stopped", ex);
		}
		return bitVectorSolver;
	}

	/**
	 * This method is based on LCK06JBugDetector#getActuallyUnsafeInstructions.
	 * 
	 * This methods returns a collection of instructions from the callees that
	 * we'll report to the user. After we propagate the unprotected instructions
	 * along the call graph, we look into the list of instructions propagated to
	 * each method. Some of these instructions are immediate instructions of the
	 * method itself, while others have originated from other methods. Let's say
	 * we'd like to report the problematic instructions in method m. These
	 * instructions will be the unprotected instructions that we have computed
	 * for method m plus some of the method invocations in m. The method
	 * invocations that we include in our collection have the following
	 * criteria: the callee has propagated some unprotected instructions and the
	 * method invocation is not inside a synchronized block of the caller.
	 * 
	 * TODO: Update the comment with how we handle local variables passed
	 * through method invocations.
	 * 
	 * @param bitVectorSolver
	 * @param unsafeSynchronizedBlock
	 * @return
	 */
	private Collection<InstructionInfo> getInstructionsToReport(final BitVectorSolver<CGNode> bitVectorSolver, final CGNode cgNode) {
		final Collection<InstructionInfo> unprotectedInstructionsThatMayAccessUnsafelySharedFields = new HashSet<InstructionInfo>();
		final Collection<InstructionInfo> synchronizedBlocks = new HashSet<InstructionInfo>();
		populateSynchronizedBlocksForNode(synchronizedBlocks, cgNode);
		IR ir = cgNode.getIR();
		if (ir == null) {
			return unprotectedInstructionsThatMayAccessUnsafelySharedFields; //should not really be null here
		}

		//Add the initial set of the given CGNode.
		CGNodeInfo cgNodeInfo = cgNodeInfoMap.get(cgNode);
		cgNodeInfo.getBitVectorContents(unprotectedInstructionsThatMayAccessUnsafelySharedFields, globalValues);

		//Add the instructions propagated from the callees.
		AnalysisUtils.collect(javaProject, new HashSet<InstructionInfo>(), cgNode, new InstructionFilter() {
			@Override
			public boolean accept(InstructionInfo instructionInfo) {
				SSAInstruction instruction = instructionInfo.getInstruction();
				if (instruction instanceof SSAAbstractInvokeInstruction) {
					//FIXME: The following condition is similar to the one in edu.illinois.keshmesh.detector.VNA00JTransferFunctionProvider.getEdgeTransferFunction(CGNode, CGNode). We should consider removing this duplication.
					if (!AnalysisUtils.isProtectedByAnySynchronizedBlock(synchronizedBlocks, instructionInfo) && AnalysisUtils.doesAllowPropagation(instructionInfo, basicAnalysisData.classHierarchy)) {
						SSAAbstractInvokeInstruction invokeInstruction = (SSAAbstractInvokeInstruction) instruction;
						// Add the unprotected instructions of the methods that are the targets of the invocation instruction. 
						Set<CGNode> possibleTargets = basicAnalysisData.callGraph.getPossibleTargets(cgNode, invokeInstruction.getCallSite());
						for (CGNode possibleTarget : possibleTargets) {
							// Add unprotected instructions coming from callees.
							if (hasPropagatedUnprotectedInstructions(bitVectorSolver, possibleTarget)) {
								unprotectedInstructionsThatMayAccessUnsafelySharedFields.add(instructionInfo);
								break;
							}
						}
					}
				}
				return false;
			}
		});
		return unprotectedInstructionsThatMayAccessUnsafelySharedFields;
	}

	/**
	 * This method is based on LCK06JBugDetector#addSolverResults.
	 * 
	 * @param results
	 * @param bitVectorSolver
	 * @param cgNode
	 */
	private boolean hasPropagatedUnprotectedInstructions(BitVectorSolver<CGNode> bitVectorSolver, CGNode cgNode) {
		IntSet value = bitVectorSolver.getIn(cgNode).getValue();
		if (value != null) {
			IntIterator intIterator = value.intIterator();
			if (intIterator.hasNext()) {
				return true;
			}
		}
		return false;
	}

	private Collection<InstructionInfo> getInstructionsToReport(BitVectorSolver<CGNode> bitVectorSolver) {
		Iterator<CGNode> cgNodesIter = basicAnalysisData.callGraph.iterator();

		final Collection<InstructionInfo> unprotectedInstructionsThatMayAccessUnsafelySharedFields = new HashSet<InstructionInfo>();
		while (cgNodesIter.hasNext()) {
			CGNode cgNode = cgNodesIter.next();
			if (!isIgnoredClass(cgNode.getMethod().getDeclaringClass())) {
				unprotectedInstructionsThatMayAccessUnsafelySharedFields.addAll(getInstructionsToReport(bitVectorSolver, cgNode));
			}
		}
		return unprotectedInstructionsThatMayAccessUnsafelySharedFields;
	}

	private BugInstances createBugInstances(Collection<InstructionInfo> instructionInfosToReport) {
		BugInstances bugInstances = new BugInstances();
		for (InstructionInfo instructionInfo : instructionInfosToReport) {
			if (!isInFakeRootMethod(instructionInfo)) {
				bugInstances.add(new BugInstance(BugPatterns.VNA00J, instructionInfo.getPosition(), new VNA00JFixInformation()));
			}
		}
		return bugInstances;
	}

	private boolean isInFakeRootMethod(InstructionInfo instructionInfo) {
		return instructionInfo.getCGNode().getMethod() instanceof AstFakeRoot;
	}

}
