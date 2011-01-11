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

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.translator.jdt.JDTJavaSourceAnalysisEngine;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ide.util.EclipseProjectPath;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.ContextInsensitiveSelector;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.ReceiverTypeContextSelector;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.strings.Atom;

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
//		return makeCustomEntryPointes(scope, cha);
	}

	private Iterable<Entrypoint> makeCustomEntryPointes(AnalysisScope analysisScope, IClassHierarchy classHierarchy) {
		//org.apache.catalina.startup.TestTomcat.testEnableNamingGlobal
		final HashSet<Entrypoint> result = HashSetFactory.make();
		ClassLoaderReference loaderReference = JavaSourceAnalysisScope.SOURCE;
		//		TypeReference declaringClassReference = TypeReference.findOrCreateClass(applicationLoader, "org/apache/catalina/startup", "TestTomcat");
//		TypeReference declaringClassReference = TypeReference.findOrCreateClass(applicationLoader, "javax/servlet/http", "HttpServlet");
		TypeReference declaringClassReference = TypeReference.findOrCreateClass(loaderReference, "org/apache/tomcat/lite/io", "SocketConnector");
		//		TypeReference declaringClassReference = TypeReference.findOrCreateClass(applicationLoader, "org/apache/naming/java", "javaURLContextFactory");
//		Atom testMethodName = Atom.findOrCreateAsciiAtom("service");
//		MethodReference testMehodReference = MethodReference.findOrCreate(declaringClassReference, testMethodName, Descriptor.findOrCreateUTF8("(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;)V"));
		Atom testMethodName = Atom.findOrCreateAsciiAtom("getSelector");
		MethodReference testMehodReference = MethodReference.findOrCreate(declaringClassReference, testMethodName, Descriptor.findOrCreateUTF8("()Lorg/apache/tomcat/lite/io/NioThread;"));
		//		Atom testMethodName = Atom.findOrCreateAsciiAtom("getInitialContext");
		//		MethodReference testMehodReference = MethodReference.findOrCreate(declaringClassReference, testMethodName, Descriptor.findOrCreateUTF8("(Ljava/util/Hashtable;)Ljavax/naming/Context;"));
		IClass declaringClass = classHierarchy.lookupClass(declaringClassReference);
		//		for (IMethod method : declaringClass.getAllMethods()){
		//			System.out.println("Signature:" + method.getSignature());
		//		}
		IMethod testMethod = declaringClass.getMethod(testMehodReference.getSelector());
		System.out.println("testMethod:" + testMethod.getSignature());
		result.add(new DefaultEntrypoint(testMethod, classHierarchy));

		return new Iterable<Entrypoint>() {
			public Iterator<Entrypoint> iterator() {
				return result.iterator();
			}
		};
	}
	@Override
	protected CallGraphBuilder getCallGraphBuilder(IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache) {
		//		ContextSelector contextSelector = new ContextInsensitiveSelector();
		//		ContextSelector contextSelector = new ReceiverTypeContextSelector();
		//		ContextSelector contextSelector = new CustomReceiverTypeContextSelector();
		ContextSelector contextSelector = new CustomReceiverInstanceContextSelector();
		Util.addDefaultSelectors(options, cha);
		Util.addDefaultBypassLogic(options, scope, Util.class.getClassLoader(), cha);
		return new KeshmeshCFABuilder(cha, options, cache, contextSelector, null);
	}

}
