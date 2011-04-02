/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

import edu.illinois.keshmesh.detector.LCK03JBugDetector;

/**
 * 
 * @author Samira Tasharofi
 * 
 */
public class LCK03JBugPattern extends BugPattern {
	public final static String LOCK = "java.util.concurrent.locks.Lock";
	public final static String CONDITION = "java.util.concurrent.locks.Condition";

	public LCK03JBugPattern() {
		super("LCK03J", "Do not synchronize on the intrinsic locks of high-level concurrency objects", new LCK03JBugDetector());
	}

	@Override
	public boolean hasFixer() {
		return true;
	}

}
