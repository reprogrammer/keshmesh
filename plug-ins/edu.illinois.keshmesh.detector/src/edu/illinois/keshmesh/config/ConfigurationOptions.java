package edu.illinois.keshmesh.config;

public class ConfigurationOptions {

	private final int objectSensitivityLevel;

	private final boolean dumpCallGraph;

	private final boolean dumpHeapGraph;

	public ConfigurationOptions(int objectSensitivityLevel, boolean dumpCallGraph, boolean dumpHeapGraph) {
		this.objectSensitivityLevel = objectSensitivityLevel;
		this.dumpCallGraph = dumpCallGraph;
		this.dumpHeapGraph = dumpHeapGraph;
	}

	public int getObjectSensitivityLevel() {
		return objectSensitivityLevel;
	}
	
	public boolean shouldDumpCallGraph() {
		return dumpCallGraph;
	}

	public boolean shouldDumpHeapGraph() {
		return dumpHeapGraph;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (dumpCallGraph ? 1231 : 1237);
		result = prime * result + (dumpHeapGraph ? 1231 : 1237);
		result = prime * result + objectSensitivityLevel;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ConfigurationOptions other = (ConfigurationOptions) obj;
		if (dumpCallGraph != other.dumpCallGraph) {
			return false;
		}
		if (dumpHeapGraph != other.dumpHeapGraph) {
			return false;
		}
		if (objectSensitivityLevel != other.objectSensitivityLevel) {
			return false;
		}
		return true;
	}

}
