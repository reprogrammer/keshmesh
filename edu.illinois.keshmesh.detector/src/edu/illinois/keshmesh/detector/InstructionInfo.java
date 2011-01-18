/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
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

	public boolean isInside(InstructionInfo that) {
		if (cgNode != that.cgNode)
			return false;
		CodePosition thisPosition = this.getPosition();
		CodePosition thatPosition = that.getPosition();
		return thatPosition.getFirstOffset() <= thisPosition.getFirstOffset() && thatPosition.getLastOffset() >= thisPosition.getLastOffset();
	}

	@Override
	public String toString() {
		return "InstructionInfo [method=" + cgNode.getMethod().getSignature() + ", ssaInstruction=" + ssaInstruction + ", instructionIndex=" + instructionIndex + "]";
	}

}