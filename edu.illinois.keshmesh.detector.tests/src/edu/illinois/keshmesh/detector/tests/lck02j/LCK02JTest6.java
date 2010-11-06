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
public class LCK02JTest6 extends LCK02JTest {

	private static final String classA = "A.java";
	private BugInstance bugInstance1, bugInstance2;

	@Before
	public void setup() throws Exception {
		setupProjectAndAnalyze("06", classA);
		bugInstance1 = createTestBugInstance(15, 17, classA, "p.A.B.class");
		bugInstance2 = createTestBugInstance(18, 20, classA, "p.A.C.class");

	}

	@Test
	public void shouldFindBugInstances() {
		checkNumberOfBugInstances(2);
		bugInstanceShouldExist(bugInstance1);
		bugInstanceShouldExist(bugInstance2);
	}

	@Test
	public void shouldFixBugInstance1() throws OperationCanceledException, IOException, CoreException {
		tryFix(bugInstance1, "01");
	}

	@Test
	public void shouldFixBugInstance2() throws OperationCanceledException, IOException, CoreException {
		tryFix(bugInstance2, "02");
	}
}
