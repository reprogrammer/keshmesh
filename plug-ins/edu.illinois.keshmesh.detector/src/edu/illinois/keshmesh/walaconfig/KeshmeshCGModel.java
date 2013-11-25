/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.walaconfig;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.eclipse.cg.model.WalaProjectCGModel;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.graph.InferGraphRoots;

public class KeshmeshCGModel extends WalaProjectCGModel {

	public KeshmeshCGModel(IJavaProject project, String exclusionsFile, int objectSensitivityLevel) throws IOException, CoreException {
		super(project, exclusionsFile);
		engine = new EclipseProjectAnalysisEngine(project, objectSensitivityLevel);
		engine.setExclusionsFile(exclusionsFile);
	}

	@Override
	protected Iterable<Entrypoint> getEntrypoints(AnalysisScope analysisScope, IClassHierarchy classHierarchy) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Collection<CGNode> inferRoots(CallGraph cg) throws WalaException {
		return InferGraphRoots.inferRoots(cg);
	}

	public PointerAnalysis getPointerAnalysis() {
		return engine.getPointerAnalysis();
	}

	public IClassHierarchy getClassHierarchy() {
		return engine.getClassHierarchy();
	}

}
