/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

package us.mn.state.dot.tms.comm.caws;

import java.io.IOException;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.HttpFileMessenger;
import us.mn.state.dot.tms.comm.Messenger;

/**
 * CAWS Message. Normally, a Message represents the bytes sent and
 * received from a device. However, the CAWS poller uses the
 * the HttpFileMessenger messenger, which reads a file via HTTP,
 * so there is no real message "sent".
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class Message implements AddressedMessage
{
	/** dms messages received from caws */
	private byte[] m_msgs = new byte[0];

	// fields
	private HttpFileMessenger m_mess;    // associated file messenger

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

	/**
	 * Send a get request message.
	 * Defined in AddressedMessage interface.
	 *
	 * @throws IOException if received response is malformed.
	 */
	public void getRequest() throws IOException {
		System.err.println("caws.Message.getRequest() called.");

		// read http file
		m_msgs = m_mess.read();
	}

	/**
	 * Send a set request message.
	 * Defined in AddressedMessage interface.
	 */
	public void setRequest(String community) throws IOException {}

	/**
	 * Send an set request message.
	 * Defined in AddressedMessage interface.
	 */
	public void setRequest() throws IOException {}

	/**
	 * get the DMS messages received.
	 */
	public byte[] getDmsMsgs() {
		return (m_msgs);
	}
}
