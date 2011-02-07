/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

import edu.illinois.keshmesh.detector.BugPatternDetector;
import edu.illinois.keshmesh.detector.LCK02JBugDetector;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK02JBugPattern extends BugPattern {

	public LCK02JBugPattern() {
		super("LCK02J", "Do not synchronize on the class object returned by getClass()");
	}

	@Override
	public BugPatternDetector createBugPatternDetector() {
		bugPatternDetector = new LCK02JBugDetector();
		return bugPatternDetector;
	}

}
