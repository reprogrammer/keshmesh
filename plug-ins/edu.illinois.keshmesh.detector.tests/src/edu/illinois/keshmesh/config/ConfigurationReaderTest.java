package edu.illinois.keshmesh.config;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigurationReaderTest {

	@Test
	public void testAbsentConfiguration() {
		ConfigurationOptionsReader configurationReader = createConfigurationReader(new AbsentConfigurationOptionsInputStreamFactory());
		assertEquals(new ConfigurationOptions(2), configurationReader.read());
	}

	@Test
	public void testMockConfiguration() {
		ConfigurationOptionsReader configurationReader = createConfigurationReader(new MockConfigurationInputStreamFactory());
		assertEquals(new ConfigurationOptions(3), configurationReader.read());
	}

	private ConfigurationOptionsReader createConfigurationReader(ConfigurationOptionsInputStreamFactory configurationInputStreamFactory) {
		return new ConfigurationOptionsReaderFactory(configurationInputStreamFactory).create();
	}
}
