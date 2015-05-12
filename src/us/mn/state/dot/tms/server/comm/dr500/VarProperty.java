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
 * Variable (by name) property.
 *
 * @author Douglas Lau
 */
public class VarProperty extends DR500Property {

	/** Name of variable */
	private final VarName vname;

	/** Value of variable */
	private int value;

	/** Create a variable property */
	public VarProperty(VarName n, int v) {
		vname = n;
		value = v;
	}

	/** Create a variable property */
	public VarProperty(VarName n) {
		this(n, 0);
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
		byte[] vn = vname.name.getBytes(ASCII);
		byte[] body = new byte[4];
		assert 2 == vn.length;
		body[0] = (byte) MsgCode.VAR_NAME_QUERY.code;
		body[1] = 0;	/* config domain */
		body[2] = vn[0];
		body[3] = vn[1];
		encodeRequest(os, body);
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		value = parseValue(decodeResponse(is));
	}

	/** Parse a variable value response */
	private int parseValue(Response resp) throws IOException {
		if (resp.msg_code != MsgCode.VAR_RESP)
			throw new ParsingException("MSG CODE:" + resp.msg_code);
		if (5 != resp.body.length)
			throw new ParsingException("LEN:" + resp.body.length);
		byte dom = resp.body[0];
		if (0 != dom)
			throw new ParsingException("DOMAIN:" + dom);
		String vn = new String(resp.body, 1, 2, ASCII);
		if (!vn.equals(vname.name))
			throw new ParsingException("NAME:" + vn);
		return parse16le(resp.body, 3);
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return vname.name + ": " + value;
	}
}
