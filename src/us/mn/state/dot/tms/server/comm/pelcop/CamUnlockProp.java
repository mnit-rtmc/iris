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
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Camera unlock property.
 *
 * @author Douglas Lau
 */
public class CamUnlockProp extends MonStatusProp {

	/** Camera unlock request code */
	static public final int REQ_CODE = 0xBC;

	/** Create a new camera unlock property */
	public CamUnlockProp(boolean l) {
		super(l);
	}

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		if (parse8(rx_buf) != 1)
			throw new ParsingException("UNLOCK A");
		int mlo = parseBCD2(rx_buf);
		int cam = parseBCD4(rx_buf);
		int mhi = parseBCD2(rx_buf);
		if (parse8(rx_buf) != 0)
			throw new ParsingException("UNLOCK B");
		setMonNumber((100 * mhi) + mlo);
		// FIXME: clear monitor locked status
	}
}
