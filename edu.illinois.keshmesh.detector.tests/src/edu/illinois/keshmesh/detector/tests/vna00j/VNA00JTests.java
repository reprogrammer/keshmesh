/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.vna00j;

import org.junit.Before;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class VNA00JTests {

	public static class VNA00JTest01 extends VNA00JTest {

		@Before
		public void setup() throws Exception {
			setupProjectAndAnalyze("01", "A.java");
		}

		@Override
		protected String getExpectedThreadSafeClasses() {
			return "[<Application,Lp/A>]";
		}
	}

}
