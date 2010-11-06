/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class BugPatterns {

	public final static BugPattern LCK02J = new LCK02J();

	private static Map<String, BugPattern> bugPatternsMap;

	static {
		bugPatternsMap = new HashMap<String, BugPattern>();
		bugPatternsMap.put(LCK02J.getName(), LCK02J);
	}

	public static BugPattern getBugPatternByName(String name) {
		return bugPatternsMap.get(name);
	}
}
