/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck01j;

import org.junit.Before;

/**
 * 
 * @author Samira Tasharofi
 * 
 */
public class LCK01JTests {

	public static class LCK01JTest01 extends LCK01JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("01", "A.java");
		}

	}

	public static class LCK01JTest02 extends LCK01JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("02", "A.java");
		}

	}

	public static class LCK01JTest03 extends LCK01JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("03", "A.java");
		}

	}

}
