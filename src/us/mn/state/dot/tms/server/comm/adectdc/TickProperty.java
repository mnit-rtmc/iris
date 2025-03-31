/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.adectdc;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Tick count property
 *
 * NOTE: Ticks only supported by TDC3(4) with 2+1, 5+1 and 8+1 classes.
 *
 * @author Douglas Lau
 */
public class TickProperty extends TdcProperty {

	/** Tick count */
	public int ticks = 0;

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		os.write(formatShort(CTRL_TICK, c));
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		byte[] buf = parseLong(is, c);
		if (buf == null)
			throw new ParsingException("Expected long frame");
		if (buf[0] != (CTRL_TICK & 0x0F))
			throw new ParsingException("Wrong CTRL: " + buf[0]);
		if (buf.length != 5)
			throw new ParsingException("Wrong len: " + buf.length);
		ticks = parse24(buf, 2);
	}

	/** Get ticks as a string */
	@Override
	public String toString() {
		return "ticks: " + ticks;
	}
}
