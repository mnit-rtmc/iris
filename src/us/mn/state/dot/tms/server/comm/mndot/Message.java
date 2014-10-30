/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * Mndot protocol message
 *
 * @author Douglas Lau
 */
public class Message implements CommMessage {

	/** Serial output stream */
	protected final OutputStream output;

	/** Serial input stream */
	protected final InputStream input;

	/** Controller */
	private final ControllerImpl controller;

	/** Controller property */
	protected MndotProperty prop;

	/** Create a new Mndot protocol message */
	public Message(OutputStream o, InputStream i, ControllerImpl c) {
		output = o;
		input = i;
		controller = c;
	}

	/** Get the controller */
	public ControllerImpl getController() {
		return controller;
	}

	/** Add a controller property */
	public void add(ControllerProperty cp) {
		if(cp instanceof MndotProperty)
			prop = (MndotProperty)cp;
	}

	/** Query the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void queryProps() throws IOException {
		if (prop != null) {
			input.skip(input.available());
			prop.doGetRequest(this);
		} else
			throw new IOException("No property");
	}

	/** Store the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void storeProps() throws IOException {
		if(prop != null)
			prop.doSetRequest(this);
		else
			throw new IOException("No property");
	}
}
