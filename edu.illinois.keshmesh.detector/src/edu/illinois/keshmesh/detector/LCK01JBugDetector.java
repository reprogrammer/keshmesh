/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.ConcreteTypeKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.NormalAllocationInNode;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;

import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.CodePosition;
import edu.illinois.keshmesh.detector.bugs.LCK01JFixInformation;
import edu.illinois.keshmesh.detector.util.AnalysisUtils;
import edu.illinois.keshmesh.util.Logger;

/**
 * 
 * @author Samira Tasharofi
 * @author Stas Negara
 * @author Mohsen Vakilian
 * 
 */
public class LCK01JBugDetector extends BugPatternDetector {
	private static final String JAVA_LANG_INTEGER = "java.lang.Integer";
	private static final String JAVA_LANG_STRING = "java.lang.String";
	private static final String JAVA_LANG_BOOLEAN = "java.lang.Boolean";
	private static final String JAVA_LANG_LONG = "java.lang.Long";
	private static final String JAVA_LANG_SHORT = "java.lang.Short";
	private static final String JAVA_LANG_FLOAT = "java.lang.Float";
	private static final String JAVA_LANG_DOUBLE = "java.lang.Double";
	private static final String JAVA_LANG_BYTE = "java.lang.Byte";

	@Override
	public IntermediateResults getIntermediateResults() {
		return null;
	}

	@Override
	public BugInstances performAnalysis(IJavaProject javaProject, BasicAnalysisData basicAnalysisData) {
		this.javaProject = javaProject;
		this.basicAnalysisData = basicAnalysisData;
		Iterator<CGNode> cgNodesIter = this.basicAnalysisData.callGraph.iterator();
		while (cgNodesIter.hasNext()) {
			CGNode cgNode = cgNodesIter.next();
			Logger.log("CGNode: " + cgNode.getIR());
		}
		BugInstances bugInstances = new BugInstances();
		Collection<InstructionInfo> unsafeSynchronizedBlocks = new HashSet<InstructionInfo>();
		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			final CGNode cgNode = cgNodesIterator.next();
			Logger.log("IR is:" + cgNode.getIR());
			IMethod method = cgNode.getMethod();
			if (!isIgnoredClass(method.getDeclaringClass())) {
				populateBugInstances(unsafeSynchronizedBlocks, cgNode, bugInstances);
			}
		}
		return bugInstances;
	}

	private void populateBugInstances(Collection<InstructionInfo> synchronizedBlocks, final CGNode cgNode, final BugInstances bugInstances) {
		AnalysisUtils.collect(javaProject, synchronizedBlocks, cgNode, new InstructionFilter() {

			@Override
			public boolean accept(InstructionInfo instructionInfo) {
				SSAInstruction instruction = instructionInfo.getInstruction();
				if (AnalysisUtils.isMonitorEnter(instruction)) {
					SSAMonitorInstruction monitorEnterInstruction = (SSAMonitorInstruction) instruction;
					Set<String> reusableLockObjectTypes = getReusableLockObjectTypes(cgNode, monitorEnterInstruction);
					if (!reusableLockObjectTypes.isEmpty()) {
						CodePosition instructionPosition = instructionInfo.getPosition();
						Logger.log("Detected an instance of LCK01-J in class " + instructionPosition.getFullyQualifiedClassName() + ", line number=" + instructionPosition.getFirstLine()
								+ ", instructionIndex= " + instructionInfo.getInstructionIndex());
						bugInstances.add(new BugInstance(BugPatterns.LCK01J, instructionPosition, new LCK01JFixInformation(reusableLockObjectTypes)));
					}
				}
				return false;
			}
		});
	}

	Set<String> getReusableLockObjectTypes(CGNode cgNode, SSAMonitorInstruction monitorInstruction) {
		if (!monitorInstruction.isMonitorEnter()) {
			throw new AssertionError("Expected a monitor enter instruction.");
		}
		//		if (monitorInstruction.getRef() == 5) {
		//			System.out.println("FOUND");
		//		} else if (monitorInstruction.getRef() == 13) {
		//			System.out.println("FOUND");
		//		}

		PointerKey lockPointer = getPointerForValueNumber(cgNode, monitorInstruction.getRef());
		Collection<InstanceKey> lockPointedInstances = getPointedInstances(lockPointer);
		Set<String> instancesTypes = new HashSet<String>();
		for (InstanceKey instanceKey : lockPointedInstances) {
			String javaType = AnalysisUtils.walaTypeNameToJavaName(instanceKey.getConcreteType().getName());
			if (javaType.equals(JAVA_LANG_INTEGER)) {
				if (instanceKey instanceof NormalAllocationInNode && isIntegerCache((NormalAllocationInNode) instanceKey)) {
					instancesTypes.add(javaType);
				}
			} else if (javaType.equals(JAVA_LANG_BOOLEAN)) {
				if (instanceKey instanceof NormalAllocationInNode && isBooleanCache((NormalAllocationInNode) instanceKey)) {
					instancesTypes.add(javaType);
					break;
				}
			} else if (javaType.equals(JAVA_LANG_STRING)) {
				if ((instanceKey instanceof NormalAllocationInNode && isStringCache((NormalAllocationInNode) instanceKey)) || instanceKey instanceof ConcreteTypeKey) {
					instancesTypes.add(javaType);
					break;
				}
			} else if (javaType.equals(JAVA_LANG_LONG)) {
				if (instanceKey instanceof NormalAllocationInNode && isLongCache((NormalAllocationInNode) instanceKey)) {
					instancesTypes.add(javaType);
					break;
				}
			} else if (javaType.equals(JAVA_LANG_SHORT)) {
				if (instanceKey instanceof NormalAllocationInNode && isShortCache((NormalAllocationInNode) instanceKey)) {
					instancesTypes.add(javaType);
					break;
				}

			} else if (javaType.equals(JAVA_LANG_FLOAT)) {
				if (instanceKey instanceof NormalAllocationInNode && isFloatCache((NormalAllocationInNode) instanceKey)) {
					instancesTypes.add(javaType);
					break;
				}

			} else if (javaType.equals(JAVA_LANG_DOUBLE)) {
				if (instanceKey instanceof NormalAllocationInNode && isDoubleCache((NormalAllocationInNode) instanceKey)) {
					instancesTypes.add(javaType);
					break;
				}
			} else if (javaType.equals(JAVA_LANG_BYTE)) {
				if (instanceKey instanceof NormalAllocationInNode && isByteCache((NormalAllocationInNode) instanceKey)) {
					instancesTypes.add(javaType);
					break;
				}
			}
		}
		return instancesTypes;

	}

	private static boolean isStringCache(NormalAllocationInNode normalAllocationInNode) {
		return (normalAllocationInNode.getNode().getMethod().getName().toString().equals("intern") && normalAllocationInNode.getNode().getMethod().getDeclaringClass().getName().toString()
				.equals("Ljava/lang/String"));
	}

	private static boolean isBooleanCache(NormalAllocationInNode normalAllocationInNode) {
		return (normalAllocationInNode.getNode().getMethod().isClinit() && normalAllocationInNode.getNode().getMethod().getDeclaringClass().getName().toString().equals("Ljava/lang/Boolean"));

	}

	private static boolean isLongCache(NormalAllocationInNode normalAllocationInNode) {
		return (normalAllocationInNode.getNode().getMethod().isClinit() && normalAllocationInNode.getNode().getMethod().getDeclaringClass().getName().toString().equals("Ljava/lang/Long$LongCache") || (normalAllocationInNode
				.getNode().getMethod().getName().toString().equals("valueOf") && normalAllocationInNode.getNode().getMethod().getDeclaringClass().getName().toString().equals("Ljava/lang/Long")));
	}

	private static boolean isShortCache(NormalAllocationInNode normalAllocationInNode) {
		return (normalAllocationInNode.getNode().getMethod().isClinit() && normalAllocationInNode.getNode().getMethod().getDeclaringClass().getName().toString().equals("Ljava/lang/Short$ShortCache") || (normalAllocationInNode
				.getNode().getMethod().getName().toString().equals("valueOf") && normalAllocationInNode.getNode().getMethod().getDeclaringClass().getName().toString().equals("Ljava/lang/Short")));
	}

	private static boolean isFloatCache(NormalAllocationInNode normalAllocationInNode) {
		return ((normalAllocationInNode.getNode().getMethod().getName().toString().equals("valueOf") && normalAllocationInNode.getNode().getMethod().getDeclaringClass().getName().toString()
				.equals("Ljava/lang/Float")));
	}

	private static boolean isDoubleCache(NormalAllocationInNode normalAllocationInNode) {
		return ((normalAllocationInNode.getNode().getMethod().getName().toString().equals("valueOf") && normalAllocationInNode.getNode().getMethod().getDeclaringClass().getName().toString()
				.equals("Ljava/lang/Double")));
	}

	private static boolean isByteCache(NormalAllocationInNode normalAllocationInNode) {
		return (normalAllocationInNode.getNode().getMethod().isClinit() && normalAllocationInNode.getNode().getMethod().getDeclaringClass().getName().toString().equals("Ljava/lang/Byte$ByteCache"));
	}

	private static boolean isIntegerCache(NormalAllocationInNode normalAllocationInNode) {
		return (normalAllocationInNode.getNode().getMethod().getName().toString().equals("valueOf") && normalAllocationInNode.getNode().getMethod().getDeclaringClass().getName().toString()
				.equals("Ljava/lang/Integer"));
	}

	private boolean isIgnoredClass(IClass klass) {
		//TODO: Should we look for bugs in JDK usage as well?
		//TODO: !!!What about other bytecodes, e.g. from the libraries, which will not allow to get the source position?
		return AnalysisUtils.isJDKClass(klass);
	}

}
