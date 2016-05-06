/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.FilterInputStream;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.InvalidAddressException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * This is an implementation of the ISO/IEC standard 3309 High-level Data
 * Link Control (HDLC) protocol, or rather, the subset of it which is
 * described in NEMA TS 3.3-1996 (NTCIP Class B profile).
 *
 * @author Douglas Lau
 */
abstract public class HDLC {

	/** End of stream exception */
	static protected final EOFException END_OF_STREAM =
		new EOFException("END OF STREAM");

	/** Maximum message size */
	static protected final int MAX_MESSAGE = 1024;

	/** FLAG is a single octet which signifies the start and end
	 * of each HDLC frame. */
	static protected final int FLAG = 0x7E;

	/** ESCAPE is a single octet which is used for replacing octets within
	 * the frame which happen to be equal to FLAG (or ESCAPE). */
	static protected final int ESCAPE = 0x7D;

	/** BIT6 is the sixth bit (starting from 1).  It is used for the
	 * transparency technique in the frame I/O streams. */
	static protected final int BIT6 = 0x20;

	/** Frame check sequence is two bytes */
	static protected final int FRAME_CHECK = 2;

	/** A FilterOutputStream which frames messages with a FLAG octet and
	 * performs a transparency technique to ensure the FLAG is not
	 * contained within the frame.  It also adds the Frame Check Sequence
	 * (FCS) to the end of the frame. */
	static public class FrameOutputStream extends FilterOutputStream {

		/** Flag to indicate a new message */
		protected boolean clear = true;

		/** CRC calculation (for FCS) */
		private CRCStream crc16;

		/** Create an HDLC frame output stream */
		public FrameOutputStream(OutputStream os) {
			super(new BufferedOutputStream(os, MAX_MESSAGE));
		}

		/** Write the specified byte to this output stream,
		 * transparantly replacing FLAG with ESCAPE + FLAG^BIT6
		 * and ESCAPE with ESCAPE + ESCAPE^BIT6. */
		public void write(int b) throws IOException {
			if(clear) {
				super.write(FLAG);
				crc16 = new CRCStream();
				clear = false;
			}
			crc16.write(b);
			if(b == FLAG || b == ESCAPE) {
				super.write(ESCAPE);
				b ^= BIT6;
			}
			super.write(b);
		}

		/** Writes out a frame check sequence (FCS) and framing flag,
		 * then flushes the whole message to the wrapped stream. */
		public void flush() throws IOException {
			/* NOTE: without ^ 0xFFFF, some DMS don't respond, */
			/*       but some still do.  What is the deal? */
			int crc = crc16.getCrc() ^ 0xFFFF;
			byte fcs1 = (byte)(~crc >> 0);
			byte fcs2 = (byte)(~crc >> 8);
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
	static public class FrameInputStream extends FilterInputStream {

		/** Buffer where scanned data is stored */
		protected final byte[] buf = new byte[MAX_MESSAGE];

		/** Number of bytes which have been scanned into the buffer */
		protected int scanned = 0;

		/** Position of next byte in scanned buffer */
		protected int pos = 0;

		/** Create a new HDLC frame input stream */
		public FrameInputStream(InputStream is) {
			super(is);
		}

		/** Scan for the beginning of the next frame */
		protected void scanFrame() throws IOException {
			pos = 0;
			scanned = 0;
			for(int i = 0; i < MAX_MESSAGE; i++) {
				int b = super.read();
				if(b < 0)
					throw END_OF_STREAM;
				if(b == FLAG)
					return;
			}
			throw new ParsingException("RANDOM LINE NOISE");
		}

		/** Scan until the next frame flag
		 * @return True if scanning needs to continue */
		protected boolean scan() throws IOException {
			while(super.available() == 0) {
				int b = super.read();
				if(b < 0)
					throw END_OF_STREAM;
				if(b == FLAG)
					return false;
				buf[scanned++] = (byte)b;
				if(scanned == MAX_MESSAGE) throw new
					ParsingException("RANDOM LINE NOISE");
			}
			int a = Math.min(super.available(),
				MAX_MESSAGE - scanned);
			int b = super.read(buf, scanned, a);
			if(b < 0)
				throw END_OF_STREAM;
			for(int i = 0; i < b; i++) {
				if(buf[scanned] == FLAG)
					return false;
				else
					scanned++;
			}
			if(scanned == MAX_MESSAGE) throw new
				ParsingException("RANDOM LINE NOISE");
			return true;
		}

		/** Scan and replace escape sequences. Replaces ESCAPE followed
		 * by FLAG ^ BIT6 with FLAG and ESCAPE followed by
		 * ESCAPE ^ BIT6 with ESCAPE. */
		protected void scanEscapes() throws ParsingException {
			for(int c = 0; c < scanned; c++) {
				int b = buf[c] & 0xFF;
				if(b == ESCAPE) {
					b = (buf[c + 1] & 0xFF) ^ BIT6;
					if(b != FLAG && b != ESCAPE) throw new
						ParsingException(
						"INVALID ESCAPE SEQUENCE");
					buf[c + 1] = (byte)b;
					System.arraycopy(buf, c + 1, buf, c,
						scanned - c);
					scanned--;
				}
			}
		}

		/** Compare frame CRC against the frame check sequence */
		protected void checkFrame() throws ChecksumException {
			int fcs = 0;
			if (scanned >= FRAME_CHECK) {
				scanned -= FRAME_CHECK;
				fcs = (buf[scanned + 0] & 0xFF) |
				      (buf[scanned + 1] & 0xFF) << 8;
			} else {
				scanned = 0;
				return;
			}
			CRCStream crc16 = new CRCStream();
			for (int c = 0; c < scanned; c++)
				crc16.write(buf[c]);
			int crc = crc16.getCrc();
			if (crc == fcs)
				return;
			byte[] corrupt = new byte[scanned];
			System.arraycopy(buf, 0, corrupt, 0, scanned);
			scanned = 0;
			throw new ChecksumException(corrupt);
		}

		/** Scan the next message */
		protected void scanMessage() throws IOException {
			scanFrame();
			while(scan());
			scanEscapes();
			checkFrame();
		}

		/** Read the next byte from the input stream. */
		public int read() throws IOException {
			while(pos >= scanned) scanMessage();
			return buf[pos++] & 0xFF;
		}

		/** Get the number of available bytes */
		public int available() { return scanned - pos; }

		/** Skip all data currently in the stream */
		public long skip(long n) throws IOException {
			scanned = 0;
			pos = 0;
			return super.skip(super.available());
		}
	}

	/** NTCIP class B addresses are restricted to 13 bits */
	static protected final int NTCIP_MAX_ADDRESS = 0x1FFF;

	/** Unnumbered Information with poll bit set */
	static protected final byte CONTROL_UI = 0x13;

	/** Unnumbered Poll */
	static protected final byte CONTROL_UP = 0x33;

	/** Unnumbered Information Final (response) */
	static protected final byte CONTROL_UIF = 0x13;

	/** Initial Protocol Identifier */
	static protected final int IPI = 0xC1;

	/** Mask for group address */
	static protected final byte ADDRESS_GROUP = 0x02;

	/** Mask for last byte of the address field */
	static protected final byte ADDRESS_LAST = 0x01;

	/** Check for a valid address */
	static private void checkAddress(int address)
		throws InvalidAddressException
	{
		if (address < 1 || address > NTCIP_MAX_ADDRESS)
			throw new InvalidAddressException(address);
	}

	/** Create an HDLC address buffer */
	static private byte[] createAddress(int address) {
		if (address < 64)
			return create1ByteAddress(address);
		else
			return create2ByteAddress(address);
	}

	/** Create a one byte HDLC address buffer */
	static protected byte[] create1ByteAddress(int address) {
		byte[] b = new byte[1];
		b[0] = (byte)((address << 2) | ADDRESS_LAST);
		return b;
	}

	/** Create a two byte HDLC address buffer */
	static protected byte[] create2ByteAddress(int address) {
		byte[] b = new byte[2];
		b[0] = (byte)((address << 2) & 0xff);
		b[1] = (byte)((address >> 5) | ADDRESS_LAST);
		return b;
	}

	/** Test if a set of bits is present */
	static protected final boolean testBits(int value, int mask) {
		return mask == (value & mask);
	}

	/** Parse an HDLC address from an input stream */
	static protected int parseAddress(InputStream is) throws IOException {
		int address = 0;
		int b = is.read();
		if(!testBits(b, ADDRESS_LAST)) {
			address = (b << 5);
			b = is.read();
		}
		if(testBits(b, ADDRESS_LAST))
			address |= (b >> 2) & 0x3f;
		else
			throw new ParsingException("INVALID ADDRESS FIELD");
		address &= NTCIP_MAX_ADDRESS;
		if(testBits(b, ADDRESS_GROUP))
			return -address;
		else
			return address;
	}

	/** A FilterOutputStream which is tied to a specific HDLC address.
	 * Many AddressedOutputStreams may be composed with a single
	 * FrameOutputStream, of which there should only be one for each
	 * serial communication line. */
	static public class AddressedOutputStream extends FilterOutputStream {

		/** Buffer to hold the HDLC address */
		protected final byte[] add_buf;

		/** Flag to indicate the next message */
		protected boolean next = true;

		/** Create an HDLC addressed output stream */
		public AddressedOutputStream(OutputStream out, int address)
			throws InvalidAddressException
		{
			super(out);
			checkAddress(address);
			add_buf = createAddress(address);
		}

		/** Write a byte to the output stream, prepending the
		 * address, control, and IPI if starting a new message. */
		public void write(int b) throws IOException {
			try {
				if(next) {
					out.write(add_buf);
					out.write(CONTROL_UI);
					out.write(IPI);
					next = false;
				}
				out.write(b);
			}
			catch(IOException e) {
				next = true;
				throw e;
			}
		}

		/** Flushes the message to the lower-level stream */
		public void flush() throws IOException {
			next = true;
			out.flush();
		}
	}

	/** A FilterInputStream which is tied to a specific HDLC address.
	 * Many AddressedInputStreams may be composed with a single
	 * FrameInputStream, of which there should only be one for each
	 * serial communication line. */
	static public class AddressedInputStream extends InputStream {

		/** HDLC address to read data from */
		protected final int address;

		/** Input stream to be filtered */
		protected final InputStream in;

		/** Create an HDLC addressed input stream */
		public AddressedInputStream(InputStream in, int address)
			throws InvalidAddressException
		{
			this.in = in;
			this.address = address;
			checkAddress(address);
		}

		/** Parse the HDLC "header" (address, control, IPI) */
		protected void parseHeader() throws IOException {
			if(parseAddress(in) != address)
				throw new ParsingException("ADDRESS MISMATCH");
			if(in.read() != CONTROL_UIF) throw new
				ParsingException("INVALID CONTROL FIELD");
			if(in.read() != IPI) throw new
				ParsingException("INVALID IPI FIELD");
		}

		/** Read a byte from the input stream, parsing the
		 * address, control, and IPI if starting a new message. */
		public int read() throws IOException {
			if(in.available() <= 0)
				parseHeader();
			return in.read();
		}

		/** Pass read to filtered stream */
		public int read(byte b[], int off, int len) throws IOException {
			for(int i = 0; i < len; i++) {
				b[off + i] = (byte)read();
			}
			return len;
		}

		/** Pass skip to filtered stream */
		public long skip(long n) throws IOException {
			return in.skip(n);
		}

		/** Pass available to filtered stream */
		public int available() throws IOException {
			return in.available();
		}

		/** Pass close to filtered stream */
		public void close() throws IOException {
			in.close();
		}
	}
}
