/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelco;

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import static us.mn.state.dot.tms.server.comm.pelco.PelcoPoller.PELCO_LOG;

/**
 * Pelco Property
 *
 * @author Douglas Lau
 * @author Timothy Johnson
 */
abstract public class PelcoProperty extends ControllerProperty {

	/** Acknowledge response */
	static private final String ACK = "AK";

	/** Negative Acknowledge response */
	static private final String NAK = "NA";

	/** End of Response byte */
	static private final int EOR = 'a';

	/** Maximum size (in bytes) of a response from switcher */
	static private final int MAX_RESPONSE = 80;

	/** Get a response from the switcher */
	protected String getResponse(InputStream is) throws IOException {
		StringBuilder resp = new StringBuilder();
		while (resp.length() <= MAX_RESPONSE) {
			int value = is.read();
			if (value < 0)
				throw new EOFException("END OF STREAM");
			if (value == EOR)
				break;
			resp.append((char)value);
		}
		return resp.toString();
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		String resp = getResponse(is);
		// FIXME: what to do if NAK is returned?
		PELCO_LOG.log("decodeStore: " + resp);
	}
}
