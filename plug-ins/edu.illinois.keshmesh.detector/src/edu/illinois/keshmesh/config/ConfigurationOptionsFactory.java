package edu.illinois.keshmesh.config;

import java.util.Map;

public class ConfigurationOptionsFactory {

	ConfigurationOptions create(Map<String, String> options) {
		return new ConfigurationOptions(Integer.valueOf(options.get(ConfigurationOptionsReader.OBJECT_SENSITIVITY_LEVEL_KEY)));
	}

}
