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
import java.io.OutputStream;

/** OutputDetector
 *
 * OutputStream wrapper used by BasicMessenger
 * to detect when data is sent.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class OutputDetector extends OutputStream {

	/** Messenger that "owns" the output stream */
	private final BasicMessenger m;

	/** OutputStream wrapped by this OutputDetector */
	private final OutputStream os;

	/** Create OutputDetector */
	public OutputDetector(BasicMessenger m, OutputStream os) {
		this.m  = m;
		this.os = os;
	}

	//--- OutputStream delegate methods

	@Override
	public void write(int b) throws IOException {
		os.write(b);
		m.outputDetected();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return os.hashCode();
	}

	/**
	 * @param b
	 * @throws IOException
	 * @see java.io.OutputStream#write(byte[])
	 */
	public void write(byte[] b) throws IOException {
		os.write(b);
		if ((b != null) && (b.length > 0))
			m.outputDetected();
	}

	/**
	 * @param b
	 * @param off
	 * @param len
	 * @throws IOException
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int off, int len) throws IOException {
		os.write(b, off, len);
		if (len > 0)
			m.outputDetected();
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return os.equals(obj);
	}

	/**
	 * @throws IOException
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws IOException {
		os.flush();
	}

	/**
	 * @throws IOException
	 * @see java.io.OutputStream#close()
	 */
	public void close() throws IOException {
		os.close();
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return os.toString();
	}
}
