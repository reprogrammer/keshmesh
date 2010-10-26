package edu.illinois.keshmesh.detector.tests.lck02j;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.BugPosition;
import edu.illinois.keshmesh.detector.bugs.LCK02JFixInformation;
import edu.illinois.keshmesh.detector.exception.Exceptions.WALAInitializationException;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian
 * 
 */
public class LCK02JTest2 extends LCK02JTest {

	@Before
	public void setup() throws Exception {
		setupProjectAndAnalyze("test-files/LCK02J/02/Test.java");
	}

	@Test
	public void shouldFindLCK02J() throws WALAInitializationException {
		Assert.assertEquals(1, bugInstances.size());
		//Linux:
		Assert.assertTrue(bugInstances.contains(new BugInstance(BugPatterns.LCK02J, new BugPosition(245, 275, targetTestClassPath), new LCK02JFixInformation("p.Test.C.class"))));
		// Windows: Assert.assertTrue(bugInstances.contains(new BugInstance(BugPatterns.LCK02J, new BugPosition(208, 249, compilationUnitPath), new LCK02JFixInformation("p.Test.class"))));
	}
}
