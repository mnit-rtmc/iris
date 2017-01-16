/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
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
 */
abstract public class AsciiDeviceProperty
		extends ControllerProperty {

	/** Charset name for ASCII */
	static protected final String ASCII = "US-ASCII";
	
	/** text command to be sent */
	protected String strSnd;

	/** max allowed response line-length */
	protected int max_chars = 80;

	/** Set to true in readResponse when 
	 * parseResponse(..) returns true. */
	protected boolean bGotValidResponse = false;
	
	protected ControllerImpl controller;

	//----------------------------------------------

	public AsciiDeviceProperty(String cmd) {
		strSnd = cmd;
	}

	public AsciiDeviceProperty(String cmd, int max) {
		strSnd = cmd;
		max_chars = max;
	}

	//----------------------------------------------

	/** Did we get a valid response? */
	public boolean gotValidResponse() {
		return bGotValidResponse;
	}
	
	//----------------------------------------------

	/** Convert command to ASCII byte array and send it. */
	protected void sendCommand(OutputStream os)
		throws IOException
	{
		os.write(strSnd.getBytes(ASCII));
	}

	//----------------------------------------------

	/** Create a new line reader.
	 * 
	 *  This method is solely so we can substitute a different
	 *  LineReader to deal with the odd "<CR><LF>" (literally,
	 *  those EIGHT characters) end-of-line marker that the
	 *  NDOR v5 gate-controller sends.  (See GateNdorV5Property
	 *  for an example that overrides this method.)
	 *  
	 * @param is InputStream to read from.
	 * @param max_chars Maximum number of characters on a line. */
	protected LineReader newLineReader(InputStream is, int max_chars)
			throws IOException {
		return new LineReader(is, max_chars);
	}

	/** Read line(s) from controller until parseResponse(...)
	 *  returns true or we run out of responses to read...
	 * @param in InputStream from device
	 * @throws IOException if a response line is longer than
	 *  max_chars.
	 */
	protected void readResponse(InputStream in)
		throws IOException
	{
		LineReader lr = newLineReader(in, max_chars);
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

	//----------------------------------------------

	/** Parse a response (line of ASCII text) from controller
	 * @param resp response from the controller
	 * @return true = response was successfully parsed.
	 *   false = we want to see more response strings
	 * @throws IOException there's a fatal error, stop looking
	 *  for a valid response. 
	 */
	protected abstract boolean parseResponse(String resp)
			throws IOException;
	/* NOTE: Override this method in classes derived
	 * from AsciiDeviceProperty to extract info from
	 * the response(s). 
	 */

	//----------------------------------------------

	/** Get a string representation of this property */
	public abstract String toString();
	/* NOTE: Override this method in classes derived
	 * from AsciiDeviceProperty to format info to add
	 * to the logfile.
	 */

	//----------------------------------------------
	// override various ControllerProperty methods
	//----------------------------------------------
	
	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		controller = c;
		sendCommand(os);
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		controller = c;
		readResponse(is);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		controller = c;
		sendCommand(os);
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		controller = c;
		readResponse(is);
	}
}
