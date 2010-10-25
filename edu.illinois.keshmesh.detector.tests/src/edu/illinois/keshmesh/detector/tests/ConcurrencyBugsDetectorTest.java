package edu.illinois.keshmesh.detector.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Before;
import org.junit.Test;

import edu.illinois.keshmesh.detector.ConcurrencyBugsDetector;
import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.BugPosition;
import edu.illinois.keshmesh.detector.exception.Exceptions.WALAInitializationException;
import edu.illinois.keshmesh.transformer.core.LCK02JFixer;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class ConcurrencyBugsDetectorTest extends AbstractTestCase {

	private static final IPath testClass1 = new Path("test-files/Test.java");

	private IPath compilationUnitPath = null;

	@Before
	public void addTestClass() throws Exception {
		setUpProject();
		File test1File = Activator.getDefault().getFileInPlugin(testClass1);
		String javaText = format(getFileContent(test1File.getAbsolutePath()));
		ICompilationUnit compilationUnit = createCU(packageP, "Test.java", javaText);
		compilationUnitPath = compilationUnit.getResource().getLocation();
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

	@Test
	public void shouldFindLCK02J() throws WALAInitializationException {
		BugInstances bugInstances = ConcurrencyBugsDetector.initAndPerformAnalysis(javaProject);
		System.out.println(bugInstances.toString());
		for (BugInstance bugInstance : bugInstances) {
			BugPosition bugPosition = bugInstance.getBugPosition();
			LCK02JFixer fixer = new LCK02JFixer(bugPosition);
			try {
				fixer.createChange(new NullProgressMonitor());
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		Assert.assertTrue(bugInstances.contains(new BugInstance(BugPatterns.LCK02J, new BugPosition(208, 249, compilationUnitPath))));
	}
}
