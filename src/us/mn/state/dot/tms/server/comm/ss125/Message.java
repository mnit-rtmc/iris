/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * SS125 message
 *
 * @author Douglas Lau
 */
public class Message implements CommMessage {

	/** Serial output stream */
	protected final OutputStream output;

	/** Serial input stream */
	protected final InputStream input;

	/** Controller for message */
	protected final ControllerImpl ctrl;

	/** SS125 destination ID (drop address) */
	protected final short dest_id;

	/** Controller property */
	protected SS125Property prop;

	/** Create a new SS125 message.
	 * @param out Output stream to write message data.
	 * @param is Input stream to read message responses.
	 * @param c Controller to send messages. */
	public Message(OutputStream out, InputStream is, ControllerImpl c) {
		output = out;
		input = is;
		ctrl = c;
		dest_id = c.getDrop();
	}

	/** Add a controller property */
	public void add(ControllerProperty cp) {
		if(cp instanceof SS125Property)
			prop = (SS125Property)cp;
	}

	/** Query the controller properties.
	 * @throws IOException On any errors sending message or receiving
	 *         response */
	public void queryProps() throws IOException {
		assert prop != null;
		byte[] body = prop.formatBodyGet();
		byte[] header = prop.formatHeader(body, dest_id);
		doPoll(header, body);
		while(!prop.isComplete()) {
			try {
				prop.parsePayload(doResponse(false));
			}
			catch(SocketTimeoutException e) {
				if(prop.hasData()) {
					OpSS125.log(ctrl, "PARTIAL INTERVAL: " +
						prop.toString());
				}
				throw e;
			}
			header[8]++;	// Increment sequence number
			body[1]++;	// Sub ID is like a packet number
		}
	}

	/** Store the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void storeProps() throws IOException {
		assert prop != null;
		byte[] body = prop.formatBodySet();
		byte[] header = prop.formatHeader(body, dest_id);
		doPoll(header, body);
		prop.parseResult(doResponse(true));
	}

	/** Perform a message poll.
	 * @param header Header of message.
	 * @param body Body of message.
	 * @throws IOException On any errors sending message. */
	protected void doPoll(byte[] header, byte[] body) throws IOException {
		input.skip(input.available());
		BufferedOutputStream bos = new BufferedOutputStream(output,256);
		bos.write(header);
		bos.write(SS125Property.CRC.calculate(header));
		bos.write(body);
		bos.write(SS125Property.CRC.calculate(body));
		bos.flush();
	}

	/** Receive a message response.
	 * @param store Flag to indicate STORE message.
	 * @return Body of message received.
	 * @throws IOException On any errors receiving response. */
	protected byte[] doResponse(boolean store) throws IOException {
		prop.delayResponse();
		byte[] rhead = prop.recvResponse(input, 10);
		byte h_crc = prop.recvResponse(input, 1)[0];
		int n_body = prop.parseHead(rhead, h_crc, dest_id);
		byte[] rbody = prop.recvResponse(input, n_body);
		byte b_crc = prop.recvResponse(input, 1)[0];
		prop.parseBody(rbody, b_crc, store);
		return rbody;
	}
}
