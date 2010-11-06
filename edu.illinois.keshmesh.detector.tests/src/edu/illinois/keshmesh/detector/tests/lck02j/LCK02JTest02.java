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
public class LCK02JTest02 extends LCK02JTest {

	@Before
	public void setup() throws Exception {
		setupProjectAndAnalyze("02", "A.java");
	}

	@Test
	public void shouldTryFixBugInstance01() throws OperationCanceledException, IOException, CoreException {
		tryFix("01");
	}

}
