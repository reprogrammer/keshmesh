package edu.illinois.keshmesh.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class WriterFactory {

	private static final String FILE_SEPARATOR = File.separator;

	private static final String KESHMESH_HOME = System.getProperty("user.home") + FILE_SEPARATOR + "keshmesh";

	public Writer createWriter(String filename) {
		try {
			File keshmeshHome = new File(KESHMESH_HOME);
			if (!keshmeshHome.exists()) {
				keshmeshHome.mkdir();
			}
			return new FileWriter(new File(KESHMESH_HOME + FILE_SEPARATOR + filename + ".csv"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
