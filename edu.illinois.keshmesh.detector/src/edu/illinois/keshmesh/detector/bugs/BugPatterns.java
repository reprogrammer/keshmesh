/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class BugPatterns {

	public final static BugPattern LCK02J = new LCK02JBugPattern();
	public final static BugPattern LCK06J = new LCK06JBugPattern();
	public final static BugPattern VNA00J = new VNA00JBugPattern();

	private static Map<String, BugPattern> bugPatternsMap;

	static {
		bugPatternsMap = new HashMap<String, BugPattern>();
		//		bugPatternsMap.put(LCK02J.getName(), LCK02J);
		//		bugPatternsMap.put(LCK06J.getName(), LCK06J);
		bugPatternsMap.put(VNA00J.getName(), VNA00J);
	}

	public static BugPattern getBugPatternByName(String name) {
		return bugPatternsMap.get(name);
	}

	public static Iterator<BugPattern> iterator() {
		return bugPatternsMap.values().iterator();
	}

}
