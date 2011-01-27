/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.walaconfig;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.translator.jdt.JDTJavaSourceAnalysisEngine;
import com.ibm.wala.ide.util.EclipseProjectPath;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.Entrypoint;
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
	protected CallGraphBuilder getCallGraphBuilder(IClassHierarchy classHierarchy, AnalysisOptions analysisOptions, AnalysisCache analysisCache) {
		return KeshmeshAnalysisEngine.getCallGraphBuilder(scope, classHierarchy, analysisOptions, analysisCache);
	}

	@Override
	protected Iterable<Entrypoint> makeDefaultEntrypoints(AnalysisScope scope, IClassHierarchy classHierarchy) {
		return KeshmeshAnalysisEngine.makeDefaultEntrypoints(JavaSourceAnalysisScope.SOURCE, classHierarchy);
	}

}
