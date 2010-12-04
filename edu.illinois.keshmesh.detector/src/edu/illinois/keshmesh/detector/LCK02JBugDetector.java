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
import com.ibm.wala.ipa.callgraph.propagation.AbstractTypeInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.NormalAllocationInNode;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.ReceiverInstanceContext;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.intset.OrdinalSet;

import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.BugPosition;
import edu.illinois.keshmesh.detector.bugs.LCK02JFixInformation;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK02JBugDetector extends BugPatternDetector {

	private static final String JAVA_LANG_CLASS = "Ljava/lang/Class"; //$NON-NLS-1$

	private static final String OBJECT_GETCLASS_SIGNATURE = "java.lang.Object.getClass()Ljava/lang/Class;"; //$NON-NLS-1$

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
	private static String getEnclosingNonanonymousClassName(TypeName typeName) {
		String packageName = typeName.getPackage().toString().replaceAll("/", ".");
		int indexOfOpenParen = packageName.indexOf('(');
		if (indexOfOpenParen != -1) {
			int indexOfLastPackageSeparator = packageName.lastIndexOf('.', indexOfOpenParen);
			return packageName.substring(0, indexOfLastPackageSeparator);
		}
		return packageName + "." + typeName.getClassName();
	}

	@Override
	public BugInstances performAnalysis(BasicAnalysisData analysisData) {
		basicAnalysisData = analysisData;
		BugInstances bugInstances = new BugInstances();
		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			CGNode cgNode = cgNodesIterator.next();
			IMethod method = cgNode.getMethod();
			Logger.log("CGNode:" + cgNode);
			IR ir = cgNode.getIR();
			if (ir != null) {
				Logger.log("IR:" + ir);
				SSAInstruction[] instructions = ir.getInstructions();
				for (int instructionIndex = 0; instructionIndex < instructions.length; instructionIndex++) {
					SSAInstruction instruction = instructions[instructionIndex];
					if (instruction instanceof SSAMonitorInstruction) {
						SSAMonitorInstruction monitorInstruction = (SSAMonitorInstruction) instruction;
						if (monitorInstruction.isMonitorEnter()) {
							Set<String> synchronizedClassTypeNames = getSynchronizedClassTypeNames(monitorInstruction, cgNode);
							if (!synchronizedClassTypeNames.isEmpty()) {
								int lineNumber = ((AstMethod) method).getLineNumber(instructionIndex);
								Position position = ((AstMethod) method).getSourcePosition(instructionIndex);
								String enclosingClassName = getEnclosingNonanonymousClassName(method.getDeclaringClass().getName());
								Logger.log("Detected an instance of LCK02-J in class " + enclosingClassName + ", line number=" + lineNumber);
								bugInstances.add(new BugInstance(BugPatterns.LCK02J, new BugPosition(position, enclosingClassName), new LCK02JFixInformation(synchronizedClassTypeNames)));
							}
						}
					}
				}
			}
		}
		return bugInstances;
	}

	private static boolean isReturnedByGetClass(NormalAllocationInNode normalAllocationInNode) {
		return normalAllocationInNode.getSite().getDeclaredType().getName().toString().equals(JAVA_LANG_CLASS)
				&& normalAllocationInNode.getNode().getMethod().getSignature().toString().equals(OBJECT_GETCLASS_SIGNATURE);
	}

	private Set<String> getSynchronizedClassTypeNames(SSAMonitorInstruction monitorInstruction, CGNode cgNode) {
		Set<String> result = new HashSet<String>();
		int lockValueNumber = monitorInstruction.getRef();
		PointerKey lockPointer = getPointerForValueNumber(cgNode, lockValueNumber);
		OrdinalSet<InstanceKey> lockObjects = basicAnalysisData.pointerAnalysis.getPointsToSet(lockPointer);
		for (InstanceKey instanceKey : lockObjects) {
			Logger.log("InstanceKey:" + instanceKey);
			if (instanceKey instanceof NormalAllocationInNode) {
				NormalAllocationInNode normalAllocationInNode = (NormalAllocationInNode) instanceKey;
				if (isReturnedByGetClass(normalAllocationInNode)) {
					result.add(getReceiverTypeName(normalAllocationInNode));
				}
			}
		}
		return result;
	}

	private static String getReceiverTypeName(AbstractTypeInNode node) {
		TypeName typeName = ((ReceiverInstanceContext) (node.getNode().getContext())).getReceiver().getConcreteType().getName();
		String fullyQualifiedName = typeName.getPackage() + "." + typeName.getClassName() + ".class";

		//WALA uses $ to refers to inner classes. We have to replace $ by . to make it a valid class name in Java source code.
		return fullyQualifiedName.replace("$", ".");
	}

}
