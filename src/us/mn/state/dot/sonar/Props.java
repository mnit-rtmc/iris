/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2017  Minnesota Department of Transportation
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

import java.util.Properties;

/**
 * Helper functions for parsing properties.
 *
 * @author Douglas Lau
 */
public class Props {

	/** Get a property or throw a configuration error */
	static public String getProp(Properties props, String k)
		throws ConfigurationError
	{
		String v = props.getProperty(k);
		if (v != null)
			return v;
		else
			throw ConfigurationError.missingProperty(k);
	}

	/** Get an integer property or throw a configuration error */
	static public int getIntProp(Properties props, String k)
		throws ConfigurationError
	{
		try {
			return Integer.parseInt(getProp(props, k));
		}
		catch (NumberFormatException e) {
			throw ConfigurationError.invalidInt(k);
		}
	}
}
