/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2014  Minnesota Department of Transportation
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
 * Reset property is used to reset the controller.
 *
 * @author Douglas Lau
 */
public class ResetProperty extends STCProperty {

	/** Create a new reset property */
	public ResetProperty(String pw) {
		super(pw);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] data = new byte[1];
		data[0] = 'R';
		os.write(formatRequest(c.getDrop(), data));
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is) {
		// No response to reset request
	}

	/** Parse a received message */
	@Override
	protected void parseMessage(byte[] msg, int len) {
		// No response messages to parse
	}

	/** Get a string representation */
	public String toString() {
		return "Reset request";
	}
}
