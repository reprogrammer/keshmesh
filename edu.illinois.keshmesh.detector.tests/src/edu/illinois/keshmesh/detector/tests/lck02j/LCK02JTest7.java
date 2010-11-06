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
public class LCK02JTest7 extends LCK02JTest {

	private static final String classA = "A.java";
	private BugInstance bugInstance1, bugInstance2, bugInstance3, bugInstance4;

	@Before
	public void setup() throws Exception {
		setupProjectAndAnalyze("07", classA);
		bugInstance1 = createTestBugInstance(15, 17, classA, "p.A.B.class");
		bugInstance2 = createTestBugInstance(21, 23, classA, "p.A.class");
		bugInstance3 = createTestBugInstance(24, 26, classA, "p.A.C.class");
		bugInstance4 = createTestBugInstance(28, 31, classA, "p.A.B.class", "p.A.C.class");
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
