/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss105;

import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * SS105 message
 *
 * @author Douglas Lau
 */
public class Message implements CommMessage {

	/** Multidrop SS105 protocol */
	static protected final boolean MULTIDROP = true;

	/** Serial output print stream */
	protected final PrintStream ps;

	/** Serial input stream */
	protected final InputStream is;

	/** SS105 drop address */
	protected final int drop;

	/** Controller property */
	protected SS105Property prop;

	/** Create a new SS105 message */
	public Message(PrintStream p, InputStream i, ControllerImpl c) {
		ps = p;
		is = i;
		drop = c.getDrop();
	}

	/** Format a request header */
	protected String formatHeader() {
		StringBuilder sb = new StringBuilder();
		if(MULTIDROP) {
			sb.append("Z0");
			sb.append(Integer.toString(drop));
			while(sb.length() < 6)
				sb.insert(2, '0');
		}
		return sb.toString();
	}

	/** Add a controller property */
	public void add(ControllerProperty cp) {
		if(cp instanceof SS105Property)
			prop = (SS105Property)cp;
	}

	/** Query the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void queryProps() throws IOException {
		if(prop != null)
			prop.doGetRequest(ps, is, formatHeader());
		else
			throw new ProtocolException("No property");
	}

	/** Store the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void storeProps() throws IOException {
		if(prop != null)
			prop.doSetRequest(ps, is, formatHeader());
		else
			throw new ProtocolException("No property");
	}
}
