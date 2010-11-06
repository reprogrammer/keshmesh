/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.tests.lck02j.LCK02JTest;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class TestBugInstanceParser {

	BugInstanceParser bugInstanceParser;
	private Path path;
	private LCK02JTest.LCK02JBugInstanceCreator bugInstanceCreator;

	@Before
	public void setup() {
		path = new Path(".");
		bugInstanceCreator = new LCK02JTest.LCK02JBugInstanceCreator();
		bugInstanceParser = new BugInstanceParser(bugInstanceCreator, path);
	}

	@Test
	public void shouldParseLCK02J() {
		List<String> lines = Arrays.asList(new String[] { "/*[LCK02J,01,p.A.class*/", "/*]*/" });
		Set<NumberedBugInstance> actualNumberedBugInstances = bugInstanceParser.parseExpectedBugInstances(lines.iterator());
		Assert.assertEquals(1, actualNumberedBugInstances.size());
		Set<NumberedBugInstance> expectedNumberedBugInstances = new HashSet<NumberedBugInstance>();
		expectedNumberedBugInstances.add(new NumberedBugInstance(bugInstanceCreator.createTestBugInstance(BugPatterns.LCK02J, 1, 2, path, "p.A.class"), "01"));
		Assert.assertEquals(expectedNumberedBugInstances.toString(), actualNumberedBugInstances.toString());
		Assert.assertEquals(expectedNumberedBugInstances, actualNumberedBugInstances);
	}
}
