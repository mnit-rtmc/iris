/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Alarm cycle property.
 *
 * @author Douglas Lau
 */
public class AlarmCycleProp extends PelcoPProp {

	/** Alarm cycle request code */
	static public final int REQ_CODE = 0xD9;

	/** Alarm response code */
	static public final int RESP_CODE = 0xD0;

	/** Cycle dir codes */
	static private final int DIR_NEXT = 1;
	static private final int DIR_PREV = 2;

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		int dir = parse8(rx_buf);
		switch (dir) {
		case DIR_NEXT:
			setErrMsg(ErrorMsg.AlmNotPresent);
			break;
		case DIR_PREV:
			setErrMsg(ErrorMsg.AlmNotPresent);
			break;
		default:
			throw new ParsingException("CYCLE DIR");
		}
	}

	/** Encode a QUERY response to keyboard */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		if (hasError()) {
			encodeError(op, tx_buf);
			return;
		}
		format8(tx_buf, RESP_CODE);
		format16(tx_buf, 0);
	}
}
