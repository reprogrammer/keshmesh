/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.util;

import java.util.Collection;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.TypeName;

import edu.illinois.keshmesh.detector.InstructionFilter;
import edu.illinois.keshmesh.detector.InstructionInfo;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class AnalysisUtils {

	public static boolean isProtectedByAnySynchronizedBlock(Collection<InstructionInfo> safeSynchronizedBlocks, InstructionInfo instruction) {
		for (InstructionInfo safeSynchronizedBlock : safeSynchronizedBlocks) {
			if (instruction.isInside(safeSynchronizedBlock)) {
				return true;
			}
		}
		return false;
	}

	public static void filter(Collection<InstructionInfo> instructionInfos, CGNode cgNode, InstructionFilter instructionFilter) {
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

	/**
	 * Findbugs needs the name of the class that contains the bug. The class
	 * name that WALA returns includes some additional information such as the
	 * method name in case of anonymous classes. But, Findbugs expects names
	 * that follow the standard Java bytecode convention. This method takes a
	 * class name as reported by WALA and returns the name of the innermost
	 * enclosing non-anonymous class of it. See issue #5 for more details.
	 * 
	 * @param walaClassName
	 * @return
	 */
	public static String getEnclosingNonanonymousClassName(TypeName typeName) {
		String packageName = typeName.getPackage().toString().replaceAll("/", ".");
		int indexOfOpenParen = packageName.indexOf('(');
		if (indexOfOpenParen != -1) {
			int indexOfLastPackageSeparator = packageName.lastIndexOf('.', indexOfOpenParen);
			return packageName.substring(0, indexOfLastPackageSeparator);
		}
		return packageName + "." + typeName.getClassName();
	}

}
