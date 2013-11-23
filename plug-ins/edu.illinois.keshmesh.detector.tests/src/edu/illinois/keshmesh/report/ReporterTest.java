package edu.illinois.keshmesh.report;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.junit.Test;

public class ReporterTest {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@Test
	public void testReport() {
		StringWriter writer = new StringWriter();
		Reporter reporter = new Reporter(writer);
		reporter.report(new KeyValuePair("k1", "v1"));
		reporter.report(new KeyValuePair("k2", "v2"));
		assertEquals("k1,v1" + LINE_SEPARATOR + "k2,v2" + LINE_SEPARATOR, writer.toString());
	}

}
