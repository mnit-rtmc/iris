/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.org815;

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import static us.mn.state.dot.tms.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * ORG-815 property
 *
 * @author Douglas Lau
 */
abstract public class Org815Property extends ControllerProperty {

	/** Maximum number of bytes in a response */
	static protected final int MAX_RESP = 256;

	/** Encode a QUERY request */
	public void encodeQuery(OutputStream os, int drop) throws IOException {
		os.write(requestQueryByte());
	}

	/** Get the QUERY request byte code */
	abstract protected byte requestQueryByte();

	/** Decode a QUERY response */
	public void decodeQuery(InputStream is, int drop) throws IOException {
		parseQuery(readLine(is));
	}

	/** Read a line of text from an input stream */
	protected String readLine(InputStream is) throws IOException {
		byte[] resp = new byte[MAX_RESP];
		int n_rcv = 0;
		while(n_rcv < MAX_RESP) {
			int r = is.read(resp, n_rcv, MAX_RESP - n_rcv);
			if(r <= 0)
				throw new EOFException("END OF STREAM");
			for(int i = 0; i < r; i++) {
				if(resp[n_rcv + i] == 13)
					return new String(resp, 0, n_rcv + i);
			}
			n_rcv += r;
		}
		throw new ParsingException("RANDOM NOISE");
	}

	/** Parse a QUERY response */
	abstract protected void parseQuery(String line) throws IOException;

	/** Parse the accumulated precipitation since last reset.
	 * @param a 7-character accumulation to parse.
	 * @return Accumulation since last reset in milimeters. */
	protected float parseAccumulation(String a) throws IOException {
		if("---.---".equals(a))
			return MISSING_DATA;
		try {
			return Float.parseFloat(a);
		}
		catch(NumberFormatException e) {
			throw new ParsingException("Invalid accum: " + a);
		}
	}
}
