/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests.lck03j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.tests.BugInstanceParser;
import edu.illinois.keshmesh.detector.tests.NumberedBugInstance;
import edu.illinois.keshmesh.util.Modes;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * @author Samira Tasharofi
 * 
 */
public class LCK03JTestBugInstanceParser {

	private static final String REENTRANT_LOCK = "java.util.concurrent.locks.ReentrantLock";
	private static final String CONDITION_OBJECT = "java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject";
	private static final String FIX_INFO = REENTRANT_LOCK + ", " + CONDITION_OBJECT;
	BugInstanceParser bugInstanceParser;
	private Path path;
	private LCK03JTest.LCK03JBugInstanceCreator bugInstanceCreator;

	@Before
	public void setup() {
		Modes.setInTestMode(true);
		BugPatterns.enableBugPatterns(BugPatterns.LCK03J);
		path = new Path(".");
		bugInstanceCreator = new LCK03JTest.LCK03JBugInstanceCreator();
		bugInstanceParser = new BugInstanceParser(bugInstanceCreator, path);
	}

	@Test
	public void shouldParseLCK03J() {
		ensureIsFound("/*[LCK03J,01," + FIX_INFO + "*/", "/*]*/");
	}

	@Test
	public void shouldParseLCK03JWithSpaces() {
		ensureIsFound("/* [LCK03J,01," + FIX_INFO + " */", "/* ] */");
	}

	private void ensureIsFound(String beginOpenMarker, String beginCloseMarker) {
		List<String> lines = Arrays.asList(new String[] { beginOpenMarker, beginCloseMarker });
		Set<NumberedBugInstance> actualNumberedBugInstances = bugInstanceParser.parseExpectedBugInstances(lines.iterator());
		Assert.assertEquals(1, actualNumberedBugInstances.size());
		Set<NumberedBugInstance> expectedNumberedBugInstances = new HashSet<NumberedBugInstance>();
		expectedNumberedBugInstances.add(new NumberedBugInstance(bugInstanceCreator.createTestBugInstance(BugPatterns.LCK03J, 1, 2, path, REENTRANT_LOCK, CONDITION_OBJECT), "01"));
		Assert.assertEquals(expectedNumberedBugInstances, actualNumberedBugInstances);
	}

}
