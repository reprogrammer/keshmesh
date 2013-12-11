/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.core.IJavaProject;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.ibm.wala.analysis.pointers.BasicHeapGraph;
import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.io.FileProvider;

import edu.illinois.keshmesh.config.ConfigurationOptions;
import edu.illinois.keshmesh.constants.Constants;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPattern;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.exception.Exceptions;
import edu.illinois.keshmesh.detector.exception.Exceptions.WALAInitializationException;
import edu.illinois.keshmesh.detector.util.AnalysisUtils;
import edu.illinois.keshmesh.detector.util.DisplayUtils;
import edu.illinois.keshmesh.report.FileWriterFactory;
import edu.illinois.keshmesh.report.KeyValuePair;
import edu.illinois.keshmesh.report.Reporter;
import edu.illinois.keshmesh.report.StringWriterFactory;
import edu.illinois.keshmesh.walaconfig.KeshmeshCGModel;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class Main {

	private static boolean hasShownGraphs = false;

	private final static StringWriterFactory stringWriterFactory = new StringWriterFactory();

	public static BugInstances initAndPerformAnalysis(IJavaProject javaProject, Reporter reporter, ConfigurationOptions configurationOptions) throws WALAInitializationException {
		BugInstances bugInstances = new BugInstances();
		int objectSensitivityLevel = configurationOptions.getObjectSensitivityLevel();
		reporter.report(new KeyValuePair("OBJECT_SENSITIVITY_LEVEL", String.valueOf(objectSensitivityLevel)));
		BasicAnalysisData basicAnalysisData = initBytecodeAnalysis(javaProject, reporter, configurationOptions);
		if (configurationOptions.shouldDumpCallGraph()) {
			dumpCallGraph(basicAnalysisData.callGraph);
		}
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

	private static BasicAnalysisData initBytecodeAnalysis(IJavaProject javaProject, Reporter reporter, ConfigurationOptions configurationOptions) throws WALAInitializationException {
		KeshmeshCGModel model;
		try {
			String exclusionsFileName = FileProvider.getFileFromPlugin(Activator.getDefault(), "EclipseDefaultExclusions.txt").getAbsolutePath();
			model = new KeshmeshCGModel(javaProject, exclusionsFileName, configurationOptions.getObjectSensitivityLevel());
			Stopwatch stopWatch = Stopwatch.createStarted();
			model.buildGraph();
			stopWatch.stop();
			reporter.report(new KeyValuePair("CALL_GRAPH_CONSTRUCTION_TIME_IN_MILLISECONDS", String.valueOf(stopWatch.elapsed(TimeUnit.MILLISECONDS))));
			reportEntryPointStatistics(reporter, model.getEntryPoints());
			dumpEntryPoints(model.getEntryPoints());
		} catch (Exception e) {
			throw new Exceptions.WALAInitializationException(e);
		}
		CallGraph callGraph = model.getGraph();
		reportCallGraphStatistics(reporter, callGraph);
		PointerAnalysis pointerAnalysis = model.getPointerAnalysis();
		HeapModel heapModel = pointerAnalysis.getHeapModel();
		BasicHeapGraph heapGraph = new BasicHeapGraph(pointerAnalysis, callGraph);
		if (configurationOptions.shouldDumpHeapGraph()) {
			dumpHeapGraph(heapGraph);
		}
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

	private static void reportEntryPointStatistics(Reporter reporter, Iterable<Entrypoint> entryPoints) {
		reporter.report(new KeyValuePair("NUMBER_OF_ENTRY_POINTS", String.valueOf(Iterables.size(entryPoints))));
	}

	private static void dumpEntryPoints(Iterable<Entrypoint> entryPoints) {
		Writer writer = new FileWriterFactory(Constants.KESHMESH_ENTRY_POINTS_FILE_NAME, stringWriterFactory).create();
		List<Entrypoint> sortedEntryPoints = sortedCopy(entryPoints);
		try {
			writer.write(Joiner.on(Constants.LINE_SEPARATOR).join(sortedEntryPoints));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static List<Entrypoint> sortedCopy(Iterable<Entrypoint> entryPoints) {
		Ordering<Entrypoint> ordering = Ordering.natural().onResultOf(new Function<Entrypoint, String>() {
			@Override
			public String apply(Entrypoint entryPoint) {
				return entryPoint.toString();
			}
		});
		return ordering.sortedCopy(entryPoints);
	}

	private static void reportCallGraphStatistics(Reporter reporter, CallGraph callGraph) {
		reporter.report(new KeyValuePair("NUMBER_OF_NODES_OF_CALL_GRAPH", String.valueOf(callGraph.getNumberOfNodes())));
		Iterable<CGNode> primordialCGNodes = Iterables.filter(callGraph, new Predicate<CGNode>() {
			@Override
			public boolean apply(CGNode cgNode) {
				return AnalysisUtils.isJDKClass(cgNode.getMethod().getDeclaringClass());
			}
		});
		reporter.report(new KeyValuePair("NUMBER_OF_PRIMORDIAL_NODES_OF_CALL_GRAPH", String.valueOf(Iterables.size(primordialCGNodes))));
		Iterable<CGNode> extensionCGNodes = Iterables.filter(callGraph, new Predicate<CGNode>() {
			@Override
			public boolean apply(CGNode cgNode) {
				return AnalysisUtils.isLibraryClass(cgNode.getMethod().getDeclaringClass());
			}
		});
		reporter.report(new KeyValuePair("NUMBER_OF_EXTENSION_NODES_OF_CALL_GRAPH", String.valueOf(Iterables.size(extensionCGNodes))));
		Iterable<CGNode> applicationCGNodes = Iterables.filter(callGraph, new Predicate<CGNode>() {
			@Override
			public boolean apply(CGNode cgNode) {
				return AnalysisUtils.isApplicationClass(cgNode.getMethod().getDeclaringClass());
			}
		});
		reporter.report(new KeyValuePair("NUMBER_OF_APPLICATION_NODES_OF_CALL_GRAPH", String.valueOf(Iterables.size(applicationCGNodes))));
	}

	private static void dumpCallGraph(CallGraph callGraph) {
		Preconditions.checkNotNull(callGraph);
		Writer writer = new FileWriterFactory(Constants.KESHMESH_CALL_GRAPH_FILE_NAME, stringWriterFactory).create();
		Iterator<CGNode> cgNodesIter = callGraph.iterator();
		try {
			while (cgNodesIter.hasNext()) {
				CGNode cgNode = cgNodesIter.next();
				IMethod method = cgNode.getMethod();
				if (AnalysisUtils.isJDKClass(method.getDeclaringClass()))
					continue;
				writer.write("**CGNode:** " + cgNode + "\n");
				IR ir = cgNode.getIR();
				if (ir != null) {
					writer.write("**IR:** " + ir + "\n");
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static void dumpHeapGraph(HeapGraph heapGraph) {
		Preconditions.checkNotNull(heapGraph);
		Writer writer = new FileWriterFactory(Constants.KESHMESH_HEAP_GRAPH_FILE_NAME, stringWriterFactory).create();
		Preconditions.checkNotNull(heapGraph);
		Iterator<Object> cgNodesIter = heapGraph.iterator();
		try {
			while (cgNodesIter.hasNext()) {
				writer.write(cgNodesIter.next() + "\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
