/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package edu.illinois.keshmesh.detector.wala;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.translator.jdt.JDTJavaSourceAnalysisEngine;
import com.ibm.wala.client.AbstractAnalysisEngine;
import com.ibm.wala.ide.util.EclipseProjectPath;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.warnings.WalaException;

/**
 * 
 * @see com.ibm.wala.eclipse.cg.model.WalaProjectCGModel
 * 
 */
abstract public class WalaProjectCGModel implements WalaCGModel {

	protected AbstractAnalysisEngine engine;

	protected CallGraph callGraph;

	protected Collection roots;

	protected WalaProjectCGModel(IJavaProject project, final String exclusionsFile) throws IOException, CoreException {
		final EclipseProjectPath ep = EclipseProjectPath.make(project, EclipseProjectPath.AnalysisScopeType.SOURCE_FOR_PROJ);

		this.engine = new JDTJavaSourceAnalysisEngine() {
			@Override
			public void buildAnalysisScope() {
				try {
					scope = ep.toAnalysisScope(new JavaSourceAnalysisScope());
					setExclusionsFile(exclusionsFile);
					scope.setExclusions(FileOfClasses.createFileOfClasses(new File(getExclusionsFile())));
				} catch (IOException e) {
					Assertions.UNREACHABLE(e.toString());
				}
			}

			@Override
			protected Iterable<Entrypoint> makeDefaultEntrypoints(AnalysisScope scope, IClassHierarchy cha) {
				return getEntrypoints(scope, cha);
			}
		};
	}

	//	protected WalaProjectCGModel(String htmlScriptFile) {
	//		this.engine = new JavaScriptAnalysisEngine() {
	//
	//			{
	//				setTranslatorFactory(new CAstRhinoTranslatorFactory());
	//			}
	//
	//			@Override
	//			protected Iterable<Entrypoint> makeDefaultEntrypoints(AnalysisScope scope, IClassHierarchy cha) {
	//				return getEntrypoints(scope, cha);
	//			}
	//		};
	//
	//		SourceFileModule script = WebUtil.extractScriptFromHTML(htmlScriptFile);
	//		engine.setModuleFiles(Collections.singleton(script));
	//	}

	public void buildGraph() throws WalaException, CancelException {
		try {
			callGraph = engine.buildDefaultCallGraph();
			roots = inferRoots(callGraph);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public CallGraph getGraph() {
		return callGraph;
	}

	public Collection getRoots() {
		return roots;
	}

	abstract protected Iterable<Entrypoint> getEntrypoints(AnalysisScope scope, IClassHierarchy cha);

	abstract protected Collection inferRoots(CallGraph cg) throws WalaException;

}
