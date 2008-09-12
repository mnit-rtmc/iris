/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.smartsensor;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import us.mn.state.dot.tms.comm.ChecksumException;
import us.mn.state.dot.tms.comm.ParsingException;

/**
 * SmartSensor Request
 *
 * @author Douglas Lau
 */
abstract public class Request {

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

	/** Poll the sensor */
	protected void doPoll(PrintStream ps, String h, String r)
		throws IOException
	{
		ps.print(h);
		ps.print(r);
		ps.print('\r');
		ps.flush();
	}

	/** Read a line of text from an input stream */
	protected String readLine(InputStream is) throws IOException {
		StringBuffer buf = new StringBuffer();
		// FIXME: this is bad; only loop for X iterations
		while(true) {
			int b = is.read();
			if(b < 0)
				throw new EOFException("END OF STREAM");
			if(b == 13)
				return buf.toString();
			else
				buf.append((char)b);
		}
	}

	/** Get response from the sensor */
	protected void doResponse(InputStream is, String h, String r)
		throws IOException
	{
		String response = readLine(is).trim();
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
			throw new SmartSensorError(response);
		}
		if(hasChecksum())
			response = compareChecksum(response);
		setResponse(response);
	}

	/** Perform a "GET" request */
	public void doGetRequest(PrintStream ps, InputStream is, String h)
		throws IOException
	{
		String req = formatGetRequest();
		is.skip(is.available());
		doPoll(ps, h, req);
		doResponse(is, h, req);
	}

	/** Perform a "SET" request */
	public void doSetRequest(PrintStream ps, InputStream is, String h)
		throws IOException
	{
		String req = formatSetRequest();
		is.skip(is.available());
		doPoll(ps, h, req);
		doResponse(is, h, req);
	}
}
