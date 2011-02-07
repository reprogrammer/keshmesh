/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAMonitorInstruction;

import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.util.AnalysisUtils;
import edu.illinois.keshmesh.util.Logger;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class VNA00JBugDetector extends BugPatternDetector {

	VNA00JIntermediateResults intermediateResults = new VNA00JIntermediateResults();

	@Override
	public IntermediateResults getIntermediateResults() {
		return intermediateResults;
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
		Collection<IClass> threadSafeClasses = getThreadSafeClasses();
		intermediateResults.setThreadSafeClasses(threadSafeClasses);
		return bugInstances;
	}

	private Collection<IClass> getThreadSafeClasses() {
		Collection<IClass> threadSafeClasses = new HashSet<IClass>();
		Iterator<CGNode> cgNodesIter = basicAnalysisData.callGraph.iterator();
		while (cgNodesIter.hasNext()) {
			CGNode cgNode = cgNodesIter.next();
			if (isThreadSafe(cgNode)) {
				threadSafeClasses.add(cgNode.getMethod().getDeclaringClass());
			}
		}
		return threadSafeClasses;
	}

	private boolean isThreadSafe(CGNode cgNode) {
		IClass declaringClass = cgNode.getMethod().getDeclaringClass();
		if (implementsRunnableInterface(declaringClass) || extendsThreadClass(declaringClass)) {
			return true;
		} else {
			return AnalysisUtils.contains(javaProject, cgNode, new InstructionFilter() {
				@Override
				public boolean accept(InstructionInfo instructionInfo) {
					return instructionInfo.getInstruction() instanceof SSAMonitorInstruction;
				}
			});
		}
	}

	private boolean extendsThreadClass(IClass klass) {
		IClass superclass = klass.getSuperclass();
		if (superclass == null) {
			return false;
		}
		if (isThreadClass(superclass)) {
			return true;
		}
		return extendsThreadClass(superclass);
	}

	private boolean implementsRunnableInterface(IClass klass) {
		for (IClass implementedInterface : klass.getAllImplementedInterfaces()) {
			if (isRunnableInterface(implementedInterface)) {
				return true;
			}
		}
		return false;
	}

	private boolean isRunnableInterface(IClass interfaceClass) {
		return AnalysisUtils.getEnclosingNonanonymousClassName(interfaceClass.getName()).equals("java.lang.Runnable");
	}

	private boolean isThreadClass(IClass klass) {
		return AnalysisUtils.getEnclosingNonanonymousClassName(klass.getName()).equals("java.lang.Thread");
	}

}
