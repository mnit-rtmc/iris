/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.natch;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import us.mn.state.dot.tms.server.comm.ControllerProp;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.utils.HexString;

/**
 * Property for Natch protocol
 *
 * @author Douglas Lau
 */
abstract public class NatchProp extends ControllerProp {

	/** UTF-8 charset */
	static protected final Charset UTF8 = Charset.forName("UTF-8");

	/** Parse a positive integer value */
	static protected int parseInt(String v) {
		try {
			return Integer.parseInt(v);
		}
		catch (NumberFormatException e) {
			return -1;
		}
	}

	/** Message ID */
	protected final String message_id;

	/** Create a new natch property */
	protected NatchProp(Counter c) {
		message_id = HexString.format(c.next(), 4);
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(Operation op, ByteBuffer rx_buf)
		throws IOException
	{
		byte[] buf = new byte[rx_buf.remaining()];
		rx_buf.get(buf);
		doRecv(new String(buf, UTF8));
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws IOException
	{
		byte[] buf = new byte[rx_buf.remaining()];
		rx_buf.get(buf);
		doRecv(new String(buf, UTF8));
	}

	/** Parse received message */
	private void doRecv(String msgs) throws IOException {
		boolean received = false;
		for (String msg : msgs.split("\n")) {
			received |= parseMsg(msg);
		}
		if (!received)
			throw new ParsingException("Invalid response");
	}

	/** Parse one received message */
	abstract protected boolean parseMsg(String msg) throws IOException;
}
