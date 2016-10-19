/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelcop;

import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ControllerProp;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * PelcoP property.
 *
 * @author Douglas Lau
 */
abstract public class PelcoPProp extends ControllerProp {

	/** Start transmission byte */
	static private final int STX = 0xA0;

	/** End transmission byte */
	static private final int ETX = 0xAF;

	/** Parse a valid packet */
	static public PelcoPProp parse(ByteBuffer rx_buf, boolean logged_in)
		throws ParsingException
	{
		scanPkt(rx_buf);
		if (parse8(rx_buf) != STX)
			throw new ParsingException("STX");
		int mc = parse8(rx_buf);
		switch (mc) {
		case AliveProp.REQ_CODE:
			return new AliveProp();
		case LoginProp.REQ_CODE:
			return new LoginProp();
		case MonStatusProp.REQ_CODE:
		case MonStatusProp.RESP_CODE:
			return new MonStatusProp(logged_in);
		case MonCycleProp.REQ_CODE:
			return new MonCycleProp(logged_in);
		case CamSelectProp.REQ_CODE:
			return new CamSelectProp(logged_in);
		case CamPrevProp.REQ_CODE:
			return new CamPrevProp(logged_in);
		case CamNextProp.REQ_CODE:
			return new CamNextProp(logged_in);
		default:
			throw new ParsingException("Unknown msg code: " + mc);
		}
	}

	/** Scan received data for a valid packet */
	static private void scanPkt(ByteBuffer rx_buf) throws ChecksumException{
		rx_buf.mark();
		while ((rx_buf.get() & 0xFF) != STX)
			rx_buf.mark();
		int xsum = STX;
		while (true) {
			int b = rx_buf.get() & 0xFF;
			xsum ^= b;
			if (b == ETX)
				break;
		}
		int c = rx_buf.get() & 0xFF;
		if (c == xsum)
			rx_buf.reset();
		else {
			rx_buf.mark();
			throw new ChecksumException();
		}
	}

	/** Parse the tail of a packet */
	static public void parseTail(ByteBuffer rx_buf) throws ParsingException{
		if (parse8(rx_buf) != ETX)
			throw new ParsingException("ETX");
		// Parse checksum (already checked)
		parse8(rx_buf);
	}

	/** Format the head of a packet */
	static public void formatHead(ByteBuffer tx_buf) {
		tx_buf.put((byte) STX);
		tx_buf.mark();
	}

	/** Format the tail of a packet */
	static public void formatTail(ByteBuffer tx_buf) {
		tx_buf.put((byte) ETX);
		tx_buf.reset();
		int xsum = STX;
		while (true) {
			int b = tx_buf.get() & 0xFF;
			xsum ^= b;
			if (b == ETX)
				break;
		}
		tx_buf.put((byte) xsum);
	}

	/** Get the next property to send */
	public PelcoPProp next() {
		return null;
	}
}
