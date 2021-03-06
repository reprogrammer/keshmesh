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
public class Logger {

	public static void log(String message) {
		if (Modes.isInDebugMode()) {
			System.out.println(message);
		}
	}

}
