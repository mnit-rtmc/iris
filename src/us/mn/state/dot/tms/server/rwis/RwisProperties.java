/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  SRF Consulting Group
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

package us.mn.state.dot.tms.server.rwis;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** A static-singelton class that holds
 *  info from the rwis.properties file.
 * 
 * @author John L. Stanley - SRF Consulting 
 */
public enum RwisProperties {
	
	SINGELTON;

	private Properties myProp; 
	
	/* Initialize the single instance of RwisProperties */
	private RwisProperties() {
		String filename = "/etc/iris/rwis.properties";
		try {
			InputStream input = new FileInputStream(filename);
			myProp = new Properties();
			myProp.load(input);
		} catch (IOException ex) {
			myProp = null;
		}
	}
	
	/* Returns true if the rwis.properties file was loaded.
	 * Returns false if it was not loaded. */
	public static boolean isLoaded() {
		return SINGELTON.myProp != null;
	}
	
	/* Returns a value from the rwis.properties file
	 * or a default string. */
	public static String get(String key, String defaultValue) {
		assert(key != null);
		assert(!key.isEmpty());
		String value = null;
		if (SINGELTON.myProp != null)
			value = SINGELTON.myProp.getProperty(key);
		return (value == null) ? defaultValue : value;
	}

	/* Returns value from rwis.properties file.
	 * Returns null if key can't be found in the file. */
	public static String get(String key) {
		return get(key, null);
	}
}
