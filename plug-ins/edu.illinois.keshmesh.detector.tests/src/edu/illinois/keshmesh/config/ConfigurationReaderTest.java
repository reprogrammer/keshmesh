package edu.illinois.keshmesh.config;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigurationReaderTest {

	@Test
	public void testAbsentConfiguration() {
		ConfigurationReader configurationReader = createConfigurationReader(new AbsentConfigurationInputStreamFactory());
		assertEquals(new ConfigurationOptions(2), configurationReader.read());
	}

	@Test
	public void testMockConfiguration() {
		ConfigurationReader configurationReader = createConfigurationReader(new MockConfigurationInputStreamFactory());
		assertEquals(new ConfigurationOptions(3), configurationReader.read());
	}

	private ConfigurationReader createConfigurationReader(ConfigurationInputStreamFactory configurationInputStreamFactory) {
		return new ConfigurationReaderFactory(configurationInputStreamFactory).create();
	}
}
