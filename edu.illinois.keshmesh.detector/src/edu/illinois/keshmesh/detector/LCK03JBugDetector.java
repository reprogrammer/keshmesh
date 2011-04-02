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
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;

import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.CodePosition;
import edu.illinois.keshmesh.detector.bugs.LCK03JBugPattern;
import edu.illinois.keshmesh.detector.bugs.LCK03JFixInformation;
import edu.illinois.keshmesh.detector.util.AnalysisUtils;

/**
 * 
 * @author Samira Tasharofi
 * 
 */
public class LCK03JBugDetector extends BugPatternDetector {

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
		Collection<CGNode> unsafeSynchronizedMethods = new HashSet<CGNode>();
		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			final CGNode cgNode = cgNodesIterator.next();
			Logger.log("IR is:" + cgNode.getIR());
			IMethod method = cgNode.getMethod();
			if (!isIgnoredClass(method.getDeclaringClass())) {
				populateBugInstances(unsafeSynchronizedBlocks, cgNode, bugInstances);
				if (AnalysisUtils.isUnsafeSynchronized(method)) {
					unsafeSynchronizedMethods.add(cgNode);
				}
			}
		}
		return bugInstances;
	}

	private void populateBugInstances(Collection<InstructionInfo> synchronizedBlocks, final CGNode cgNode, final BugInstances bugInstances) {
		AnalysisUtils.filter(javaProject, synchronizedBlocks, cgNode, new InstructionFilter() {

			@Override
			public boolean accept(InstructionInfo instructionInfo) {
				SSAInstruction instruction = instructionInfo.getInstruction();
				if (AnalysisUtils.isMonitorEnter(instruction)) {
					SSAMonitorInstruction monitorEnterInstruction = (SSAMonitorInstruction) instruction;
					Set<String> monitorExpressionTypes = getMonitorExpressionTypes(cgNode, monitorEnterInstruction);
					if (monitorExpressionTypes.contains(LCK03JBugPattern.LOCK) || monitorExpressionTypes.contains(LCK03JBugPattern.CONDITION)) {
						CodePosition instructionPosition = instructionInfo.getPosition();
						Logger.log("Detected an instance of LCK03-J in class " + instructionPosition.getFullyQualifiedClassName() + ", line number=" + instructionPosition.getFirstLine()
								+ ", instructionIndex= " + instructionInfo.getInstructionIndex());
						bugInstances.add(new BugInstance(BugPatterns.LCK03J, instructionPosition, new LCK03JFixInformation(monitorExpressionTypes)));

					}
				}
				return false;
			}
		});
	}

	/**
	 * FIXME: Instead of returning a set, return an object with the following
	 * methods:
	 * 
	 * boolean isLock()
	 * 
	 * boolean isCondition()
	 * 
	 * Set<String> getPossibleType() : returns the most specific types that the
	 * lock could point to.
	 * 
	 * 
	 * @param cgNode
	 * @param monitorInstruction
	 * @return
	 */
	Set<String> getMonitorExpressionTypes(CGNode cgNode, SSAMonitorInstruction monitorInstruction) {
		if (!monitorInstruction.isMonitorEnter()) {
			throw new AssertionError("Expected a monitor enter instruction.");
		}
		PointerKey lockPointer = getPointerForValueNumber(cgNode, monitorInstruction.getRef());
		Collection<InstanceKey> lockPointedInstances = getPointedInstances(lockPointer);
		Set<String> instancesTypes = new HashSet<String>();
		for (InstanceKey instanceKey : lockPointedInstances) {
			boolean impelementedLock = false;
			Collection<IClass> implementedInterfaces = instanceKey.getConcreteType().getAllImplementedInterfaces();
			for (IClass implementedInterface : implementedInterfaces) {
				String interfaceType = AnalysisUtils.walaTypeNameToJavaName(implementedInterface.getName());
				if (interfaceType.equals(LCK03JBugPattern.LOCK) || interfaceType.equals(LCK03JBugPattern.CONDITION)) {
					instancesTypes.add(interfaceType);
					impelementedLock = true;
				}
			}
			if (!impelementedLock)
				instancesTypes.add(AnalysisUtils.walaTypeNameToJavaName(instanceKey.getConcreteType().getName()));
		}
		return instancesTypes;

	}

	private boolean isIgnoredClass(IClass klass) {
		//TODO: Should we look for bugs in JDK usage as well?
		//TODO: !!!What about other bytecodes, e.g. from the libraries, which will not allow to get the source position?
		return AnalysisUtils.isJDKClass(klass);
	}

}
