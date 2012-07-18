/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ssi;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.HttpFileMessenger;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * SSI Message
 *
 * @author Michael Darter
 */
public class SsiMessage implements CommMessage {

	/** Associated messenger */
	private final HttpFileMessenger messenger;

	/** Property to parse */
	private SsiProperty ssi_prop = null;

	/** Create a new message */
	public SsiMessage(HttpFileMessenger m) {
		SsiPoller.log("creating new message");
		messenger = m;
	}

	/** Add a controller property */
	public void add(ControllerProperty cp) {
		SsiPoller.log("adding property");
		if(cp instanceof SsiProperty)
			ssi_prop = (SsiProperty)cp;
	}

	/** Query the controller properties, but only if the URL last
	 * modification time changed.
	 * @throws IOException On any errors sending or receiving. */
	public void queryProps() throws IOException {
		SsiPoller.log("queryProps called");
		if(ssi_prop != null)
			ssi_prop.doGetRequest(messenger.getInputStream());
		else
			throw new ProtocolException("No property");
	}

	/** Store the controller properties.
	 * @throws IOException On any errors sending or receiving. */
	public void storeProps() throws IOException {
		throw new ProtocolException("STORE not supported");
	}
}
