/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.utils.LineReader;

/**
 * Simplified ASCII command/response property.
 * (A property where the command to the device
 *  -and- the response from that device are
 *  both ASCII text.)
 *
 * In it's most basic form, this allows creating
 * an ASCII-based IRIS property by defining four
 * things:
 *   1) command string to send (strSnd)
 *   2) method to parse the response (parseResponse)
 *   3) variable(s) to hold info from response
 *   4) toString method.
 *
 * This class bypasses the question of "Is a
 * protocol-command (property) a query-operation
 * or a store-operation?" and treats both the same.
 *
 * @author John Stanley - SRF Consulting
 * @author Douglas Lau
 */
abstract public class AsciiDeviceProperty extends ControllerProperty {

	/** Charset name for ASCII */
	static private final String ASCII = "US-ASCII";

	/** Maximum number of characters for line reader */
	static protected final int MAX_CHARS = 200;

	/** Text command to be sent */
	private final String strSnd;

	/** Set to true in readResponse when
	 * parseResponse(..) returns true. */
	protected boolean bGotValidResponse = false;

	/** Create a new ASCII device property */
	protected AsciiDeviceProperty(String cmd) {
		strSnd = cmd;
	}

	/** Did we get a valid response? */
	public boolean gotValidResponse() {
		return bGotValidResponse;
	}

	/** Convert command to ASCII byte array and send it. */
	protected void sendCommand(OutputStream os)
		throws IOException
	{
		os.write(strSnd.getBytes(ASCII));
	}

	/** Create a new line reader.
	 * @param is InputStream to read from. */
	protected LineReader newLineReader(InputStream is) throws IOException {
		return new LineReader(is, MAX_CHARS);
	}

	/** Read line(s) from controller until parseResponse(...)
	 *  returns true or we run out of responses to read...
	 * @param in InputStream from device
	 * @throws IOException if a response line is longer than MAX_CHARS */
	protected void readResponse(InputStream in) throws IOException {
		LineReader lr = newLineReader(in);
		String resp = lr.readLine();
		bGotValidResponse = false;
		while (resp != null) {
			if (parseResponse(resp)) {
				bGotValidResponse = true;
				return;
			}
			resp = lr.readLine();
		}
	}

	/** Parse a response (line of ASCII text) from controller
	 * @param resp response from the controller
	 * @return true = response was successfully parsed.
	 *         false = we want to see more response strings
	 * @throws IOException there's a fatal error, stop looking
	 *                     for a valid response. */
	abstract protected boolean parseResponse(String resp) throws IOException;

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		sendCommand(os);
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		readResponse(is);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		sendCommand(os);
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		readResponse(is);
	}
}
