package edu.illinois.keshmesh.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import edu.illinois.keshmesh.constants.Constants;

public class FileWriterFactory implements WriterFactory {

	private final String filename;

	private final StringWriterFactory stringWriterFactory;

	public FileWriterFactory(String filename, StringWriterFactory stringWriterFactory) {
		this.filename = filename;
		this.stringWriterFactory = stringWriterFactory;
	}

	@Override
	public Writer create() {
		try {
			File keshmeshHome = new File(Constants.KESHMESH_HOME);
			if (!keshmeshHome.exists()) {
				return stringWriterFactory.create();
			}
			return new FileWriter(new File(Constants.KESHMESH_HOME + Constants.FILE_SEPARATOR + filename + ".csv"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
