/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.ntcip.ASN1OctetStr;

/**
 * MessageActivationCode type
 *
 * @author Douglas Lau
 */
abstract public class MessageActivationCode extends ASN1OctetStr {

	/** Message duration */
	protected int duration;

	/** Get the message duration */
	public int getDuration() {
		return duration;
	}

	/** Activation priority */
	protected int priority;

	/** Get the activation priority */
	public int getPriority() {
		return priority;
	}

	/** Memory type */
	protected int memory;

	/** Get the memory type */
	public int getMemory() {
		return memory;
	}

	/** Message number */
	protected int number;

	/** Get the message number */
	public int getNumber() {
		return number;
	}

	/** Cyclic redundancy check */
	protected int crc;

	/** Get the CRC */
	public int getCrc() {
		return crc;
	}

	/** Source address */
	protected int address;

	/** Get the source address */
	public int getAddress() {
		return address;
	}

	/** Set the octet string value */
	public void setOctetString(byte[] value) {
		ByteArrayInputStream bis = new ByteArrayInputStream(value);
		DataInputStream dis = new DataInputStream(bis);
		try {
			duration = dis.readUnsignedShort();
			priority = dis.readUnsignedByte();
			memory = dis.readUnsignedByte();
			number = dis.readUnsignedShort();
			crc = dis.readUnsignedShort();
			address = dis.readInt();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/** Get the octet string value */
	public byte[] getOctetString() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeShort(duration);
			dos.writeByte(priority);
			dos.writeByte(memory);
			dos.writeShort(number);
			dos.writeShort(crc);
			dos.writeInt(address);
			return bos.toByteArray();
		}
		catch(IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder b = new StringBuilder();
		b.append(duration);
		b.append(",");
		b.append(priority);
		b.append(",");
		b.append(DmsMessageMemoryType.getDescription(memory));
		b.append(",");
		b.append(number);
		b.append(",");
		b.append(crc);
		b.append(",");
		b.append(address);
		return b.toString();
	}
}
