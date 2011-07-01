/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.walaconfig;

import java.util.HashSet;
import java.util.Iterator;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXCFABuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.strings.Atom;

import edu.illinois.keshmesh.annotations.EntryPoint;
import edu.illinois.keshmesh.detector.util.AnalysisUtils;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class KeshmeshAnalysisEngine {

	public static Iterable<Entrypoint> makeDefaultEntrypoints(ClassLoaderReference classLoaderReference, IClassHierarchy classHierarchy) {
		//Iterable<Entrypoint> mainEntrypoints = Util.makeMainEntrypoints(analysisScope.getApplicationLoader(), classHierarchy);
		//		return new FilteredIterable(mainEntrypoints);
		//		return Util.makeMainEntrypoints(classLoaderReference, classHierarchy);
		return makeAnnotatedEntryPoints(classHierarchy);
	}

	public static CallGraphBuilder getCallGraphBuilder(AnalysisScope analysisScope, IClassHierarchy classHierarchy, AnalysisOptions analysisOptions, AnalysisCache analysisCache) {
		ContextSelector contextSelector = new KObjectSensitiveContextSelector();
		Util.addDefaultSelectors(analysisOptions, classHierarchy);
		Util.addDefaultBypassLogic(analysisOptions, analysisScope, Util.class.getClassLoader(), classHierarchy);
		//		return new KeshmeshCFABuilder(classHierarchy, analysisOptions, analysisCache, contextSelector, null);
		return makeZeroOneCFABuilder(analysisOptions, analysisCache, classHierarchy, analysisScope, contextSelector, null);
	}

	public static SSAPropagationCallGraphBuilder makeZeroOneCFABuilder(AnalysisOptions options, AnalysisCache cache, IClassHierarchy cha, AnalysisScope scope, ContextSelector customSelector,
			SSAContextInterpreter customInterpreter) {

		if (options == null) {
			throw new IllegalArgumentException("options is null");
		}
		Util.addDefaultSelectors(options, cha);
		Util.addDefaultBypassLogic(options, scope, Util.class.getClassLoader(), cha);

		return ZeroXCFABuilder.make(cha, options, cache, customSelector, customInterpreter, ZeroXInstanceKeys.ALLOCATIONS | ZeroXInstanceKeys.SMUSH_MANY | ZeroXInstanceKeys.SMUSH_THROWABLES);
	}

	public static Iterable<Entrypoint> makeAnnotatedEntryPoints(IClassHierarchy classHierarchy) {
		final HashSet<Entrypoint> result = HashSetFactory.make();
		Iterator<IClass> classIterator = classHierarchy.iterator();
		while (classIterator.hasNext()) {
			IClass klass = classIterator.next();
			if (!AnalysisUtils.isJDKClass(klass)) {
				// Logger.log("Visiting class " + klass);
				for (IMethod method : klass.getDeclaredMethods()) {
					try {
						if (!(method instanceof ShrikeCTMethod)) {
							throw new RuntimeException("@EntryPoint only works for byte code.");
						}
						// Logger.log("Visiting method " + method);
						for (Annotation annotation : ((ShrikeCTMethod) method).getAnnotations(true)) {
							//	Logger.log("Visiting annotation " + annotation);
							if (isEntryPointClass(annotation.getType().getName())) {
								result.add(new DefaultEntrypoint(method, classHierarchy));
								break;
							}
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return new Iterable<Entrypoint>() {
			public Iterator<Entrypoint> iterator() {
				return result.iterator();
			}
		};
	}

	private static boolean isEntryPointClass(TypeName typeName) {
		return (AnalysisUtils.walaTypeNameToJavaName(typeName).equals(EntryPoint.class.getName()));
	}

	public static Iterable<Entrypoint> makeCustomEntryPoints(AnalysisScope analysisScope, IClassHierarchy classHierarchy) {
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

	//	public static Iterable<Entrypoint> makeCustomEntryPoints(AnalysisScope analysisScope, IClassHierarchy classHierarchy) {
	//		//org.apache.catalina.startup.TestTomcat.testEnableNamingGlobal
	//		final HashSet<Entrypoint> result = HashSetFactory.make();
	//		ClassLoaderReference applicationLoader = analysisScope.getApplicationLoader();
	//		//		TypeReference declaringClassReference = TypeReference.findOrCreateClass(applicationLoader, "org/apache/catalina/startup", "TestTomcat");
	//		//		TypeReference declaringClassReference = TypeReference.findOrCreateClass(applicationLoader, "javax/servlet/http", "HttpServlet");
	//		TypeReference declaringClassReference = TypeReference.findOrCreateClass(applicationLoader, "org/apache/catalina/startup", "TestTomcat$HelloWorldJndi");
	//		//		TypeReference declaringClassReference = TypeReference.findOrCreateClass(applicationLoader, "org/apache/naming/java", "javaURLContextFactory");
	//		//		Atom testMethodName = Atom.findOrCreateAsciiAtom("service");
	//		//		MethodReference testMehodReference = MethodReference.findOrCreate(declaringClassReference, testMethodName, Descriptor.findOrCreateUTF8("(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;)V"));
	//		Atom testMethodName = Atom.findOrCreateAsciiAtom("doGet");
	//		MethodReference testMehodReference = MethodReference.findOrCreate(declaringClassReference, testMethodName,
	//				Descriptor.findOrCreateUTF8("(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V"));
	//		//		Atom testMethodName = Atom.findOrCreateAsciiAtom("getInitialContext");
	//		//		MethodReference testMehodReference = MethodReference.findOrCreate(declaringClassReference, testMethodName, Descriptor.findOrCreateUTF8("(Ljava/util/Hashtable;)Ljavax/naming/Context;"));
	//		IClass declaringClass = classHierarchy.lookupClass(declaringClassReference);
	//		//		for (IMethod method : declaringClass.getAllMethods()){
	//		//			System.out.println("Signature:" + method.getSignature());
	//		//		}
	//		IMethod testMethod = declaringClass.getMethod(testMehodReference.getSelector());
	//		System.out.println("testMethod:" + testMethod.getSignature());
	//		result.add(new DefaultEntrypoint(testMethod, classHierarchy));
	//
	//		return new Iterable<Entrypoint>() {
	//			public Iterator<Entrypoint> iterator() {
	//				return result.iterator();
	//			}
	//		};
	//	}
	//
	//	static class FilteredIterable implements Iterable<Entrypoint> {
	//
	//		Iterable<Entrypoint> originalIterable;
	//
	//		public FilteredIterable(Iterable<Entrypoint> originalIterable) {
	//			super();
	//			this.originalIterable = originalIterable;
	//		}
	//
	//		@Override
	//		public Iterator<Entrypoint> iterator() {
	//			final HashSet<Entrypoint> filteredEntrypoints = HashSetFactory.make();
	//			Iterator<Entrypoint> originalItarator = originalIterable.iterator();
	//			while (originalItarator.hasNext()) {
	//				Entrypoint nextEntrypoint = originalItarator.next();
	//				if (nextEntrypoint.getMethod().getDeclaringClass().getName().getPackage().toString().contains("org/apache/catalina/startup/Bootstrap")) {
	//					filteredEntrypoints.add(nextEntrypoint);
	//				}
	//			}
	//			return filteredEntrypoints.iterator();
	//		}
	//	}

}
