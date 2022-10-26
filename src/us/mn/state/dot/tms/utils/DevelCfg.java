/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  SRF Consulting Group
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Convenience class to allow easy string substitutions
 * in development environments (Eclipse and others).
 * 
 * This allows running development versions of IRIS in
 * Eclipse without having to modify strings in main-line
 * code or in the default properties files.
 * See the devel-example.cfg file for more details.
 * 
 * This uses an enum-singleton trick to initialize a
 * single instance of the DevelCfg class at class-load
 * time.
 * 
 * If a file named devel.cfg is placed in the default
 * run directory, with properties-style entries like
 *    key = value
 * those values will be substituted for various strings
 * anywhere the DevelCfg.get(...) method is used.
 * 
 * In Eclipse, you can use different devel.cfg files
 * for different run/debug configurations.  To do this,
 * go to Run>Run Configurations>Java Application.  Then
 * select the name of the run-configuration where you
 * want to use a different devel.cfg.  Select the Arguments
 * tab.  Then in the "VM arguments" box, add:
 *    -Ddevel.cfg=./devel-differentName.cfg
 * (Don't put it in the "Program arguments" box...)
 *
 * @author John L. Stanley - SRF Consulting
 */

public enum DevelCfg {
	
	SINGELTON;

	private Properties myProp; 
	
	/* Initialize the single instance of DevelCfg */
	private DevelCfg() {
		boolean required = false;
		String filename = null;
		try {
			filename = System.getProperty("devel.cfg", null);
			if (filename == null)
				filename = "./devel.cfg";
			else
				required = true;
			InputStream input = new FileInputStream(filename);
			myProp = new Properties();
			myProp.load(input);
		} catch (IOException ex) {
			if (required) {
				System.err.print("Fatal error: Can't read devel-cfg file: "+filename);
				System.exit(-1);
			}
			myProp = null;
		}
	}
	
	/* Returns true if the devel.cfg file was loaded.
	 * Returns false if it was not loaded. */
	public static boolean isLoaded() {
		return SINGELTON.myProp != null;
	}
	
	/* Returns a value from the devel.cfg file, a different
	 * property file, or a default string.  In that order.
	 * 
	 * If the key ends in ".dir", get(...) will create the
	 * directory specified by the returned value if it
	 * doesn't already exist.
	 */
	public static String get(String key, Properties p, String defaultValue) {
		assert(key != null);
		assert(!key.isEmpty());
		String value = null;
		if (SINGELTON.myProp != null)
			value = SINGELTON.myProp.getProperty(key);
		if ((value == null) && (p != null))
			value = p.getProperty(key);
		if (value == null)
			value = defaultValue;
		if ((value != null) && key.endsWith(".dir")) {
			try {
				File dir = new File(value);
				if (!dir.exists())
					dir.mkdirs();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return value;
	}

	/* Returns value from devel.cfg file.
	 * Returns null if key can't be found in the file. */
	public static String get(String key) {
		return get(key, null, null);
	}

	/* Returns value from devel.cfg or a default string */
	public static String get(String key, String defaultValue) {
		return get(key, null, defaultValue);
	}

	/* Returns value from devel.cfg or a different property file.
	 * Returns null if key can't be found in either file. */
	public static String get(String key, Properties p) {
		return get(key, p, null);
	}
}

