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
