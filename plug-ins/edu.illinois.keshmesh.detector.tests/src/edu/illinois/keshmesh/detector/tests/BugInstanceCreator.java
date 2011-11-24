/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.tests;

import org.eclipse.core.runtime.IPath;

import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugPattern;
import edu.illinois.keshmesh.detector.bugs.FixInformation;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public interface BugInstanceCreator {

	public FixInformation createFixInformation(String... replacements);

	public BugInstance createTestBugInstance(BugPattern bugPattern, int firstLine, int lastLine, IPath targetFilePath, String... replacements);

}
