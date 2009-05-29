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

/**
 * Ntcip DmsActivateMessage object
 *
 * @author Douglas Lau
 */
public class DmsActivateMessage extends MessageActivationCode {

	/** Create a new DmsActivateMessage object */
	public DmsActivateMessage() {
	}

	/** Create a new DmsActivateMessage object
	 * @param d duration (in minutes)
	 * @param p activation priority
	 * @param m memory type
	 * @param n message number
	 * @param c CRC (dmsMsgMessageCRC)
	 * @param a source address */
	public DmsActivateMessage(int d, int p, DmsMessageMemoryType.Enum m,
		 int n, int c, int a)
	{
		duration = d;
		priority = p;
		memory = m.ordinal();
		number = n;
		crc = c;
		address = a;
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIBNode.signControl.createOID(new int[] {3, 0});
	}
}
