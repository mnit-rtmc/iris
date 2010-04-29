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

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * This is an input stream for reading data in BCD (binary-coded-decimal).
 * It converts either 8 or 16-bit BCD data from a stream to integer values.
 *
 * @author Douglas Lau
 */
public class BCDInputStream extends InputStream {

	/** Mask for one BCD digit */
	static protected final int DIGIT_MASK = 0x0F;

	/** Input stream to read data from */
	protected final InputStream wrapped;

	/** Create a new BCD input stream */
	public BCDInputStream(InputStream w) {
		wrapped = w;
	}

	/** Read one byte from the input stream */
	public int read() throws IOException {
		int i = wrapped.read();
		if(i >= 0)
			return i;
		else
			throw new EOFException();
	}

	/** Get the first digit (on the right) */
	static protected int parseDigit(int i) throws ParsingException {
		i &= DIGIT_MASK;
		if(i >= 0 && i < 10)
			return i;
		else
			throw new ParsingException("Invalid BCD: " + i);
	}

	/** Read a 2-digit BCD value from the stream.  Note: two BCD digits
	 * will occupy one byte in the stream. */
	public int read2() throws IOException {
		int i = read();
		return 10 * parseDigit(i >> 4) + parseDigit(i);
	}

	/** Read a 4-digit BCD value from the stream.  Note: four BCD digits
	 * will occupy two bytes in the stream. */
	public int read4() throws IOException {
		return (100 * read2()) + read2();
	}
}
