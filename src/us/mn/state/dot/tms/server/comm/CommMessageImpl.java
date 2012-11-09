/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.LinkedList;

/**
 * Comm message implementation.
 *
 * @author Douglas Lau
 */
public class CommMessageImpl<T extends ControllerProperty>
	implements CommMessage<T>
{
	/** Comm output stream */
	private final OutputStream output;

	/** Comm input stream */
	private final InputStream input;

	/** Drop address */
	private final int drop;

	/** Controller properties */
	private final LinkedList<T> props;

	/** Create a new comm message.
	 * @param out Output stream to write message data.
	 * @param is Input stream to read message responses.
	 * @param d Drop address. */
	public CommMessageImpl(OutputStream out, InputStream is, int d) {
		output = out;
		input = is;
		drop = d;
		props = new LinkedList<T>();
	}

	/** Add a controller property */
	public void add(T cp) {
		props.add(cp);
	}

	/** Query the controller properties.
	 * @throws IOException On any errors sending message or receiving
	 *         response */
	public void queryProps() throws IOException {
		input.skip(input.available());
		for(T p: props)
			p.encodeQuery(output, drop);
		output.flush();
		for(T p: props)
			p.decodeQuery(input, drop);
	}

	/** Store the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void storeProps() throws IOException {
		input.skip(input.available());
		for(T p: props)
			p.encodeStore(output, drop);
		output.flush();
		for(T p: props)
			p.decodeStore(input, drop);
	}
}
