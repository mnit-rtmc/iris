/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * PropertyLoader is a utility class to load property files.
 *
 * @author Douglas Lau
 */
public class PropertyLoader {

	/** Load properties from the specified location */
	static public Properties load(String loc) throws IOException {
		Properties props = new Properties();
		InputStream s = getInputStream(loc);
		if (s != null) {
			try {
				props.load(s);
			}
			finally {
				s.close();
			}
		}
		return props;
	}

	/** Get an input stream for the specified property location */
	static private InputStream getInputStream(String loc)
		throws IOException
	{
		try {
			return new FileInputStream(loc);
		}
		catch (IOException e) {
			try {
				return new URL(loc).openStream();
			}
			catch (IOException ee) {
				throw e;
			}
		}
	}
}
