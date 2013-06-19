/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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

/**
 * Reset property is used to reset the controller.
 *
 * @author Douglas Lau
 */
public class ResetProperty extends STCProperty {

	/** Encode a STORE request */
	@Override public void encodeStore(OutputStream os, int drop)
		throws IOException
	{
		byte[] data = new byte[1];
		data[0] = 'R';
		os.write(formatRequest(drop, data));
	}

	/** Decode a STORE response */
	@Override public void decodeStore(InputStream is, int drop)
		throws IOException
	{
		// No response to reset request
	}

	/** Parse a received message */
	@Override protected void parseMessage(byte[] msg, int len)
		throws IOException
	{
		// No response messages to parse
	}

	/** Get a string representation */
	public String toString() {
		return "Reset request";
	}
}
