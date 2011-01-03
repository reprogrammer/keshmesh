/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.client.AbstractAnalysisEngine;
import com.ibm.wala.ide.util.EclipseProjectPath;
import com.ibm.wala.ide.util.EclipseProjectPath.AnalysisScopeType;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.ContextInsensitiveSelector;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.IClassHierarchy;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class EclipseProjectAnalysisEngine extends AbstractAnalysisEngine {

	protected final IJavaProject javaProject;

	public EclipseProjectAnalysisEngine(IJavaProject javaProject) {
		this.javaProject = javaProject;
	}

	@Override
	public void buildAnalysisScope() throws IOException {
		try {
			EclipseProjectPath eclipseProjectPath = EclipseProjectPath.make(javaProject, AnalysisScopeType.NO_SOURCE);
			scope = eclipseProjectPath.toAnalysisScope(new File(getExclusionsFile()));
		} catch (CoreException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected CallGraphBuilder getCallGraphBuilder(IClassHierarchy classHierarchy, AnalysisOptions analysisOptions, AnalysisCache analysisCache) {
		//		ContextSelector contextSelector = new CustomReceiverInstanceContextSelector();
		ContextSelector contextSelector = new ContextInsensitiveSelector();
		//		ContextSelector contextSelector = new ReceiverTypeContextSelector();
		//		ContextSelector contextSelector = new CustomReceiverTypeContextSelector();
		Util.addDefaultSelectors(analysisOptions, classHierarchy);
		Util.addDefaultBypassLogic(analysisOptions, scope, Util.class.getClassLoader(), classHierarchy);
		return new KeshmeshCFABuilder(classHierarchy, analysisOptions, analysisCache, contextSelector, null);
	}

	@Override
	protected Iterable<Entrypoint> makeDefaultEntrypoints(AnalysisScope analysisScope, IClassHierarchy classHierarchy) {
		Iterable<Entrypoint> mainEntrypoints = Util.makeMainEntrypoints(analysisScope.getApplicationLoader(), classHierarchy);
		return mainEntrypoints;
	}

}
