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
		Collection<InstructionInfo> unsafeSynchronizedBlocks = getUnsafeSynchronizedBlocks();
		if (unsafeSynchronizedBlocks.isEmpty()) {
			return bugInstances;
		}
		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			CGNode cgNode = cgNodesIterator.next();
			//TODO: Should we look for bugs in JDK usage as well?
			if (!isJDKClass(cgNode.getMethod().getDeclaringClass())) {
				Collection<InstructionInfo> modifyingStaticFieldsInstructions = getModifyingStaticFieldsInstructions(cgNode);
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
				instructionInfos.add(new InstructionInfo(cgNode, instruction, instructionIndex));
			}
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
				if (ssaInstruction instanceof SSAFieldAccessInstruction && ((SSAFieldAccessInstruction) ssaInstruction).isStatic()) {
					if (ssaInstruction instanceof SSAPutInstruction) {
						return true;
					} else { //SSAGetInstruction
						Iterator<SSAInstruction> staticFieldUsesIterator = defUse.getUses(ssaInstruction.getDef());
						while (staticFieldUsesIterator.hasNext()) {
							SSAInstruction staticFieldUse = staticFieldUsesIterator.next();
							if (staticFieldUse instanceof SSAAbstractInvokeInstruction) {
								return true;
							}
						}
					}
				}
				return false;
			}
		});

		for (InstructionInfo modifyInstruction : modifyingStaticFieldsInstructions) {
			System.out.println("MODIFY: " + modifyInstruction);
		}
		return modifyingStaticFieldsInstructions;
	}

	private Collection<InstructionInfo> getUnsafeSynchronizedBlocks() {
		Collection<InstructionInfo> unsafeSynchronizedBlocks = new HashSet<InstructionInfo>();
		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			final CGNode cgNode = cgNodesIterator.next();
			filter(unsafeSynchronizedBlocks, cgNode, new InstructionFilter() {

				@Override
				public boolean accept(SSAInstruction ssaInstruction) {
					if (ssaInstruction instanceof SSAMonitorInstruction) {
						SSAMonitorInstruction monitorInstruction = (SSAMonitorInstruction) ssaInstruction;
						if (monitorInstruction.isMonitorEnter()) {
							if (!isSafe(cgNode, monitorInstruction)) {
								return true;
							}
						}
					}
					return false;
				}
			});
		}
		return unsafeSynchronizedBlocks;
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

	@Deprecated
	private static boolean isInside(Position container, Position containee) {
		return container.getFirstOffset() <= containee.getFirstOffset() && container.getLastOffset() >= containee.getLastOffset();
	}

	private static class InstructionInfo {
		private final CGNode cgNode;
		private final SSAInstruction ssaInstruction;
		private final int instructionIndex;

		public InstructionInfo(CGNode cgNode, SSAInstruction ssaInstruction, int instructionIndex) {
			this.cgNode = cgNode;
			this.ssaInstruction = ssaInstruction;
			this.instructionIndex = instructionIndex;
		}

		public Position getPosition() {
			return ((AstMethod) cgNode.getMethod()).getSourcePosition(instructionIndex);
		}

		public boolean isInside(InstructionInfo that) {
			Position thisPosition = this.getPosition();
			Position thatPosition = that.getPosition();
			return thatPosition.getFirstOffset() <= thisPosition.getFirstOffset() && thatPosition.getLastOffset() >= thisPosition.getLastOffset();
		}
	}
}
