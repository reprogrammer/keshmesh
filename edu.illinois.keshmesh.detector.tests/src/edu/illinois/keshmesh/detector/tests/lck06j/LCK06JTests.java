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
			setupProjectAndAnalyze("01", "A.java");
		}

		@Override
		protected String getExpectedStaticFields() {
			return "[< Application, Lp/A, a, <Application,Lp/A> >, < Application, Lp/A, counter, <Primordial,I> >, < Application, Lp/A, static_assigned, <Application,Lp/B> >, < Application, Lp/A, static_unassigned, <Application,Lp/B> >]";
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

	public static class LCK06JTest08 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("08", "A.java");
		}

	}

	public static class LCK06JTest09 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("09", "A.java");
		}

	}

	public static class LCK06JTest10 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("10", "A.java");
		}

	}

	public static class LCK06JTest11 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("11", "A.java");
		}

	}

	public static class LCK06JTest12 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("12", "A.java");
		}

	}

	public static class LCK06JTest13 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("13", "A.java");
		}

	}

	public static class LCK06JTest14 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("14", "A.java");
		}

	}

	public static class LCK06JTest15 extends LCK06JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("15", "A.java");
		}

	}

}
