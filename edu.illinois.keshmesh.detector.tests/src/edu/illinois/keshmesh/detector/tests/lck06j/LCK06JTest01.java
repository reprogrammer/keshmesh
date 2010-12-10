/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck06j;

import org.junit.Before;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian
 * 
 */
public class LCK06JTest01 extends LCK06JTest {

	@Before
	public void setup() throws Exception {
		setupProjectAndAnalyze("01", "A.java", "B.java");
	}

	//	@Test
	//	public void shouldTryFixBugInstance01() throws OperationCanceledException, IOException, CoreException {
	//		tryFix("01");
	//	}

}
