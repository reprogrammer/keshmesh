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
public class LCK02JTest7 extends LCK02JTest {

	private static final String classA = "A.java";

	@Before
	public void setup() throws Exception {
		setupProjectAndAnalyze("07", classA);
	}

	@Test
	public void shouldFindLCK02J() {
		checkNumberOfBugInstances(4);
		bugInstanceShouldExist(13, 15, classA, "p.A.B.class");
		bugInstanceShouldExist(19, 21, classA, "p.A.class");
		bugInstanceShouldExist(22, 24, classA, "p.A.C.class");
		bugInstanceShouldExist(26, 29, classA, "p.A.B.class", "p.A.C.class");
	}
}
