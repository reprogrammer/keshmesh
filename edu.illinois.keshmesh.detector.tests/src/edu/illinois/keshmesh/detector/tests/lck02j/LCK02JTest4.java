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
public class LCK02JTest4 extends LCK02JTest {

	private static final String classA = "A.java";
	private BugInstance bugInstance1;

	@Before
	public void setup() throws Exception {
		setupProjectAndAnalyze("04", classA);
		bugInstance1 = createTestBugInstnace(24, 27, classA, "p.A.B.class", "p.A.C.class");

	}

	@Test
	public void shouldFindBugInstances() {
		checkNumberOfBugInstances(1);
		bugInstanceShouldExist(bugInstance1);
	}

	@Test
	public void shouldNotFixBugInstance() throws OperationCanceledException, IOException, CoreException {
		tryFix(bugInstance1);
	}
}
