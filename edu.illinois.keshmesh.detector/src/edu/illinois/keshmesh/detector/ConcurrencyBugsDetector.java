package edu.illinois.keshmesh.detector;

import java.util.Iterator;

import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.analysis.pointers.BasicHeapGraph;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AbstractTypeInNode;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.NormalAllocationInNode;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.ReceiverInstanceContext;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.intset.OrdinalSet;
import com.ibm.wala.util.io.FileProvider;

import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.BugPosition;
import edu.illinois.keshmesh.detector.bugs.LCK02JFixInformation;
import edu.illinois.keshmesh.detector.exception.Exceptions;
import edu.illinois.keshmesh.detector.exception.Exceptions.WALAInitializationException;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian
 * 
 */
public class ConcurrencyBugsDetector {

	//	public static String REGRESSION_EXCLUSIONS = "dat" + System.getProperty("file.separator") + "Java60RegressionExclusions.txt";

	//	private static final String MAIN_METHOD_SELECTOR = "main([Ljava/lang/String;)V";

	private static final String JAVA_LANG_CLASS = "Ljava/lang/Class";

	private static final String OBJECT_GETCLASS_SIGNATURE = "java.lang.Object.getClass()Ljava/lang/Class;";

	private static DetectorJavaSourceAnalysisEngine engine;
	//private static AnalysisScope analysisScope;
	//private static AnalysisOptions analysisOptions;
	private static ClassHierarchy classHierarchy;
	private static CallGraph callGraph;
	private static PointerAnalysis pointerAnalysis;
	private static HeapModel heapModel;
	private static BasicHeapGraph basicHeapGraph;

	public static BugInstances initAndPerformAnalysis(IJavaProject javaProject) throws WALAInitializationException {
		initAnalysis(javaProject);
		return performAnalysis();
	}

	private static void initAnalysis(IJavaProject javaProject) throws WALAInitializationException {
		//		InputStream regressionExclusions = new FileInputStream(new File(FileLocator.toFileURL(FileLocator.find(Activator.getContext().getBundle(), new Path(REGRESSION_EXCLUSIONS), null)).toURI()));
		//		analysisScope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, regressionExclusions);
		//		classHierarchy = ClassHierarchy.make(analysisScope);
		//
		//		analysisOptions = new AnalysisOptions(analysisScope, makeMainMethodEntrypoints());
		//
		//		ContextSelector contextSelector = new CustomContextSelector();
		//
		//		com.ibm.wala.ipa.callgraph.CallGraphBuilder builder = Util.makeVanillaZeroOneCFABuilder(analysisOptions, new AnalysisCache(), classHierarchy, analysisScope, contextSelector, null);

		//		callGraph = builder.makeCallGraph(analysisOptions, null);
		//		pointerAnalysis = builder.getPointerAnalysis();
		//		heapModel = pointerAnalysis.getHeapModel();
		//		basicHeapGraph = new BasicHeapGraph(pointerAnalysis, callGraph);

		try {
			String exclusionsFileName = FileProvider.getFileFromPlugin(Activator.getDefault(), "EclipseDefaultExclusions.txt").getAbsolutePath();
			engine = new DetectorJavaSourceAnalysisEngine(javaProject, exclusionsFileName);
			callGraph = engine.buildDefaultCallGraph();
		} catch (Exception e) {
			throw new Exceptions.WALAInitializationException(e);
		}
		pointerAnalysis = engine.getPointerAnalysis();
		heapModel = pointerAnalysis.getHeapModel();
		basicHeapGraph = new BasicHeapGraph(pointerAnalysis, callGraph);
	}

	//	public static Iterable<Entrypoint> makeMainMethodEntrypoints() {
	//		if (analysisScope == null) {
	//			throw new IllegalArgumentException("analysisScope is null");
	//		}
	//		if (classHierarchy == null) {
	//			throw new IllegalArgumentException("classHierarchy is null");
	//		}
	//
	//		final HashSet<Entrypoint> result = HashSetFactory.make();
	//
	//		Iterator<IClass> classIterator = classHierarchy.iterator();
	//		while (classIterator.hasNext()) {
	//			IClass klass = classIterator.next();
	//			if (analysisScope.isApplicationLoader(klass.getClassLoader())) {
	//				Iterator<IMethod> methodIterator = klass.getDeclaredMethods().iterator();
	//				while (methodIterator.hasNext()) {
	//					IMethod method = methodIterator.next();
	//					if (method.getSelector().toString().equals(MAIN_METHOD_SELECTOR)) {
	//						System.out.println("Entrypoint signature: " + method.getSignature());
	//						Entrypoint entrypoint = new DefaultEntrypoint(method, classHierarchy);
	//						result.add(entrypoint);
	//					}
	//				}
	//			}
	//		}
	//		return new Iterable<Entrypoint>() {
	//			public Iterator<Entrypoint> iterator() {
	//				return result.iterator();
	//			}
	//		};
	//	}

	private static String getReceiverTypeName(AbstractTypeInNode node) {
		TypeName typeName = ((ReceiverInstanceContext) (node.getNode().getContext())).getReceiver().getConcreteType().getName();
		String fullyQualifiedName = typeName.getPackage() + "." + typeName.getClassName() + ".class";

		//WALA uses $ to refers to inner classes. We have to replace $ by . to make it a valid class name in Java source code.
		return fullyQualifiedName.replace("$", ".");
	}

	private static BugInstances performAnalysis() {
		BugInstances bugInstances = new BugInstances();
		Iterator<CGNode> cgNodesIterator = callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			CGNode cgNode = cgNodesIterator.next();
			IMethod method = cgNode.getMethod();
			System.out.println("CGNode:" + cgNode);
			IR ir = cgNode.getIR();
			if (ir != null) {
				System.out.println("IR:" + ir);
				SSAInstruction[] instructions = ir.getInstructions();
				for (int instructionIndex = 0; instructionIndex < instructions.length; instructionIndex++) {
					SSAInstruction instruction = instructions[instructionIndex];
					if (instruction instanceof SSAMonitorInstruction) {
						SSAMonitorInstruction monitorInstruction = (SSAMonitorInstruction) instruction;
						if (monitorInstruction.isMonitorEnter()) {
							int lockValueNumber = monitorInstruction.getRef();
							PointerKey lockPointer = heapModel.getPointerKeyForLocal(cgNode, lockValueNumber);
							OrdinalSet<InstanceKey> lockObjects = pointerAnalysis.getPointsToSet(lockPointer);
							for (InstanceKey instanceKey : lockObjects) {
								System.out.println("InstanceKey:" + instanceKey);
								if (instanceKey instanceof NormalAllocationInNode) {
									NormalAllocationInNode normalAllocationInNode = (NormalAllocationInNode) instanceKey;
									if (normalAllocationInNode.getSite().getDeclaredType().getName().toString().equals(JAVA_LANG_CLASS)) {
										if (normalAllocationInNode.getNode().getMethod().getSignature().toString().equals(OBJECT_GETCLASS_SIGNATURE)) {
											int lineNumber = ((AstMethod) method).getLineNumber(instructionIndex);
											Position position = ((AstMethod) method).getSourcePosition(instructionIndex);
											System.out.println("Detected an instance of LCK02-J in class " + method.getDeclaringClass().getName() + ", line number=" + lineNumber);
											bugInstances.add(new BugInstance(BugPatterns.LCK02J, new BugPosition(position), new LCK02JFixInformation(getReceiverTypeName(normalAllocationInNode))));
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return bugInstances;
	}

}
