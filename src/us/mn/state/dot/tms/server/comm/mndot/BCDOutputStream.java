/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is an output stream for writing data in BCD (binary-coded-decimal).
 * It writes 8- or 16-bit integer values to a stream, encoded as BCD.
 *
 * @author Douglas Lau
 */
public class BCDOutputStream extends OutputStream {

	/** Get the first digit (on the right) */
	static protected int digit1(int i) {
		return i % 10;
	}

	/** Get the second digit (from the right) */
	static protected int digit2(int i) {
		return (i / 10) % 10;
	}

	/** Get the third digit (from the right) */
	static protected int digit3(int i) {
		return (i / 100) % 10;
	}

	/** Get the fourth digit (from the right) */
	static protected int digit4(int i) {
		return (i / 1000) % 10;
	}

	/** Output stream to write data to */
	protected final OutputStream wrapped;

	/** Create a new BCD output stream */
	public BCDOutputStream(OutputStream w) {
		wrapped = w;
	}

	/** Write one byte to the output stream */
	public void write(int b) throws IOException {
		wrapped.write(b);
	}

	/** Write the specified integer to the output stream as an
	 * 8-bit BCD.  Note: the valid range for an 8-bit BCD
	 * value is 0 to 99. */
	public void write2(int i) throws IOException {
		if(i < 0 || i > 99)
			throw new IOException("Cannot encode BCD.2: " + i);
		write(digit2(i) << 4 | digit1(i));
	}

	/** Write the specified integer to the output stream as a
	 * 16-bit BCD.  Note: the valid range for a 16-bit BCD
	 * value is 0 to 9999. */
	public void write4(int i) throws IOException {
		if(i < 0 || i > 9999)
			throw new IOException("Cannot encode BCD.4: " + i);
		write(digit4(i) << 4 | digit3(i));
		write(digit2(i) << 4 | digit1(i));
	}
}
