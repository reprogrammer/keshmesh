package edu.illinois.keshmesh.detector.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Test;

import edu.illinois.keshmesh.detector.ConcurrencyBugsDetector;
import edu.illinois.keshmesh.detector.exception.Exceptions.WALAInitializationException;

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
					} catch (WALAInitializationException e) {
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

	private void performTest() throws WALAInitializationException {
		System.out.println("Compilation done");
		//String classpath = javaProject.getOutputLocation().toOSString();
		ConcurrencyBugsDetector.initAndPerformAnalysis(javaProject);
	}
}
