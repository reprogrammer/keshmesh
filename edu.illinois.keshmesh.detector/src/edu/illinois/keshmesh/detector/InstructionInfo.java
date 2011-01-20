/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAInstruction;

import edu.illinois.keshmesh.detector.bugs.CodePosition;
import edu.illinois.keshmesh.detector.util.AnalysisUtils;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class InstructionInfo {

	private final IJavaProject javaProject;
	private final CGNode cgNode;
	private final SSAInstruction ssaInstruction;
	private final int instructionIndex;

	public InstructionInfo(IJavaProject javaProject, CGNode cgNode, int instructionIndex) {
		this.javaProject = javaProject;
		this.cgNode = cgNode;
		this.instructionIndex = instructionIndex;
		this.ssaInstruction = cgNode.getIR().getInstructions()[instructionIndex];
	}

	public CodePosition getPosition() {
		IMethod method = cgNode.getMethod();
		return AnalysisUtils.getPosition(javaProject, method, instructionIndex);
	}

	public CGNode getCGNode() {
		return cgNode;
	}

	public SSAInstruction getInstruction() {
		return ssaInstruction;
	}

	public int getInstructionIndex() {
		return instructionIndex;
	}

	public boolean isInside(InstructionInfo that) {
		if (!(AnalysisUtils.isMonitorEnter(that.ssaInstruction))) {
			throw new RuntimeException("Should not check 'is inside' relation for an instruction that is not a monitor enter: " + that);
		}
		if (cgNode != that.cgNode) {
			return false;
		}
		return that.getInsideInstructionIndexes().contains(instructionIndex);
	}

	private Collection<InstructionInfo> getMatchingMonitorExits() {
		if (!(AnalysisUtils.isMonitorEnter(ssaInstruction))) {
			throw new RuntimeException("Should not look matching monitor exits of an instruction that is not a monitor enter: " + this);
		}
		Collection<InstructionInfo> matchingMonitorExits = new HashSet<InstructionInfo>();
		final int monitorEnterLockValueNumber = ssaInstruction.getUse(0);
		final int monitorEnterLineNumber = getPosition().getFirstLine();
		AnalysisUtils.filter(javaProject, matchingMonitorExits, cgNode, new InstructionFilter() {
			@Override
			public boolean accept(InstructionInfo instructionInfo) {
				if (AnalysisUtils.isMonitorExit(instructionInfo.getInstruction())) {
					if (instructionInfo.getInstruction().getUse(0) == monitorEnterLockValueNumber && instructionInfo.getPosition().getFirstLine() == monitorEnterLineNumber) {
						return true;
					}
				}
				return false;
			}
		});
		return matchingMonitorExits;
	}

	private Collection<Integer> getInsideInstructionIndexes() {
		Collection<Integer> matchingMonitorExitIndexes = getIndexes(getMatchingMonitorExits());
		SSACFG controlFlowGraph = cgNode.getIR().getControlFlowGraph();
		Collection<Integer> visitedInstructionIndexes = new HashSet<Integer>();
		LinkedList<ISSABasicBlock> workList = new LinkedList<ISSABasicBlock>();
		workList.add(controlFlowGraph.getBlockForInstruction(instructionIndex));
		while (!workList.isEmpty()) {
			ISSABasicBlock basicBlock = workList.poll();
			Iterator<ISSABasicBlock> succNodesIterator = controlFlowGraph.getSuccNodes(basicBlock);
			while (succNodesIterator.hasNext()) {
				ISSABasicBlock succBasicBlock = succNodesIterator.next();
				boolean visitedNewInstructions = visitBasicBlockInstructions(succBasicBlock, matchingMonitorExitIndexes, visitedInstructionIndexes);
				if (visitedNewInstructions && !containsMonitorExit(succBasicBlock, matchingMonitorExitIndexes)) {
					workList.add(succBasicBlock);
				}
			}
		}
		return visitedInstructionIndexes;
	}

	private boolean visitBasicBlockInstructions(ISSABasicBlock basicBlock, Collection<Integer> matchingMonitorExitIndexes, Collection<Integer> visitedInstructionIndexes) {
		boolean visitedNewInstructions = false;
		for (int instructionIndex = basicBlock.getFirstInstructionIndex(); instructionIndex <= basicBlock.getLastInstructionIndex(); instructionIndex++) {
			if (matchingMonitorExitIndexes.contains(instructionIndex)) {
				break;
			}
			if (!visitedInstructionIndexes.contains(instructionIndex)) {
				visitedInstructionIndexes.add(instructionIndex);
				visitedNewInstructions = true;
			}
		}
		return visitedNewInstructions;
	}

	private boolean containsMonitorExit(ISSABasicBlock basicBlock, Collection<Integer> matchingMonitorExitIndexes) {
		for (int instructionIndex = basicBlock.getFirstInstructionIndex(); instructionIndex <= basicBlock.getLastInstructionIndex(); instructionIndex++) {
			if (matchingMonitorExitIndexes.contains(instructionIndex)) {
				return true;
			}
		}
		return false;
	}

	private Collection<Integer> getIndexes(Collection<InstructionInfo> instructionInfos) {
		Collection<Integer> instructionIndexes = new HashSet<Integer>();
		for (InstructionInfo instructionInfo : instructionInfos) {
			instructionIndexes.add(instructionInfo.instructionIndex);
		}
		return instructionIndexes;
	}

	@Override
	public String toString() {
		return "InstructionInfo [method=" + cgNode.getMethod().getSignature() + ", ssaInstruction=" + ssaInstruction + ", instructionIndex=" + instructionIndex + "]";
	}

}