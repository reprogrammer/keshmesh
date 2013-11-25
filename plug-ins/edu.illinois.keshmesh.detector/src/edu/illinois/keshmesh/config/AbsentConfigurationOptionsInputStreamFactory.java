package edu.illinois.keshmesh.config;

import java.io.InputStream;

import com.google.common.base.Optional;

public class AbsentConfigurationOptionsInputStreamFactory extends ConfigurationOptionsInputStreamFactory {

	public Optional<InputStream> createInputStream() {
		return Optional.absent();
	}

}
