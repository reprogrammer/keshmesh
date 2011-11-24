/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck03j;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugPattern;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.FixInformation;
import edu.illinois.keshmesh.detector.bugs.LCK03JFixInformation;
import edu.illinois.keshmesh.detector.tests.AbstractTestCase;
import edu.illinois.keshmesh.detector.tests.BugInstanceCreator;
import edu.illinois.keshmesh.detector.util.CollectionUtils;
import edu.illinois.keshmesh.transformer.core.LCK03JFixer;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
abstract public class LCK03JTest extends AbstractTestCase {

	@Override
	protected BugPattern getBugPattern() {
		return BugPatterns.LCK03J;
	}

	@Override
	protected void fixBugInstance(BugInstance bugInstance) throws OperationCanceledException, CoreException {
		LCK03JFixer fixer = new LCK03JFixer(bugInstance);
		if (fixer.checkInitialConditions(new NullProgressMonitor()).isOK()) {
			fixer.createChange(new NullProgressMonitor());
		}
	}

	@Override
	protected BugInstanceCreator getBugInstanceCreator() {
		return new LCK03JBugInstanceCreator();
	}

	protected static class LCK03JBugInstanceCreator extends AbstractTestCase.GeneralBugInstanceCreator {

		@Override
		public FixInformation createFixInformation(String... replacements) {
			return new LCK03JFixInformation(CollectionUtils.asSet(replacements));
		}

	}

}
