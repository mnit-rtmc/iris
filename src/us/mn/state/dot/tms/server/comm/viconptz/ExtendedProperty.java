/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.viconptz;

/**
 * Extended Vicon property.
 *
 * @author Douglas Lau
 */
abstract public class ExtendedProperty extends ViconPTZProperty {

	/** Mask for extended command requests (second byte) */
	static private final byte EXTENDED_CMD = 0x50;

	/** Get command parameter 1 */
	abstract protected int getParam1();

	/** Get command parameter 2 */
	abstract protected int getParam2();

	/** Create extended Vicon packet */
	@Override
	protected byte[] createPacket(int drop) {
		int p1 = getParam1();
		int p2 = getParam2();
		byte[] pkt = new byte[10];
		pkt[0] = (byte)(0x80 | (drop >> 4));
		pkt[1] = (byte)((0x0f & drop) | EXTENDED_CMD);
		pkt[2] = panTiltFlags();
		pkt[3] = lensFlags();
		pkt[4] = auxBits();
		pkt[5] = presetBits();
		pkt[6] = (byte)((p1 >>> 7) & 0x7f);
		pkt[7] = (byte)((p1 >>> 0) & 0x7f);
		pkt[8] = (byte)((p2 >>> 7) & 0x7f);
		pkt[9] = (byte)((p2 >>> 0) & 0x7f);
		return pkt;
	}
}
