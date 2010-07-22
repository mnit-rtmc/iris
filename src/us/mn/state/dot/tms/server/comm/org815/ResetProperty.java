/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Reset property
 *
 * @author Douglas Lau
 */
public class ResetProperty extends Org815Property {

	/** Get a string value of the property */
	public String toString() {
		return "reset accumulator";
	}

	/** Encode a QUERY request */
	public void encodeQuery(OutputStream os, int drop) throws IOException {
		throw new ProtocolException("QUERY not supported");
	}

	/** Get the QUERY request byte code */
	protected byte requestQueryByte() {
		// Not supported
		return 0;
	}

	/** Parse a QUERY response */
	protected void parseQuery(String line) throws IOException {
		throw new ProtocolException("QUERY not supported");
	}

	/** Encode a STORE request */
	public void encodeStore(OutputStream os, int drop) throws IOException {
		os.write(requestStoreByte());
	}

	/** Get the STORE request byte code */
	protected byte requestStoreByte() {
		return (byte)'R';
	}

	/** Decode a STORE response */
	public void decodeStore(InputStream is, int drop) throws IOException {
		parseStore(readLine(is));
	}

	/** Parse a STORE response */
	protected void parseStore(String line) throws IOException {
		if(line.length() != 7)
			throw new ParsingException("Invalid response: " + line);
		parseAccumulation(line);
	}
}
