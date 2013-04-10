/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.ntcip.ASN1OctetString;

/**
 * Color of LEDs on a monochrome sign.  This object was added in 1203v2.
 *
 * @author Douglas Lau
 */
public class MonochromeColor extends ASN1OctetString {

	/** Create a new MonochromeColor object */
	public MonochromeColor() {
		super(MIB1203.vmsCfg.create(new int[] {7, 0}));
	}

	/** Get the value array.
	 * @return Array of 6 bytes (3 RGB on and 3 RGB off colors) */
	private byte[] valArray() {
		byte[] val = value;
		if(val.length == 6)
			return val;
		else
			return new byte[6];
	}

	/** Get the foreground color.
	 * @return Array of 3 bytes (RGB of foreground color). */
	public byte[] getForeground() {
		byte[] fg = new byte[3];
		System.arraycopy(valArray(), 0, fg, 0, 3);
		return fg;
	}

	/** Get the background color.
	 * @return Array of 3 bytes (RGB of background color). */
	public byte[] getBackground() {
		byte[] bg = new byte[3];
		System.arraycopy(valArray(), 3, bg, 0, 3);
		return bg;
	}
}
