/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
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
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.strings.Atom;

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
//		ContextSelector contextSelector = new ContextInsensitiveSelector();
		//		ContextSelector contextSelector = new ReceiverTypeContextSelector();
				ContextSelector contextSelector = new CustomReceiverTypeContextSelector();
		Util.addDefaultSelectors(analysisOptions, classHierarchy);
		Util.addDefaultBypassLogic(analysisOptions, scope, Util.class.getClassLoader(), classHierarchy);
		return new KeshmeshCFABuilder(classHierarchy, analysisOptions, analysisCache, contextSelector, null);
	}

	@Override
	protected Iterable<Entrypoint> makeDefaultEntrypoints(AnalysisScope analysisScope, IClassHierarchy classHierarchy) {
		//Iterable<Entrypoint> mainEntrypoints = Util.makeMainEntrypoints(analysisScope.getApplicationLoader(), classHierarchy);
		//		return new FilteredIterable(mainEntrypoints);
		return makeCustomEntryPointes(analysisScope, classHierarchy);
	}

	private Iterable<Entrypoint> makeCustomEntryPointes(AnalysisScope analysisScope, IClassHierarchy classHierarchy) {
		//org.apache.catalina.startup.TestTomcat.testEnableNamingGlobal
		final HashSet<Entrypoint> result = HashSetFactory.make();
		ClassLoaderReference applicationLoader = analysisScope.getApplicationLoader();
		TypeReference declaringClassReference = TypeReference.findOrCreateClass(applicationLoader, "org/apache/catalina/startup", "TestTomcat");
		Atom testMethodName = Atom.findOrCreateAsciiAtom("testEnableNamingGlobal");
		MethodReference testMehodReference = MethodReference.findOrCreate(declaringClassReference, testMethodName, Descriptor.findOrCreateUTF8("()V"));
		IClass declaringClass = classHierarchy.lookupClass(declaringClassReference);
		IMethod testMethod = declaringClass.getMethod(testMehodReference.getSelector());
		System.out.println("testMethod:" + testMethod.getSignature());
		result.add(new DefaultEntrypoint(testMethod, classHierarchy));

		return new Iterable<Entrypoint>() {
			public Iterator<Entrypoint> iterator() {
				return result.iterator();
			}
		};
	}

	static class FilteredIterable implements Iterable<Entrypoint> {

		Iterable<Entrypoint> originalIterable;

		public FilteredIterable(Iterable<Entrypoint> originalIterable) {
			super();
			this.originalIterable = originalIterable;
		}

		@Override
		public Iterator<Entrypoint> iterator() {
			final HashSet<Entrypoint> filteredEntrypoints = HashSetFactory.make();
			Iterator<Entrypoint> originalItarator = originalIterable.iterator();
			while (originalItarator.hasNext()) {
				Entrypoint nextEntrypoint = originalItarator.next();
				if (nextEntrypoint.getMethod().getDeclaringClass().getName().getPackage().toString().contains("org/apache/catalina/startup/Bootstrap")) {
					filteredEntrypoints.add(nextEntrypoint);
				}
			}
			return filteredEntrypoints.iterator();
		}
	}

}
