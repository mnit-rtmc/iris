/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Mndot Property
 * FIXME: convert to use ControllerProperty encode/decode methods.
 *
 * @author Douglas Lau
 */
abstract public class MndotProperty extends ControllerProperty {

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

	/** Create a request packet.
	 * @param c Controller.
	 * @param cat Category code.
	 * @param n_bytes Number of additional bytes.
	 * @return Request packet. */
	static protected byte[] createRequest(ControllerImpl c, int cat,
		int n_bytes)
	{
		byte[] pkt = new byte[3 + n_bytes];
		pkt[OFF_DROP_CAT] = dropCat(c, cat);
		pkt[OFF_LENGTH] = (byte)n_bytes;
		return pkt;
	}

	/** Make the initical drop/category byte */
	static protected byte dropCat(ControllerImpl c, int cat) {
		int drop = c.getDrop();
		CommProtocol cp = c.getProtocol();
		if (cp == CommProtocol.MNDOT_5)
			return (byte)(drop << 3 | cat);
		else
			return (byte)(drop << 4 | cat);
	}

	/** Calculate the checksum for a request packet */
	static protected void calculateChecksum(byte[] pkt) {
		pkt[pkt.length - 1] = checksum(pkt);
	}

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
		output.write(req);
		output.flush();
	}

	/** Get a response from an input stream */
	protected byte[] getResponse(InputStream is, int expected)
		throws IOException
	{
		byte[] buf = new byte[expected];
		int b = is.read(buf);
		if(b < 0)
			throw new EOFException("END OF STREAM");
		if(b != buf.length) {
			byte[] res = new byte[b];
			System.arraycopy(buf, 0, res, 0, b);
			return res;
		}
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

	/** Get response from the controller.
	 * @param m Message.
	 * @param c Controller.
	 * @param expected Expected number of bytes in response.
	 * @return Response packet. */
	private byte[] doResponse(Message m, ControllerImpl c, int expected)
		throws IOException
	{
		if (expected > 0) {
			byte[] res = getResponse(m.input, expected);
			validateChecksum(res);
			m.validateResponse(c, res);
			return res;
		} else
			return new byte[0];
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

	/** Get the expected number of octets in response to a GET request */
	abstract protected int expectedGetOctets();

	/** Set the value of the GET request */
	protected void parseGetResponse(byte[] buf) throws IOException {
		// override this if necessary
	}

	/** Perform a "GET" request */
	public void doGetRequest(Message m) throws IOException {
		ControllerImpl c = m.getController();
		encodeQuery(c, m.output);
		m.output.flush();
		byte[] res = doResponse(m, c, expectedGetOctets());
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
