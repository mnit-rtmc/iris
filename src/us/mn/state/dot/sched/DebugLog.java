/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.sched;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * DebugLog is a class for logging debugging information.  Logging can be
 * enabled or disabled by creating or deleting the log file.  Just use "touch
 * {filename}" to start logging.
 *
 * @author Douglas Lau
 */
public final class DebugLog {

	/** Path to store log files */
	static private File PATH = new File(".");

	/** Initial message when creating log file */
	static private String MESSAGE = "Created DebugLog";

	/** Default exception handler */
	static private ExceptionHandler HANDLER = new ExceptionHandler() {
		public boolean handle(Exception e) {
			e.printStackTrace();
			return true;
		}
	};

	/** Initialize the debug log mechanism.
	 * @param p File path to store log files.
	 * @param m Initial message when creating log file.
	 * @param h Default exception handler. */
	static public void init(File p, String m, ExceptionHandler h) {
		if (p.isDirectory())
			PATH = p;
		MESSAGE = m;
		HANDLER = h;
	}

	/** Initialize the debug log mechanism.
	 * @param p File path to store log files.
	 * @param m Initial message when creating log file. */
	static public void init(File p, String m) {
		init(p, m, HANDLER);
	}

	/** Exception handler */
	private final ExceptionHandler handler;

	/** Handle an exception */
	private void handleException(Exception e) {
		if (handler != null)
			handler.handle(e);
		else
			HANDLER.handle(e);
	}

	/** Create a new debug log */
	public DebugLog(String fn) {
		this(fn, null);
	}

	/** Create a new debug log */
	public DebugLog(String fn, ExceptionHandler h) {
		name = fn;
		handler = h;
		log(MESSAGE);
	}

	/** Name of log file */
	private final String name;

	/** Get the logging file */
	public File getFile() {
		return new File(PATH, name);
	}

	/** Debug log buffered writer */
	private BufferedWriter bw = null;

	/** Check if we can write to the log file */
	private synchronized boolean canWrite(File file) throws IOException {
		boolean w = file.canWrite();
		if (bw != null && (!w || file.length() == 0)) {
			bw.close();
			bw = null;
		}
		return w;
	}

	/** Check if the debug log is open for writing */
	public boolean isOpen() {
		try {
			return canWrite(getFile());
		}
		catch (IOException e) {
			handleException(e);
			return false;
		}
	}

	/** Log a message in the debug log file */
	public synchronized void log(String m) {
		try {
			File file = getFile();
			if (canWrite(file))
				log(file, m);
		}
		catch (IOException e) {
			handleException(e);
		}
	}

	/** Write a message in the debug log file */
	private void log(File file, String m) throws IOException {
		if (bw == null) {
			bw = new BufferedWriter(new FileWriter(file, true));
			log(bw, "DebugLog: " + name);
		}
		log(bw, m);
		bw.flush();
	}

	/** Write a log message to a buffered writer */
	private void log(BufferedWriter bw, String m) throws IOException {
		bw.write(TimeSteward.currentDateTimeString(true));
		bw.write(" ");
		bw.write(m);
		bw.newLine();
	}
}
