package edu.illinois.keshmesh.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;

public class MockConfigurationInputStreamFactory extends ConfigurationOptionsInputStreamFactory {

	public Optional<InputStream> createInputStream() {
		String content = ConfigurationOptionsReader.OBJECT_SENSITIVITY_LEVEL_KEY + "=3";
		InputStream is = new ByteArrayInputStream(content.getBytes(Charsets.UTF_8));
		return Optional.of(is);
	}

}
