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
package us.mn.state.dot.tms.server.comm.pelco;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Pelco message
 *
 * @author Douglas Lau
 * @author Timothy Johnson
 */
public class Message implements CommMessage {

	/** Acknowledge response */
	static protected final String ACK = "AK";

	/** Negative Acknowledge response */
	static protected final String NO_ACK = "NA";

	/** End of Response byte */
	static protected final int EOR = 'a';

	/** Maximum size (in bytes) of a response from switcher */
	static protected final int MAX_RESPONSE = 80;

	/** Serial output stream */
	protected final OutputStream os;

	/** Serial input stream */
	protected final InputStream is;

	/** Chained request buffer */
	protected final LinkedList<PelcoRequest> requests =
		new LinkedList<PelcoRequest>();

	/** Create a new Pelco message */
	public Message(OutputStream o, InputStream i) {
		os = o;
		is = i;
	}

	/** Get a string of the message */
	public String toString() {
		StringBuilder b = new StringBuilder();
		for(PelcoRequest req: requests)
			b.append(req.toString());
		return b.toString();
	}

	/** Add a request object to this message */
	public void add(Object r) {
		if(r instanceof PelcoRequest)
			requests.add((PelcoRequest)r);
	}

	/** Perform a "get" request */
	public void getRequest() throws IOException {
		throw new ProtocolException("GET request not supported");
	}

	/** Perform a "set" request */
	public void setRequest() throws IOException {
		String req = toString();
		is.skip(is.available());
		os.write(req.getBytes());
		os.flush();
		getResponse();
	}

	/** Get a response from the switcher */
	protected String getResponse() throws IOException {
		StringBuilder resp = new StringBuilder();
		while(resp.length() <= MAX_RESPONSE) {
			int value = is.read();
			if(value < 0)
				throw new EOFException("END OF STREAM");
			resp.append((char)value);
			if(value == EOR)
				break;
		}
		return resp.toString();
	}
}
