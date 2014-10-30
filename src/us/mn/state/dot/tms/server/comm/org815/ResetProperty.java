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
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Reset property
 *
 * @author Douglas Lau
 */
public class ResetProperty extends Org815Property {

	/** Get a string value of the property */
	@Override
	public String toString() {
		return "reset accumulator";
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		throw new ProtocolException("QUERY not supported");
	}

	/** Get the QUERY request byte code */
	@Override
	protected byte requestQueryByte() {
		// Not supported
		return 0;
	}

	/** Parse a QUERY response */
	@Override
	protected void parseQuery(String line) throws IOException {
		throw new ProtocolException("QUERY not supported");
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		os.write(requestStoreByte());
	}

	/** Get the STORE request byte code */
	private byte requestStoreByte() {
		return (byte)'R';
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		LineReader lr = new LineReader(is, MAX_RESP);
		String line = lr.readLine();
		if (line != null)
			parseStore(line);
		else
			throw new EOFException("END OF STREAM");
	}

	/** Parse a STORE response */
	private void parseStore(String line) throws IOException {
		if (line.length() != 7)
			throw new ParsingException("Invalid response: " + line);
		parseAccumulation(line);
	}
}
