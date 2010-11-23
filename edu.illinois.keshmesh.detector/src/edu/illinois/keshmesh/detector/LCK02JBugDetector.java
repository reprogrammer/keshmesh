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

	private static final String JAVA_LANG_CLASS = "Ljava/lang/Class";

	private static final String OBJECT_GETCLASS_SIGNATURE = "java.lang.Object.getClass()Ljava/lang/Class;";

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
							Set<String> synchronizedClassTypeNames = getSynchronizedClassTypeNames(monitorInstruction, cgNode);
							if (!synchronizedClassTypeNames.isEmpty()) {
								int lineNumber = ((AstMethod) method).getLineNumber(instructionIndex);
								Position position = ((AstMethod) method).getSourcePosition(instructionIndex);
								System.err.println("Detected an instance of LCK02-J in class " + method.getDeclaringClass().getName() + ", line number=" + lineNumber);
								bugInstances.add(new BugInstance(BugPatterns.LCK02J, new BugPosition(position), new LCK02JFixInformation(synchronizedClassTypeNames)));
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
			System.out.println("InstanceKey:" + instanceKey);
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
