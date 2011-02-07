/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import edu.illinois.keshmesh.util.Modes;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public abstract class IntermediateResults {

	protected boolean canSaveIntermediateResult(Object intermediateResult) {
		if (!Modes.isInProductionMode()) {
			if (intermediateResult != null) {
				throw new RuntimeException("Saved the same intermediate result more than once.");
			}
			return true;
		}
		return false;
	}

}
