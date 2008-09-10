/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.ntcip;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.FilterInputStream;
import us.mn.state.dot.tms.comm.ChecksumException;
import us.mn.state.dot.tms.comm.ParsingException;
import us.mn.state.dot.tms.comm.PortException;

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

	/** Invalid address exception */
	static public final class InvalidAddressException
		extends IndexOutOfBoundsException
	{
		/** Create an invalid address exception */
		public InvalidAddressException(int address) {
			super("Invalid HDLC address: " + address);
		}
	}

	/** Look-up table for CRC calculations */
	static protected final int[] TABLE = {
		0x0000,0x1189,0x2312,0x329b,0x4624,0x57ad,0x6536,0x74bf,
		0x8c48,0x9dc1,0xaf5a,0xbed3,0xca6c,0xdbe5,0xe97e,0xf8f7,
		0x1081,0x0108,0x3393,0x221a,0x56a5,0x472c,0x75b7,0x643e,
		0x9cc9,0x8d40,0xbfdb,0xae52,0xdaed,0xcb64,0xf9ff,0xe876,
		0x2102,0x308b,0x0210,0x1399,0x6726,0x76af,0x4434,0x55bd,
		0xad4a,0xbcc3,0x8e58,0x9fd1,0xeb6e,0xfae7,0xc87c,0xd9f5,
		0x3183,0x200a,0x1291,0x0318,0x77a7,0x662e,0x54b5,0x453c,
		0xbdcb,0xac42,0x9ed9,0x8f50,0xfbef,0xea66,0xd8fd,0xc974,
		0x4204,0x538d,0x6116,0x709f,0x0420,0x15a9,0x2732,0x36bb,
		0xce4c,0xdfc5,0xed5e,0xfcd7,0x8868,0x99e1,0xab7a,0xbaf3,
		0x5285,0x430c,0x7197,0x601e,0x14a1,0x0528,0x37b3,0x263a,
		0xdecd,0xcf44,0xfddf,0xec56,0x98e9,0x8960,0xbbfb,0xaa72,
		0x6306,0x728f,0x4014,0x519d,0x2522,0x34ab,0x0630,0x17b9,
		0xef4e,0xfec7,0xcc5c,0xddd5,0xa96a,0xb8e3,0x8a78,0x9bf1,
		0x7387,0x620e,0x5095,0x411c,0x35a3,0x242a,0x16b1,0x0738,
		0xffcf,0xee46,0xdcdd,0xcd54,0xb9eb,0xa862,0x9af9,0x8b70,
		0x8408,0x9581,0xa71a,0xb693,0xc22c,0xd3a5,0xe13e,0xf0b7,
		0x0840,0x19c9,0x2b52,0x3adb,0x4e64,0x5fed,0x6d76,0x7cff,
		0x9489,0x8500,0xb79b,0xa612,0xd2ad,0xc324,0xf1bf,0xe036,
		0x18c1,0x0948,0x3bd3,0x2a5a,0x5ee5,0x4f6c,0x7df7,0x6c7e,
		0xa50a,0xb483,0x8618,0x9791,0xe32e,0xf2a7,0xc03c,0xd1b5,
		0x2942,0x38cb,0x0a50,0x1bd9,0x6f66,0x7eef,0x4c74,0x5dfd,
		0xb58b,0xa402,0x9699,0x8710,0xf3af,0xe226,0xd0bd,0xc134,
		0x39c3,0x284a,0x1ad1,0x0b58,0x7fe7,0x6e6e,0x5cf5,0x4d7c,
		0xc60c,0xd785,0xe51e,0xf497,0x8028,0x91a1,0xa33a,0xb2b3,
		0x4a44,0x5bcd,0x6956,0x78df,0x0c60,0x1de9,0x2f72,0x3efb,
		0xd68d,0xc704,0xf59f,0xe416,0x90a9,0x8120,0xb3bb,0xa232,
		0x5ac5,0x4b4c,0x79d7,0x685e,0x1ce1,0x0d68,0x3ff3,0x2e7a,
		0xe70e,0xf687,0xc41c,0xd595,0xa12a,0xb0a3,0x8238,0x93b1,
		0x6b46,0x7acf,0x4854,0x59dd,0x2d62,0x3ceb,0x0e70,0x1ff9,
		0xf78f,0xe606,0xd49d,0xc514,0xb1ab,0xa022,0x92b9,0x8330,
		0x7bc7,0x6a4e,0x58d5,0x495c,0x3de3,0x2c6a,0x1ef1,0x0f78
	};

	/** Calculate the next CRC value from an existing CRC and one byte */
	static protected int calculateCRC(int crc, int b) {
		return (crc >> 8) ^ TABLE[(b ^ crc) & 0xFF];
	}

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

	/** INITIAL_CRC is the initial CRC value for a message */
	static protected final int INITIAL_CRC = 0xFFFF;

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
		protected int crc;

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
				crc = INITIAL_CRC;
				clear = false;
			}
			crc = calculateCRC(crc, b);
			if(b == FLAG || b == ESCAPE) {
				super.write(ESCAPE);
				b ^= BIT6;
			}
			super.write(b);
		}

		/** Writes out a frame check sequence (FCS) and framing flag,
		 * then flushes the whole message to the wrapped stream. */
		public void flush() throws IOException {
			byte fcs1 = (byte)~crc;
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
			if(scanned >= FRAME_CHECK) {
				scanned -= FRAME_CHECK;
				fcs = (buf[scanned] & 0xFF) |
					(buf[scanned + 1] & 0xFF) << 8;
			} else {
				scanned = 0;
				return;
			}
			int crc = INITIAL_CRC;
			for(int c = 0; c < scanned; c++) {
				int b = buf[c] & 0xFF;
				crc = calculateCRC(crc, b);
			}
			if((crc ^ fcs) == INITIAL_CRC)
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

	/** Create an HDLC address buffer */
	static protected byte[] createAddress(int address) {
		if(address < 1 || address > NTCIP_MAX_ADDRESS)
			throw new InvalidAddressException(address);
		if(address < 64)
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
		public AddressedOutputStream(OutputStream out, int address) {
			super(out);
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
				throw new PortException(e.getMessage());
			}
		}

		/** Flushes the message to the lower-level stream */
		public void flush() throws IOException {
			next = true;
			try { out.flush(); }
			catch(IOException e) {
				throw new PortException(e.getMessage());
			}
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
		public AddressedInputStream(InputStream in, int address) {
			this.in = in;
			this.address = address;
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
