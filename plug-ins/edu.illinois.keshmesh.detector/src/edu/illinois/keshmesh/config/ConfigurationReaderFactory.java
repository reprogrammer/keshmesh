package edu.illinois.keshmesh.config;

public class ConfigurationReaderFactory {

	private final ConfigurationInputStreamFactory configurationInputStreamFactory;

	public ConfigurationReaderFactory(ConfigurationInputStreamFactory configurationInputStreamFactory) {
		this.configurationInputStreamFactory = configurationInputStreamFactory;
	}

	public ConfigurationReader create() {
		return new ConfigurationReader(configurationInputStreamFactory.createInputStream());
	}

}
