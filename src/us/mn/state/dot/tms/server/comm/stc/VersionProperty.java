/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.stc;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * Version property reads the controller version.
 *
 * @author Douglas Lau
 */
public class VersionProperty extends STCProperty {

	/** Create a new version property */
	public VersionProperty(String pw) {
		super(pw);
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] data = new byte[1];
		data[0] = 'V';
		os.write(formatRequest(c.getDrop(), data));
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		parseFrame(is, c.getDrop());
	}

	/** Parse a received message */
	@Override
	protected void parseMessage(byte[] msg, int len)
		throws IOException
	{
		if (msg[0] != 'V')
			super.parseMessage(msg, len);
		version = new String(msg, 1, len - 1, ASCII).trim();
	}

	/** Version string */
	private String version = "";

	/** Get the version (for controller version property) */
	public String getVersion() {
		return version;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "Version: " + version;
	}
}
