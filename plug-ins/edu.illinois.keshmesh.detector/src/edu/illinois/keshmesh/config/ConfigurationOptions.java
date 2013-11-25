package edu.illinois.keshmesh.config;

public class ConfigurationOptions {

	private final int objectSensitivityLevel;

	public ConfigurationOptions(int objectSensitivityLevel) {
		this.objectSensitivityLevel = objectSensitivityLevel;
	}

	public int getObjectSensitivityLevel() {
		return objectSensitivityLevel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (objectSensitivityLevel != other.objectSensitivityLevel) {
			return false;
		}
		return true;
	}

}
