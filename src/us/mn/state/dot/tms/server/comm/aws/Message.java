/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.aws;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * AWS Message.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class Message implements CommMessage {

	/** Associated messenger */
	protected final Messenger messenger;

	/** AWS property to parse */
	protected AwsProperty prop = null;

	/** Create a new message */
	public Message(Messenger mess) {
		messenger = mess;
	}

	/** Add a controller property */
	public void add(ControllerProperty cp) {
		if(cp instanceof AwsProperty)
			prop = (AwsProperty)cp;
	}

	/** Send a get request message.
	 * Defined in CommMessage interface.
	 * @throws IOException if received response is malformed. */
	public void getRequest() throws IOException {
		if(prop == null)
			throw new ProtocolException("No property");
		prop.doGetRequest(messenger.getInputStream());
	}

	/** Send an set request message. Defined in the
	 * CommMessage interface. */
	public void setRequest() throws IOException {}
}
