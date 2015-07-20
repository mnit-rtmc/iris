/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.snmp;

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Basic Encoding Rules for ASN.1
 *
 * @author Douglas Lau
 */
abstract public class BER extends ASN1 {

	/** End of stream exception */
	static protected final EOFException END_OF_STREAM =
		new EOFException("END OF STREAM");

	/** Constant to check the high bit of a byte */
	static public final byte HIGH_BIT = (byte)0x80;

	/** Constant to check the low seven bits of a byte */
	static public final byte SEVEN_BITS = 0x7F;

	/** Reserved length code constant */
	static public final int RESERVED = 0xFF;

	/** Tag numbers equal or greater than ONE_OCTET are encoded with more
	 * than one octet */
	static private final int ONE_OCTET = 0x1F;

	/** Encode a BER identifier to the output stream */
	protected void encodeIdentifier(Tag tag) throws IOException {
		byte first = tag.getClazz();
		int number = tag.getNumber();
		if (tag.isConstructed())
			first |= Tag.CONSTRUCTED;
		if (number < ONE_OCTET) {
			encoder.write(first | number);
			return;
		}
		encoder.write(first | ONE_OCTET);
		byte[] buffer = new byte[5];
		int start = 4;
		buffer[start] = (byte)(number & SEVEN_BITS);
		for (number >>= 7; number > SEVEN_BITS; number >>= 7) {
			buffer[--start] = (byte)
				((number & SEVEN_BITS) | HIGH_BIT);
		}
		encoder.write(buffer, start, 5 - start);
	}

	/** Encode a BER length */
	protected void encodeLength(int length) throws IOException {
		if (length < 128)
			encoder.write(length);
		else if (length < 256) {
			encoder.write(HIGH_BIT | 1);
			encoder.write(length);
		} else {
			encoder.write(HIGH_BIT | 2);
			encoder.write((byte)(length >> 8));
			encoder.write((byte)(length & 0xFF));
		}
	}

	/** Encode a boolean value */
	protected void encodeBoolean(boolean value) throws IOException {
		encodeIdentifier(ASN1Tag.BOOLEAN);
		encodeLength(1);
		if (value)
			encoder.write(0xFF);
		else
			encoder.write(0x00);
	}

	/** Encode an integer value */
	protected void encodeInteger(int value) throws IOException {
		byte[] buffer = new byte[4];
		int len = 0;
		boolean flag = false;
		for (int shift = 23; shift > 0; shift -= 8) {
			int test = (value >> shift) & 0x1FF;
			if (test != 0 && test != 0x1FF)
				flag = true;
			if (flag)
				buffer[len++] = (byte)(test >> 1);
		}
		buffer[len++] = (byte)(value & 0xFF);
		encodeIdentifier(ASN1Tag.INTEGER);
		encodeLength(len);
		encoder.write(buffer, 0, len);
	}

	/** Encode an octet string */
	protected void encodeOctetString(byte[] string) throws IOException {
		encodeIdentifier(ASN1Tag.OCTET_STRING);
		encodeLength(string.length);
		encoder.write(string);
	}

	/** Encode a null value */
	protected void encodeNull() throws IOException {
		encodeIdentifier(ASN1Tag.NULL);
		encodeLength(0);
	}

	/** Encode an object identifier */
	protected void encodeObjectIdentifier(int[] oid) throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		bs.write(oid[0] * 40 + oid[1]);
		for (int i = 2; i < oid.length; i++) {
			int subid = oid[i];
			if (subid > SEVEN_BITS) {
				bs.write(HIGH_BIT | (subid >> 7));
				subid &= SEVEN_BITS;
			}
			bs.write(subid);
		}
		byte[] buffer = bs.toByteArray();
		encodeIdentifier(ASN1Tag.OBJECT_IDENTIFIER);
		encodeLength(buffer.length);
		encoder.write(buffer);
	}

	/** Encode a sequence (or sequence-of) */
	protected void encodeSequence(byte[] seq) throws IOException {
		encodeIdentifier(ASN1Tag.SEQUENCE);
		encodeLength(seq.length);
		encoder.write(seq);
	}

	/** Decode a BER identifier (tag) */
	protected Tag decodeIdentifier(InputStream is) throws IOException {
		int first = is.read();
		if (first < 0)
			throw END_OF_STREAM;
		byte clazz = (byte)(first & Tag.CLASS_MASK);
		boolean constructed = (first & Tag.CONSTRUCTED) != 0;
		int number = (first & ONE_OCTET);
		if (number == ONE_OCTET)
			number = decodeSubidentifier(is);
		return getTag(clazz, constructed, number);
	}

	/** Decode a BER subidentifier */
	protected int decodeSubidentifier(InputStream is) throws IOException {
		int number = 0;
		for (int i = 0; i < 4; i++) {
			int next = is.read();
			if (next < 0)
				throw END_OF_STREAM;
			number <<= 7;
			number |= (next & SEVEN_BITS);
			if ((next & HIGH_BIT) != 0)
				return number;
		}
		throw new ParsingException("INVALID SUBIDENTIFIER");
	}

	/** Decode a BER length */
	protected int decodeLength(InputStream is) throws IOException {
		int first = is.read();
		if (first < 0)
			throw END_OF_STREAM;
		if (first == RESERVED)
			throw new ParsingException("RESERVED LENGTH CODE");
		int length = first & SEVEN_BITS;
		if (length != first) {
			if (length == 0)
				throw new ParsingException("INDEFINITE LENGTH");
			int i = length;
			for (length = 0; i > 0; i--) {
				length <<= 8;
				int lg = is.read();
				if (lg < 0)
					throw END_OF_STREAM;
				length |= lg;
			}
		}
		if (length > is.available()) {
			throw new ParsingException("INVALID LENGTH: " + length +
				" > " + is.available());
		}
		return length;
	}

	/** Decode an integer */
	protected int decodeInteger(InputStream is) throws IOException {
		Tag tag = decodeIdentifier(is);
		// Skyline signs return dmsFreeChangeableMemory and 
		// dmsFreeVolatileMemory as INTEGER_SKYLINE instead of INTEGER
		if (tag != ASN1Tag.INTEGER && tag != SNMPTag.INTEGER_SKYLINE)
			throw new ParsingException("EXPECTED AN INTEGER TAG");
		int length = decodeLength(is);
		if (length < 1 || length > 4)
			throw new ParsingException("INVALID INTEGER LENGTH");
		int value = is.read();
		if (value < 0)
			throw END_OF_STREAM;
		value = (byte)value;	// NOTE: cast to preserve sign
		for (int i = 1; i < length; i++) {
			value <<= 8;
			int v = is.read();
			if (v < 0)
				throw END_OF_STREAM;
			value |= v;
		}
		return value;
	}

	/** Decode an octet string */
	protected byte[] decodeOctetString(InputStream is) throws IOException {
		if (decodeIdentifier(is) != ASN1Tag.OCTET_STRING)
			throw new ParsingException("EXPECTED OCTET STRING TAG");
		int length = decodeLength(is);
		if (length < 0)
			throw new ParsingException("NEGATIVE STRING LENGTH");
		byte[] buffer = new byte[length];
		if (length > 0) {
			int blen = is.read(buffer);
			if (blen < 0)
				throw END_OF_STREAM;
			if (blen != length)
				throw new ParsingException("READ STRING FAIL");
		}
		return buffer;
	}

	/** Decode an object identifier */
	protected int[] decodeObjectIdentifier(InputStream is)
		throws IOException
	{
		if (decodeIdentifier(is) != ASN1Tag.OBJECT_IDENTIFIER) {
			throw new ParsingException(
				"EXPECTED OBJECT IDENTIFIER TAG");
		}
		int length = decodeLength(is);
		if (length < 1)
			throw new ParsingException("NEGATIVE OID LENGTH");
		byte[] buffer = new byte[length];
		if (length > 0) {
			int blen = is.read(buffer);
			if (blen < 0)
				throw END_OF_STREAM;
			if (blen != length)
				throw new ParsingException("READ OID FAIL");
		}
		// NOTE: when the length is zero, there is no OID
		return new int[0];
	}

	/** Decode a sequence (or sequence-of)
	  * @return Length of sequence */
	protected int decodeSequence(InputStream is) throws IOException {
		if (decodeIdentifier(is) != ASN1Tag.SEQUENCE)
			throw new ParsingException("EXPECTED SEQUENCE TAG");
		return decodeLength(is);
	}
}
