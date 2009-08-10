/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.smartsensor;

import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * SmartSensor message
 *
 * @author Douglas Lau
 */
public class Message implements AddressedMessage {

	/** Multidrop SmartSensor protocol */
	static protected final boolean MULTIDROP = true;

	/** Serial output print stream */
	protected final PrintStream ps;

	/** Serial input stream */
	protected final InputStream is;

	/** SmartSensor drop address */
	protected final int drop;

	/** Request object */
	protected Request req;

	/** Create a new SmartSensor message */
	public Message(PrintStream p, InputStream i, ControllerImpl c) {
		ps = p;
		is = i;
		drop = c.getDrop();
	}

	/** Format a request header */
	protected String formatHeader() {
		StringBuffer b = new StringBuffer();
		if(MULTIDROP) {
			b.append("Z0");
			b.append(Integer.toString(drop));
			while(b.length() < 6)
				b.insert(2, '0');
		}
		return b.toString();
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
		req.doGetRequest(ps, is, formatHeader());
	}

	/** Perform a "set" request */
	public void setRequest() throws IOException {
		if(req == null)
			throw new ProtocolException("No request");
		req.doSetRequest(ps, is, formatHeader());
	}
}
