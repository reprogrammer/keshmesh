package edu.illinois.keshmesh.config;

public class ConfigurationOptionsReaderFactory {

	private final ConfigurationOptionsInputStreamFactory configurationInputStreamFactory;

	public ConfigurationOptionsReaderFactory(ConfigurationOptionsInputStreamFactory configurationInputStreamFactory) {
		this.configurationInputStreamFactory = configurationInputStreamFactory;
	}

	public ConfigurationOptionsReader create() {
		return new ConfigurationOptionsReader(configurationInputStreamFactory.createInputStream());
	}

}
