/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.io.Writer;
import java.util.Iterator;

import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.analysis.pointers.BasicHeapGraph;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.perf.Stopwatch;

import edu.illinois.keshmesh.config.ConfigurationInputStreamFactory;
import edu.illinois.keshmesh.config.ConfigurationOptions;
import edu.illinois.keshmesh.config.ConfigurationReaderFactory;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPattern;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.exception.Exceptions;
import edu.illinois.keshmesh.detector.exception.Exceptions.WALAInitializationException;
import edu.illinois.keshmesh.detector.util.DisplayUtils;
import edu.illinois.keshmesh.report.KeyValuePair;
import edu.illinois.keshmesh.report.Reporter;
import edu.illinois.keshmesh.report.WriterFactory;
import edu.illinois.keshmesh.walaconfig.KeshmeshCGModel;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class Main {

	private static Stopwatch stopWatch = new Stopwatch();

	private static boolean hasShownGraphs = false;

	private static ConfigurationOptions readConfigurationOptions() {
		return new ConfigurationReaderFactory(new ConfigurationInputStreamFactory()).create().read();
	}

	public static BugInstances initAndPerformAnalysis(IJavaProject javaProject) throws WALAInitializationException {
		Writer writer = new WriterFactory().createWriter(javaProject.getProject().getName());
		Reporter reporter = new Reporter(writer);
		BugInstances bugInstances = new BugInstances();
		int objectSensitivityLevel = readConfigurationOptions().getObjectSensitivityLevel();
		reporter.report(new KeyValuePair("OBJECT_SENSITIVITY_LEVEL", String.valueOf(objectSensitivityLevel)));
		BasicAnalysisData basicAnalysisData = initBytecodeAnalysis(javaProject, reporter, objectSensitivityLevel);
		Iterator<BugPattern> bugPatternsIterator = BugPatterns.iterator();
		while (bugPatternsIterator.hasNext()) {
			BugPattern bugPattern = bugPatternsIterator.next();
			BugPatternDetector bugPatternDetector = bugPattern.createBugPatternDetector();
			stopWatch.start();
			BugInstances instancesOfCurrentBugPattern = bugPatternDetector.performAnalysis(javaProject, basicAnalysisData);
			stopWatch.stop();
			reporter.report(new KeyValuePair("BUG_PATTERN_" + bugPattern.getName() + "_DETECTION_TIME_IN_MILLISECONDS", String.valueOf(stopWatch.getElapsedMillis())));
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
			stopWatch.start();
			model.buildGraph();
			stopWatch.stop();
			reporter.report(new KeyValuePair("CALL_GRAPH_CONSTRUCTION_TIME_IN_MILLISECONDS", String.valueOf(stopWatch.getElapsedMillis())));
		} catch (Exception e) {
			throw new Exceptions.WALAInitializationException(e);
		}
		CallGraph callGraph = model.getGraph();
		reporter.report(new KeyValuePair("NUMBER_OF_NODES_OF_CALL_GRAPH", String.valueOf(callGraph.getNumberOfNodes())));
		PointerAnalysis pointerAnalysis = model.getPointerAnalysis();
		HeapModel heapModel = pointerAnalysis.getHeapModel();
		BasicHeapGraph basicHeapGraph = new BasicHeapGraph(pointerAnalysis, callGraph);
		if (!hasShownGraphs) {
			try {
				DisplayUtils.displayGraph(callGraph);
				DisplayUtils.displayGraph(basicHeapGraph);
				hasShownGraphs = true;
			} catch (WalaException e) {
				throw new WALAInitializationException(e);
			}
		}
		IClassHierarchy classHierarchy = model.getClassHierarchy();
		return new BasicAnalysisData(classHierarchy, callGraph, pointerAnalysis, heapModel, basicHeapGraph);
	}

}
