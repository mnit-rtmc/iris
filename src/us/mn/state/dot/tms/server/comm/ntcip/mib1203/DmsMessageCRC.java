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

import java.io.DataOutputStream;
import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ntcip.ASN1Integer;
import us.mn.state.dot.tms.server.comm.ntcip.CRC16;

/**
 * DmsMessageCRC is a CRC based on the dmsMessageMultiString, dmsMessageBeacon,
 * and dmsMessagePixelService objects.  These values are encoded using OER
 * (NTCIP 1102) and the CRC is performed on the result.
 *
 * @author Douglas Lau
 */
public class DmsMessageCRC extends ASN1Integer {

	/** Calculate the CRC for a message */
	static public int calculate(String multi, int beacon, int srv) {
		CRC16 crc16 = new CRC16();
		try {
			DataOutputStream dos = new DataOutputStream(crc16);
			dos.write(multi.getBytes());
			dos.writeByte(beacon);
			dos.writeByte(srv);
			int crc = crc16.getCrc() ^ CRC16.INITIAL_CRC;
			return ((crc & 0xFF) << 8) | ((crc >> 8) & 0xFF);
		}
		catch(IOException e) {
			// This should never happen
			return 0;
		}
	}

	/** Create a new DmsMessageCRC object */
	public DmsMessageCRC(DmsMessageMemoryType.Enum m, int number) {
		super(MIB1203.dmsMessageEntry.create(new int[] {
			5, m.ordinal(), number}));
	}
}
