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

import org.eclipse.core.runtime.NullProgressMonitor;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.dataflow.graph.BitVectorFramework;
import com.ibm.wala.dataflow.graph.BitVectorSolver;
import com.ibm.wala.fixpoint.BitVectorVariable;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
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

import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.util.AnalysisUtils;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK06JBugDetector extends BugPatternDetector {

	enum SynchronizedBlockKind {
		SAFE, UNSAFE
	}

	private static final String PRIMORDIAL_CLASSLOADER_NAME = "Primordial";

	private final Set<InstanceKey> instancesPointedByStaticFields = new HashSet<InstanceKey>();

	public BugInstances performAnalysis(BasicAnalysisData analysisData) {
		basicAnalysisData = analysisData;
		populateAllInstancesPointedByStaticFields();
		BugInstances bugInstances = new BugInstances();
		Collection<InstructionInfo> unsafeSynchronizedBlocks = getUnsafeSynchronizedBlocks();
		if (unsafeSynchronizedBlocks.isEmpty()) {
			return bugInstances;
		}
		Map<CGNode, CGNodeInfo> cgNodeInfoMap = new HashMap<CGNode, CGNodeInfo>();
		OrdinalSetMapping<InstructionInfo> globalValues = MutableMapping.make();

		populateUnsafeModifyingStaticFieldsInstructionsMap(cgNodeInfoMap, globalValues);

		BitVectorSolver<CGNode> bitVectorSolver = propagateUnsafeModifyingStaticFieldsInstructions(cgNodeInfoMap, globalValues);

		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			CGNode cgNode = cgNodesIterator.next();
			IntSet value = bitVectorSolver.getIn(cgNode).getValue();
			if (value != null) {
				IntIterator intIterator = value.intIterator();
				System.out.println("CGNode: " + cgNode.getMethod().getSignature());
				while (intIterator.hasNext()) {
					InstructionInfo instructionInfo = globalValues.getMappedObject(intIterator.next());
					System.out.println("\tPropagated instruction: " + instructionInfo);
				}
			}
		}
		return bugInstances;
	}

	private BitVectorSolver<CGNode> propagateUnsafeModifyingStaticFieldsInstructions(final Map<CGNode, CGNodeInfo> cgNodeInfoMap, OrdinalSetMapping<InstructionInfo> globalValues) {
		LCK06JTransferFunctionProvider transferFunctions = new LCK06JTransferFunctionProvider(basicAnalysisData.callGraph, cgNodeInfoMap);

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
			bitVectorSolver.solve(new NullProgressMonitor());
		} catch (CancelException ex) {
			ex.printStackTrace();
		}
		return bitVectorSolver;
	}

	private void populateUnsafeModifyingStaticFieldsInstructionsMap(Map<CGNode, CGNodeInfo> cgNodeInfoMap, OrdinalSetMapping<InstructionInfo> globalValues) {
		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			CGNode cgNode = cgNodesIterator.next();
			BitVector bitVector = new BitVector();
			Collection<InstructionInfo> safeSynchronizedBlocks = new HashSet<InstructionInfo>();
			//TODO: Should we look for bugs in JDK usage as well?
			if (!isJDKClass(cgNode.getMethod().getDeclaringClass())) {
				Collection<InstructionInfo> modifyingStaticFieldsInstructions = getModifyingStaticFieldsInstructions(cgNode);
				populateSynchronizedBlocksForNode(safeSynchronizedBlocks, cgNode, SynchronizedBlockKind.SAFE);
				Collection<InstructionInfo> unsafeModifyingStaticFieldsInstructions = new HashSet<InstructionInfo>();
				for (InstructionInfo modifyingStaticFieldInstruction : modifyingStaticFieldsInstructions) {
					if (!AnalysisUtils.isProtectedByAnySynchronizedBlock(safeSynchronizedBlocks, modifyingStaticFieldInstruction)) {
						unsafeModifyingStaticFieldsInstructions.add(modifyingStaticFieldInstruction);
					}
				}
				for (InstructionInfo modifyInstruction : modifyingStaticFieldsInstructions) {
					System.out.println("MODIFY: " + modifyInstruction);
				}
				for (InstructionInfo unsafeModifyInstruction : unsafeModifyingStaticFieldsInstructions) {
					bitVector.set(globalValues.add(unsafeModifyInstruction));
					System.out.println("UNSAFE MODIFY: " + unsafeModifyInstruction);
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

		filter(modifyingStaticFieldsInstructions, cgNode, new InstructionFilter() {

			@Override
			public boolean accept(SSAInstruction ssaInstruction) {
				if (ssaInstruction instanceof SSAPutInstruction && ((SSAPutInstruction) ssaInstruction).isStatic()) {
					return true;
				} else if (ssaInstruction instanceof SSAAbstractInvokeInstruction) {
					for (int i = 0; i < ssaInstruction.getNumberOfUses(); i++) {
						SSAInstruction defInstruction = defUse.getDef(ssaInstruction.getUse(i));
						if (defInstruction instanceof SSAGetInstruction && ((SSAGetInstruction) defInstruction).isStatic()) {
							return true;
						}
					}
				}
				return false;
			}
		});
		return modifyingStaticFieldsInstructions;
	}

	private Collection<InstructionInfo> getUnsafeSynchronizedBlocks() {
		Collection<InstructionInfo> unsafeSynchronizedBlocks = new HashSet<InstructionInfo>();
		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			final CGNode cgNode = cgNodesIterator.next();
			populateSynchronizedBlocksForNode(unsafeSynchronizedBlocks, cgNode, SynchronizedBlockKind.UNSAFE);
		}
		return unsafeSynchronizedBlocks;
	}

	private void populateSynchronizedBlocksForNode(Collection<InstructionInfo> synchronizedBlocks, final CGNode cgNode, final SynchronizedBlockKind synchronizedBlockKind) {
		filter(synchronizedBlocks, cgNode, new InstructionFilter() {

			@Override
			public boolean accept(SSAInstruction ssaInstruction) {
				if (ssaInstruction instanceof SSAMonitorInstruction) {
					SSAMonitorInstruction monitorInstruction = (SSAMonitorInstruction) ssaInstruction;
					if (monitorInstruction.isMonitorEnter()) {
						if (synchronizedBlockKind == SynchronizedBlockKind.SAFE) {
							return isSafe(cgNode, monitorInstruction);
						} else {
							return !isSafe(cgNode, monitorInstruction);
						}
					}
				}
				return false;
			}
		});
	}

	private boolean isSafe(CGNode cgNode, SSAMonitorInstruction monitorInstruction) {
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
			System.out.println("Static field: " + staticField);
			PointerKey staticFieldPointer = basicAnalysisData.heapModel.getPointerKeyForStaticField(staticField);
			Collection<InstanceKey> pointedInstances = getPointedInstances(staticFieldPointer);
			for (InstanceKey instance : pointedInstances) {
				System.out.println("Pointed instance: " + instance);
			}
			instancesPointedByStaticFields.addAll(pointedInstances);
		}
	}

	private Set<IField> getAllStaticFields() {
		Set<IField> staticFields = new HashSet<IField>();
		Iterator<IClass> classIterator = basicAnalysisData.classHierarchy.iterator();
		while (classIterator.hasNext()) {
			IClass klass = classIterator.next();
			if (!isJDKClass(klass)) {
				staticFields.addAll(klass.getAllStaticFields());
			}
		}
		return staticFields;
	}

	private boolean isJDKClass(IClass klass) {
		return klass.getClassLoader().getName().toString().equals(PRIMORDIAL_CLASSLOADER_NAME);
	}

	public interface InstructionFilter {
		public boolean accept(SSAInstruction ssaInstruction);
	}

	private void filter(Collection<InstructionInfo> instructionInfos, CGNode cgNode, InstructionFilter instructionFilter) {
		assert instructionInfos != null;
		IR ir = cgNode.getIR();
		if (ir == null) {
			return;
		}
		SSAInstruction[] instructions = ir.getInstructions();
		for (int instructionIndex = 0; instructionIndex < instructions.length; instructionIndex++) {
			SSAInstruction instruction = instructions[instructionIndex];
			if (instructionFilter == null || instructionFilter.accept(instruction)) {
				instructionInfos.add(new InstructionInfo(cgNode, instructionIndex));
			}
		}
	}

}
