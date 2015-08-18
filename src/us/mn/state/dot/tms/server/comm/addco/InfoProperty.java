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
package us.mn.state.dot.tms.server.comm.addco;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Addco Information Property.
 *
 * @author Douglas Lau
 */
public class InfoProperty extends AddcoProperty {

	/** Length of info request (bytes) */
	static private final int INFO_REQ_LEN = 7;

	/** Length of info response (bytes) */
	static private final int INFO_RESP_LEN = 12;

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		os.write(formatQuery(c.getDrop()));
	}

	/** Format a QUERY request */
	private byte[] formatQuery(int address) throws IOException {
		byte[] buf = new byte[INFO_REQ_LEN];
		format8(buf, 0, MsgCode.NORMAL.code);
		format16le(buf, 1, INFO_REQ_LEN + 2);	// + 2 FCS bytes
		format16le(buf, 3, address);
		buf[5] = 'G';
		buf[6] = 'I';
		return buf;
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		decodeHead(is, MsgCode.ACK_MORE);
		int len = decodeHead(is, MsgCode.NORMAL);
		if (len != INFO_RESP_LEN)
			throw new ParsingException("MSG LEN: " + len);
		parseQuery(decodeBody(is, c.getDrop(), INFO_RESP_LEN));
	}

	/** Parse a query response */
	private void parseQuery(byte[] body) throws IOException {
		checkCommand(body, "RI");
		// Don't know what body[4] contains
		d_volts = body[5] & 0xFF;
		// Don't know what body[6] contains
	}

	/** Deci-volts */
	private int d_volts;

	/** Get the volts */
	public float getVolts() {
		return d_volts / 10f;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("volts: ");
		sb.append(getVolts());
		return sb.toString();
	}
}
