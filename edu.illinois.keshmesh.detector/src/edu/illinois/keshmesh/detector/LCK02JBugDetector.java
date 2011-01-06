/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AbstractTypeInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.NormalAllocationInNode;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.ReceiverInstanceContext;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.intset.OrdinalSet;

import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.BugPosition;
import edu.illinois.keshmesh.detector.bugs.LCK02JFixInformation;
import edu.illinois.keshmesh.detector.util.AnalysisUtils;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK02JBugDetector extends BugPatternDetector {

	private static final String JAVA_LANG_CLASS = "Ljava/lang/Class"; //$NON-NLS-1$

	@Override
	public BugInstances performAnalysis(IJavaProject javaProject, BasicAnalysisData analysisData) {
		basicAnalysisData = analysisData;
		BugInstances bugInstances = new BugInstances();
		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			final CGNode cgNode = cgNodesIterator.next();
			IMethod method = cgNode.getMethod();
			Logger.log("CGNode:" + cgNode);
			if (AnalysisUtils.isJDKClass(method.getDeclaringClass()))
				continue;
			IR ir = cgNode.getIR();
			if (ir != null) {
				if (method.getName().toString().contains("getInitialContext")) {
					System.out.println("FOUND");
				}
				//				Logger.log("IR:" + ir);
				//				Collection<InstructionInfo> synchronizedBlocksOnGetClassInstructions = new HashSet<InstructionInfo>();
				//				AnalysisUtils.filter(javaProject, synchronizedBlocksOnGetClassInstructions, cgNode, new InstructionFilter() {
				//					@Override
				//					public boolean accept(SSAInstruction ssaInstruction) {
				//						if (ssaInstruction instanceof SSAMonitorInstruction) {
				//							SSAMonitorInstruction monitorInstruction = (SSAMonitorInstruction) ssaInstruction;
				//							if (monitorInstruction.isMonitorEnter()) {
				//								Set<String> synchronizedClassTypeNames = getSynchronizedClassTypeNames(monitorInstruction, cgNode);
				//								if (!synchronizedClassTypeNames.isEmpty()) {
				//									return true;
				//								}
				//							}
				//						}
				//						return false;
				//					}
				//				});
				//				for (InstructionInfo instructionInfo : synchronizedBlocksOnGetClassInstructions) {
				//					Position instructionPosition = instructionInfo.getPosition();
				//					Set<String> synchronizedClassTypeNames = getSynchronizedClassTypeNames((SSAMonitorInstruction) instructionInfo.getInstruction(), cgNode);
				//					String enclosingClassName = AnalysisUtils.getEnclosingNonanonymousClassName(method.getDeclaringClass().getName());
				//					Logger.log("Detected an instance of LCK02-J in class " + enclosingClassName + ", line number=" + instructionPosition.getFirstLine());
				//					bugInstances.add(new BugInstance(BugPatterns.LCK02J, new BugPosition(instructionPosition, enclosingClassName), new LCK02JFixInformation(synchronizedClassTypeNames)));
				//				}

				SSAInstruction[] instructions = ir.getInstructions();
				for (int instructionIndex = 0; instructionIndex < instructions.length; instructionIndex++) {
					SSAInstruction instruction = instructions[instructionIndex];
					if (instruction instanceof SSAMonitorInstruction) {
						SSAMonitorInstruction monitorInstruction = (SSAMonitorInstruction) instruction;
						if (monitorInstruction.isMonitorEnter()) {
							Set<String> synchronizedClassTypeNames = getSynchronizedClassTypeNames(monitorInstruction, cgNode);
							if (!synchronizedClassTypeNames.isEmpty()) {
								InstructionInfo instructionInfo = new InstructionInfo(javaProject, cgNode, instructionIndex);
								Position instructionPosition = instructionInfo.getPosition();
								String enclosingClassName = AnalysisUtils.getEnclosingNonanonymousClassName(method.getDeclaringClass().getName());
								Logger.log("Detected an instance of LCK02-J in class " + enclosingClassName + ", line number=" + instructionPosition.getFirstLine() + ", instructionIndex= "
										+ instructionIndex);
								bugInstances.add(new BugInstance(BugPatterns.LCK02J, new BugPosition(instructionPosition, enclosingClassName), new LCK02JFixInformation(synchronizedClassTypeNames)));
							}
						}
					}
				}
			}
		}
		return bugInstances;
	}

	private static boolean isReturnedByGetClass(NormalAllocationInNode normalAllocationInNode) {
		return normalAllocationInNode.getSite().getDeclaredType().getName().toString().equals(JAVA_LANG_CLASS) && AnalysisUtils.isObjectGetClass(normalAllocationInNode.getNode().getMethod());
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
					//					addSynchronizedClassTypeNames(result, normalAllocationInNode);
					result.add(getReceiverTypeName(normalAllocationInNode));
				}
			}
		}
		return result;
	}

	private void addSynchronizedClassTypeNames(Set<String> result, NormalAllocationInNode normalAllocationInNode) {
		{
			CGNode normalAllocationCGNode = normalAllocationInNode.getNode();
			Iterator<CGNode> predNodesIterator = basicAnalysisData.callGraph.getPredNodes(normalAllocationCGNode);
			while (predNodesIterator.hasNext()) {
				CGNode predNode = predNodesIterator.next();
				Iterator<CallSiteReference> possibleSitesIterator = basicAnalysisData.callGraph.getPossibleSites(predNode, normalAllocationCGNode);
				while (possibleSitesIterator.hasNext()) {
					CallSiteReference possibleSite = possibleSitesIterator.next();
					SSAAbstractInvokeInstruction[] calls = predNode.getIR().getCalls(possibleSite);
					for (SSAAbstractInvokeInstruction invokeInstruction : calls) {
						int invocationReceiverValueNumber = invokeInstruction.getReceiver();
						PointerKey pointerKeyForReceiver = getPointerForValueNumber(predNode, invocationReceiverValueNumber);
						OrdinalSet<InstanceKey> receiverObjects = basicAnalysisData.pointerAnalysis.getPointsToSet(pointerKeyForReceiver);
						for (InstanceKey receiverInstanceKey : receiverObjects) {
							result.add(getJavaClassName(receiverInstanceKey.getConcreteType().getName()));
						}
					}
				}
			}
		}
	}

	private static String getReceiverTypeName(AbstractTypeInNode node) {
		TypeName typeName = ((ReceiverInstanceContext) (node.getNode().getContext())).getReceiver().getConcreteType().getName();
		return getJavaClassName(typeName);
	}

	private static String getJavaClassName(TypeName typeName) {
		String fullyQualifiedName = typeName.getPackage() + "." + typeName.getClassName() + ".class";

		//WALA uses $ to refers to inner classes. We have to replace $ by . to make it a valid class name in Java source code.
		return fullyQualifiedName.replace("$", ".").replace("/", ".");
	}

}
