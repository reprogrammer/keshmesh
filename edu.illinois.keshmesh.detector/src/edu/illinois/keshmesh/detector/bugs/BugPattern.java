package edu.illinois.keshmesh.detector.bugs;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
abstract public class BugPattern {

	protected final String description;

	public BugPattern(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "BugPattern: " + description;
	}
}
