/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck02j;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.junit.Before;
import org.junit.Test;

import edu.illinois.keshmesh.detector.bugs.BugInstance;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian
 * 
 */
public class LCK02JTest8 extends LCK02JTest {

	private static final String classA = "A.java";
	private static final String classB = "B.java";
	private static final String classC = "C.java";
	private BugInstance bugInstance1, bugInstance2, bugInstance3, bugInstance4;

	@Before
	public void setup() throws Exception {
		setupProjectAndAnalyze("08", classA, classB, classC);
		bugInstance1 = createTestBugInstance(14, 16, classA, "p.B.class");
		bugInstance2 = createTestBugInstance(20, 22, classA, "p.C.class");
		bugInstance3 = createTestBugInstance(12, 14, classB, "p.B.class");
		bugInstance4 = createTestBugInstance(18, 20, classB, "p.A.class", "p.C.class");
	}

	@Test
	public void shouldFindBugInstances() {
		checkNumberOfBugInstances(4);
		bugInstanceShouldExist(bugInstance1);
		bugInstanceShouldExist(bugInstance2);
		bugInstanceShouldExist(bugInstance3);
		bugInstanceShouldExist(bugInstance4);
	}

	@Test
	public void shouldFixBugInstance1() throws OperationCanceledException, IOException, CoreException {
		tryFix(bugInstance1, "01");
	}

	@Test
	public void shouldFixBugInstance2() throws OperationCanceledException, IOException, CoreException {
		tryFix(bugInstance2, "02");
	}

	@Test
	public void shouldFixBugInstance3() throws OperationCanceledException, IOException, CoreException {
		tryFix(bugInstance3, "03");
	}

	@Test
	public void shouldNotFixBugInstance4() throws OperationCanceledException, IOException, CoreException {
		tryFix(bugInstance4, "04");
	}
}
