/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.core.IJavaProject;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.ibm.wala.analysis.pointers.BasicHeapGraph;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.io.FileProvider;

import edu.illinois.keshmesh.config.ConfigurationOptions;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPattern;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.exception.Exceptions;
import edu.illinois.keshmesh.detector.exception.Exceptions.WALAInitializationException;
import edu.illinois.keshmesh.detector.util.AnalysisUtils;
import edu.illinois.keshmesh.detector.util.DisplayUtils;
import edu.illinois.keshmesh.report.KeyValuePair;
import edu.illinois.keshmesh.report.Reporter;
import edu.illinois.keshmesh.util.Logger;
import edu.illinois.keshmesh.walaconfig.KeshmeshCGModel;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class Main {

	private static boolean hasShownGraphs = false;

	public static BugInstances initAndPerformAnalysis(IJavaProject javaProject, Reporter reporter, ConfigurationOptions configurationOptions) throws WALAInitializationException {
		BugInstances bugInstances = new BugInstances();
		int objectSensitivityLevel = configurationOptions.getObjectSensitivityLevel();
		reporter.report(new KeyValuePair("OBJECT_SENSITIVITY_LEVEL", String.valueOf(objectSensitivityLevel)));
		BasicAnalysisData basicAnalysisData = initBytecodeAnalysis(javaProject, reporter, objectSensitivityLevel);
		reportCallGraph(basicAnalysisData.callGraph);
		Iterator<BugPattern> bugPatternsIterator = BugPatterns.iterator();
		while (bugPatternsIterator.hasNext()) {
			BugPattern bugPattern = bugPatternsIterator.next();
			BugPatternDetector bugPatternDetector = bugPattern.createBugPatternDetector();
			Stopwatch stopWatch = Stopwatch.createStarted();
			BugInstances instancesOfCurrentBugPattern = bugPatternDetector.performAnalysis(javaProject, basicAnalysisData);
			stopWatch.stop();
			reporter.report(new KeyValuePair("BUG_PATTERN_" + bugPattern.getName() + "_DETECTION_TIME_IN_MILLISECONDS", String.valueOf(stopWatch.elapsed(TimeUnit.MILLISECONDS))));
			bugInstances.addAll(instancesOfCurrentBugPattern);
			reporter.report(new KeyValuePair("NUMBER_OF_INSTANCES_OF_BUG_PATTERN_" + bugPattern.getName(), String.valueOf(instancesOfCurrentBugPattern.size())));
		}
		reporter.close();
		return bugInstances;
	}

	private static BasicAnalysisData initBytecodeAnalysis(IJavaProject javaProject, Reporter reporter, int objectSensitivityLevel) throws WALAInitializationException {
		KeshmeshCGModel model;
		try {
			String exclusionsFileName = FileProvider.getFileFromPlugin(Activator.getDefault(), "EclipseDefaultExclusions.txt").getAbsolutePath();
			model = new KeshmeshCGModel(javaProject, exclusionsFileName, objectSensitivityLevel);
			Stopwatch stopWatch = Stopwatch.createStarted();
			model.buildGraph();
			stopWatch.stop();
			reporter.report(new KeyValuePair("CALL_GRAPH_CONSTRUCTION_TIME_IN_MILLISECONDS", String.valueOf(stopWatch.elapsed(TimeUnit.MILLISECONDS))));
			reporter.report(new KeyValuePair("NUMBER_OF_ENTRY_POINTS", String.valueOf(model.getNumberOfEntryPoints())));
		} catch (Exception e) {
			throw new Exceptions.WALAInitializationException(e);
		}
		CallGraph callGraph = model.getGraph();
		reporter.report(new KeyValuePair("NUMBER_OF_NODES_OF_CALL_GRAPH", String.valueOf(callGraph.getNumberOfNodes())));
		PointerAnalysis pointerAnalysis = model.getPointerAnalysis();
		HeapModel heapModel = pointerAnalysis.getHeapModel();
		BasicHeapGraph heapGraph = new BasicHeapGraph(pointerAnalysis, callGraph);
		reporter.report(new KeyValuePair("NUMBER_OF_NODES_OF_HEAP_GRAPH", String.valueOf(heapGraph.getNumberOfNodes())));
		if (!hasShownGraphs) {
			try {
				DisplayUtils.displayGraph(callGraph);
				DisplayUtils.displayGraph(heapGraph);
				hasShownGraphs = true;
			} catch (WalaException e) {
				throw new WALAInitializationException(e);
			}
		}
		IClassHierarchy classHierarchy = model.getClassHierarchy();
		reporter.report(new KeyValuePair("NUMBER_OF_CLASSES", String.valueOf(classHierarchy.getNumberOfClasses())));
		return new BasicAnalysisData(classHierarchy, callGraph, pointerAnalysis, heapModel, heapGraph);
	}

	private static void reportCallGraph(CallGraph callGraph) {
		Preconditions.checkNotNull(callGraph);
		Iterator<CGNode> cgNodesIter = callGraph.iterator();
		while (cgNodesIter.hasNext()) {
			CGNode cgNode = cgNodesIter.next();
			IMethod method = cgNode.getMethod();
			if (AnalysisUtils.isJDKClass(method.getDeclaringClass()))
				continue;
			Logger.log("**CGNode:** " + cgNode);
			IR ir = cgNode.getIR();
			if (ir != null) {
				Logger.log("**IR:** " + ir);
			}
		}
	}

}
