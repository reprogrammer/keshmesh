package edu.illinois.keshmesh.report;

public class ReporterFactory {

	public Reporter create(WriterFactory writerFactory) {
		return new Reporter(writerFactory.create());
	}

}
