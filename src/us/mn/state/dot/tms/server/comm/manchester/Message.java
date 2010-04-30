/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.manchester;

import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Manchester message
 *
 * @author Douglas Lau
 */
public class Message implements CommMessage {

	/** Serial output stream */
	protected final OutputStream os;

	/** Camera receiver drop address */
	protected final int drop;

	/** Create a new Manchester message */
	public Message(OutputStream o, int d) {
		os = o;
		drop = d;
	}

	/** Controller property */
	protected ManchesterRequest request = null;

	/** Add a controller property */
	public void add(ControllerProperty cp) {
		if(cp instanceof ManchesterRequest)
			request = (ManchesterRequest)cp;
	}

	/** Perform a "get" request */
	public void getRequest() throws IOException {
		throw new ProtocolException("GET request not supported");
	}

	/** Perform a "set" request */
	public void setRequest() throws IOException {
		if(request != null) {
			os.write(request.format(drop));
			os.flush();
		}
	}
}
