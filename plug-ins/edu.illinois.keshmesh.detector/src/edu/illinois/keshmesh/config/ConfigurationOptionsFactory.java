package edu.illinois.keshmesh.config;

import java.util.Map;

import edu.illinois.keshmesh.constants.Constants;

public class ConfigurationOptionsFactory {

	ConfigurationOptions create(Map<String, String> options) {
		String objectSensitivityLevelString = options.get(ConfigurationOptionsReader.OBJECT_SENSITIVITY_LEVEL_KEY);
		return new ConfigurationOptions(toObjectSensitivityLevel(objectSensitivityLevelString));
	}

	private int toObjectSensitivityLevel(String objectSensitivityLevelString) {
		if (Constants.INFINITY.equals(objectSensitivityLevelString.toUpperCase())) {
			return Integer.MAX_VALUE;
		} else {
			return Integer.valueOf(objectSensitivityLevelString);
		}
	}

}
