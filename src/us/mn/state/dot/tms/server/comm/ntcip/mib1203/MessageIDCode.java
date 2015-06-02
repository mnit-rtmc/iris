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
public class MessageIDCode extends ASN1OctetString {

	/** Create a new MessageIDCode */
	public MessageIDCode(MIBNode n) {
		super(n);
	}

	/** Memory type */
	private DmsMessageMemoryType memory = DmsMessageMemoryType.undefined;

	/** Set the memory type */
	public void setMemoryType(DmsMessageMemoryType m) {
		if (m == null)
			throw new IllegalArgumentException();
		memory = m;
	}

	/** Get the memory type */
	public DmsMessageMemoryType getMemoryType() {
		return memory;
	}

	/** Message number */
	private int number;

	/** Set the message number */
	public void setNumber(int n) {
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
		crc = c;
	}

	/** Get the CRC */
	public int getCrc() {
		return crc;
	}

	/** Set the octet string value.
	 * Note: the value from ASN1OctetString is not used. */
	@Override
	public void setOctetString(byte[] v) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(v);
		DataInputStream dis = new DataInputStream(bis);
		memory = DmsMessageMemoryType.fromOrdinal(
			dis.readUnsignedByte());
		number = dis.readUnsignedShort();
		crc = dis.readUnsignedShort();
	}

	/** Get the octet string value.
	 * Note: the value from ASN1OctetString is not used. */
	@Override
	public byte[] getOctetString() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeByte(memory.ordinal());
		dos.writeShort(number);
		dos.writeShort(crc);
		return bos.toByteArray();
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
