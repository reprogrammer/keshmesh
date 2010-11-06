/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck02j;

import java.io.File;
import java.io.IOException;
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

	/*
	 * Maps absolute path of the input Java file into the absolute path of the
	 * file in the target workspace.
	 */
	protected Map<String, IPath> inputFileToTargetMap = new HashMap<String, IPath>();

	protected BugInstances bugInstances;
	protected String testNumber;

	private static String join(String... pathElements) {
		StringBuilder sb = new StringBuilder();
		for (String pathElement : pathElements) {
			if (!pathElement.isEmpty()) {
				sb.append(pathElement);
				sb.append(File.separator);
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	private String getPathForInputFile(String inputFileName) {
		String prefix = join("test-files", "LCK02J", testNumber);
		Assert.assertTrue(String.format("The path %s contains \"in\"", prefix), !prefix.contains("in"));
		return join(prefix, "in", inputFileName);
	}

	private String getPathForOutputFile(String inputFileName, String bugInstanceNumber) {
		return inputFileName.replaceFirst("in", join("out", bugInstanceNumber));
	}

	private String getTestID() {
		return "LCK02J-" + testNumber;
	}

	public void setupProjectAndAnalyze(String testNumber, String... inputFileNames) throws Exception {
		this.testNumber = testNumber;
		setUpProject(getTestID());
		for (String inputFileName : inputFileNames) {
			addFile(inputFileName);
		}
		findBugs();
	}

	private void addFile(String inputFileName) throws Exception {
		String inputFileString = getPathForInputFile(inputFileName);
		Path inputFilePath = new Path(inputFileString);
		File inputFile = Activator.getDefault().getFileInPlugin(inputFilePath);
		String inputFileContents = format(getFileContent(inputFile.getAbsolutePath()));
		ICompilationUnit compilationUnit = createCU(packageP, inputFilePath.lastSegment(), inputFileContents);
		inputFileToTargetMap.put(inputFileString, compilationUnit.getResource().getLocation());
	}

	public IPath getTargetPathForInputFile(String inputFileName) {
		String pathForInputFile = getPathForInputFile(inputFileName);
		if (!inputFileToTargetMap.containsKey(pathForInputFile)) {
			throw new RuntimeException("Could not find the path to test class \"" + pathForInputFile + "\"");
		}
		return inputFileToTargetMap.get(pathForInputFile);
	}

	public void findBugs() throws WALAInitializationException {
		bugInstances = ConcurrencyBugsDetector.initAndPerformAnalysis(javaProject);
		System.out.println(bugInstances);
	}

	private void fixBugInstance(BugInstance bugInstance) throws OperationCanceledException, CoreException {
		Assert.assertNotNull("Could not find bug instance.", bugInstance);
		LCK02JFixer fixer = new LCK02JFixer(bugInstance);
		if (fixer.checkInitialConditions(new NullProgressMonitor()).isOK()) {
			fixer.createChange(new NullProgressMonitor());
		}
	}

	protected void bugInstanceShouldExist(BugInstance bugInstance) {
		Assert.assertTrue(bugInstances.contains(bugInstance));
	}

	protected BugInstance createTestBugInstnace(int firstLine, int lastLine, String className, String... replacements) {
		return new BugInstance(BugPatterns.LCK02J, new BugPosition(firstLine, lastLine, getTargetPathForInputFile(className)), new LCK02JFixInformation(SetUtils.asSet(replacements)));
	}

	public void checkNumberOfBugInstances(int numOfBugInstances) {
		Assert.assertEquals(numOfBugInstances, bugInstances.size());
	}

	public void tryFix(BugInstance bugInstance) throws OperationCanceledException, IOException, CoreException {
		tryFix(bugInstance, "");
	}

	public void tryFix(BugInstance bugInstance, String bugInstanceNumber) throws IOException, OperationCanceledException, CoreException {
		fixBugInstance(bugInstances.find(bugInstance));
		checkFix(bugInstanceNumber);
	}

	private void checkFix(String bugInstanceNumber) throws IOException {
		for (Map.Entry<String, IPath> entry : inputFileToTargetMap.entrySet()) {
			compareFiles(getPathForOutputFile(entry.getKey(), bugInstanceNumber), entry.getValue().toPortableString());
		}
	}

	private static void compareFiles(String expectedFilePath, String actualFilePath) throws IOException {
		Assert.assertEquals(getFileContent(expectedFilePath), getFileContent(actualFilePath));
	}

}
