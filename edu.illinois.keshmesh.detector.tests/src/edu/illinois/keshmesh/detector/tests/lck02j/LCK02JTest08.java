/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck02j;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian
 * 
 */
public class LCK02JTest08 extends LCK02JTest {

	@Before
	public void setup() throws Exception {
		setupProjectAndAnalyze("08", "A.java", "B.java", "C.java");
	}

	@Test
	public void shouldTryFixBugInstance01() throws OperationCanceledException, IOException, CoreException {
		tryFix("01");
	}

	@Test
	public void shouldTryFixBugInstance02() throws OperationCanceledException, IOException, CoreException {
		tryFix("02");
	}

	@Test
	public void shouldTryFixBugInstance03() throws OperationCanceledException, IOException, CoreException {
		tryFix("03");
	}

	@Test
	public void shouldTryFixBugInstance04() throws OperationCanceledException, IOException, CoreException {
		tryFix("04");
	}

}
