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
import java.io.FilterInputStream;
import us.mn.state.dot.tms.server.comm.ChecksumException;
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

	/** Invalid escape sequence */
	static private final ParsingException INVALID_ESC =
		new ParsingException("INVALID ESCAPE SEQUENCE");

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

	/** Frame check sequence is two bytes */
	static private final int FRAME_CHECK = 2;

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
			byte fcs1 = (byte)(crc >> 0);
			byte fcs2 = (byte)(crc >> 8);
			write(fcs1);
			write(fcs2);
			super.write(FLAG);
			super.flush();
			clear = true;
		}
	}

	/** A FilterInputStream which reads messages framed with a FLAG octet
	 * and performs a transparency technique to collapse ESCAPE sequences
	 * into single octets.  It also computes a CRC and compares it with
	 * the frame check sequence.<p>
	 *
	 * Note: this class does not extend BufferedInputStream because of
	 * timing problems associated with filling the buffer. */
	static public class FInputStream extends FilterInputStream {

		/** Buffer where scanned data is stored */
		private final byte[] buf = new byte[MAX_MESSAGE];

		/** Number of bytes which have been scanned into the buffer */
		private int scanned = 0;

		/** Position of next byte in scanned buffer */
		private int pos = 0;

		/** Create a new ADDCO input stream */
		public FInputStream(InputStream is) {
			super(is);
		}

		/** Scan for the beginning of the next frame */
		private void scanFrame() throws IOException {
			pos = 0;
			scanned = 0;
			for (int i = 0; i < MAX_MESSAGE; i++) {
				int b = super.read();
				if (b < 0)
					throw END_OF_STREAM;
				if (FLAG == b)
					return;
			}
			throw NOISE;
		}

		/** Scan until the next frame flag
		 * @return True if scanning needs to continue */
		private boolean scan() throws IOException {
			while (super.available() == 0) {
				int b = super.read();
				if (b < 0)
					throw END_OF_STREAM;
				if (FLAG == b)
					return false;
				buf[scanned++] = (byte)b;
				if (MAX_MESSAGE == scanned)
					throw NOISE;
			}
			int a = Math.min(super.available(),
				MAX_MESSAGE - scanned);
			int b = super.read(buf, scanned, a);
			if (b < 0)
				throw END_OF_STREAM;
			for (int i = 0; i < b; i++) {
				if (FLAG == buf[scanned])
					return false;
				else
					scanned++;
			}
			if (MAX_MESSAGE == scanned)
				throw NOISE;
			return true;
		}

		/** Scan and replace escape sequences. Replaces ESCAPE followed
		 * by FLAG ^ BIT6 with FLAG and ESCAPE followed by
		 * ESCAPE ^ BIT6 with ESCAPE. */
		private void scanEscapes() throws ParsingException {
			for (int c = 0; c < scanned; c++) {
				int b = buf[c] & 0xFF;
				if (ESCAPE == b) {
					b = (buf[c + 1] & 0xFF) ^ BIT6;
					if (b != FLAG && b != ESCAPE)
						throw INVALID_ESC;
					buf[c + 1] = (byte)b;
					System.arraycopy(buf, c + 1, buf, c,
						scanned - c);
					scanned--;
				}
			}
		}

		/** Compare frame CRC against the frame check sequence */
		private void checkFrame() throws ChecksumException {
			int fcs = 0;
			if (scanned >= FRAME_CHECK) {
				scanned -= FRAME_CHECK;
				fcs = (buf[scanned + 0] & 0xFF) |
				      (buf[scanned + 1] & 0xFF) << 8;
			} else {
				scanned = 0;
				return;
			}
			int crc = crc16.seed;
			for (int c = 0; c < scanned; c++)
				crc = crc16.step(crc, buf[c]);
			if (crc16.result(crc) == fcs)
				return;
			byte[] corrupt = new byte[scanned];
			System.arraycopy(buf, 0, corrupt, 0, scanned);
			scanned = 0;
			throw new ChecksumException(corrupt);
		}

		/** Scan the next message */
		private void scanMessage() throws IOException {
			scanFrame();
			while (scan());
			scanEscapes();
			checkFrame();
		}

		/** Read the next byte from the input stream */
		@Override
		public int read() throws IOException {
			while (pos >= scanned)
				scanMessage();
			return buf[pos++] & 0xFF;
		}

		/** Get the number of available bytes */
		@Override
		public int available() {
			return scanned - pos;
		}

		/** Skip all data currently in the stream */
		@Override
		public long skip(long n) throws IOException {
			scanned = 0;
			pos = 0;
			return super.skip(super.available());
		}
	}
}
