package edu.illinois.keshmesh.report;

public class ReporterFactory {

	public Reporter create(WriterFactory writerFactory, KeyValuePair header) {
		Reporter reporter = new Reporter(writerFactory.create());
		reporter.report(header);
		return reporter;
	}

}
