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
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.graph.InferGraphRoots;
import com.ibm.wala.util.warnings.WalaException;

public class KeshmeshCGModelWithMain extends WalaProjectCGModel {

	public KeshmeshCGModelWithMain(IJavaProject project, String exclusionsFile) throws IOException, CoreException {
		super(project, exclusionsFile);
		engine = new EclipseProjectAnalysisEngine(project);
		engine.setExclusionsFile(exclusionsFile);
	}

	@Override
	protected Iterable<Entrypoint> getEntrypoints(AnalysisScope analysisScope, IClassHierarchy classHierarchy) {
		return Util.makeMainEntrypoints(analysisScope.getApplicationLoader(), classHierarchy);
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
