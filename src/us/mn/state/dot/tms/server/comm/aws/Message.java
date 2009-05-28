/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.HttpFileMessenger;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.utils.Log;

/**
 * AWS Message. Normally, a Message represents the bytes sent and
 * received from a device. However, the AWS poller uses the
 * the HttpFileMessenger messenger, which reads a file via HTTP,
 * so there is no real message "sent".
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class Message implements AddressedMessage
{
	/** dms messages received from AWS */
	private byte[] m_msgs = new byte[0];

	// associated file messenger
	private HttpFileMessenger m_mess;

	/** Create a new message */
	public Message(Messenger mess) {
		if(!(mess instanceof HttpFileMessenger)) {
			throw new IllegalArgumentException(
			    "wrong type of messenger arg.");
		}
		m_mess = (HttpFileMessenger) mess;
	}

	/**
	 * Add an object to this message.
	 * Defined in AddressedMessage interface.
	 */
	public void add(Object mo) {}

	/** Send a get request message.
	 *  Defined in AddressedMessage interface.
	 *  @throws IOException if received response is malformed. */
	public void getRequest() throws IOException {
		Log.finest("aws.Message.getRequest() called.");
		m_msgs = m_mess.read();
	}

	/** Send a set request message. Defined in the 
	 * AddressedMessage interface. */
	public void setRequest(String community) throws IOException {}

	/** Send an set request message. Defined in the
	 *  AddressedMessage interface. */
	public void setRequest() throws IOException {}

	/** Get the DMS messages received. */
	public byte[] getDmsMsgs() {
		return (m_msgs);
	}
}
