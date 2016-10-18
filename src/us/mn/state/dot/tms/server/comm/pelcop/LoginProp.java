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

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Login property.
 *
 * @author Douglas Lau
 */
public class LoginProp extends PelcoPProp {

	/** Login request code */
	static public final int REQ_CODE = 0xF4;

	/** Login response code */
	static public final int RESP_CODE = 0xD3;

	/** Login PIN */
	private int pin;

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		if (parse8(rx_buf) != 1)
			throw new ParsingException("LOGIN A");
		pin = parseBCD4(rx_buf);
		if (parse8(rx_buf) != 1)
			throw new ParsingException("LOGIN B");
	}

	/** Encode a QUERY response to keyboard */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		format8(tx_buf, RESP_CODE);
		if (isSuccess()) {
			format8(tx_buf, 1);
			format8(tx_buf, 0x17);		// kbd ID
		} else
			format8(tx_buf, 0);
	}

	/** Is login successful? */
	public boolean isSuccess() {
		// FIXME: check pin here
		return true;
	}

	/** Get the next property to send */
	@Override
	public PelcoPProp next() {
		return isSuccess() ? new DateTimeProp() : null;
	}
}
