/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.util.Date;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * DebugLog is a class for logging debugging information. Logging can be
 * enabled or disabled by creating or deleting the log file. Just use "touch
 * {filename}" to start logging.
 *
 * @author Douglas Lau
 */
public class DebugLog {

	/** Directory to store IRIS log files */
	static protected final String LOG_FILE_DIR = "/var/log/tms/";

	/** Create a new debug log */
	public DebugLog(String name) {
		file = new File(LOG_FILE_DIR + name);
		log("IRIS @@VERSION@@ restarted");
	}

	/** File to log debugging information */
	protected final File file;

	/** Debug log print writer */
	protected PrintWriter pw = null;

	/** Check if we can write to the log file */
	protected synchronized boolean canWrite() throws IOException {
		boolean w = file.canWrite();
		if(!w && pw != null) {
			pw.close();
			pw = null;
		}
		if(w && pw == null) {
			pw = new PrintWriter(new BufferedWriter(
				new FileWriter(file, true)), true);
		}
		return w;
	}

	/** Check if the debug log is open for writing */
	public boolean isOpen() {
		try {
			return canWrite();
		}
		catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/** Log a message in the debug log file */
	public synchronized void log(String m) {
		try {
			if(canWrite())
				pw.println(new Date().toString() + " " + m);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
