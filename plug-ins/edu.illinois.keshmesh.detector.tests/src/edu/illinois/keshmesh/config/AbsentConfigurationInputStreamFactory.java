package edu.illinois.keshmesh.config;

import java.io.InputStream;

import com.google.common.base.Optional;

public class AbsentConfigurationInputStreamFactory extends ConfigurationInputStreamFactory {

	public Optional<InputStream> createInputStream() {
		return Optional.absent();
	}

}
