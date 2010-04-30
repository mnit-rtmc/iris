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
package us.mn.state.dot.tms.server.comm.viconptz;

import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Vicon message
 *
 * @author Douglas Lau
 */
public class Message implements CommMessage {

	/** Serial output stream */
	protected final OutputStream os;

	/** Camera receiver drop address */
	protected final int drop;

	/** Create a new Vicon message */
	public Message(OutputStream o, int d) {
		os = o;
		drop = d;
	}

	/** Controller property */
	protected ViconPTZProperty prop;

	/** Add a controller property */
	public void add(ControllerProperty cp) {
		if(cp instanceof ViconPTZProperty)
			prop = (ViconPTZProperty)cp;
	}

	/** Query the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void queryProps() throws IOException {
		throw new ProtocolException("QUERY not supported");
	}

	/** Store the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void storeProps() throws IOException {
		if(prop != null)
			prop.encodeStore(os, drop);
		os.flush();
	}
}
