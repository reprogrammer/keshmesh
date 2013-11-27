package edu.illinois.keshmesh.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class ConfigurationOptionsReader {

	private final Optional<InputStream> inputStream;

	private final ConfigurationOptionsFactory configurationOptionsFactory = new ConfigurationOptionsFactory();

	private static final String OBJECT_SENSITIVITY_LEVEL_DEFAULT_VALUE = "2";

	static final String OBJECT_SENSITIVITY_LEVEL_KEY = "object_sensitivity_level";

	public ConfigurationOptionsReader(Optional<InputStream> inputStream) {
		this.inputStream = inputStream;
	}

	private Properties createDefaultProperties() {
		Properties properties = new Properties();
		properties.setProperty(OBJECT_SENSITIVITY_LEVEL_KEY, OBJECT_SENSITIVITY_LEVEL_DEFAULT_VALUE);
		return properties;
	}

	private Properties loadProperties(Optional<InputStream> optionalInputStream) {
		Properties defaultProperties = createDefaultProperties();
		if (!optionalInputStream.isPresent()) {
			return defaultProperties;
		}
		InputStream inputStream = optionalInputStream.get();
		Properties properties = new Properties(defaultProperties);
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			throw new RuntimeException();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				throw new RuntimeException();
			}
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
