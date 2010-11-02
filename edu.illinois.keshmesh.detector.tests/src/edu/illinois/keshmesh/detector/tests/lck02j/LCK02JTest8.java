/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck02j;

import org.junit.Before;
import org.junit.Test;

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

	@Before
	public void setup() throws Exception {
		setupProjectAndAnalyze("08", classA, classB, classC);
	}

	@Test
	public void shouldFindLCK02J() {
		checkNumberOfBugInstances(4);
		bugInstanceShouldExist(12, 14, classA, "p.B.class");
		bugInstanceShouldExist(18, 20, classA, "p.C.class");
		bugInstanceShouldExist(10, 12, classB, "p.B.class");
		bugInstanceShouldExist(16, 18, classB, "p.A.class", "p.C.class");
	}
}
