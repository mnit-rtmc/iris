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
		char st = status ? '1' : '0';
		String msg = "PS," + message_id + ',' + pin + ',' + st + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		String msg = "PS," + message_id + ',' + pin + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Get the message code */
	@Override
	protected String code() {
		return "ps";
	}

	/** Get the number of response parameters */
	@Override
	protected int parameters() {
		return 4;
	}

	/** Parse parameters for a received message */
	@Override
	protected boolean parseParams(String[] param) {
		if (param[2].equals(pin)) {
			status = parseBool(param[3]);
			return true;
		} else
			return false;
	}
}
