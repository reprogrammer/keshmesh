/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;

import edu.illinois.keshmesh.detector.bugs.BugInstances;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK06JBugDetector implements BugPatternDetector {

	private BasicAnalysisData basicAnalysisData = null;

	public BugInstances performAnalysis(BasicAnalysisData analysisData) {
		basicAnalysisData = analysisData;
		BugInstances bugInstances = new BugInstances();
		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			CGNode cgNode = cgNodesIterator.next();
			IMethod method = cgNode.getMethod();
			System.out.println("CGNode:" + cgNode);
			IR ir = cgNode.getIR();
			if (ir != null) {
				System.out.println("IR:" + ir);
				SSAInstruction[] instructions = ir.getInstructions();
				for (int instructionIndex = 0; instructionIndex < instructions.length; instructionIndex++) {
					SSAInstruction instruction = instructions[instructionIndex];
					if (instruction instanceof SSAMonitorInstruction) {
						SSAMonitorInstruction monitorInstruction = (SSAMonitorInstruction) instruction;
						if (monitorInstruction.isMonitorEnter()) {
							AstMethod astMethod = (AstMethod) method;
							int lineNumber = astMethod.getLineNumber(instructionIndex);
							Position position = astMethod.getSourcePosition(instructionIndex);
							Set<SSAInstruction> containedInstructions = getContainedInstructions(astMethod, ir, position);
							for (SSAInstruction containedInstruction : containedInstructions) {
								System.out.println("Contained instruction: " + containedInstruction);
							}
						}
					}
				}
			}
		}
		return bugInstances;
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

}
