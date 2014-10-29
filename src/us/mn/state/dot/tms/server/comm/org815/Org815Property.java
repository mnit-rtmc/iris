/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.utils.LineReader;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.ControllerImpl;
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
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		os.write(requestQueryByte());
	}

	/** Get the QUERY request byte code */
	abstract protected byte requestQueryByte();

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		LineReader lr = new LineReader(is, MAX_RESP);
		String line = lr.readLine();
		if(line != null)
			parseQuery(line);
		else
			throw new EOFException("END OF STREAM");
	}

	/** Parse a QUERY response */
	abstract protected void parseQuery(String line) throws IOException;

	/** Parse the accumulated precipitation since last reset.
	 * @param a 7-character accumulation to parse.
	 * @return Accumulation since last reset in millimeters. */
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
