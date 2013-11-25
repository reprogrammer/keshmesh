package edu.illinois.keshmesh.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import edu.illinois.keshmesh.constants.Constants;

public class FileWriterFactory implements WriterFactory {

	private final String filename;

	public FileWriterFactory(String filename) {
		this.filename = filename;
	}

	@Override
	public Writer create() {
		try {
			File keshmeshHome = new File(Constants.KESHMESH_HOME);
			if (!keshmeshHome.exists()) {
				keshmeshHome.mkdir();
			}
			return new FileWriter(new File(Constants.KESHMESH_HOME + Constants.FILE_SEPARATOR + filename + ".csv"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
