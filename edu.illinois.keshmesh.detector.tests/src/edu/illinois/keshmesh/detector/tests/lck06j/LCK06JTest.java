/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck06j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNotNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.junit.Assert;
import org.junit.Test;

import edu.illinois.keshmesh.detector.LCK06JIntermediateResults;
import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugPattern;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.FixInformation;
import edu.illinois.keshmesh.detector.bugs.LCK06JFixInformation;
import edu.illinois.keshmesh.detector.tests.AbstractTestCase;
import edu.illinois.keshmesh.detector.tests.BugInstanceCreator;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
abstract public class LCK06JTest extends AbstractTestCase {

	@Override
	protected BugPattern getBugPattern() {
		return BugPatterns.LCK06J;
	}

	@Override
	protected boolean fixBugInstance(BugInstance bugInstance) throws OperationCanceledException, CoreException {
		Assert.assertNotNull("Could not find bug instance.", bugInstance);
		return false; //nothing was fixed
	}

	protected String getExpectedStaticFields() {
		return null;
	}

	@Test
	public void testStaticFields() {
		assumeNotNull(getExpectedStaticFields());
		LCK06JIntermediateResults actualIntermediateResults = (LCK06JIntermediateResults) getIntermediateResults();
		assertEquals(getExpectedStaticFields(), actualIntermediateResults.getStaticFields());
	}

	@Override
	protected BugInstanceCreator getBugInstanceCreator() {
		return new LCK06JBugInstanceCreator();
	}

	protected static class LCK06JBugInstanceCreator extends AbstractTestCase.GeneralBugInstanceCreator {

		@Override
		public FixInformation createFixInformation(String... replacements) {
			return new LCK06JFixInformation();
		}

	}

}
