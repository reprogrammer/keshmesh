package edu.illinois.keshmesh.detector.exception;

public class Exceptions {

	@SuppressWarnings("serial")
	public static class WALAInitializationException extends Exception {

		public WALAInitializationException(Exception e) {
			super(e);
		}

	}

}
