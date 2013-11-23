package edu.illinois.keshmesh.report;

public class KeyValuePair {

	private final String key;

	private final String value;

	public KeyValuePair(String key, String value) {
		this.key = key;
		this.value = value;
	}

	String getKey() {
		return key;
	}

	String getValue() {
		return value;
	}

}
