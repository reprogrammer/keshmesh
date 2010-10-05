package edu.illinois.keshmesh.detector.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.Assert;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import edu.illinois.keshmesh.detector.ConcurrencyBugsDetector;
import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.Position;
import edu.illinois.keshmesh.detector.exception.Exceptions.WALAInitializationException;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class ConcurrencyBugsDetectorTest extends AbstractTestCase {

	public static final IPath testClass1 = new Path("test-files/Test.java");

	@Before
	public void addTestClass() throws Exception {
		setUpProject();
		File test1File = Activator.getDefault().getFileInPlugin(testClass1);
		String javaText = format(getFileContent(test1File.getAbsolutePath()));
		createCU(packageP, "Test.java", javaText);
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
		Assert.assertTrue(bugInstances.contains(new BugInstance(BugPatterns.LCK02J, new Position(203, 253))));
	}
}
