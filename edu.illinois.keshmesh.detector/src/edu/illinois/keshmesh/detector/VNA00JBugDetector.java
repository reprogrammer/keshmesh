/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

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
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.impl.GraphInverter;
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

	@Override
	public IntermediateResults getIntermediateResults() {
		return intermediateResults;
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
		Collection<IClass> threadSafeClasses = getThreadSafeClasses();
		intermediateResults.setThreadSafeClasses(threadSafeClasses);

		populateMapOfunsafeInstructionsThatAccessUnprotectedFields();

		BitVectorSolver<CGNode> bitVectorSolver = propagateInstructionThatAccessesUnprotectedFields();
		Collection<InstructionInfo> instructionInfosToReport = getInstructionsToReport(bitVectorSolver);

		return createBugInstances(instructionInfosToReport);
	}

	private Collection<IClass> getThreadSafeClasses() {
		Collection<IClass> threadSafeClasses = new HashSet<IClass>();
		Iterator<CGNode> cgNodesIter = basicAnalysisData.callGraph.iterator();
		while (cgNodesIter.hasNext()) {
			CGNode cgNode = cgNodesIter.next();
			if (!isIgnoredClass(cgNode.getMethod().getDeclaringClass()) && isThreadSafe(cgNode)) {
				threadSafeClasses.add(cgNode.getMethod().getDeclaringClass());
			}
		}
		return threadSafeClasses;
	}

	private boolean isThreadSafe(CGNode cgNode) {
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
	 * FIXME: This method is doing too much work and needs to be split into
	 * smaller methods.
	 */
	private void populateMapOfunsafeInstructionsThatAccessUnprotectedFields() {
		Map<CGNode, Collection<InstructionInfo>> intermediateMapOfUnsafeInstructions = null;

		if (!Modes.isInProductionMode()) {
			intermediateMapOfUnsafeInstructions = new HashMap<CGNode, Collection<InstructionInfo>>();
		}

		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			CGNode cgNode = cgNodesIterator.next();
			BitVector bitVector = new BitVector();
			Collection<InstructionInfo> synchronizedBlocks = new HashSet<InstructionInfo>();
			Collection<InstructionInfo> unsafeInstructionsThatAccessUnprotectedFields = new HashSet<InstructionInfo>();
			if (canContainUnsafeAccesses(cgNode.getMethod())) {
				Collection<InstructionInfo> instructionsThatAccessUnprotectedFields = getInstructionsThatAccessUnprotectedFields(cgNode);
				populateSynchronizedBlocksForNode(synchronizedBlocks, cgNode);
				for (InstructionInfo instructionThatAccessesUnprotectedFields : instructionsThatAccessUnprotectedFields) {
					if (!AnalysisUtils.isProtectedByAnySynchronizedBlock(synchronizedBlocks, instructionThatAccessesUnprotectedFields)) {
						unsafeInstructionsThatAccessUnprotectedFields.add(instructionThatAccessesUnprotectedFields);
					}
				}
				for (InstructionInfo instructionThatAccessesUnprotectedFields : instructionsThatAccessUnprotectedFields) {
					Logger.log("MODIFY: " + instructionThatAccessesUnprotectedFields);
				}
				for (InstructionInfo unsafeInstruction : unsafeInstructionsThatAccessUnprotectedFields) {
					bitVector.set(globalValues.add(unsafeInstruction));
					Logger.log("UNSAFE INSTRUCTION: " + unsafeInstruction);
				}
			}
			cgNodeInfoMap.put(cgNode, new CGNodeInfo(synchronizedBlocks, bitVector));

			if (!Modes.isInProductionMode() && intermediateMapOfUnsafeInstructions != null) {
				intermediateMapOfUnsafeInstructions.put(cgNode, unsafeInstructionsThatAccessUnprotectedFields);
			}
		}

		if (!Modes.isInProductionMode()) {
			intermediateResults.setUnsafeInstructionsThatAccessUnprotectedFields(intermediateMapOfUnsafeInstructions);
		}
	}

	private boolean canContainUnsafeAccesses(IMethod method) {
		return !method.isSynchronized() && !isIgnoredClass(method.getDeclaringClass());
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
	private Collection<InstructionInfo> getInstructionsThatAccessUnprotectedFields(CGNode cgNode) {
		Collection<InstructionInfo> instructionsThatAccessUnprotectedFields = new HashSet<InstructionInfo>();
		IR ir = cgNode.getIR();
		if (ir == null) {
			return instructionsThatAccessUnprotectedFields;
		}
		final DefUse defUse = new DefUse(ir);

		AnalysisUtils.collect(javaProject, instructionsThatAccessUnprotectedFields, cgNode, new InstructionFilter() {

			@Override
			public boolean accept(InstructionInfo instructionInfo) {
				return doesAccessUnprotectedField(defUse, instructionInfo.getInstruction());
			}
		});
		return instructionsThatAccessUnprotectedFields;
	}

	/**
	 * 
	 * See LCK06JBugDetector#canModifyStaticField.
	 * 
	 * @param defUse
	 * @param ssaInstruction
	 * @return
	 */
	private boolean doesAccessUnprotectedField(final DefUse defUse, SSAInstruction ssaInstruction) {
		for (int i = 0; i < ssaInstruction.getNumberOfUses(); i++) {
			SSAInstruction defInstruction = defUse.getDef(ssaInstruction.getUse(i));
			if (defInstruction instanceof SSAGetInstruction && !isProtected((SSAFieldAccessInstruction) defInstruction)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param fieldAccessInstruction
	 * @return true if the changes to the given field are visible to other
	 *         threads.
	 */
	private boolean isProtected(SSAFieldAccessInstruction fieldAccessInstruction) {
		return isFinal(fieldAccessInstruction) || isVolatile(fieldAccessInstruction);
	}

	/**
	 * 
	 * See LCK06JBugDetector#isStaticNonFinal.
	 * 
	 * @param fieldAccessInstruction
	 * @return
	 */
	private boolean isFinal(SSAFieldAccessInstruction fieldAccessInstruction) {
		IField accessedField = basicAnalysisData.classHierarchy.resolveField(fieldAccessInstruction.getDeclaredField());
		return accessedField.isFinal();
	}

	private boolean isVolatile(SSAFieldAccessInstruction fieldAccessInstruction) {
		IField accessedField = basicAnalysisData.classHierarchy.resolveField(fieldAccessInstruction.getDeclaredField());
		return accessedField.isVolatile();
	}

	/**
	 * This method is based on
	 * LCK06JBugDetector#propagateUnsafeModifyingStaticFieldsInstructions. The
	 * only difference is in the first line when the actual transfer function is
	 * instantiated.
	 * 
	 * @return
	 */
	private BitVectorSolver<CGNode> propagateInstructionThatAccessesUnprotectedFields() {
		VNA00JTransferFunctionProvider transferFunctions = new VNA00JTransferFunctionProvider(javaProject, basicAnalysisData.callGraph, cgNodeInfoMap);

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
	 * This methods returns a collection of instructions from the that we'll
	 * report to the user. After we propagate the unprotected access along the
	 * call graph, we look into the list of instructions propagated to each
	 * method. Some of these instructions are immediate instructions of the
	 * method itself, while others have originated from other methods. Let's say
	 * we'd like to report the problematic instructions in method m. These
	 * instructions will be the unprotected instructions that we computed for
	 * method m plus some of the method invocations in m. The method invocations
	 * that we include in our collection have the following criteria: the callee
	 * has propagated some unprotected accesses and the method invocation is not
	 * inside a synchronized block of the caller.
	 * 
	 * @param bitVectorSolver
	 * @param unsafeSynchronizedBlock
	 * @return
	 */
	private Collection<InstructionInfo> getInstructionsToReport(final BitVectorSolver<CGNode> bitVectorSolver, final CGNode cgNode) {
		final Collection<InstructionInfo> instructionsThatAccessUnprotectedFields = new HashSet<InstructionInfo>();
		final Collection<InstructionInfo> synchronizedBlocks = new HashSet<InstructionInfo>();
		populateSynchronizedBlocksForNode(synchronizedBlocks, cgNode);
		IR ir = cgNode.getIR();
		if (ir == null) {
			return instructionsThatAccessUnprotectedFields; //should not really be null here
		}

		//Add the initial set of the given CGNode.
		CGNodeInfo cgNodeInfo = cgNodeInfoMap.get(cgNode);
		cgNodeInfo.getBitVectorContents(instructionsThatAccessUnprotectedFields, globalValues);

		//Add the instructions propagated from 
		AnalysisUtils.collect(javaProject, new HashSet<InstructionInfo>(), cgNode, new InstructionFilter() {
			@Override
			public boolean accept(InstructionInfo instructionInfo) {
				SSAInstruction instruction = instructionInfo.getInstruction();
				if (instruction instanceof SSAAbstractInvokeInstruction) {
					//FIXME: The following condition is similar to the one in edu.illinois.keshmesh.detector.VNA00JTransferFunctionProvider.getEdgeTransferFunction(CGNode, CGNode). We should consider removing this duplication.
					if (!AnalysisUtils.isProtectedByAnySynchronizedBlock(synchronizedBlocks, instructionInfo) && !AnalysisUtils.areAllArgumentsLocal(instructionInfo)) {
						SSAAbstractInvokeInstruction invokeInstruction = (SSAAbstractInvokeInstruction) instruction;
						// Add the unsafe accesses of the methods that are the targets of the invocation instruction. 
						Set<CGNode> possibleTargets = basicAnalysisData.callGraph.getPossibleTargets(cgNode, invokeInstruction.getCallSite());
						for (CGNode possibleTarget : possibleTargets) {
							// Add unsafe operations coming from callees.
							if (hasPropagatedUnprotectedAccesses(bitVectorSolver, possibleTarget)) {
								instructionsThatAccessUnprotectedFields.add(instructionInfo);
								break;
							}
						}
					}
				}
				return false;
			}
		});
		return instructionsThatAccessUnprotectedFields;
	}

	/**
	 * This method is based on LCK06JBugDetector#addSolverResults.
	 * 
	 * @param results
	 * @param bitVectorSolver
	 * @param cgNode
	 */
	private boolean hasPropagatedUnprotectedAccesses(BitVectorSolver<CGNode> bitVectorSolver, CGNode cgNode) {
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

		final Collection<InstructionInfo> instructionsThatAccessUnprotectedFields = new HashSet<InstructionInfo>();
		while (cgNodesIter.hasNext()) {
			CGNode cgNode = cgNodesIter.next();
			if (!isIgnoredClass(cgNode.getMethod().getDeclaringClass())) {
				instructionsThatAccessUnprotectedFields.addAll(getInstructionsToReport(bitVectorSolver, cgNode));
			}
		}
		return instructionsThatAccessUnprotectedFields;
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
