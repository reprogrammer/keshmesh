package edu.illinois.keshmesh.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.google.common.base.Optional;

import edu.illinois.keshmesh.constants.Constants;

public class ConfigurationOptionsInputStreamFactory {

	public Optional<InputStream> createInputStream() {
		try {
			return Optional.<InputStream> of(new FileInputStream(Constants.KESHMESH_PROPERTIES_FILE));
		} catch (FileNotFoundException e) {
			return Optional.absent();
		}
	}

}
