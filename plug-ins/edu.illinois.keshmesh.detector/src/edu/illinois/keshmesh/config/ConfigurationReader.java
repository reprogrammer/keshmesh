package edu.illinois.keshmesh.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class ConfigurationReader {

	private final Optional<InputStream> inputStream;

	private final ConfigurationOptionsFactory configurationOptionsFactory = new ConfigurationOptionsFactory();

	private static final String OBJECT_SENSITIVITY_LEVEL_DEFAULT_VALUE = "2";

	static final String OBJECT_SENSITIVITY_LEVEL_KEY = "object_sensitivity_level";

	public ConfigurationReader(Optional<InputStream> inputStream) {
		this.inputStream = inputStream;
	}

	private Properties createDefaultProperties() {
		Properties properties = new Properties();
		properties.setProperty(OBJECT_SENSITIVITY_LEVEL_KEY, OBJECT_SENSITIVITY_LEVEL_DEFAULT_VALUE);
		return properties;
	}

	private Properties loadProperties(Optional<InputStream> inputStream) {
		Properties defaultProperties = createDefaultProperties();
		if (!inputStream.isPresent()) {
			return defaultProperties;
		}
		Properties properties = new Properties(defaultProperties);
		try {
			properties.load(inputStream.get());
		} catch (IOException e) {
			throw new RuntimeException();
		}
		return properties;
	}

	private ImmutableMap<String, String> loadConfigurationOptionsMap(Optional<InputStream> inputStream) {
		return Maps.fromProperties(loadProperties(inputStream));
	}

	public ConfigurationOptions read() {
		return configurationOptionsFactory.create(loadConfigurationOptionsMap(inputStream));
	}

}
