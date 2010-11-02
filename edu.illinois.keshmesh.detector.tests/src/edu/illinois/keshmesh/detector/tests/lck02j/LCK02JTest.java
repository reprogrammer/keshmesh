/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck02j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;

import edu.illinois.keshmesh.detector.ConcurrencyBugsDetector;
import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.BugPosition;
import edu.illinois.keshmesh.detector.bugs.LCK02JFixInformation;
import edu.illinois.keshmesh.detector.exception.Exceptions.WALAInitializationException;
import edu.illinois.keshmesh.detector.tests.AbstractTestCase;
import edu.illinois.keshmesh.detector.tests.Activator;
import edu.illinois.keshmesh.detector.util.SetUtils;
import edu.illinois.keshmesh.transformer.core.LCK02JFixer;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
abstract public class LCK02JTest extends AbstractTestCase {

	protected Map<String, IPath> targetTestClassPathMap = new HashMap<String, IPath>();
	protected BugInstances bugInstances;
	protected String testNumber;

	private String getPathForClass(String testClass) {
		return "test-files/LCK02J/" + testNumber + "/" + testClass;
	}

	private String getTestID() {
		return "LCK02J-" + testNumber;
	}

	public void setupProjectAndAnalyze(String testNumber, String... testClasses) throws Exception {
		this.testNumber = testNumber;
		setUpProject(getTestID());
		for (String testClass : testClasses) {
			addTestClass(testClass);
		}
		findBugs();
	}

	private void addTestClass(String testClass) throws Exception {
		String pathForClass = getPathForClass(testClass);
		Path testClassPath = new Path(pathForClass);
		File test1File = Activator.getDefault().getFileInPlugin(testClassPath);
		String javaText = format(getFileContent(test1File.getAbsolutePath()));
		ICompilationUnit compilationUnit = createCU(packageP, testClassPath.lastSegment(), javaText);
		targetTestClassPathMap.put(pathForClass, compilationUnit.getResource().getLocation());
	}

	public IPath getTargetPathForClass(String testClass) {
		String pathForClass = getPathForClass(testClass);
		if (!targetTestClassPathMap.containsKey(pathForClass)) {
			throw new RuntimeException("Could not find the path to test class \"" + pathForClass + "\"");
		}
		return targetTestClassPathMap.get(pathForClass);
	}

	public void findBugs() throws WALAInitializationException {
		bugInstances = ConcurrencyBugsDetector.initAndPerformAnalysis(javaProject);
		System.out.println(bugInstances);
	}

	public void findAndFixBugs(BugInstance bugInstance) {
		LCK02JFixer fixer = new LCK02JFixer(bugInstance);
		try {
			if (fixer.checkInitialConditions(new NullProgressMonitor()).isOK()) {
				fixer.createChange(new NullProgressMonitor());
			}
		} catch (OperationCanceledException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	protected void bugInstanceShouldExist(int firstLine, int lastLine, String className, String... replacements) {
		Assert.assertTrue(bugInstances.contains(new BugInstance(BugPatterns.LCK02J, new BugPosition(firstLine, lastLine, getTargetPathForClass(className)), new LCK02JFixInformation(SetUtils
				.asSet(replacements)))));
	}

	public void checkNumberOfBugInstances(int numOfBugInstances) {
		Assert.assertEquals(numOfBugInstances, bugInstances.size());
	}

}
