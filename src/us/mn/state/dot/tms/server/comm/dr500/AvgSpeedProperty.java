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
 * Average speed property.
 *
 * @author Douglas Lau
 */
public class AvgSpeedProperty extends DR500Property {

	/** Body of query request */
	static private final byte[] BODY = new byte[] {
		(byte) MsgCode.AVG_SPEED_QUERY.code
	};

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		encodeRequest(os, BODY);
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		speed = parseSpeed(decodeResponse(is));
	}

	/** Parse a speed value response */
	private Integer parseSpeed(Response resp) throws IOException {
		checkMsgCode(resp, MsgCode.AVG_SPEED_RESP);
		if (1 != resp.body.length)
			throw new ParsingException("LEN:" + resp.body.length);
		int sp = resp.body[0];
		return (sp > 0) ? sp : null;
	}

	/** Average speed */
	private Integer speed;

	/** Get the average speed */
	public Integer getSpeed() {
		return speed;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "avg speed:" + speed;
	}
}
