/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Log convenience methods.
 * @created 05/15/09
 * @author Michael Darter
 */
public class Log {

	/** Logger instance */
	private static Logger m_logger = 
		Logger.getLogger("us.mn.state.dot.tms");

	/** Don't instantiate */
	private Log(){}

	/** root logging method used by other methods */
	private static void logRoot(Level l, String msg) {
		m_logger.log(l, msg == null ? "" : msg);
	}

	/** Log message */
	public static void severe(String msg) {
		Log.logRoot(Level.SEVERE, msg);
	}

	/** Log message */
	public static void warning(String msg) {
		Log.logRoot(Level.WARNING, msg);
	}

	/** Log message */
	public static void info(String msg) {
		Log.logRoot(Level.INFO, msg);
	}

	/** Log message */
	public static void config(String msg) {
		Log.logRoot(Level.CONFIG, msg);
	}

	/** Log message */
	public static void fine(String msg) {
		Log.logRoot(Level.FINE, msg);
	}

	/** Log message */
	public static void finer(String msg) {
		Log.logRoot(Level.FINER, msg);
	}

	/** Log message */
	public static void finest(String msg) {
		Log.logRoot(Level.FINEST, msg);
	}
}
