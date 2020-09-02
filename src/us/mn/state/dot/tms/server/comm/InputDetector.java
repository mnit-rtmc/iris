/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

import java.io.IOException;
import java.io.InputStream;

/** InputDetector
 *
 * InputStream wrapper used by BasicMessenger
 * to detect when data is received.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class InputDetector extends InputStream {

	/** Messenger that "owns" the input stream */
	private final BasicMessenger m;

	/** InputStream wrapped by this InputDetector */
	private final InputStream is;

	/** Create InputDetector */
	public InputDetector(BasicMessenger m, InputStream is) {
		this.m  = m;
		this.is = is;
	}

	//--- InputStream delegate methods

	@Override
	public int read() throws IOException {
		int ret = is.read();
		if (ret >= 0)
			m.inputDetected();
		return ret;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int ret = is.read(b);
		if (ret > 0)
			m.inputDetected();
		return ret;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int ret = is.read(b, off, len);
		if (ret > 0)
			m.inputDetected();
		return ret;
	}

	@Override
	public long skip(long n) throws IOException {
		long ret = is.skip(n);
		if (ret > 0)
			m.inputDetected();
		return ret;
	}

	@Override
	public int available() throws IOException {
		return is.available();
	}

	@Override
	public void close() throws IOException {
		is.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		is.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		is.reset();
	}

	@Override
	public boolean markSupported() {
		return is.markSupported();
	}
}
