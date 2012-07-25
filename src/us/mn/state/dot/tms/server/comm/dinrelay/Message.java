/*
 * IRIS -- Intelligent Roadway Information System
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
package us.mn.state.dot.tms.server.comm.dinrelay;

import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.HttpFileMessenger;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * DIN relay message
 *
 * @author Douglas Lau
 */
public class Message implements CommMessage {

	/** Associated messenger */
	private final HttpFileMessenger messenger;

	/** Controller for message */
	private final ControllerImpl controller;

	/** Property to parse */
	private DinRelayProperty prop = null;

	/** Create a new message */
	public Message(HttpFileMessenger m, ControllerImpl c) {
		messenger = m;
		controller = c;
	}

	/** Add a controller property */
	public void add(ControllerProperty cp) {
		if(cp instanceof DinRelayProperty)
			prop = (DinRelayProperty)cp;
	}

	/** Query the controller properties.
	 * @throws IOException On any errors sending or receiving. */
	public void queryProps() throws IOException {
		DinRelayPoller.log("queryProps called");
		if(prop != null) {
			messenger.setPath(prop.path);
			prop.decodeQuery(messenger.getInputStream(controller),
				0);
		} else
			throw new ProtocolException("No property");
	}

	/** Store the controller properties.
	 * @throws IOException On any errors sending or receiving. */
	public void storeProps() throws IOException {
		DinRelayPoller.log("storeProps called");
		if(prop != null) {
			messenger.setPath(prop.path);
			prop.decodeStore(messenger.getInputStream(controller),
				0);
		} else
			throw new ProtocolException("No property");
	}
}
