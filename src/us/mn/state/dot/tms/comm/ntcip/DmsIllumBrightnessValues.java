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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Ntcip DmsIllumBrightnessValues object
 *
 * @author Douglas Lau
 */
public class DmsIllumBrightnessValues extends Illum implements ASN1OctetString {

	/** Create a new DmsIllumBrightnessValues object */
	public DmsIllumBrightnessValues(int[] table) {
		super(7);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			int levels = table.length / 3;
			dos.writeByte(levels);
			for(int i = 0; i < levels * 3; i++)
				dos.writeShort(table[i]);
			brightness = bos.toByteArray();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				dos.close();
				bos.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** Get the object name */
	protected String getName() { return "dmsIllumBrightnessValues"; }

	/** Brightness values */
	protected byte[] brightness;

	/** Set the octet string value */
	public void setOctetString(byte[] value) { brightness = value; }

	/** Get the octet string value */
	public byte[] getOctetString() { return brightness; }

	/** Get the brightness table */
	public int[] getTable() {
		ByteArrayInputStream bis = new ByteArrayInputStream(brightness);
		DataInputStream dis = new DataInputStream(bis);
		try {
			int[] table = new int[3 * dis.readByte()];
			for(int i = 0; i < table.length; i++)
				table[i] = dis.readUnsignedShort();
			return table;
		}
		catch(IOException e) {
			e.printStackTrace();
			return new int[0];
		}
		finally {
			try {
				dis.close();
				bis.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** Get the object value */
	public String getValue() {
		StringBuffer buffer = new StringBuffer();
		int[] table = getTable();
		int levels = table.length / 3;
		buffer.append(levels);
		for(int i = 0; i < levels; i++) {
			int j = i * 3;
			buffer.append(", (").append(table[j]).append(",");
			buffer.append(table[j + 1]).append(",");
			buffer.append(table[j + 2]).append(")");
		}
		return buffer.toString();
	}
}
