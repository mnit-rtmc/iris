/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2009  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Canoga message
 *
 * @author Douglas Lau
 */
public class Message implements AddressedMessage {

	/** Serial output stream */
	protected final OutputStream os;

	/** Serial input stream */
	protected final InputStream is;

	/** Canoga drop address */
	protected final int drop;

	/** Request object */
	protected Request req;

	/** Create a new Canoga message */
	public Message(OutputStream o, InputStream i, int d) {
		os = o;
		is = i;
		drop = d;
	}

	/** Add a request object to this message */
	public void add(Object mo) {
		if(mo instanceof Request)
			req = (Request)mo;
		else
			req = null;
	}

	/** Perform a "get" request */
	public void getRequest() throws IOException {
		if(req == null)
			throw new ProtocolException("No request");
		req.doGetRequest(os, is, drop);
	}

	/** Perform a "set" request */
	public void setRequest() throws IOException {
		if(req == null)
			throw new ProtocolException("No request");
		req.doSetRequest(os, is, drop);
	}
}
