/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.walaconfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.client.AbstractAnalysisEngine;
import com.ibm.wala.ide.util.EclipseProjectPath;
import com.ibm.wala.ide.util.EclipseProjectPath.AnalysisScopeType;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.ClassTargetSelector;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.BypassClassTargetSelector;
import com.ibm.wala.ipa.summaries.BypassMethodTargetSelector;
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.XMLMethodSummaryReader;
import com.ibm.wala.types.MemberReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.strings.Atom;

import edu.illinois.keshmesh.detector.util.AnalysisUtils;

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
			analysisScope = eclipseProjectPath.toAnalysisScope(new File(getExclusionsFile()));
		} catch (CoreException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 
	 * See com.ibm.wala.ipa.callgraph.impl.Util.addBypassLogic(AnalysisOptions,
	 * AnalysisScope, ClassLoader, String, IClassHierarchy)
	 * 
	 * @param classHierarchy
	 * @param analysisOptions
	 * @throws IllegalArgumentException
	 */
	private void addCustomBypassLogic(IClassHierarchy classHierarchy, AnalysisOptions analysisOptions) throws IllegalArgumentException {
		ClassLoader classLoader = Util.class.getClassLoader();
		if (classLoader == null) {
			throw new IllegalArgumentException("classLoader is null");
		}

		Util.addDefaultSelectors(analysisOptions, classHierarchy);

		InputStream inputStream = classLoader.getResourceAsStream(Util.nativeSpec);
		XMLMethodSummaryReader methodSummaryReader = new XMLMethodSummaryReader(inputStream, analysisScope);

		MethodTargetSelector customMethodTargetSelector = getCustomBypassMethodTargetSelector(classHierarchy, analysisOptions, methodSummaryReader);
		analysisOptions.setSelector(customMethodTargetSelector);

		ClassTargetSelector customClassTargetSelector = new BypassClassTargetSelector(analysisOptions.getClassTargetSelector(), methodSummaryReader.getAllocatableClasses(), classHierarchy,
				classHierarchy.getLoader(analysisScope.getLoader(Atom.findOrCreateUnicodeAtom("Synthetic"))));
		analysisOptions.setSelector(customClassTargetSelector);
	}

	private BypassMethodTargetSelector getCustomBypassMethodTargetSelector(IClassHierarchy classHierarchy, AnalysisOptions analysisOptions, XMLMethodSummaryReader summary) {
		return new KeshmeshBypassMethodTargetSelector(analysisOptions.getMethodTargetSelector(), summary.getSummaries(), summary.getIgnoredPackages(), classHierarchy);
	}

	@Override
	protected CallGraphBuilder getCallGraphBuilder(IClassHierarchy classHierarchy, AnalysisOptions analysisOptions, AnalysisCache analysisCache) {
		System.out.println("edu.illinois.keshmesh.walaconfig.EclipseProjectAnalysisEngine.getCallGraphBuilder(IClassHierarchy, AnalysisOptions, AnalysisCache)");
		addCustomBypassLogic(classHierarchy, analysisOptions);
		return KeshmeshAnalysisEngine.getCallGraphBuilder(analysisScope, classHierarchy, analysisOptions, analysisCache);
	}

	@Override
	protected Iterable<Entrypoint> makeDefaultEntrypoints(AnalysisScope analysisScope, IClassHierarchy classHierarchy) {
		return KeshmeshAnalysisEngine.makeDefaultEntrypoints(analysisScope.getApplicationLoader(), classHierarchy);
	}

}

class KeshmeshBypassMethodTargetSelector extends BypassMethodTargetSelector {

	public KeshmeshBypassMethodTargetSelector(MethodTargetSelector parent, Map<MethodReference, MethodSummary> methodSummaries, Set<Atom> ignoredPackages, IClassHierarchy cha) {
		super(parent, methodSummaries, ignoredPackages, cha);
	}

	@Override
	protected boolean canIgnore(MemberReference m) {
		if (AnalysisUtils.isLibraryClass(m.getDeclaringClass()) || (AnalysisUtils.isJDKClass(m.getDeclaringClass()) && !AnalysisUtils.isObjectGetClass(m))) {
			return true;
		} else {
			return super.canIgnore(m);
		}
	}
}
