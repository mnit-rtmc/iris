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

/**
 * This is a BCD (binary-coded-decimal) stream wrapper. It provides both an
 * InputStream and an OutputStream. The input stream converts either 8 or
 * 16-bit BCD data from a stream to integer values. The output stream
 * converts integers to 8 or 16-bit BCD data.
 *
 * @author Douglas Lau
 */
public interface BCD {

	/** An output stream which converts integers to BCD data */
	static public final class OutputStream extends java.io.OutputStream {

		/** Output stream to write data to */
		protected final java.io.OutputStream out;

		/** Create a new BCD output stream */
		public OutputStream(java.io.OutputStream os) {
			out = os;
		}

		/** Write one byte to the output stream */
		public void write(int b) throws IOException {
			out.write(b);
		}

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

		/** Write the specified integer to the output stream as an
		 * 8-bit BCD.  Note: the valid range for an 8-bit BCD
		 * value is 0 to 99. */
		public void write8Bit(int i) throws IOException {
			if(i < 0 || i > 99) {
				throw new NumberFormatException(
					"Invalid 8-bit BCD: " + i);
			}
			write(digit2(i) << 4 | digit1(i));
		}

		/** Write the specified integer to the output stream as a
		 * 16-bit BCD.  Note: the valid range for a 16-bit BCD
		 * value is 0 to 9999. */
		public void write16Bit(int i) throws IOException {
			if(i < 0 || i > 9999) {
				throw new NumberFormatException(
					"Invalid 16-bit BCD: " + i);
			}
			write(digit4(i) << 4 | digit3(i));
			write(digit2(i) << 4 | digit1(i));
		}
	}

	/** An input stream which converts BCD data to integers */
	static public final class InputStream extends java.io.InputStream {

		/** Mask for one BCD digit */
		static protected final int DIGIT_MASK = 0x0F;

		/** Input stream to read data from */
		protected final java.io.InputStream in;

		/** Create a new BCD input stream */
		public InputStream(java.io.InputStream is) {
			in = is;
		}

		/** Read one byte from the input stream */
		public int read() throws IOException {
			return in.read();
		}

		/** Get the first digit (on the right) */
		static protected int digit1(int i) {
			i &= DIGIT_MASK;
			if(i < 0 || i > 9) {
				throw new NumberFormatException(
					"Invalid BCD: " + i);
			}
			else return i;
		}

		/** Get the second digit (on the right) */
		static protected int digit2(int i) {
			return digit1(i >> 4);
		}

		/** Read an 8-bit BCD value from the stream.  Note: the valid
		 * range for an 8-bit BCD value is 0 to 99. */
		public int read8Bit() throws IOException {
			int i = read();
			return 10 * digit2(i) + digit1(i);
		}

		/** Read a 16-bit BCD value from the stream.  Note: the valid
		 * range for a 16-bit BCD value is 0 to 9999. */
		public int read16Bit() throws IOException {
			int hi = read();
			int lo = read();
			return 1000 * digit2(hi) + 100 * digit1(hi) +
				10 * digit2(lo) + digit1(lo);
		}
	}
}
