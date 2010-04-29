/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.canoga;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Canoga Request
 *
 * @author Douglas Lau
 */
abstract public class CanogaRequest {

	/** Maximum number of tries when reading a message response */
	static protected final int MAX_TRIES = 5;

	/** Format a basic "GET" request */
	abstract protected byte[] formatPayloadGet() throws IOException;

	/** Format a basic "SET" request */
	abstract protected byte[] formatPayloadSet() throws IOException;

	/** Set the requested value */
	abstract protected void setValue(byte[] v);

	/** Get the requested value */
	abstract protected String getValue();

	/** Get the ASCII hex digit for the given nybble */
	static protected byte hex_digit(byte b) {
		b &= 0x0F;
		if(b < 10)
			return (byte)('0' + b);
		else
			return (byte)('A' + b - 10);
	}

	/** Get the MSN ASCII hex digit */
	static protected byte hex_msn(byte b) {
		return hex_digit((byte)(b >> 4));
	}

	/** Get the LSN ASCII hex digit */
	static protected byte hex_lsn(byte b) {
		return hex_digit(b);
	}

	/** Parse one ASCII hex digit */
	static protected byte parse_hex_digit(byte n) throws ParsingException {
		if(n >= (byte)'0' && n <= (byte)'9')
			return (byte)(n - '0');
		if(n >= (byte)'A' && n <= (byte)'F')
			return (byte)(10 + n - 'A');
		throw new ParsingException("INVALID HEX DIGIT: " + n);
	}

	/** Parse an ASCII hexadecimal field in a byte array */
	static protected byte parse_hex(byte[] res, int offset)
		throws ParsingException
	{
		byte msn = parse_hex_digit(res[offset]);
		byte lsn = parse_hex_digit(res[offset + 1]);
		return (byte)(msn << 4 | lsn);
	}

	/** Calculate the checksum of a buffer */
	static protected byte checksum(byte[] buf) {
		byte xsum = 0;
		for(int i = 0; i < buf.length; i++)
			xsum ^= buf[i];
		return xsum;
	}

	/** Poll the Canoga card */
	protected void doPoll(OutputStream os, byte[] req) throws IOException {
		os.write(req);
		os.flush();
	}

	/** Get a response from an input stream */
	protected byte[] getResponse(InputStream is) throws IOException {
		byte[] buf = new byte[expectedResponseOctets()];
		for(int i = 0, tries = 0; i < buf.length; tries++) {
			if(tries > MAX_TRIES)
				throw new ParsingException("TOO MANY TRIES");
			int b = is.read(buf, i, buf.length - i);
			if(b < 0)
				throw new EOFException("END OF STREAM");
			i += b;
		}
		return buf;
	}

	/** Offset for message header */
	static protected final int OFF_HEADER = 0;

	/** Offset for message length field */
	static protected final int OFF_LENGTH = 1;

	/** Offset for message address field */
	static protected final int OFF_ADDRESS  = 3;

	/** Offset for message type field */
	static protected final int OFF_MTYPE = 4;

	/** Offset for message payload field */
	static protected final int OFF_PAYLOAD = 5;

	/** Validate a response message */
	protected void validateResponse(byte[] req, byte[] res)
		throws ParsingException
	{
		if(res[OFF_HEADER] != '<')
			throw new ParsingException("INVALID HEADER");
		if(res[res.length - 1] != '>')
			throw new ParsingException("INVALID TERMINATOR");
		if(res.length < 8 || res.length != parse_hex(res, OFF_LENGTH))
			throw new ParsingException("INVALID LENGTH");
		if(res[OFF_ADDRESS] != req[OFF_ADDRESS])
			throw new ParsingException("DROP ADDRESS MISMATCH");
		if(res[OFF_MTYPE] != req[OFF_MTYPE])
			throw new ParsingException("MESSAGE TYPE MISMATCH");
		validateChecksum(res);
	}

	/** Compare the response with its trailing checksum */
	protected void validateChecksum(byte[] res) throws ChecksumException,
		ParsingException
	{
		int rl = res.length;
		byte paysum = parse_hex(res, rl - 3);
		// Clear received checksum for comparison
		res[rl - 1] = 0;
		res[rl - 2] = 0;
		res[rl - 3] = 0;
		byte hexsum = checksum(res);
		if(paysum != hexsum)
			throw new ChecksumException(""+ paysum +" != "+ hexsum);
	}

	/** Get the message payload */
	protected String getPayload(byte[] res) {
		return new String(res, OFF_PAYLOAD, res.length - 8);
	}

	/** Get response from the detector card */
	protected byte[] doResponse(InputStream is, byte[] req)
		throws IOException
	{
		byte[] res = getResponse(is);
		validateResponse(req, res);
		return res;
	}

	/** Format a request message */
	protected byte[] format(byte drop, byte[] payload) {
		byte len = (byte)(payload.length + 7);
		byte[] req = new byte[len];
		req[OFF_HEADER] = '<';
		req[OFF_LENGTH] = hex_msn(len);
		req[OFF_LENGTH + 1] = hex_lsn(len);
		req[OFF_ADDRESS] = drop;
		System.arraycopy(payload, 0, req, OFF_MTYPE, payload.length);
		byte xsum = checksum(req);
		req[req.length - 3] = hex_msn(xsum);
		req[req.length - 2] = hex_lsn(xsum);
		req[req.length - 1] = '>';
		return req;
	}

	/** Get the expected number of octets in response */
	abstract protected int expectedResponseOctets();

	/** Perform a "GET" request */
	public void doGetRequest(OutputStream os, InputStream is, int drop)
		throws IOException
	{
		byte[] req = format((byte)drop, formatPayloadGet());
		is.skip(is.available());
		doPoll(os, req);
		byte[] res = doResponse(is, req);
		setValue(res);
	}

	/** Perform a "SET" request */
	public void doSetRequest(OutputStream os, InputStream is, int drop)
		throws IOException
	{
		byte[] req = format((byte)drop, formatPayloadSet());
		is.skip(is.available());
		doPoll(os, req);
		doResponse(is, req);
	}
}
