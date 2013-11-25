package edu.illinois.keshmesh.report;

import java.io.StringWriter;
import java.io.Writer;

public class StringWriterFactory implements WriterFactory {

	@Override
	public Writer create() {
		return new StringWriter();
	}

}
