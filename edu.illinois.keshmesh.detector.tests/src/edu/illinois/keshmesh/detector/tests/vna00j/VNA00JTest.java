/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.vna00j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNotNull;
import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.junit.Test;

import edu.illinois.keshmesh.detector.VNA00JIntermediateResults;
import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugPattern;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.FixInformation;
import edu.illinois.keshmesh.detector.bugs.VNA00JFixInformation;
import edu.illinois.keshmesh.detector.tests.AbstractTestCase;
import edu.illinois.keshmesh.detector.tests.BugInstanceCreator;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
abstract public class VNA00JTest extends AbstractTestCase {

	@Override
	protected BugPattern getBugPattern() {
		return BugPatterns.VNA00J;
	}

	@Override
	protected void fixBugInstance(BugInstance bugInstance) throws OperationCanceledException, CoreException {
		Assert.assertNotNull("Could not find bug instance.", bugInstance);
	}

	protected String getExpectedThreadSafeClasses() {
		return null;
	}

	@Test
	public void testThreadSafeClasses() {
		assumeNotNull(getExpectedThreadSafeClasses());
		VNA00JIntermediateResults actualIntermediateResults = (VNA00JIntermediateResults) getIntermediateResults();
		assertEquals(getExpectedThreadSafeClasses(), actualIntermediateResults.getThreadSafeClasses());
	}

	protected String getExpectedUnsafeInstructionsThatAccessUnprotectedFields() {
		return null;
	}

	@Test
	public void testUnsafeInstructionsThatAccessUnprotectedFields() {
		assumeNotNull(getExpectedUnsafeInstructionsThatAccessUnprotectedFields());
		VNA00JIntermediateResults actualIntermediateResults = (VNA00JIntermediateResults) getIntermediateResults();
		assertEquals(getExpectedUnsafeInstructionsThatAccessUnprotectedFields(), actualIntermediateResults.getUnsafeInstructionsThatAccessUnprotectedFields());
	}

	@Override
	protected BugInstanceCreator getBugInstanceCreator() {
		return new VNA00JBugInstanceCreator();
	}

	protected static class VNA00JBugInstanceCreator extends AbstractTestCase.GeneralBugInstanceCreator {

		@Override
		public FixInformation createFixInformation(String... replacements) {
			return new VNA00JFixInformation();
		}

	}

}
