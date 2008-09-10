/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.mndot;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.comm.ChecksumException;
import us.mn.state.dot.tms.comm.ParsingException;
import us.mn.state.dot.tms.comm.PortException;

/**
 * Mndot Request
 *
 * @author Douglas Lau
 */
abstract public class Request {

	/** Exception thrown for unexpected response length */
	static protected final ParsingException RESPONSE_LENGTH =
		new ParsingException("UNEXPECTED RESPONSE LENGTH");

	/** "Shut up" command category code */
	static protected final int SHUT_UP = 0;

	/** Remote level-1 restart command category code */
	static protected final int LEVEL_1_RESTART = 1;

	/** Synchronize clock command category code */
	static protected final int SYNCHRONIZE_CLOCK = 2;

	/** Query record count command category code */
	static protected final int QUERY_RECORD_COUNT = 3;

	/** Send next record command category code */
	static protected final int SEND_NEXT_RECORD = 4;

	/** Delete oldest record command category code */
	static protected final int DELETE_OLDEST_RECORD = 5;

	/** Write memory command category code */
	static protected final int WRITE_MEMORY = 6;

	/** Read memory command category code */
	static protected final int READ_MEMORY = 7;

	/** Offset for DROP/CAT or DROP/STAT field */
	static protected final int OFF_DROP_CAT = 0;

	/** Offset for message length field */
	static protected final int OFF_LENGTH = 1;

	/** Offset for message payload field */
	static protected final int OFF_PAYLOAD = 2;

	/** Calculate the checksum of a buffer */
	static protected byte checksum(byte[] buf) {
		byte xsum = 0;
		for(int i = 0; i < buf.length - 1; i++)
			xsum ^= buf[i];
		return xsum;
	}

	/** Poll the 170 controller */
	protected void doPoll(OutputStream output, byte[] req)
		throws IOException
	{
		try {
			output.write(req);
			output.flush();
		}
		catch(IOException e) {
			// FIXME: hack to force the port to be reopened
			throw new PortException(e.getMessage());
		}
	}

	/** Get a response from an input stream */
	protected byte[] getResponse(InputStream is, int expected)
		throws IOException
	{
		byte[] buf = new byte[expected];
		int b = is.read(buf);
		if(b < 0)
			throw new EOFException();
		if(b != buf.length)
			throw RESPONSE_LENGTH;
		return buf;
	}

	/** Compare the response with its trailing checksum */
	protected void validateChecksum(byte[] res) throws ChecksumException,
		ParsingException
	{
		byte paysum = res[res.length - 1];
		byte xsum = checksum(res);
		if(paysum != xsum)
			throw new ChecksumException(res);
	}

	/** Get response from the controller */
	protected byte[] doResponse(Message m, byte[] req, int expected)
		throws IOException
	{
		if(expected > 0) {
			byte[] res = getResponse(m.input, expected);
			validateChecksum(res);
			m.validateResponse(req, res);
			return res;
		} else
			return new byte[0];
	}

	/** Format a basic "GET" request */
	abstract protected byte[] formatPayloadGet(Message m)
		throws IOException;

	/** Get the expected number of octets in response to a GET request */
	abstract protected int expectedGetOctets();

	/** Set the value of the GET request */
	protected void parseGetResponse(byte[] buf) {
		// override this if necessary
	}

	/** Perform a "GET" request */
	public void doGetRequest(Message m) throws IOException {
		byte[] req = formatPayloadGet(m);
		m.input.skip(m.input.available());
		doPoll(m.output, req);
		byte[] res = doResponse(m, req, expectedGetOctets());
		parseGetResponse(res);
	}

	/** Format a basic "SET" request */
	abstract protected byte[] formatPayloadSet(Message m)
		throws IOException;

	/** Get the expected number of octets in response to a SET request */
	abstract protected int expectedSetOctets();

	/** Parse the response to a SET request */
	protected void parseSetResponse(byte[] buf) {
		// override this if necessary
	}

	/** Perform a "SET" request */
	public void doSetRequest(Message m) throws IOException {
		byte[] req = formatPayloadSet(m);
		m.input.skip(m.input.available());
		doPoll(m.output, req);
		byte[] res = doResponse(m, req, expectedSetOctets());
		parseSetResponse(res);
	}
}
