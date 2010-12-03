/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAInstruction;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class InstructionInfo {
	private final CGNode cgNode;
	private final SSAInstruction ssaInstruction;
	private final int instructionIndex;

	public InstructionInfo(CGNode cgNode, int instructionIndex) {
		this.cgNode = cgNode;
		this.instructionIndex = instructionIndex;
		this.ssaInstruction = cgNode.getIR().getInstructions()[instructionIndex];
	}

	public Position getPosition() {
		return ((AstMethod) cgNode.getMethod()).getSourcePosition(instructionIndex);
	}

	public boolean isInside(InstructionInfo that) {
		if (cgNode != that.cgNode)
			return false;
		Position thisPosition = this.getPosition();
		Position thatPosition = that.getPosition();
		return thatPosition.getFirstOffset() <= thisPosition.getFirstOffset() && thatPosition.getLastOffset() >= thisPosition.getLastOffset();
	}

	@Override
	public String toString() {
		return "InstructionInfo [method=" + cgNode.getMethod().getSignature() + ", ssaInstruction=" + ssaInstruction + ", instructionIndex=" + instructionIndex + "]";
	}

}