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

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dataflow.graph.BitVectorFramework;
import com.ibm.wala.dataflow.graph.BitVectorSolver;
import com.ibm.wala.fixpoint.BitVectorVariable;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
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
import edu.illinois.keshmesh.detector.bugs.CodePosition;
import edu.illinois.keshmesh.detector.bugs.LCK06JFixInformation;
import edu.illinois.keshmesh.detector.util.AnalysisUtils;

/**
 * 
 * An unsafe instruction of a CGNode is an unsafe modification of a static
 * field. This modification occurs directly or indirectly inside the CGNode.
 * 
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK06JBugDetector extends BugPatternDetector {

	enum SynchronizedBlockKind {
		SAFE, UNSAFE
	}

	public static final String PRIMORDIAL_CLASSLOADER_NAME = "Primordial"; //$NON-NLS-1$

	private final Set<InstanceKey> instancesPointedByStaticFields = new HashSet<InstanceKey>();

	private final OrdinalSetMapping<InstructionInfo> globalValues = MutableMapping.make();

	private final Map<CGNode, CGNodeInfo> cgNodeInfoMap = new HashMap<CGNode, CGNodeInfo>();

	@Override
	public BugInstances performAnalysis(IJavaProject javaProject, BasicAnalysisData basicAnalysisData) {
		this.javaProject = javaProject;
		this.basicAnalysisData = basicAnalysisData;
		Iterator<CGNode> cgNodesIter = this.basicAnalysisData.callGraph.iterator();
		while (cgNodesIter.hasNext()) {
			CGNode cgNode = cgNodesIter.next();
			Logger.log("CGNode: " + cgNode.getIR());
		}
		populateAllInstancesPointedByStaticFields();
		BugInstances bugInstances = new BugInstances();
		Collection<InstructionInfo> unsafeSynchronizedBlocks = new HashSet<InstructionInfo>();
		Collection<CGNode> unsafeSynchronizedMethods = new HashSet<CGNode>();
		populatedUnsafeSynchronizedStructures(unsafeSynchronizedBlocks, unsafeSynchronizedMethods);
		if (unsafeSynchronizedBlocks.isEmpty() && unsafeSynchronizedMethods.isEmpty()) {
			return bugInstances;
		}

		populateUnsafeModifyingStaticFieldsInstructionsMap();

		BitVectorSolver<CGNode> bitVectorSolver = propagateUnsafeModifyingStaticFieldsInstructions();

		Iterator<CGNode> cgNodesIterator = this.basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			CGNode cgNode = cgNodesIterator.next();
			IntSet value = bitVectorSolver.getIn(cgNode).getValue();
			if (value != null) {
				IntIterator intIterator = value.intIterator();
				Logger.log("CGNode: " + cgNode.getMethod().getSignature());
				while (intIterator.hasNext()) {
					InstructionInfo instructionInfo = globalValues.getMappedObject(intIterator.next());
					Logger.log("\tPropagated instruction: " + instructionInfo);
				}
			}
		}

		reportActuallyUnsafeInstructions(bugInstances, unsafeSynchronizedBlocks, unsafeSynchronizedMethods, bitVectorSolver);

		return bugInstances;
	}

	/*
	 * If an unsafe instruction i is inside an unsafe synchronized block b in a
	 * method m, then, if m is safe, we don't report i. Also, if i is inside
	 * some safe synchronized block in m, then we don't report it as well. But
	 * we report i for all other cases, including scenarios, where method m is
	 * called inside a safe synchronized block in some other method m2 (or m2 is
	 * a safe synchronized method).
	 */
	private void reportActuallyUnsafeInstructions(BugInstances bugInstances, Collection<InstructionInfo> unsafeSynchronizedBlocks, Collection<CGNode> unsafeSynchronizedMethods,
			BitVectorSolver<CGNode> bitVectorSolver) {
		reportActuallyUnsafeInstructionsOfSynchronizedBlocks(bugInstances, unsafeSynchronizedBlocks, bitVectorSolver);
		reportActuallyUnsafeInstructionsOfMethods(bugInstances, unsafeSynchronizedMethods, bitVectorSolver);
	}

	/*
	 * Report the instructions of synchronized instance methods that have unsafe
	 * instructions.
	 */
	private void reportActuallyUnsafeInstructionsOfMethods(BugInstances bugInstances, Collection<CGNode> unsafeSynchronizedMethods, BitVectorSolver<CGNode> bitVectorSolver) {
		for (CGNode unsafeSynchronizedMethod : unsafeSynchronizedMethods) {
			Collection<InstructionInfo> actuallyUnsafeInstructions = new HashSet<InstructionInfo>();
			addSolverResults(actuallyUnsafeInstructions, bitVectorSolver, unsafeSynchronizedMethod);
			reportActuallyUnsafeInstructionsOfMethod(unsafeSynchronizedMethod, actuallyUnsafeInstructions, bugInstances);
		}
	}

	/*
	 * Report the instructions of synchronized blocks that take a nonstatic lock
	 * but has unsafe instructions.
	 */
	private void reportActuallyUnsafeInstructionsOfSynchronizedBlocks(BugInstances bugInstances, Collection<InstructionInfo> unsafeSynchronizedBlocks, BitVectorSolver<CGNode> bitVectorSolver) {
		for (InstructionInfo unsafeSynchronizedBlock : unsafeSynchronizedBlocks) {
			IMethod method = unsafeSynchronizedBlock.getCGNode().getMethod();

			// If the method is safe, i.e. static and synchronized, we report no instances of LCK06J in that method. Therefore, we ignore the unsafe synchronized blocks in it.
			if (AnalysisUtils.isSafeSynchronized(method)) {
				continue;
			}

			Collection<InstructionInfo> actuallyUnsafeInstructions = getActuallyUnsafeInstructions(bitVectorSolver, unsafeSynchronizedBlock);
			reportActuallyUnsafeInstructionsOfSynchronizedBlock(unsafeSynchronizedBlock, actuallyUnsafeInstructions, bugInstances);
		}
	}

	private void reportActuallyUnsafeInstructionsOfSynchronizedBlock(InstructionInfo unsafeSynchronizedBlock, Collection<InstructionInfo> actuallyUnsafeInstructions, BugInstances bugInstances) {
		reportActuallyUnsafeInstructions(unsafeSynchronizedBlock.getCGNode(), unsafeSynchronizedBlock.getPosition(), actuallyUnsafeInstructions, bugInstances);
		Logger.log("Unsafe instructions of " + unsafeSynchronizedBlock + " are " + actuallyUnsafeInstructions.toString());
	}

	private void reportActuallyUnsafeInstructionsOfMethod(CGNode unsafeSynchronizedMethod, Collection<InstructionInfo> actuallyUnsafeInstructions, BugInstances bugInstances) {
		reportActuallyUnsafeInstructions(unsafeSynchronizedMethod, getPosition(unsafeSynchronizedMethod), actuallyUnsafeInstructions, bugInstances);
		Logger.log("Unsafe instructions of " + unsafeSynchronizedMethod + " are " + actuallyUnsafeInstructions.toString());
	}

	private void reportActuallyUnsafeInstructions(CGNode cgNode, CodePosition position, Collection<InstructionInfo> actuallyUnsafeInstructions, BugInstances bugInstances) {
		if (!actuallyUnsafeInstructions.isEmpty()) {
			//			TypeName enclosingClassName = cgNode.getMethod().getDeclaringClass().getName();
			//			bugInstances.add(new BugInstance(BugPatterns.LCK06J, new CodePosition(position, AnalysisUtils.getEnclosingNonanonymousClassName(enclosingClassName)), new LCK06JFixInformation()));
			Collection<IField> unsafeStaticFields = getUnsafeStaticFields(actuallyUnsafeInstructions);
			//			for (IField unsafeStaticField : unsafeStaticFields) {
			//				CGNode node = unsafeStaticField
			//			}
			LCK06JFixInformation fixInfo = new LCK06JFixInformation(getUnsafeStaticFields(actuallyUnsafeInstructions));
			Collection<IField> fields = fixInfo.getStaticFieldNames();
			for (IField field : fields) {
				System.out.println(field.getName());
			}
			bugInstances.add(new BugInstance(BugPatterns.LCK06J, position, fixInfo));
		}
	}

	//TODO: is modifiedStaticFields is enough to report or it is not?
	private Collection<IField> getUnsafeStaticFields(Collection<InstructionInfo> actuallyUnsafeInstructions) {
		Collection<IField> unsafeStaticFields = new HashSet<IField>();
		for (InstructionInfo unsafeInstructionInfo : actuallyUnsafeInstructions) {
			Collection<IField> modifiedStaticFields = getModifiedStaticFields(unsafeInstructionInfo);
			unsafeStaticFields.addAll(modifiedStaticFields);
		}

		return unsafeStaticFields;
	}

	private Collection<IField> getModifiedStaticFields(InstructionInfo unsafeInstructionInfo) {
		SSAInstruction ssaInstruction = unsafeInstructionInfo.getInstruction();
		Collection<IField> unsafeStaticFields = new HashSet<IField>();
		if (ssaInstruction instanceof SSAPutInstruction) {
			IField accessedField = getStaticNonFinalField((SSAFieldAccessInstruction) ssaInstruction);
			unsafeStaticFields.add(accessedField);
		} else if (ssaInstruction instanceof SSAAbstractInvokeInstruction) {
			for (int i = 0; i < ssaInstruction.getNumberOfUses(); i++) {
				int valueNumber = ssaInstruction.getUse(i);
				PointerKey pointer = getPointerForValueNumber(unsafeInstructionInfo.getCGNode(), valueNumber);
				Collection<InstanceKey> variablePointedInstances = getPointedInstances(pointer);
				for (IField staticField : getAllStaticFields()) {
					Logger.log("Static field: " + staticField);
					PointerKey staticFieldPointer = basicAnalysisData.heapModel.getPointerKeyForStaticField(staticField);
					Collection<InstanceKey> staticFieldPointedInstances = getPointedInstances(staticFieldPointer);
					boolean isIntersected = false;
					for (InstanceKey staticInstance : staticFieldPointedInstances) {
						Logger.log("Pointed instance: " + staticInstance);
						if (variablePointedInstances.contains(staticInstance))
							isIntersected = true;
					}
					if (isIntersected)
						unsafeStaticFields.add(staticField);
				}
			}
		}
		return unsafeStaticFields;
	}

	private IField getStaticNonFinalField(SSAFieldAccessInstruction fieldAccessInstruction) {
		IField accessedField = basicAnalysisData.classHierarchy.resolveField(fieldAccessInstruction.getDeclaredField());
		if (!(fieldAccessInstruction.isStatic() && !accessedField.isFinal())) {
			throw new AssertionError("Expected an instruction accessing a nonfinal static field.");
		}
		return accessedField;
	}

	/**
	 * @param results
	 *            The unsafe instructions of the CGNode will get added to this
	 *            collection.
	 * @param bitVectorSolver
	 *            It maps every CGNode to a collection of its unsafe
	 *            instructions.
	 * @param cgNode
	 */
	public void addSolverResults(Collection<InstructionInfo> results, BitVectorSolver<CGNode> bitVectorSolver, CGNode cgNode) {
		IntSet value = bitVectorSolver.getIn(cgNode).getValue();
		if (value != null) {
			IntIterator intIterator = value.intIterator();
			while (intIterator.hasNext()) {
				InstructionInfo instructionInfo = globalValues.getMappedObject(intIterator.next());
				results.add(instructionInfo);
			}
		}
	}

	/**
	 * After we propagate statements that modify static fields through the call
	 * graph, we have to filter the result just to keep the modifications that
	 * occur inside an unsafe synchronized block. Direct modifications to static
	 * fields or indirect ones through method calls inside an unsafe
	 * synchronized block are called actual unsafe modifications.
	 * 
	 * @return a collection of instructions that modify static fields and are
	 *         directly or indirectly inside the given unsafeSynchronizedBlock
	 *         but are not directly or indirectly inside a safe synchronized
	 *         block.
	 */
	private Collection<InstructionInfo> getActuallyUnsafeInstructions(final BitVectorSolver<CGNode> bitVectorSolver, final InstructionInfo unsafeSynchronizedBlock) {
		final Collection<InstructionInfo> unsafeInstructions = new HashSet<InstructionInfo>();
		final CGNode cgNode = unsafeSynchronizedBlock.getCGNode();
		final Collection<InstructionInfo> safeSynchronizedBlocks = new HashSet<InstructionInfo>();
		populateSynchronizedBlocksForNode(safeSynchronizedBlocks, cgNode, SynchronizedBlockKind.SAFE);
		IR ir = cgNode.getIR();
		if (ir == null) {
			return unsafeInstructions; //should not really be null here
		}
		final DefUse defUse = new DefUse(ir);
		AnalysisUtils.filter(javaProject, new HashSet<InstructionInfo>(), cgNode, new InstructionFilter() {
			@Override
			public boolean accept(InstructionInfo instructionInfo) {
				SSAInstruction instruction = instructionInfo.getInstruction();
				if (instructionInfo.isInside(unsafeSynchronizedBlock) && !AnalysisUtils.isProtectedByAnySynchronizedBlock(safeSynchronizedBlocks, instructionInfo)) {
					if (canModifyStaticField(defUse, instruction)) {
						unsafeInstructions.add(instructionInfo);
					}
					if (instruction instanceof SSAAbstractInvokeInstruction) {
						SSAAbstractInvokeInstruction invokeInstruction = (SSAAbstractInvokeInstruction) instruction;
						// Add the unsafe modifications of the methods that are the targets of the invocation instruction. 
						Set<CGNode> possibleTargets = basicAnalysisData.callGraph.getPossibleTargets(cgNode, invokeInstruction.getCallSite());
						for (CGNode possibleTarget : possibleTargets) {
							// Add unsafe operations coming from callees.
							addSolverResults(unsafeInstructions, bitVectorSolver, possibleTarget);
						}
					}
				}
				return false;
			}
		});
		return unsafeInstructions;
	}

	private BitVectorSolver<CGNode> propagateUnsafeModifyingStaticFieldsInstructions() {
		LCK06JTransferFunctionProvider transferFunctions = new LCK06JTransferFunctionProvider(javaProject, basicAnalysisData.callGraph, cgNodeInfoMap);

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
	 * Populates the initial sets for modifying static fields of all CGNodes.
	 * 
	 * @param cgNodeInfoMap
	 * @param globalValues
	 */
	private void populateUnsafeModifyingStaticFieldsInstructionsMap() {
		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			CGNode cgNode = cgNodesIterator.next();
			BitVector bitVector = new BitVector();
			Collection<InstructionInfo> safeSynchronizedBlocks = new HashSet<InstructionInfo>();
			if (!AnalysisUtils.isSafeSynchronized(cgNode.getMethod()) && !isIgnoredClass(cgNode.getMethod().getDeclaringClass())) {
				Collection<InstructionInfo> modifyingStaticFieldsInstructions = getModifyingStaticFieldsInstructions(cgNode);
				populateSynchronizedBlocksForNode(safeSynchronizedBlocks, cgNode, SynchronizedBlockKind.SAFE);
				Collection<InstructionInfo> unsafeModifyingStaticFieldsInstructions = new HashSet<InstructionInfo>();
				for (InstructionInfo modifyingStaticFieldInstruction : modifyingStaticFieldsInstructions) {
					if (!AnalysisUtils.isProtectedByAnySynchronizedBlock(safeSynchronizedBlocks, modifyingStaticFieldInstruction)) {
						unsafeModifyingStaticFieldsInstructions.add(modifyingStaticFieldInstruction);
					}
				}
				for (InstructionInfo modifyInstruction : modifyingStaticFieldsInstructions) {
					Logger.log("MODIFY: " + modifyInstruction);
				}
				for (InstructionInfo unsafeModifyInstruction : unsafeModifyingStaticFieldsInstructions) {
					bitVector.set(globalValues.add(unsafeModifyInstruction));
					Logger.log("UNSAFE MODIFY: " + unsafeModifyInstruction);
				}
			}
			cgNodeInfoMap.put(cgNode, new CGNodeInfo(safeSynchronizedBlocks, bitVector));
		}
	}

	private Collection<InstructionInfo> getModifyingStaticFieldsInstructions(CGNode cgNode) {
		Collection<InstructionInfo> modifyingStaticFieldsInstructions = new HashSet<InstructionInfo>();
		IR ir = cgNode.getIR();
		if (ir == null) {
			return modifyingStaticFieldsInstructions;
		}
		final DefUse defUse = new DefUse(ir);

		AnalysisUtils.filter(javaProject, modifyingStaticFieldsInstructions, cgNode, new InstructionFilter() {

			@Override
			public boolean accept(InstructionInfo instructionInfo) {
				return canModifyStaticField(defUse, instructionInfo.getInstruction());
			}
		});
		return modifyingStaticFieldsInstructions;
	}

	private boolean canModifyStaticField(final DefUse defUse, SSAInstruction ssaInstruction) {
		if (ssaInstruction instanceof SSAPutInstruction) {
			return isStaticNonFinal((SSAFieldAccessInstruction) ssaInstruction);
		} else if (ssaInstruction instanceof SSAAbstractInvokeInstruction) {
			for (int i = 0; i < ssaInstruction.getNumberOfUses(); i++) {
				SSAInstruction defInstruction = defUse.getDef(ssaInstruction.getUse(i));
				if (defInstruction instanceof SSAGetInstruction && isStaticNonFinal((SSAFieldAccessInstruction) defInstruction)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isStaticNonFinal(SSAFieldAccessInstruction fieldAccessInstruction) {
		IField accessedField = basicAnalysisData.classHierarchy.resolveField(fieldAccessInstruction.getDeclaredField());
		return fieldAccessInstruction.isStatic() && !accessedField.isFinal();
	}

	/*
	 * FIXME: A synchronized block that is nested inside a safe one is
	 * considered safe (See edu.illinois.keshmesh.detector.LCK06JBugDetector.
	 * getActuallyUnsafeInstructions(BitVectorSolver<CGNode>, InstructionInfo)).
	 * So, this method should not return such synchronized blocks.
	 */
	private void populatedUnsafeSynchronizedStructures(Collection<InstructionInfo> unsafeSynchronizedBlocks, Collection<CGNode> unsafeSynchronizedMethods) {
		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			final CGNode cgNode = cgNodesIterator.next();
			Logger.log("IR is:" + cgNode.getIR());
			IMethod method = cgNode.getMethod();
			if (!isIgnoredClass(method.getDeclaringClass())) {
				populateSynchronizedBlocksForNode(unsafeSynchronizedBlocks, cgNode, SynchronizedBlockKind.UNSAFE);
				if (AnalysisUtils.isUnsafeSynchronized(method)) {
					unsafeSynchronizedMethods.add(cgNode);
				}
			}
		}
	}

	private void populateSynchronizedBlocksForNode(Collection<InstructionInfo> synchronizedBlocks, final CGNode cgNode, final SynchronizedBlockKind synchronizedBlockKind) {
		AnalysisUtils.filter(javaProject, synchronizedBlocks, cgNode, new InstructionFilter() {

			@Override
			public boolean accept(InstructionInfo instructionInfo) {
				SSAInstruction instruction = instructionInfo.getInstruction();
				if (AnalysisUtils.isMonitorEnter(instruction)) {
					SSAMonitorInstruction monitorEnterInstruction = (SSAMonitorInstruction) instruction;
					if (synchronizedBlockKind == SynchronizedBlockKind.SAFE) {
						return isSafe(cgNode, monitorEnterInstruction);
					} else {
						return !isSafe(cgNode, monitorEnterInstruction);
					}
				}
				return false;
			}
		});
	}

	boolean isSafe(CGNode cgNode, SSAMonitorInstruction monitorInstruction) {
		assert (monitorInstruction.isMonitorEnter());
		PointerKey lockPointer = getPointerForValueNumber(cgNode, monitorInstruction.getRef());
		Collection<InstanceKey> lockPointedInstances = getPointedInstances(lockPointer);
		if (lockPointedInstances.isEmpty() || !instancesPointedByStaticFields.containsAll(lockPointedInstances)) {
			return false;
		}
		return true;
	}

	private void populateAllInstancesPointedByStaticFields() {
		for (IField staticField : getAllStaticFields()) {
			Logger.log("Static field: " + staticField);
			PointerKey staticFieldPointer = basicAnalysisData.heapModel.getPointerKeyForStaticField(staticField);
			Collection<InstanceKey> pointedInstances = getPointedInstances(staticFieldPointer);
			for (InstanceKey instance : pointedInstances) {
				Logger.log("Pointed instance: " + instance);
			}
			instancesPointedByStaticFields.addAll(pointedInstances);
		}
	}

	private Set<IField> getAllStaticFields() {
		Set<IField> staticFields = new HashSet<IField>();
		Iterator<IClass> classIterator = basicAnalysisData.classHierarchy.iterator();
		while (classIterator.hasNext()) {
			IClass klass = classIterator.next();
			if (!isIgnoredClass(klass)) {
				staticFields.addAll(klass.getAllStaticFields());
			}
		}
		return staticFields;
	}

	private boolean isIgnoredClass(IClass klass) {
		//TODO: Should we look for bugs in JDK usage as well?
		//TODO: !!!What about other bytecodes, e.g. from the libraries, which will not allow to get the source position?
		return AnalysisUtils.isJDKClass(klass);
	}

	private CodePosition getPosition(CGNode cgNode) {
		IMethod method = cgNode.getMethod();
		return AnalysisUtils.getPosition(javaProject, method, 0);
	}

}
