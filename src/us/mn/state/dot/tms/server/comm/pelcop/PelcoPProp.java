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
import us.mn.state.dot.tms.server.comm.ControllerProp;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * PelcoP property.
 *
 * @author Douglas Lau
 */
abstract public class PelcoPProp extends ControllerProp {

	/** Start transmission byte */
	static public final int STX = 0xA0;

	/** End transmission byte */
	static public final int ETX = 0xAF;

	/** Parse a valid packet */
	static public PelcoPProp parse(ByteBuffer rx_buf)
		throws ParsingException
	{
		if (parse8(rx_buf) != STX)
			throw new ParsingException("STX");
		int mc = parse8(rx_buf);
		switch (mc) {
		case MonStatusProp.REQ_CODE:
			return new MonStatusProp();
		default:
			throw new ParsingException("Unknown msg code: " + mc);
		}
	}

	/** Parse the tail of a packet */
	static public void parseTail(ByteBuffer rx_buf) throws ParsingException{
		if (parse8(rx_buf) != ETX)
			throw new ParsingException("ETX");
		// Parse checksum (already checked)
		parse8(rx_buf);
	}
}
