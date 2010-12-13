/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck06j;

import org.junit.Before;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK06JTests {

	public static class LCK06JTest01 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("01", "A.java", "B.java");
		}

	}

	public static class LCK06JTest02 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("02", "A.java");
		}

	}

	public static class LCK06JTest03 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("03", "A.java");
		}

	}

	public static class LCK06JTest04 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("04", "A.java");
		}

	}

	public static class LCK06JTest05 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("05", "A.java");
		}

	}

	public static class LCK06JTest06 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("06", "A.java");
		}

	}

	public static class LCK06JTest07 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("07", "A.java");
		}

	}

}
