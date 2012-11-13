/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2012  Minnesota Department of Transportation
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
import java.io.InputStreamReader;
import java.io.PrintStream;
import us.mn.state.dot.tms.utils.LineReader;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * SS105 Property
 * FIXME: convert to use ControllerProperty encode/decode methods.
 *
 * @author Douglas Lau
 */
abstract public class SS105Property extends ControllerProperty {

	/** Multidrop SS105 protocol */
	static private final boolean MULTIDROP = true;

	/** Maximum number of bytes in a response */
	static protected final int MAX_RESP = 256;

	/** Check if the request has a checksum */
	abstract protected boolean hasChecksum();

	/** Format a basic "GET" request */
	abstract protected String formatGetRequest() throws IOException;

	/** Format a basic "SET" request */
	abstract protected String formatSetRequest() throws IOException;

	/** Set the response to the request */
	abstract protected void setResponse(String r) throws IOException;

	/** Convert an integer to a hexadecimal number padded to d digits */
	static protected String hex(int n, int d) {
		String b = Integer.toHexString(n).toUpperCase();
		if(n < 0) {
			while(b.length() < d)
				b = "F" + b;
		} else {
			while(b.length() < d)
				b = "0" + b;
		}
		return b;
	}

	/** Calculate the checksum of a buffer */
	static protected String checksum(String buf) {
		int sum = 0;
		for(int i = 0; i < buf.length(); i++)
			sum += buf.charAt(i);
		return hex(sum, 4);
	}

	/** Compare the response with its trailing checksum */
	static protected String compareChecksum(String r) throws IOException {
		if(r.length() < 4)
			throw new ParsingException("INCOMPLETE RESPONSE");
		String payload = r.substring(0, r.length() - 4);
		String hexsum = checksum(payload);
		if(r.endsWith(hexsum))
			return payload;
		else
			throw new ChecksumException();
	}

	/** Format a request header */
	static private String formatHeader(int drop) {
		StringBuilder sb = new StringBuilder();
		if(MULTIDROP) {
			sb.append("Z0");
			sb.append(Integer.toString(drop));
			while(sb.length() < 6)
				sb.insert(2, '0');
		}
		return sb.toString();
	}

	/** Poll the sensor */
	protected void doPoll(PrintStream ps, String h, String r)
		throws IOException
	{
		ps.print(h);
		ps.print(r);
		ps.print('\r');
		ps.flush();
	}

	/** Get response from the sensor */
	protected void doResponse(InputStream is, String h, String r)
		throws IOException
	{
		InputStreamReader isr = new InputStreamReader(is, "US-ASCII");
		LineReader lr = new LineReader(isr, MAX_RESP);
		String line = lr.readLine();
		if(line != null)
			parseResponse(line.trim(), h, r);
		else
			throw new EOFException("END OF STREAM");
	}

	/** Parse a response from the sensor */
	private void parseResponse(String response, String h, String r)
		throws IOException
	{
		if(response.startsWith(h))
			response = response.substring(h.length());
		else
			throw new ParsingException("INVALID RESPONSE HEADER");
		if(response.startsWith(r.substring(0, 2)))
			response = response.substring(2);
		else
			throw new ParsingException("INVALID RESPONSE");
		if(response.endsWith("~"))
			response = response.substring(0, response.length() - 1);
		else
			throw new ParsingException("INVALID RESPONSE TAIL");
		if(response.equals("Failure") ||
		   response.equals("Invalid") ||
		   response.equals("Empty"))
		{
			throw new ControllerException(response);
		}
		if(hasChecksum())
			response = compareChecksum(response);
		setResponse(response);
	}

	/** Perform a "GET" request */
	public void doGetRequest(PrintStream ps, InputStream is, int drop)
		throws IOException
	{
		String h = formatHeader(drop);
		String req = formatGetRequest();
		is.skip(is.available());
		doPoll(ps, h, req);
		doResponse(is, h, req);
	}

	/** Perform a "SET" request */
	public void doSetRequest(PrintStream ps, InputStream is, int drop)
		throws IOException
	{
		String h = formatHeader(drop);
		String req = formatSetRequest();
		is.skip(is.available());
		doPoll(ps, h, req);
		doResponse(is, h, req);
	}
}
