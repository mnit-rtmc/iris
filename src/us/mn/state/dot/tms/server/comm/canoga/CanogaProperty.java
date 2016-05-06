/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2016  Minnesota Department of Transportation
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.InvalidAddressException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Canoga property.
 *
 * @author Douglas Lau
 */
abstract public class CanogaProperty extends ControllerProperty {

	/** Offset for message header */
	static private final int OFF_HEADER = 0;

	/** Offset for message length field */
	static private final int OFF_LENGTH = 1;

	/** Offset for message address field */
	static private final int OFF_ADDRESS  = 3;

	/** Offset for message type field */
	static private final int OFF_MTYPE = 4;

	/** Offset for message payload field */
	static private final int OFF_PAYLOAD = 5;

	/** Get the ASCII hex digit for the given nybble */
	static private byte hex_digit(byte b) {
		b &= 0x0F;
		if(b < 10)
			return (byte)('0' + b);
		else
			return (byte)('A' + b - 10);
	}

	/** Get the MSN ASCII hex digit */
	static private byte hex_msn(byte b) {
		return hex_digit((byte)(b >> 4));
	}

	/** Get the LSN ASCII hex digit */
	static private byte hex_lsn(byte b) {
		return hex_digit(b);
	}

	/** Parse one ASCII hex digit */
	static private byte parse_hex_digit(byte n) throws ParsingException {
		if(n >= (byte)'0' && n <= (byte)'9')
			return (byte)(n - '0');
		if(n >= (byte)'A' && n <= (byte)'F')
			return (byte)(10 + n - 'A');
		throw new ParsingException("INVALID HEX DIGIT: " + n);
	}

	/** Parse an ASCII hexadecimal field in a byte array */
	static private byte parse_hex(byte[] res, int offset)
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

	/** Maximum address allowed for backplane addressing */
	static private final int ADDRESS_MAX_BACKPLANE = 15;

	/** Minimum address allowed for EEPROM programmable */
	static private final int ADDRESS_MIN_EEPROM = 128;

	/** Wildcard address */
	static private final int ADDRESS_WILDCARD = 255;

	/** Check if a drop address is valid */
	static private boolean isAddressValid(int drop) {
		return (drop >= 0 && drop <= ADDRESS_MAX_BACKPLANE) ||
		       (drop >= ADDRESS_MIN_EEPROM && drop <= ADDRESS_WILDCARD);
	}

	/** Format a request message */
	static private byte[] formatRequest(int drop, byte[] payload)
		throws IOException
	{
		if (!isAddressValid(drop))
			throw new InvalidAddressException(drop);
		byte len = (byte)(payload.length + 7);
		byte[] req = new byte[len];
		req[OFF_HEADER] = '<';
		req[OFF_LENGTH] = hex_msn(len);
		req[OFF_LENGTH + 1] = hex_lsn(len);
		req[OFF_ADDRESS] = (byte)drop;
		System.arraycopy(payload, 0, req, OFF_MTYPE, payload.length);
		byte xsum = checksum(req);
		req[req.length - 3] = hex_msn(xsum);
		req[req.length - 2] = hex_lsn(xsum);
		req[req.length - 1] = '>';
		return req;
	}

	/** Validate the response with its trailing checksum */
	static private void validateChecksum(byte[] res)
		throws ParsingException
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

	/** Format a basic "GET" request */
	abstract protected byte[] formatPayloadGet() throws IOException;

	/** Format a basic "SET" request */
	abstract protected byte[] formatPayloadSet() throws IOException;

	/** Get the property name */
	abstract protected String getName();

	/** Set the requested value */
	abstract protected void setValue(byte[] v);

	/** Get the requested value */
	abstract protected String getValue();

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return getName() + ": " + getValue();
	}

	/** Most recent request */
	private byte[] request = new byte[0];

	/** Perform a property request */
	private void doRequest(OutputStream os, byte[] req) throws IOException {
		os.write(req);
		request = req;
	}

	/** Get the expected number of octets in response */
	abstract protected int expectedResponseOctets();

	/** Validate a response message */
	protected void validateResponse(byte[] req, byte[] res)
		throws ParsingException
	{
		if(res.length < 8)
			throw new ParsingException("MESSAGE TOO SHORT");
		if(res[OFF_HEADER] != '<')
			throw new ParsingException("INVALID HEADER");
		if(res.length != parse_hex(res, OFF_LENGTH))
			throw new ParsingException("INVALID LENGTH");
		if(res[res.length - 1] != '>')
			throw new ParsingException("INVALID TERMINATOR");
		if(res[OFF_ADDRESS] != req[OFF_ADDRESS])
			throw new ParsingException("DROP ADDRESS MISMATCH");
		if(res[OFF_MTYPE] != req[OFF_MTYPE])
			throw new ParsingException("MESSAGE TYPE MISMATCH");
		validateChecksum(res);
	}

	/** Get response from the detector card */
	private byte[] doResponse(InputStream is, byte[] req)
		throws IOException
	{
		byte[] res = recvResponse(is, expectedResponseOctets());
		validateResponse(req, res);
		return res;
	}

	/** Get the message payload */
	protected String getPayload(byte[] res) {
		assert res.length >= 8;
		return new String(res, OFF_PAYLOAD, res.length - 8);
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		doRequest(os, formatRequest(c.getDrop(), formatPayloadGet()));
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		setValue(doResponse(is, request));
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		doRequest(os, formatRequest(c.getDrop(), formatPayloadSet()));
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		doResponse(is, request);
	}
}
