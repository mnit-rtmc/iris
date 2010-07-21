/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.org815;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * ORG-815 message
 *
 * @author Douglas Lau
 */
public class Message implements CommMessage {

	/** Serial output stream */
	protected final OutputStream output;

	/** Serial input stream */
	protected final InputStream input;

	/** Controller property */
	protected Org815Property prop;

	/** Create a new ORG-815 message.
	 * @param out Output stream to write message data.
	 * @param is Input stream to read message responses. */
	public Message(OutputStream out, InputStream is) {
		output = out;
		input = is;
	}

	/** Add a controller property */
	public void add(ControllerProperty cp) {
		if(cp instanceof Org815Property)
			prop = (Org815Property)cp;
	}

	/** Query the controller properties.
	 * @throws IOException On any errors sending message or receiving
	 *         response */
	public void queryProps() throws IOException {
		assert prop != null;
		input.skip(input.available());
		prop.encodeQuery(output, 0);
		output.flush();
		prop.decodeQuery(input, 0);
	}

	/** Store the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void storeProps() throws IOException {
		assert prop != null;
		input.skip(input.available());
		prop.encodeStore(output, 0);
		output.flush();
		prop.decodeStore(input, 0);
	}
}
