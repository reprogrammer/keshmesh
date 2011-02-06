/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.util;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class Modes {

	static final String KESHMESH_DEBUG_MODE = "KESHMESH_DEBUG_MODE"; //$NON-NLS-1$

	static boolean inTestMode = false;

	public static boolean isInDebugMode() {
		return System.getenv(KESHMESH_DEBUG_MODE) != null;
	}

	public static boolean isInTestMode() {
		return inTestMode;
	}

	public static void setInTestMode(boolean inTestMode) {
		Modes.inTestMode = inTestMode;
	}

	public static boolean isInProductionMode() {
		return !isInTestMode() && !isInDebugMode();
	}

}
