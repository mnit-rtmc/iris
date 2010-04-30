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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Canoga message
 *
 * @author Douglas Lau
 */
public class Message implements CommMessage {

	/** Serial output stream */
	protected final OutputStream os;

	/** Serial input stream */
	protected final InputStream is;

	/** Canoga drop address */
	protected final int drop;

	/** Canoga property */
	protected CanogaProperty prop;

	/** Create a new Canoga message */
	public Message(OutputStream o, InputStream i, int d) {
		os = o;
		is = i;
		drop = d;
	}

	/** Add a controller property */
	public void add(ControllerProperty cp) {
		if(cp instanceof CanogaProperty)
			prop = (CanogaProperty)cp;
	}

	/** Perform a "get" request */
	public void getRequest() throws IOException {
		if(prop == null)
			throw new ProtocolException("No property");
		prop.doGetRequest(os, is, drop);
	}

	/** Perform a "set" request */
	public void setRequest() throws IOException {
		if(prop == null)
			throw new ProtocolException("No property");
		prop.doSetRequest(os, is, drop);
	}
}
