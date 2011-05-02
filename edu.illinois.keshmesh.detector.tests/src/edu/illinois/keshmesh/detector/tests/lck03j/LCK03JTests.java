/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck03j;

import org.junit.Before;

/**
 * 
 * @author Samira Tasharofi
 * 
 */
public class LCK03JTests {

	public static class LCK03JTest01 extends LCK03JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("01", "A.java");
		}

	}

	public static class LCK03JTest02 extends LCK03JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("02", "A.java");
		}

	}

	public static class LCK03JTest03 extends LCK03JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("03", "A.java");
		}

	}

	public static class LCK03JTest04 extends LCK03JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("04", "A.java");
		}

	}

	public static class LCK03JTest05 extends LCK03JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("05", "A.java");
		}

	}

}
