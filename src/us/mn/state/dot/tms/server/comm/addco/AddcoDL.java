/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.addco;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import us.mn.state.dot.tms.server.comm.CRC;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Addco Data Link layer stuff.  Based on HDLC from ntcip package.
 *
 * @author Douglas Lau
 */
abstract public class AddcoDL {

	/** End of stream exception */
	static private final EOFException END_OF_STREAM =
		new EOFException("END OF STREAM");

	/** Line noise exception */
	static private final ParsingException NOISE =
		new ParsingException("RANDOM LINE NOISE");

	/** Maximum message size */
	static private final int MAX_MESSAGE = 1024;

	/** FLAG is a single octet which signifies the start and end
	 * of each frame. */
	static private final int FLAG = 0x7E;

	/** ESCAPE is a single octet which is used for replacing octets within
	 * the frame which happen to be equal to FLAG (or ESCAPE). */
	static private final int ESCAPE = 0x7D;

	/** BIT6 is the sixth bit (starting from 1).  It is used for the
	 * transparency technique in the frame I/O streams. */
	static private final int BIT6 = 0x20;

	/** CRC-16 algorithm */
	static private final CRC crc16 = new CRC(16, 0x8005, 0xFFFF, true);

	/** A FilterOutputStream which frames messages with a FLAG octet and
	 * performs a transparency technique to ensure the FLAG is not
	 * contained within the frame.  It also adds the Frame Check Sequence
	 * (FCS) to the end of the frame. */
	static public class FOutputStream extends FilterOutputStream {

		/** Flag to indicate a new message */
		private boolean clear = true;

		/** Current CRC value (for FCS) */
		private int crc = crc16.seed;

		/** Create an ADDCO output stream */
		public FOutputStream(OutputStream os) {
			super(new BufferedOutputStream(os, MAX_MESSAGE));
		}

		/** Write the specified byte to this output stream,
		 * transparantly replacing FLAG with ESCAPE + FLAG^BIT6
		 * and ESCAPE with ESCAPE + ESCAPE^BIT6. */
		@Override
		public void write(int b) throws IOException {
			if (clear) {
				super.write(FLAG);
				crc = crc16.seed;
				clear = false;
			}
			crc = crc16.step(crc, b);
			if (FLAG == b || ESCAPE == b) {
				super.write(ESCAPE);
				b ^= BIT6;
			}
			super.write(b);
		}

		/** Writes out a frame check sequence (FCS) and framing flag,
		 * then flushes the whole message to the wrapped stream. */
		@Override
		public void flush() throws IOException {
			int fcs = crc16.result(crc);
			byte fcs1 = (byte)(crc >> 8);
			byte fcs2 = (byte)(crc >> 0);
			write(fcs1);
			write(fcs2);
			super.write(FLAG);
			super.flush();
			clear = true;
		}
	}

	/** An InputStream which reads messages framed with a FLAG octet
	 * and performs a transparency technique to collapse ESCAPE sequences
	 * into single octets. */
	static public class FInputStream extends InputStream {

		/** Wrapped input stream */
		private final InputStream wrapped;

		/** Buffer where data is stored */
		private final byte[] buf = new byte[MAX_MESSAGE];

		/** Number of bytes which have been read into the buffer */
		private int n_bytes = 0;

		/** Position of next byte in buffer */
		private int pos = 0;

		/** Was previous byte an ESCAPE? */
		private boolean esc = false;

		/** Create a new ADDCO input stream */
		public FInputStream(InputStream is) {
			wrapped = is;
		}

		/** Read the next byte from the input stream */
		@Override
		public int read() throws IOException {
			for (int i = 0; i < 10; i++) {
				readAvailLoop();
				int b = readNext();
				if (b >= 0)
					return b;
			}
			throw NOISE;
		}

		/** Read any available data into buffer */
		private void readAvailLoop() throws IOException {
			for (int i = 0; i < 10; i++) {
				if (pos < n_bytes)
					return;
				readAvailable();
			}
			throw NOISE;
		}

		/** Read all available data into buffer */
		private void readAvailable() throws IOException {
			pos = 0;
			n_bytes = wrapped.read(buf);
			if (n_bytes < 0)
				throw END_OF_STREAM;
			if (MAX_MESSAGE == n_bytes)
				throw NOISE;
		}

		/** Read the next byte in buffer */
		private int readNext() {
			assert pos < n_bytes;
			int b = buf[pos] & 0xFF;
			pos++;
			if (FLAG == b) {
				esc = false;
				return -1;
			}
			if (esc) {
				esc = false;
				return b ^ BIT6;
			}
			if (ESCAPE == b) {
				esc = true;
				return -1;
			}
			return b;
		}

		/** Get the number of available bytes */
		@Override
		public int available() {
			return n_bytes - pos;
		}

		/** Skip all data currently in the stream */
		@Override
		public long skip(long n) throws IOException {
			n_bytes = 0;
			pos = 0;
			return wrapped.skip(wrapped.available());
		}
	}
}
