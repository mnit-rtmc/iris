/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
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
import java.net.SocketTimeoutException;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * An input stream which throws an EOFException if too many timeout exceptions
 * have occurred.  The exception thrown becomes a HangUpException once the modem
 * is in the "connected" state.
 *
 * @author Douglas Lau
 */
public class ModemInputStream extends InputStream {

	/** Connect failed exception */
	static private final EOFException CONNECT_FAILED =
		new EOFException("CONNECT FAILED");

	/** Disconnected exception */
	static private final EOFException DISCONNECTED =
		new EOFException("DISCONNECTED");

	/** Hung up exception */
	static private final EOFException HUNG_UP = new HangUpException();

	/** Get the error retry threshold */
	static private int getRetryThreshold() {
		return SystemAttrEnum.OPERATION_RETRY_THRESHOLD.getInt();
	}

	/** Wrapped input stream */
	private final InputStream wrapped;

	/** Flag indicating that modem has connected */
	private boolean connected = false;

	/** Count of timeout errors */
	private int n_timeout = 0;

	/** Exception for too many timeout errors */
	private EOFException eof = CONNECT_FAILED;

	/** Create a modem input stream */
	public ModemInputStream(InputStream is) {
		wrapped = is;
	}

	/** Set the modem to a connected state */
	public void setConnected() {
		eof = DISCONNECTED;
		connected = true;
	}

	/** Close the input stream */
	@Override
	public void close() throws IOException {
		wrapped.close();
	}

	/** Get the number of available bytes */
	@Override
	public int available() throws IOException {
		return wrapped.available();
	}

	/** Mark the position in the input stream */
	@Override
	public void mark(int readlimit) {
		wrapped.mark(readlimit);
	}

	/** Test if marks are supported */
	@Override
	public boolean markSupported() {
		return wrapped.markSupported();
	}

	/** Reset to last mark position */
	@Override
	public void reset() throws IOException {
		wrapped.reset();
	}

	/** Skip all data currently in the stream */
	@Override
	public long skip(long n) throws IOException {
		return wrapped.skip(n);
	}

	/** Read the next byte */
	@Override
	public int read() throws IOException {
		try {
			int n_byte = wrapped.read();
			if (n_byte >= 0)
				handleRead(1);
			return n_byte;
		}
		catch (SocketTimeoutException e) {
			handleTimeout(e);
			return -1;
		}
	}

	/** Handle a socket timeout exception */
	private void handleTimeout(SocketTimeoutException e) throws IOException{
		n_timeout++;
		if (n_timeout < getRetryThreshold())
			throw e;
		else
			throw eof;
	}

	/** Read a buffer of data */
	@Override
	public int read(byte[] b) throws IOException {
		try {
			int n_bytes = wrapped.read(b);
			handleRead(n_bytes);
			return n_bytes;
		}
		catch (SocketTimeoutException e) {
			handleTimeout(e);
			return -1;
		}
	}

	/** Read a buffer of data */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		try {
			int n_bytes = wrapped.read(b, off, len);
			handleRead(n_bytes);
			return n_bytes;
		}
		catch (SocketTimeoutException e) {
			handleTimeout(e);
			return -1;
		}
	}

	/** Handle a data read event */
	private void handleRead(int n_bytes) {
		if (n_bytes > 0) {
			n_timeout = 0;
			if (connected)
				eof = HUNG_UP;
		}
	}
}
