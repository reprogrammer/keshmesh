/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.exception;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class Exceptions {

	@SuppressWarnings("serial")
	public static class WALAInitializationException extends Exception {

		public WALAInitializationException(Exception e) {
			super(e);
		}

	}

}
