/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck01j;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugPattern;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.FixInformation;
import edu.illinois.keshmesh.detector.bugs.LCK01JFixInformation;
import edu.illinois.keshmesh.detector.tests.AbstractTestCase;
import edu.illinois.keshmesh.detector.tests.BugInstanceCreator;
import edu.illinois.keshmesh.detector.util.CollectionUtils;

/**
 * 
 * @author Samira Tasharofi
 * 
 */
abstract public class LCK01JTest extends AbstractTestCase {

	@Override
	protected BugPattern getBugPattern() {
		return BugPatterns.LCK01J;
	}

	@Override
	protected void fixBugInstance(BugInstance bugInstance) throws OperationCanceledException, CoreException {
		Assert.assertNotNull("Could not find bug instance.", bugInstance);
	}

	@Override
	protected BugInstanceCreator getBugInstanceCreator() {
		return new LCK01JBugInstanceCreator();
	}

	protected static class LCK01JBugInstanceCreator extends AbstractTestCase.GeneralBugInstanceCreator {

		@Override
		public FixInformation createFixInformation(String... replacements) {
			return new LCK01JFixInformation(CollectionUtils.asSet(replacements));
		}

	}

}
