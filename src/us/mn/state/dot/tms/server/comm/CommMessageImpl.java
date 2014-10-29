/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * Comm message implementation.
 *
 * @author Douglas Lau
 */
public class CommMessageImpl<T extends ControllerProperty>
	implements CommMessage<T>
{
	/** Messenger object */
	private final Messenger messenger;

	/** Controller to send message */
	private final ControllerImpl controller;

	/** Controller properties */
	private final LinkedList<T> props;

	/** Create a new comm message.
	 * @param m Messenger to use for communication.
	 * @param c Controller to send message. */
	public CommMessageImpl(Messenger m, ControllerImpl c) {
		messenger = m;
		controller = c;
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
		messenger.drain();
		OutputStream os = messenger.getOutputStream(controller);
		if (os != null) {
			for (T p: props)
				p.encodeQuery(controller, os);
			os.flush();
		}
		for (T p: props) {
			p.decodeQuery(controller, messenger.getInputStream(
				p.getPath(), controller));
		}
	}

	/** Store the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void storeProps() throws IOException {
		messenger.drain();
		OutputStream os = messenger.getOutputStream(controller);
		if (os != null) {
			for (T p: props)
				p.encodeStore(controller, os);
			os.flush();
		}
		for (T p: props) {
			p.decodeStore(controller, messenger.getInputStream(
				p.getPath(), controller));
		}
	}
}
