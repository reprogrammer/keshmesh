/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.util;

import java.util.Collection;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.types.TypeName;

import edu.illinois.keshmesh.detector.InstructionFilter;
import edu.illinois.keshmesh.detector.InstructionInfo;
import edu.illinois.keshmesh.detector.bugs.CodePosition;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class AnalysisUtils {

	private static final String OBJECT_GETCLASS_SIGNATURE = "java.lang.Object.getClass()Ljava/lang/Class;"; //$NON-NLS-1$

	public static IPath getWorkspaceLocation() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation();
	}

	public static boolean isProtectedByAnySynchronizedBlock(Collection<InstructionInfo> safeSynchronizedBlocks, InstructionInfo instruction) {
		for (InstructionInfo safeSynchronizedBlock : safeSynchronizedBlocks) {
			if (instruction.isInside(safeSynchronizedBlock)) {
				return true;
			}
		}
		return false;
	}

	public static void collect(IJavaProject javaProject, Collection<InstructionInfo> instructionInfos, CGNode cgNode, InstructionFilter instructionFilter) {
		if (instructionInfos == null) {
			throw new RuntimeException("Expected a valid collection to store the results in.");
		}

		IR ir = cgNode.getIR();
		if (ir == null) {
			return;
		}
		SSAInstruction[] instructions = ir.getInstructions();
		for (int instructionIndex = 0; instructionIndex < instructions.length; instructionIndex++) {
			SSAInstruction instruction = instructions[instructionIndex];
			InstructionInfo instructionInfo = new InstructionInfo(javaProject, cgNode, instructionIndex);
			if (instruction != null && (instructionFilter == null || instructionFilter.accept(instructionInfo))) {
				instructionInfos.add(instructionInfo);
			}
		}
	}

	/**
	 * Remove the code duplication in
	 * 
	 * {@link #collect(IJavaProject, Collection, CGNode, InstructionFilter)}
	 * 
	 * and
	 * 
	 * {@link #contains(IJavaProject, CGNode, InstructionFilter)}.
	 */
	public static boolean contains(IJavaProject javaProject, CGNode cgNode, InstructionFilter instructionFilter) {
		IR ir = cgNode.getIR();
		if (ir == null) {
			return false;
		}
		SSAInstruction[] instructions = ir.getInstructions();
		for (int instructionIndex = 0; instructionIndex < instructions.length; instructionIndex++) {
			SSAInstruction instruction = instructions[instructionIndex];
			InstructionInfo instructionInfo = new InstructionInfo(javaProject, cgNode, instructionIndex);
			if (instruction != null && (instructionFilter == null || instructionFilter.accept(instructionInfo))) {
				return true;
			}
		}
		return false;
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
		String className = typeName.getClassName().toString();
		int indexOfDollarSign = className.indexOf('$');
		if (indexOfDollarSign != -1 && Character.isDigit(className.charAt(indexOfDollarSign + 1))) {
			className = className.substring(0, indexOfDollarSign);
		}
		return packageName + "." + className;
	}

	public static boolean isJDKClass(IClass klass) {
		boolean isJDKClass = klass.getClassLoader().getName().toString().equals(AnalysisUtils.PRIMORDIAL_CLASSLOADER_NAME);
		return isJDKClass;
	}

	public static boolean isObjectGetClass(IMethod method) {
		return method.getSignature().toString().equals(OBJECT_GETCLASS_SIGNATURE);
	}

	public static String walaTypeNameToJavaName(TypeName typeName) {
		String fullyQualifiedName = typeName.getPackage() + "." + typeName.getClassName();

		//WALA uses $ to refers to inner classes. We have to replace "$" by "." to make it a valid class name in Java source code.
		return fullyQualifiedName.replace("$", ".").replace("/", ".");
	}

	public static CodePosition getPosition(IJavaProject javaProject, IMethod method, int instructionIndex) {
		String enclosingClassName = getEnclosingNonanonymousClassName(method.getDeclaringClass().getName());
		if (method instanceof AstMethod) {
			AstMethod astMethod = (AstMethod) method;
			return new CodePosition(astMethod.getSourcePosition(instructionIndex), enclosingClassName);
		} else if (method instanceof ShrikeCTMethod) {
			ShrikeCTMethod shrikeMethod = (ShrikeCTMethod) method;
			try {
				ICompilationUnit compilationUnit = javaProject.findType(enclosingClassName).getCompilationUnit();
				if (compilationUnit == null) {
					throw new RuntimeException("Could not find a compilation unit for the class: " + enclosingClassName);
				}
				IPath fullPath = getWorkspaceLocation().append(compilationUnit.getPath());
				int lineNumber = shrikeMethod.getLineNumber(shrikeMethod.getBytecodeIndex(instructionIndex));
				return new CodePosition(lineNumber, lineNumber, fullPath, enclosingClassName);
			} catch (JavaModelException e) {
				throw new RuntimeException(e);
			} catch (InvalidClassFileException e) {
				throw new RuntimeException(e);
			}
		}
		throw new RuntimeException("Unexpected method class: " + method.getClass());
	}

	public static boolean isMonitorEnter(SSAInstruction ssaInstruction) {
		return ssaInstruction instanceof SSAMonitorInstruction && ((SSAMonitorInstruction) ssaInstruction).isMonitorEnter();
	}

	public static boolean isMonitorExit(SSAInstruction ssaInstruction) {
		return ssaInstruction instanceof SSAMonitorInstruction && !((SSAMonitorInstruction) ssaInstruction).isMonitorEnter();
	}

	public static final String PRIMORDIAL_CLASSLOADER_NAME = "Primordial"; //$NON-NLS-1$

}
