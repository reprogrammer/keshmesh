/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Iterator;

import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.analysis.pointers.BasicHeapGraph;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.io.FileProvider;

import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPattern;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.exception.Exceptions;
import edu.illinois.keshmesh.detector.exception.Exceptions.WALAInitializationException;
import edu.illinois.keshmesh.detector.util.DisplayUtils;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class Main {

	private static DetectorJavaSourceAnalysisEngine engine;
	private static IClassHierarchy classHierarchy;
	private static CallGraph callGraph;
	private static PointerAnalysis pointerAnalysis;
	private static HeapModel heapModel;
	private static BasicHeapGraph basicHeapGraph;

	public static BugInstances initAndPerformAnalysis(IJavaProject javaProject) throws WALAInitializationException {
		BugInstances bugInstances = new BugInstances();
		BasicAnalysisData basicAnalysisData = initAnalysis(javaProject);
		Iterator<BugPattern> bugPatternsIterator = BugPatterns.iterator();
		while (bugPatternsIterator.hasNext()) {
			BugPattern bugPattern = bugPatternsIterator.next();
			bugInstances.addAll(bugPattern.getBugPatternDetector().performAnalysis(basicAnalysisData));
		}
		return bugInstances;
	}

	private static BasicAnalysisData initAnalysis(IJavaProject javaProject) throws WALAInitializationException {
		try {
			String exclusionsFileName = FileProvider.getFileFromPlugin(Activator.getDefault(), "EclipseDefaultExclusions.txt").getAbsolutePath();
			engine = new DetectorJavaSourceAnalysisEngine(javaProject, exclusionsFileName);
			callGraph = engine.buildDefaultCallGraph();
			pointerAnalysis = engine.getPointerAnalysis();
			heapModel = pointerAnalysis.getHeapModel();
			basicHeapGraph = new BasicHeapGraph(pointerAnalysis, callGraph);
			classHierarchy = engine.getClassHierarchy();
			DisplayUtils.displayGraph(basicHeapGraph);
		} catch (Exception e) {
			throw new Exceptions.WALAInitializationException(e);
		}
		return new BasicAnalysisData(classHierarchy, callGraph, pointerAnalysis, heapModel, basicHeapGraph);
	}
}
