/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.addco;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.Messenger;

/**
 * Addco Messenger
 *
 * @author Douglas Lau
 */
public class AddcoMessenger extends Messenger {

	/** Wrapped messenger */
	private final Messenger wrapped;

	/** Create a new Addco messenger */
	public AddcoMessenger(Messenger m) {
		wrapped = m;
	}

	/** Open the messenger */
	@Override
	public void open() throws IOException {
		wrapped.open();
		output = new AddcoDL.FOutputStream(wrapped.getOutputStream());
		input = new AddcoDL.FInputStream(wrapped.getInputStream(""));
	}

	/** Close the messenger */
	@Override
	public void close() {
		wrapped.close();
		output = null;
		input = null;
	}
}
