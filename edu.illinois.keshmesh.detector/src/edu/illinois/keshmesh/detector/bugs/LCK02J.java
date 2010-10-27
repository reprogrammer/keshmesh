/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class LCK02J extends BugPattern {

	public LCK02J() {
		super("LCK02-J. Do not synchronize on the class object returned by getClass()");
	}
}
