/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck02j;

import org.junit.Before;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK02JTests {

	public static class LCK02JTest01 extends LCK02JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("01", "A.java");
		}

	}

	public static class LCK02JTest02 extends LCK02JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("02", "A.java");
		}

	}

	public static class LCK02JTest03 extends LCK02JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("03", "A.java");
		}

	}

	public static class LCK02JTest04 extends LCK02JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("04", "A.java");
		}

	}

	public static class LCK02JTest05 extends LCK02JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("05", "A.java");

		}

	}

	public static class LCK02JTest06 extends LCK02JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("06", "A.java");

		}

	}

	public static class LCK02JTest07 extends LCK02JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("07", "A.java");
		}

	}

	public static class LCK02JTest08 extends LCK02JTest {

		@Before
		@Override
		public void setup() throws Exception {
			setupProjectAndAnalyze("08", "A.java", "B.java", "C.java");
		}
	}

	public static class LCK02JTest09 extends LCK02JTest {

		@Before
		@Override
		public void setup() throws Exception {
			setupProjectAndAnalyze("09", "A.java");
		}
	}

	public static class LCK02JTest10 extends LCK02JTest {

		@Before
		@Override
		public void setup() throws Exception {
			setupProjectAndAnalyze("10", "A.java");
		}
	}

	public static class LCK02JTest11 extends LCK02JTest {

		@Before
		@Override
		public void setup() throws Exception {
			setupProjectAndAnalyze("11", "A.java");
		}
	}

}
