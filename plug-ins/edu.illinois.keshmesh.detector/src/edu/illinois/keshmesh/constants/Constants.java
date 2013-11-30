package edu.illinois.keshmesh.constants;

import java.io.File;

public class Constants {

	public static final String FILE_SEPARATOR = File.separator;

	public static final String KESHMESH_HOME = System.getProperty("user.home") + Constants.FILE_SEPARATOR + "keshmesh";

	public static final String KESHMESH_PROPERTIES_FILE = KESHMESH_HOME + FILE_SEPARATOR + "keshmesh.properties";

	public static final String KESHMESH_CALL_GRAPH_FILE_NAME = "call-graph.md";

	public static final String KESHMESH_HEAP_GRAPH_FILE_NAME = "heap-graph.md";

}
