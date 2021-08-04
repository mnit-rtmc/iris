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
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * Pin Status property
 *
 * @author Douglas Lau
 */
public class PinStatusProp extends NatchProp {

	/** Pin number */
	private final String pin;

	/** Pin status */
	private boolean status;

	/** Get the pin status */
	public boolean getStatus() {
		return status;
	}

	/** Create a new pin status property */
	public PinStatusProp(Counter c, int p, boolean st) {
		super(c);
		pin = Integer.toString(p);
		status = st;
	}

	/** Create a new pin status property */
	public PinStatusProp(Counter c, int p) {
		super(c);
		pin = Integer.toString(p);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("PS,");
		sb.append(message_id);
		sb.append(',');
		sb.append(pin);
		sb.append(',');
		sb.append(status ? '1' : '0');
		sb.append('\n');
		tx_buf.put(sb.toString().getBytes(UTF8));
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("PS,");
		sb.append(message_id);
		sb.append(',');
		sb.append(pin);
		sb.append('\n');
		tx_buf.put(sb.toString().getBytes(UTF8));
	}

	/** Parse received message */
	@Override
	protected boolean parseMsg(String msg) throws IOException {
		String[] param = msg.split(",");
		if (param.length == 4 &&
		    param[0].equals("ps") &&
		    param[1].equals(message_id) &&
		    param[2].equals(pin))
		{
			status = param[3].equals("1");
			return true;
		}
		return false;
	}
}
