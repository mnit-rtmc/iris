/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
 * Copyright (C) 2008-2010  AHMCT, University of California
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

package us.mn.state.dot.tms.server.comm.cohuptz;

import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Cohu PTZ message
 *
 * @author Travis Swanston
 */
public class Message implements CommMessage {

	/** output stream */
	protected final OutputStream os;

	/** drop address */
	protected final int drop;

	/** Create a new message */
	public Message(OutputStream o, int d) {
		os = o;
		drop = d;
	}

	/** Property for the message */
	protected CohuPTZProperty prop = null;

	/** Add a controller property */
	@Override
	public void add(ControllerProperty cp) {
		if(cp instanceof CohuPTZProperty)
			prop = (CohuPTZProperty)cp;
	}

	/** Query the controller properties. */
	@Override
	public void queryProps() throws IOException {
		throw new ProtocolException("QUERY not supported");
	}

	/**
	 * Store the controller properties.
	 *
	 * @throws IOException On any errors sending request or receiving
	 *                     response
	 */
	@Override
	public void storeProps() throws IOException {
		if (prop != null) prop.encodeStore(os, drop);
		os.flush();
	}

}
