/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss105;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.utils.LineReader;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.InvalidAddressException;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * SS105 Property
 *
 * @author Douglas Lau
 */
abstract public class SS105Property extends ControllerProperty {

	/** Charset name for ASCII */
	static private final String ASCII = "US-ASCII";

	/** Multidrop SS105 protocol */
	static private final boolean MULTIDROP = true;

	/** Maximum number of bytes in a response */
	static private final int MAX_RESP = 256;

	/** Calculate the checksum of a buffer */
	static protected String checksum(String buf) {
		int sum = 0;
		for(int i = 0; i < buf.length(); i++)
			sum += buf.charAt(i);
		return HexString.format(sum, 4);
	}

	/** Compare the response with its trailing checksum */
	static private String compareChecksum(String r) throws IOException {
		if(r.length() < 4)
			throw new ParsingException("INCOMPLETE RESPONSE");
		String payload = r.substring(0, r.length() - 4);
		String hexsum = checksum(payload);
		if(r.endsWith(hexsum))
			return payload;
		else
			throw new ChecksumException();
	}

	/** Check if a drop address is valid */
	static public boolean isAddressValid(int drop) {
		return drop >= 0 && drop <= 9999;
	}

	/** Format a request header */
	static private String formatHeader(int drop) throws IOException {
		StringBuilder sb = new StringBuilder();
		if (MULTIDROP) {
			if (!isAddressValid(drop))
				throw new InvalidAddressException(drop);
			sb.append("Z0");
			sb.append(Integer.toString(drop));
			while(sb.length() < 6)
				sb.insert(2, '0');
		}
		return sb.toString();
	}

	/** Check if the request has a checksum */
	abstract protected boolean hasChecksum();

	/** Get response from the sensor */
	private String getResponse(InputStream is) throws IOException {
		LineReader lr = new LineReader(is, MAX_RESP);
		String line = lr.readLine();
		if(line != null)
			return line.trim();
		else
			throw new EOFException("END OF STREAM");
	}

	/** Parse a response from the sensor */
	private String parseResponse(String res, String h, String req)
		throws IOException
	{
		if(res.startsWith(h))
			res = res.substring(h.length());
		else
			throw new ParsingException("INVALID RESPONSE HEADER");
		if(res.startsWith(req.substring(0, 2)))
			res = res.substring(2);
		else
			throw new ParsingException("INVALID RESPONSE");
		if(res.endsWith("~"))
			res = res.substring(0, res.length() - 1);
		else
			throw new ParsingException("INVALID RESPONSE TAIL");
		if(res.equals("Failure") ||
		   res.equals("Invalid") ||
		   res.equals("Empty"))
		{
			throw new ControllerException(res);
		}
		if(hasChecksum())
			return compareChecksum(res);
		else
			return res;
	}

	/** Format a basic "GET" request */
	protected String formatGetRequest() throws IOException {
		throw new ProtocolException("GET request not supported");
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		String h = formatHeader(c.getDrop());
		String req = formatGetRequest();
		os.write(new String(h + req + '\r').getBytes(ASCII));
	}

	/** Parse the response to a QUERY */
	protected void parseQuery(String res) throws IOException {
		// Sub-classes can override
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		String line = getResponse(is);
		String h = formatHeader(c.getDrop());
		String req = formatGetRequest();
		parseQuery(parseResponse(line, h, req));
	}

	/** Format a basic "SET" request */
	protected String formatSetRequest() throws IOException {
		throw new ProtocolException("SET request not supported");
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		String h = formatHeader(c.getDrop());
		String req = formatSetRequest();
		os.write(new String(h + req + '\r').getBytes(ASCII));
	}

	/** Parse the response to a STORE */
	protected void parseStore(String res) throws IOException {
		// Sub-classes can override
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		String line = getResponse(is);
		String h = formatHeader(c.getDrop());
		String req = formatSetRequest();
		parseStore(parseResponse(line, h, req));
	}
}
