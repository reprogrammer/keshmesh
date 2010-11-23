/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;

import edu.illinois.keshmesh.detector.bugs.BugInstances;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK06JBugDetector extends BugPatternDetector {

	private static final String PRIMORDIAL_CLASSLOADER_NAME = "Primordial";

	private final Set<InstanceKey> instancesPointedByStaticFields = new HashSet<InstanceKey>();

	public BugInstances performAnalysis(BasicAnalysisData analysisData) {
		basicAnalysisData = analysisData;
		populateAllInstancesPointedByStaticFields();
		BugInstances bugInstances = new BugInstances();
		Set<SynchronizedBlock> unsafeSynchronizedBlocks = getUnsafeSynchronizedBlocks();
		if (unsafeSynchronizedBlocks.isEmpty()) {
			return bugInstances;
		}
		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			CGNode cgNode = cgNodesIterator.next();
			//TODO: Should we look for bugs in JDK usage as well?
			if (!isJDKClass(cgNode.getMethod().getDeclaringClass())) {
				IR ir = cgNode.getIR();
				if (ir != null) {
					Set<SSAInstruction> modifyingStaticFieldsInstructions = new HashSet<SSAInstruction>();
					DefUse defUse = new DefUse(ir);
					SSAInstruction[] instructions = ir.getInstructions();
					for (int instructionIndex = 0; instructionIndex < instructions.length; instructionIndex++) {
						SSAInstruction instruction = instructions[instructionIndex];
						if (instruction instanceof SSAFieldAccessInstruction && ((SSAFieldAccessInstruction) instruction).isStatic()) {
							if (instruction instanceof SSAPutInstruction) {
								modifyingStaticFieldsInstructions.add(instruction);
							} else { //SSAGetInstruction
								Iterator<SSAInstruction> staticFieldUsesIterator = defUse.getUses(instruction.getDef());
								while (staticFieldUsesIterator.hasNext()) {
									SSAInstruction staticFieldUse = staticFieldUsesIterator.next();
									if (staticFieldUse instanceof SSAAbstractInvokeInstruction) {
										modifyingStaticFieldsInstructions.add(staticFieldUse);
									}
								}
							}
						}
					}
					for (SSAInstruction modifyInstruction : modifyingStaticFieldsInstructions) {
						System.out.println("MODIFY: " + modifyInstruction);
					}
				}
			}
		}
		//							AstMethod astMethod = (AstMethod) method;
		//							int lineNumber = astMethod.getLineNumber(instructionIndex);
		//							Position position = astMethod.getSourcePosition(instructionIndex);
		//							Set<SSAInstruction> containedInstructions = getContainedInstructions(astMethod, ir, position);
		//							for (SSAInstruction containedInstruction : containedInstructions) {
		//								System.out.println("Contained instruction: " + containedInstruction);
		//							}
		return bugInstances;
	}

	private Set<SynchronizedBlock> getUnsafeSynchronizedBlocks() {
		Set<SynchronizedBlock> unsafeSynchronizedBlocks = new HashSet<SynchronizedBlock>();
		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			CGNode cgNode = cgNodesIterator.next();
			IR ir = cgNode.getIR();
			if (ir != null) {
				SSAInstruction[] instructions = ir.getInstructions();
				for (int instructionIndex = 0; instructionIndex < instructions.length; instructionIndex++) {
					SSAInstruction instruction = instructions[instructionIndex];
					if (instruction instanceof SSAMonitorInstruction) {
						SSAMonitorInstruction monitorInstruction = (SSAMonitorInstruction) instruction;
						if (monitorInstruction.isMonitorEnter()) {
							PointerKey lockPointer = getPointerForValueNumber(cgNode, monitorInstruction.getRef());
							Collection<InstanceKey> lockPointedInstances = getPointedInstances(lockPointer);
							if (lockPointedInstances.isEmpty() || !instancesPointedByStaticFields.containsAll(lockPointedInstances)) {
								unsafeSynchronizedBlocks.add(new SynchronizedBlock(cgNode, monitorInstruction));
							}
						}
					}
				}
			}
		}
		return unsafeSynchronizedBlocks;
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

	private Set<SSAInstruction> getContainedInstructions(AstMethod method, IR ir, Position containingPosition) {
		Set<SSAInstruction> containedInstructions = new HashSet<SSAInstruction>();
		SSAInstruction[] instructions = ir.getInstructions();
		for (int instructionIndex = 0; instructionIndex < instructions.length; instructionIndex++) {
			SSAInstruction instruction = instructions[instructionIndex];
			Position instructionPosition = method.getSourcePosition(instructionIndex);
			if (instructionPosition != null) {
				if (isInside(containingPosition, instructionPosition)) {
					containedInstructions.add(instruction);
				}
			}
		}
		return containedInstructions;
	}

	private static boolean isInside(Position container, Position containee) {
		return container.getFirstOffset() <= containee.getFirstOffset() && container.getLastOffset() >= containee.getLastOffset();
	}

	private static class SynchronizedBlock {
		private final CGNode cgNode;
		private final SSAMonitorInstruction monitorEnterInstruction;

		public SynchronizedBlock(CGNode cgNode, SSAMonitorInstruction monitorEnterInstruction) {
			this.cgNode = cgNode;
			this.monitorEnterInstruction = monitorEnterInstruction;
		}

	}
}
