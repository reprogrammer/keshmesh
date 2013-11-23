package edu.illinois.keshmesh.report;

import java.io.IOException;
import java.io.Writer;

public class Reporter {

	private static final String SEPARATOR = ",";

	private final Writer writer;

	public Reporter(Writer writer) {
		this.writer = writer;
	}

	public void report(KeyValuePair keyValuePair) {
		try {
			writer.write(keyValuePair.getKey());
			writer.write(SEPARATOR);
			writer.write(keyValuePair.getValue());
			writer.write(System.getProperty("line.separator"));
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
