/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import us.mn.state.dot.tms.server.comm.snmp.ASN1OctetString;
import us.mn.state.dot.tms.server.comm.snmp.MIBNode;

/**
 * A MessageIDCode is a code which identifies a message.  It is encoded using
 * OER (NTCIP 1102).
 *
 * @author Douglas Lau
 */
abstract public class MessageIDCode extends ASN1OctetString {

	/** Encode message ID value.
	 * @param m Message memory type.
	 * @param n Message number.
	 * @param c CRC of message.
	 * @return Encoded array of bytes.
	 * @throws IllegalArgumentException, if encoding fails. */
	static private byte[] encodeValue(DmsMessageMemoryType.Enum m, int n,
		int c)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeByte(m.ordinal());
			dos.writeShort(n);
			dos.writeShort(c);
			return bos.toByteArray();
		}
		catch(IOException e) {
			throw new IllegalArgumentException(e);
		}
		finally {
			try {
				dos.close();
				bos.close();
			}
			catch(IOException e) {
				// exceptions on close are stupid -- ignore
			}
		}
	}

	/** Create a new MessageIDCode */
	protected MessageIDCode(MIBNode n) {
		super(n);
	}

	/** Memory type */
	protected DmsMessageMemoryType.Enum memory;

	/** Set the memory type */
	public void setMemoryType(DmsMessageMemoryType.Enum m) {
		value = encodeValue(m, number, crc);
		memory = m;
	}

	/** Get the memory type */
	public DmsMessageMemoryType.Enum getMemoryType() {
		return memory;
	}

	/** Message number */
	protected int number;

	/** Set the message number */
	public void setNumber(int n) {
		value = encodeValue(memory, n, crc);
		number = n;
	}

	/** Get the message number */
	public int getNumber() {
		return number;
	}

	/** Cyclic redundancy check */
	private int crc;

	/** Set the CRC */
	public void setCrc(int c) {
		value = encodeValue(memory, number, c);
		crc = c;
	}

	/** Get the CRC */
	public int getCrc() {
		return crc;
	}

	/** Set the octet string value */
	@Override
	public void setOctetString(byte[] v) {
		ByteArrayInputStream bis = new ByteArrayInputStream(v);
		DataInputStream dis = new DataInputStream(bis);
		try {
			memory = DmsMessageMemoryType.Enum.fromOrdinal(
				dis.readUnsignedByte());
			number = dis.readUnsignedShort();
			crc = dis.readUnsignedShort();
			value = v;
		}
		catch(IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/** Get the object value */
	@Override
	public String getValue() {
		StringBuilder b = new StringBuilder();
		b.append(memory);
		b.append(",");
		b.append(number);
		b.append(",");
		b.append(crc);
		return b.toString();
	}
}
