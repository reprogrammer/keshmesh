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
public class LCK02JTest4 extends LCK02JTest {

	private static final String classA = "A.java";

	@Before
	public void setup() throws Exception {
		setupProjectAndAnalyze("04", classA);
	}

	@Test
	public void shouldFindLCK02J() {
		checkNumberOfBugInstances(1);
		bugInstanceShouldExist(22, 25, classA, "p.A.B.class", "p.A.C.class");
	}
}
