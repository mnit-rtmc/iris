/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import us.mn.state.dot.tms.ControllerImpl;

/**
 * A Messenger is a class which can poll a field controller and get the
 * response. Subclasses are SocketMessenger, HDLCMessenger, etc.
 *
 * @author Douglas Lau
 */
abstract public class Messenger {

	/** Input stream */
	protected InputStream input;

	/** Output stream */
	protected OutputStream output;

	/** Open the messenger */
	abstract public void open() throws IOException;

	/** Close the messenger */
	abstract public void close();

	/** Drain any bytes from the input stream */
	protected void drain() throws IOException {
		if(input != null) {
			int a = input.available();
			if(a > 0)
				input.skip(a);
		}
	}

	/** Set the messenger timeout */
	abstract public void setTimeout(int t) throws IOException;

	/** Get the input stream */
	public InputStream getInputStream() {
		return input;
	}

	/** Get an input stream for the specified controller */
	public InputStream getInputStream(ControllerImpl c)
		throws EOFException
	{
		InputStream is = input;
		if(is == null)
			throw new EOFException("MESSENGER CLOSED");
		else
			return input;
	}

	/** Get the output stream */
	public OutputStream getOutputStream() {
		return output;
	}

	/** Get an output stream for the specified controller */
	public OutputStream getOutputStream(ControllerImpl c)
		throws EOFException
	{
		OutputStream os = output;
		if(os == null)
			throw new EOFException("MESSENGER CLOSED");
		else
			return os;
	}

	/** Check the exception to see if a stream needs to be reopened */
	protected boolean shouldReopen(IOException e) {
		return !(e instanceof SerialIOException ||
			e instanceof SocketTimeoutException);
	}

	/** Handle an IO exception */
	public synchronized void handleException(IOException e)
		throws IOException
	{
		if(shouldReopen(e)) {
			close();
			open();
		} else
			drain();
	}
}
