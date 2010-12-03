/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class Modes {

	static final String KESHMESH_DEBUG_MODE = "KESHMESH_DEBUG_MODE"; //$NON-NLS-1$

	public static boolean isInDebugMode() {
		return System.getenv(KESHMESH_DEBUG_MODE) != null;
	}

}
