package edu.illinois.keshmesh.detector.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Test;

import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.illinois.keshmesh.detector.ConcurrencyBugsDetector;

public class ConcurrencyBugsDetectorTest extends AbstractTestCase {

	public static final IPath test1 = new Path("test-files/Test.java");

	@Test
	public void testRenameMethodRefactoringWrapping() throws Exception {
		File test1File = Activator.getDefault().getFileInPlugin(test1);
		String javaText = format(getFileContent(test1File.getAbsolutePath()));
		ICompilationUnit cu = createCU(packageP, "Test.java", javaText);
		proceedAfterBuilding();
	}

	private void proceedAfterBuilding() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor() {

			private boolean isDone = false;

			@Override
			public void done() {
				if (!isDone) {
					isDone = true;
					try {
						performTest();
					} catch (JavaModelException e) {
						e.printStackTrace();
					} catch (ClassHierarchyException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (CallGraphBuilderCancelException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}

		});
	}

	private String getFileContent(String fileName) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		StringBuilder sb = new StringBuilder();
		String str;
		while ((str = in.readLine()) != null) {
			sb.append(str);
		}
		in.close();
		return sb.toString();
	}

	private void performTest() throws JavaModelException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException, IOException, URISyntaxException {
		System.out.println("Compilation done");
		String classpath = javaProject.getOutputLocation().toOSString();
		ConcurrencyBugsDetector.initAndPerformAnalysis(classpath);
	}
}
