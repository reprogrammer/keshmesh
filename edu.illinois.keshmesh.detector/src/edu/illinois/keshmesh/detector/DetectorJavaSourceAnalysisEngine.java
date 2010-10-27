/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.cast.java.ipa.callgraph.AstJavaZeroOneContainerCFABuilder;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.translator.jdt.JDTJavaSourceAnalysisEngine;
import com.ibm.wala.ide.util.EclipseProjectPath;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.config.FileOfClasses;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class DetectorJavaSourceAnalysisEngine extends JDTJavaSourceAnalysisEngine {

	private IJavaProject javaProject = null;
	private String exclusionsFile = null;

	public DetectorJavaSourceAnalysisEngine(IJavaProject javaProject, String exclusionsFile) {
		this.javaProject = javaProject;
		this.exclusionsFile = exclusionsFile;
	}

	@Override
	public void buildAnalysisScope() throws IOException {
		try {
			EclipseProjectPath projectPath = EclipseProjectPath.make(javaProject, EclipseProjectPath.AnalysisScopeType.SOURCE_FOR_PROJ);
			scope = projectPath.toAnalysisScope(new JavaSourceAnalysisScope());
			setExclusionsFile(exclusionsFile);
			scope.setExclusions(FileOfClasses.createFileOfClasses(new File(getExclusionsFile())));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Iterable<Entrypoint> makeDefaultEntrypoints(AnalysisScope scope, IClassHierarchy cha) {
		return com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(JavaSourceAnalysisScope.SOURCE, cha);
	}

	@Override
	protected CallGraphBuilder getCallGraphBuilder(IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache) {
		ContextSelector contextSelector = new CustomContextSelector();
		//com.ibm.wala.ipa.callgraph.CallGraphBuilder builder = Util.makeVanillaZeroOneCFABuilder(options, cache, cha, scope, null, null);
		Util.addDefaultSelectors(options, cha);
		Util.addDefaultBypassLogic(options, scope, Util.class.getClassLoader(), cha);
		return new AstJavaZeroOneContainerCFABuilder(cha, options, cache, contextSelector, null);

		//return new ZeroCFABuilderFactory().make(options, cache, cha, scope, false);
		//return builder;
	}

}
