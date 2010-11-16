/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck02j;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugPattern;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.FixInformation;
import edu.illinois.keshmesh.detector.bugs.LCK02JFixInformation;
import edu.illinois.keshmesh.detector.tests.AbstractTestCase;
import edu.illinois.keshmesh.detector.tests.BugInstanceCreator;
import edu.illinois.keshmesh.detector.util.SetUtils;
import edu.illinois.keshmesh.transformer.core.LCK02JFixer;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
abstract public class LCK02JTest extends AbstractTestCase {

	@Override
	protected BugPattern getBugPattern() {
		return BugPatterns.LCK02J;
	}

	@Override
	protected void fixBugInstance(BugInstance bugInstance) throws OperationCanceledException, CoreException {
		Assert.assertNotNull("Could not find bug instance.", bugInstance);
		LCK02JFixer fixer = new LCK02JFixer(bugInstance);
		if (fixer.checkInitialConditions(new NullProgressMonitor()).isOK()) {
			fixer.createChange(new NullProgressMonitor());
		}
	}

	@Override
	protected BugInstanceCreator getBugInstanceCreator() {
		return new LCK02JBugInstanceCreator();
	}

	protected static class LCK02JBugInstanceCreator extends AbstractTestCase.GeneralBugInstanceCreator {

		@Override
		public FixInformation createFixInformation(String... replacements) {
			return new LCK02JFixInformation(SetUtils.asSet(replacements));
		}

	}

}
