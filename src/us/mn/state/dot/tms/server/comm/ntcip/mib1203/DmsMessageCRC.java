/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.ntcip.CRCStream;

/**
 * DmsMessageCRC is a CRC based on the dmsMessageMultiString, dmsMessageBeacon,
 * and dmsMessagePixelService objects.  These values are encoded using OER
 * (NTCIP 1102) and the CRC is performed on the result.
 *
 * @author Douglas Lau
 */
public class DmsMessageCRC {

	/** Calculate the CRC for a message */
	static private int calculate(String multi, int beacon, int srv) {
		CRCStream crc16 = new CRCStream();
		try {
			DataOutputStream dos = new DataOutputStream(crc16);
			dos.write(multi.getBytes());
			dos.writeByte(beacon);
			dos.writeByte(srv);
			return crc16.getCrcSwapped();
		}
		catch (IOException e) {
			// This should never happen
			return 0;
		}
	}

	/** Calculate the CRC for a message.
	 * @param multi MULTI string.
	 * @param beacon_enabled Beacon enabled flag.
	 * @param src Pixel service enabled flag.
	 * @return CRC-16 of message. */
	static public int calculate(String multi, boolean beacon_enabled,
		int srv)
	{
		return calculate(multi, (beacon_enabled) ? 1 : 0, srv);
	}

	/** Don't instantiate */
	private DmsMessageCRC() { }
}
