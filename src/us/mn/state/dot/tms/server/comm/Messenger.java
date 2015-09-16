/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * A Messenger is a class which can poll a field controller and get the
 * response. Subclasses are StreamMessenger, HDLCMessenger, etc.
 *
 * @author Douglas Lau
 */
abstract public class Messenger {

	/** Exception thrown when messenger is closed */
	static private final EOFException CLOSED = new EOFException(
		"MESSENGER CLOSED");

	/** Input stream */
	protected InputStream input;

	/** Output stream */
	protected OutputStream output;

	/** Open the messenger */
	abstract public void open() throws IOException;

	/** Close the messenger */
	abstract public void close();

	/** Set the messenger timeout */
	abstract public void setTimeout(int t) throws IOException;

	/** Get the messenger timeout */
	abstract public int getTimeout();

	/** Get the input stream.
	 * @param path Relative path name.  Only needed for protocols which
	 *             require it, such as HTTP.
	 * @return An input stream for reading from the messenger. */
	public InputStream getInputStream(String path) throws IOException {
		return input;
	}

	/** Get an input stream for the specified controller.
	 * @param path Relative path name.  Only needed for protocols which
	 *             require it, such as HTTP.
	 * @param c Controller to read from.
	 * @return An input stream for reading from the messenger. */
	public InputStream getInputStream(String path, ControllerImpl c)
		throws IOException
	{
		InputStream is = getInputStream(path);
		if (is == null)
			throw CLOSED;
		else
			return input;
	}

	/** Close the input stream */
	protected final void closeInput() {
		InputStream is = input;
		if (is != null) {
			try {
				is.close();
			}
			catch (IOException e) {
				// Ignore
			}
		}
		input = null;
	}

	/** Get the output stream */
	public OutputStream getOutputStream() {
		return output;
	}

	/** Get an output stream for the specified controller */
	public OutputStream getOutputStream(ControllerImpl c)
		throws IOException
	{
		OutputStream os = getOutputStream();
		if (os == null)
			throw CLOSED;
		else
			return os;
	}

	/** Close the output stream */
	protected final void closeOutput() {
		OutputStream os = output;
		if (os != null) {
			try {
				os.close();
			}
			catch (IOException e) {
				// Ignore
			}
		}
		output = null;
	}

	/** Drain any bytes from the input stream */
	public void drain() throws IOException {
		InputStream is = input;
		if (is != null) {
			int a = is.available();
			if (a > 0)
				is.skip(a);
		}
	}
}
