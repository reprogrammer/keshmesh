/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;

import edu.illinois.keshmesh.config.AbsentConfigurationOptionsInputStreamFactory;
import edu.illinois.keshmesh.config.ConfigurationOptions;
import edu.illinois.keshmesh.config.ConfigurationOptionsReaderFactory;
import edu.illinois.keshmesh.detector.IntermediateResults;
import edu.illinois.keshmesh.detector.Main;
import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPattern;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.CodePosition;
import edu.illinois.keshmesh.detector.bugs.FixInformation;
import edu.illinois.keshmesh.detector.exception.Exceptions.WALAInitializationException;
import edu.illinois.keshmesh.report.Reporter;
import edu.illinois.keshmesh.report.ReporterFactory;
import edu.illinois.keshmesh.report.StringWriterFactory;
import edu.illinois.keshmesh.util.Logger;
import edu.illinois.keshmesh.util.Modes;

/**
 * 
 * For each detected bug in the test input file, a number is assigned. For each
 * bug number, in the output folder a sub folder with that number is created
 * which contains the input file with that bug fixed
 * 
 */
@SuppressWarnings("restriction")
public abstract class AbstractTestCase {

	private boolean isBuildDone = false;

	private IJavaProject javaProject;

	private IPackageFragmentRoot fragmentRoot;

	private IPackageFragment packageP;

	private String testNumber;

	/*
	 * Maps absolute path of the input Java file into the absolute path of the
	 * file in the target workspace.
	 */
	private Map<String, IPath> inputFileToTargetMap;

	private Set<NumberedBugInstance> expectedBugInstances;

	private BugInstances bugInstances;

	static final String CONTAINER = "src";
	static final String PACKAGE_NAME = "p";

	private void setUpProject(String testID) throws Exception {
		javaProject = TestSetupHelper.createAndInitializeProject(testID);
		//Should be called after the projects are created
		//TestSetupHelper.setAutoBuilding(false);
		fragmentRoot = JavaProjectHelper.addSourceContainer(javaProject, CONTAINER);
		packageP = fragmentRoot.createPackageFragment(PACKAGE_NAME, true, null);
	}

	@Test
	public void shouldFindAllBugInstances() {
		assertEquals(expectedBugInstances.size(), bugInstances.size());
	}

	@Test
	public void bugInstancesShouldExist() {
		for (NumberedBugInstance numberedBugInstance : expectedBugInstances) {
			bugInstanceShouldExist(numberedBugInstance.getBugInstance());
		}
	}

	//	@After
	public void tearDown() throws Exception {
		JavaProjectHelper.performDummySearch();
		JavaProjectHelper.delete(javaProject);
	}

	protected abstract BugPattern getBugPattern();

	protected IntermediateResults getIntermediateResults() {
		return getBugPattern().getBugPatternDetector().getIntermediateResults();
	}

	protected abstract void fixBugInstance(BugInstance bugInstance) throws OperationCanceledException, CoreException;

	protected abstract BugInstanceCreator getBugInstanceCreator();

	class BugInstanceMatcher extends BaseMatcher<BugInstance> {
		private final BugInstance bugInstance;

		public BugInstanceMatcher(BugInstance expectedBugInstance) {
			bugInstance = expectedBugInstance;
		}

		@Override
		public boolean matches(Object arg) {
			if (bugInstance == null || arg == null) {
				return bugInstance == null && arg == null;
			} else {
				return bugInstance.portableEquals(arg);
			}
		}

		@Override
		public void describeTo(Description description) {
			description.appendValue(bugInstance);
		}

	}

	private void bugInstanceShouldExist(BugInstance expectedBugInstance) {
		IsCollectionContaining<BugInstance> collectionContainsExpectedBugInstance = new IsCollectionContaining<BugInstance>(new BugInstanceMatcher(expectedBugInstance));
		assertThat(bugInstances, collectionContainsExpectedBugInstance);
	}

	private String getPathForInputFile(String inputFileName) {
		String prefix = TestSetupHelper.join("test-files", getBugPattern().getName(), testNumber);
		assertTrue(String.format("The path %s contains \"in\"", prefix), !prefix.contains("in"));
		return TestSetupHelper.join(prefix, "in", inputFileName);
	}

	protected void setupProjectAndAnalyze(String testNumber, String... inputFileNames) throws Exception {
		this.testNumber = testNumber;
		inputFileToTargetMap = new HashMap<String, IPath>();
		expectedBugInstances = new HashSet<NumberedBugInstance>();
		setUpProject(getTestID());
		for (String inputFileName : inputFileNames) {
			addFile(inputFileName);
		}
		parseExpectedBugInstances(inputFileNames);
		buildSynchronously();
		findBugs();
	}

	private void buildSynchronously() throws CoreException, InterruptedException {
		final Object lock = new Object();
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, new BuildProgressMonitor(lock));
		synchronized (lock) {
			if (!isBuildDone) {
				lock.wait();
			}
		}
	}

	private void parseExpectedBugInstances(String... inputFileNames) throws IOException {
		for (String inputFileName : inputFileNames) {
			BugInstanceParser bugInstanceParser = new BugInstanceParser(getBugInstanceCreator(), getTargetPathForInputFile(inputFileName));
			expectedBugInstances.addAll(bugInstanceParser.parseExpectedBugInstances());
		}
	}

	private String getTestID() {
		return getBugPattern().getName() + "-" + testNumber;
	}

	private void addFile(String inputFileName) throws Exception {
		String inputFileString = getPathForInputFile(inputFileName);
		Path inputFilePath = new Path(inputFileString);
		File inputFile = Activator.getDefault().getFileInPlugin(inputFilePath);
		String inputFileContents = TestSetupHelper.format(TestSetupHelper.getFileContent(inputFile.getAbsolutePath()));
		ICompilationUnit compilationUnit = TestSetupHelper.createCU(packageP, inputFilePath.lastSegment(), inputFileContents);
		inputFileToTargetMap.put(inputFileString, compilationUnit.getResource().getLocation());
	}

	private IPath getTargetPathForInputFile(String inputFileName) {
		String pathForInputFile = getPathForInputFile(inputFileName);
		if (!inputFileToTargetMap.containsKey(pathForInputFile)) {
			throw new RuntimeException("Could not find the path to test class \"" + pathForInputFile + "\"");
		}
		return inputFileToTargetMap.get(pathForInputFile);
	}

	private void findBugs() throws WALAInitializationException {
		Modes.setInTestMode(true);
		BugPatterns.enableBugPatterns(getBugPattern());
		Reporter reporter = new ReporterFactory().create(new StringWriterFactory());
		ConfigurationOptions configurationOptions = new ConfigurationOptionsReaderFactory(new AbsentConfigurationOptionsInputStreamFactory()).create().read();
		bugInstances = Main.initAndPerformAnalysis(javaProject, reporter, configurationOptions);
		Logger.log(bugInstances.toString());
	}

	private void tryFix(BugInstance bugInstance, String bugInstanceNumber) throws IOException, OperationCanceledException, CoreException {
		if (bugInstances.size() == 1)
			bugInstanceNumber = "";
		BugInstance actualBugInstance = bugInstances.portableFind(bugInstance);
		Assert.assertNotNull("Could not find bug instance.", actualBugInstance);
		fixBugInstance(actualBugInstance);
		if (bugInstance.getBugPattern().hasFixer()) {
			checkFix(bugInstanceNumber);
		}
	}

	private void checkFix(String bugInstanceNumber) throws IOException {
		for (Map.Entry<String, IPath> entry : inputFileToTargetMap.entrySet()) {
			TestSetupHelper.compareFiles(TestSetupHelper.getPathForOutputFile(entry.getKey(), bugInstanceNumber), entry.getValue().toPortableString());
		}
	}

	protected void tryFix(String bugInstanceNumber) throws OperationCanceledException, IOException, CoreException {
		boolean foundBugInstance = false;
		for (NumberedBugInstance numberedBugInstance : expectedBugInstances) {
			if (numberedBugInstance.getNumber().equals(bugInstanceNumber)) {
				tryFix(numberedBugInstance.getBugInstance(), numberedBugInstance.getNumber());
				foundBugInstance = true;
			}
		}
		assertTrue(String.format("Could not find bug instance number %s.", bugInstanceNumber), foundBugInstance);
	}

	@Before
	public abstract void setup() throws Exception;

	private Collection<String> getExpectedBugInstanceNumbers() {
		Collection<String> expectedBugInstanceNumbers = new LinkedList<String>();
		for (NumberedBugInstance numberedBugInstance : expectedBugInstances) {
			expectedBugInstanceNumbers.add(numberedBugInstance.getNumber());
		}
		return expectedBugInstanceNumbers;
	}

	@Test
	public void tryFixExpectedBugInstances() throws Exception {
		for (String bugInstanceNumber : getExpectedBugInstanceNumbers()) {
			setup();
			tryFix(bugInstanceNumber);
		}
	}

	private final class BuildProgressMonitor extends NullProgressMonitor {
		private final Object lock;

		private BuildProgressMonitor(Object lock) {
			this.lock = lock;
		}

		@Override
		public void done() {
			synchronized (lock) {
				if (!isBuildDone) {
					isBuildDone = true;
					lock.notify();
				}
			}
		}
	}

	protected static abstract class GeneralBugInstanceCreator implements BugInstanceCreator {

		@Override
		public BugInstance createTestBugInstance(BugPattern bugPattern, int firstLine, int lastLine, IPath targetFilePath, String... replacements) {
			return new BugInstance(bugPattern, new CodePosition(firstLine, lastLine, targetFilePath, null), createFixInformation(replacements));
		}

		@Override
		public abstract FixInformation createFixInformation(String... replacements);

	}

}
