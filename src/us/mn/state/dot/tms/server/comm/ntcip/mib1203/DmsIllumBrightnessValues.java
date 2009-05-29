/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
 * Ntcip DmsIllumBrightnessValues object
 *
 * @author Douglas Lau
 */
public class DmsIllumBrightnessValues extends ASN1OctetStr {

	/** Get the object identifier */
	public int[] getOID() {
		return MIBNode.illum.createOID(new int[] {7, 0});
	}

	/** Set the brightness table */
	public void setTable(int[][] table) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeByte(table.length);
			for(int[] level: table) {
				dos.writeShort(level[0]); // lightOutput
				dos.writeShort(level[1]); // photocellLevelDown
				dos.writeShort(level[2]); // photocellLevelUp
			}
			value = bos.toByteArray();
		}
		finally {
			dos.close();
			bos.close();
		}
	}

	/** Get the brightness table */
	public int[][] getTable() throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(value);
		DataInputStream dis = new DataInputStream(bis);
		try {
			int[][] table = new int[dis.readByte()][3];
			for(int[] level: table) {
				level[0] = dis.readUnsignedShort();
				level[1] = dis.readUnsignedShort();
				level[2] = dis.readUnsignedShort();
			}
			return table;
		}
		finally {
			dis.close();
			bis.close();
		}
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder b = new StringBuilder();
		try {
			int[][] table = getTable();
			b.append(table.length);
			for(int[] level: table) {
				b.append(", (");
				b.append(level[0]);
				b.append(",");
				b.append(level[1]);
				b.append(",");
				b.append(level[2]);
				b.append(")");
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return b.toString();
	}
}
