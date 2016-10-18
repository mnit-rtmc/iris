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
 * Monitor status property.  The CM9760-KBD sends this request to the matrix
 * about every 0.75 seconds.  The matrix should send a monitor status response
 * back.  If logon is required, an empty monitor status response is sent.
 *
 * @author Douglas Lau
 */
public class MonStatusProp extends PelcoPProp {

	/** Monitor status request code */
	static public final int REQ_CODE = 0xBA;

	/** Monitor status response code */
	static public final int RESP_CODE = 0xB1;

	/** Video monitor number */
	private int monitor;

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		int mlo = parseBCD2(rx_buf);
		if (parse8(rx_buf) != 1)
			throw new ParsingException("MON EXT");
		int mhi = parseBCD2(rx_buf);
		monitor = (100 * mhi) + mlo;
	}

	/** Encode a QUERY response to keyboard */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf) {
		format8(tx_buf, RESP_CODE);
		format8(tx_buf, 0);
		format8(tx_buf, 0);
	}
}
