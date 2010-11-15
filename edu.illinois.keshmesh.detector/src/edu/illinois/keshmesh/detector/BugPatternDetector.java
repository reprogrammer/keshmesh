/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import edu.illinois.keshmesh.detector.bugs.BugInstances;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public interface BugPatternDetector {

	public BugInstances performAnalysis(BasicAnalysisData basicAnalysisData);
}
