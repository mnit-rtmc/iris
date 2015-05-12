/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dr500;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Variable (by index) property.
 *
 * @author Douglas Lau
 */
public class VarIndexProperty extends DR500Property {

	/** Variable index */
	private final byte index;

	/** Name of variable */
	private String vname = "";

	/** Value of variable */
	private int value;

	/** Create a variable (by index) property */
	public VarIndexProperty(byte i) {
		index = i;
	}

	/** Get the value of the variable */
	public int getValue() {
		return value;
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] body = new byte[3];
		body[0] = (byte) MsgCode.VAR_INDEX_QUERY.code;
		body[1] = 0;	/* config domain */
		body[2] = index;
		encodeRequest(os, body);
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		Response resp = decodeResponse(is);
		vname = parseName(resp);
		value = parseValue(resp);
	}

	/** Parse a variable name response */
	private String parseName(Response resp) throws IOException {
		checkMsgCode(resp, MsgCode.VAR_RESP);
		if (5 != resp.body.length)
			throw new ParsingException("LEN:" + resp.body.length);
		byte dom = resp.body[0];
		if (0 != dom)
			throw new ParsingException("DOMAIN:" + dom);
		return new String(resp.body, 1, 2, ASCII);
	}

	/** Parse a variable value response */
	private int parseValue(Response resp) throws IOException {
		return parse16le(resp.body, 3);
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return vname + ": " + value;
	}
}
