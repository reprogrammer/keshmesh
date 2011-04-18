/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

import edu.illinois.keshmesh.detector.LCK01JBugDetector;

/**
 * 
 * @author Samira Tasharofi
 * 
 */
public class LCK01JBugPattern extends BugPattern {

	public LCK01JBugPattern() {
		super("LCK01J", "Do not synchronize on objects that may be reused", new LCK01JBugDetector());
	}

	@Override
	public boolean hasFixer() {
		return true;
	}

}
