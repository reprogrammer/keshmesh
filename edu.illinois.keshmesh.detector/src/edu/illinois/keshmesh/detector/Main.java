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
import edu.illinois.keshmesh.walaconfig.DetectorJavaSourceAnalysisEngine;
import edu.illinois.keshmesh.walaconfig.KeshmeshCGModelWithMain;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class Main {

	public static BugInstances initAndPerformAnalysis(IJavaProject javaProject) throws WALAInitializationException {
		BugInstances bugInstances = new BugInstances();
		BasicAnalysisData basicAnalysisData = initBytecodeAnalysis(javaProject);
		Iterator<BugPattern> bugPatternsIterator = BugPatterns.iterator();
		while (bugPatternsIterator.hasNext()) {
			BugPattern bugPattern = bugPatternsIterator.next();
			BugInstances instancesOfCurrentBugPattern = bugPattern.createBugPatternDetector().performAnalysis(javaProject, basicAnalysisData);
			bugInstances.addAll(instancesOfCurrentBugPattern);
		}
		return bugInstances;
	}

	//FIXME: Make the selection between initAnalysis and initSourceAnalysis cleaner by using patterns such as Strategy.

	private static BasicAnalysisData initBytecodeAnalysis(IJavaProject javaProject) throws WALAInitializationException {
		KeshmeshCGModelWithMain model;
		try {
			String exclusionsFileName = FileProvider.getFileFromPlugin(Activator.getDefault(), "EclipseDefaultExclusions.txt").getAbsolutePath();
			model = new KeshmeshCGModelWithMain(javaProject, exclusionsFileName);
			model.buildGraph();
		} catch (Exception e) {
			throw new Exceptions.WALAInitializationException(e);
		}
		CallGraph callGraph = model.getGraph();
		PointerAnalysis pointerAnalysis = model.getPointerAnalysis();
		HeapModel heapModel = pointerAnalysis.getHeapModel();
		BasicHeapGraph basicHeapGraph = new BasicHeapGraph(pointerAnalysis, callGraph);
		//		try {
		//			DisplayUtils.displayGraph(basicHeapGraph);
		//		} catch (WalaException e) {
		//			throw new WALAInitializationException(e);
		//		}
		IClassHierarchy classHierarchy = model.getClassHierarchy();
		return new BasicAnalysisData(classHierarchy, callGraph, pointerAnalysis, heapModel, basicHeapGraph);
	}

	private static BasicAnalysisData initSourceAnalysis(IJavaProject javaProject) throws WALAInitializationException {
		try {
			String exclusionsFileName = FileProvider.getFileFromPlugin(Activator.getDefault(), "EclipseDefaultExclusions.txt").getAbsolutePath();
			DetectorJavaSourceAnalysisEngine engine = new DetectorJavaSourceAnalysisEngine(javaProject, exclusionsFileName);
			CallGraph callGraph = engine.buildDefaultCallGraph();
			PointerAnalysis pointerAnalysis = engine.getPointerAnalysis();
			HeapModel heapModel = pointerAnalysis.getHeapModel();
			BasicHeapGraph basicHeapGraph = new BasicHeapGraph(pointerAnalysis, callGraph);
			IClassHierarchy classHierarchy = engine.getClassHierarchy();
			//DisplayUtils.displayGraph(basicHeapGraph);
			return new BasicAnalysisData(classHierarchy, callGraph, pointerAnalysis, heapModel, basicHeapGraph);
		} catch (Exception e) {
			throw new Exceptions.WALAInitializationException(e);
		}
	}

}
