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
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * AWS Message.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class Message implements AddressedMessage {

	/** Associated messenger */
	protected final Messenger messenger;

	/** AWS request to parse */
	protected AwsRequest req = null;

	/** Create a new message */
	public Message(Messenger mess) {
		messenger = mess;
	}

	/** Add a request object to this message */
	public void add(Object mo) {
		if(mo instanceof AwsRequest)
			req = (AwsRequest)mo;
	}

	/** Send a get request message.
	 * Defined in AddressedMessage interface.
	 * @throws IOException if received response is malformed. */
	public void getRequest() throws IOException {
		if(req == null)
			throw new ProtocolException("No request");
		req.doGetRequest(messenger.getInputStream());
	}

	/** Send an set request message. Defined in the
	 * AddressedMessage interface. */
	public void setRequest() throws IOException {}
}
