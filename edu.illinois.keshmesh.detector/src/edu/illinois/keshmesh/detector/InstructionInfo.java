/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.cast.tree.impl.LineNumberPosition;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.TypeName;

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

	public Position getPosition() {
		IMethod method = cgNode.getMethod();
		if (method instanceof AstMethod) {
			AstMethod astMethod = (AstMethod) method;
			return astMethod.getSourcePosition(instructionIndex);
		} else if (method instanceof ShrikeCTMethod) {
			ShrikeCTMethod shrikeMethod = (ShrikeCTMethod) method;
			TypeName typeName = shrikeMethod.getDeclaringClass().getName();
			String fullyQualifiedName = AnalysisUtils.getEnclosingNonanonymousClassName(typeName);
			try {
				IPath fullPath = AnalysisUtils.getWorkspaceLocation().append(javaProject.findType(fullyQualifiedName).getCompilationUnit().getPath());
				URL url = new URL("file:" + fullPath);
				return new LineNumberPosition(url, url, shrikeMethod.getLineNumber(shrikeMethod.getBytecodeIndex(instructionIndex)));
			} catch (JavaModelException e) {
				throw new RuntimeException(e);
			} catch (InvalidClassFileException e) {
				throw new RuntimeException(e);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
		throw new RuntimeException("Unexpected method class: " + method.getClass());
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
		Position thisPosition = this.getPosition();
		Position thatPosition = that.getPosition();
		return thatPosition.getFirstOffset() <= thisPosition.getFirstOffset() && thatPosition.getLastOffset() >= thisPosition.getLastOffset();
	}

	@Override
	public String toString() {
		return "InstructionInfo [method=" + cgNode.getMethod().getSignature() + ", ssaInstruction=" + ssaInstruction + ", instructionIndex=" + instructionIndex + "]";
	}

}