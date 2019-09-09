/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2017  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.sonar;

import java.io.IOException;
import java.security.GeneralSecurityException;
import us.mn.state.dot.sonar.SonarException;

/**
 * This exception indicates a SONAR configuration error
 *
 * @author Douglas Lau
 */
public class ConfigurationError extends SonarException {

	/** Create a new configuration error exception */
	private ConfigurationError(String m) {
		super("Configuration Error: " + m);
	}

	/** Create an "invalid int" error */
	static public ConfigurationError invalidInt(String p) {
		return new ConfigurationError("Invalid int: " + p);
	}

	/** Create a "missing property" error */
	static public ConfigurationError missingProperty(String p) {
		return new ConfigurationError("Missing " + p + " property");
	}

	/** Create a "cannot read" error */
	static public ConfigurationError cannotRead(String keystore,
		IOException e)
	{
		return new ConfigurationError("Cannot read " + keystore + ", " +
			e.getMessage());
	}

	/** Create a "general security" error */
	static public ConfigurationError generalSecurity(
		GeneralSecurityException e)
	{
		return new ConfigurationError("Security error, " +
			e.getMessage());
	}
}
