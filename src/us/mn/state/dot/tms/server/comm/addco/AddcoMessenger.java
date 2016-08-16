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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.Messenger;

/**
 * Addco Messenger
 *
 * @author Douglas Lau
 */
public class AddcoMessenger extends Messenger {

	/** Wrapped messenger */
	private final Messenger wrapped;

	/** Input stream */
	private final InputStream input;

	/** Output stream */
	private final OutputStream output;

	/** Create a new Addco messenger */
	public AddcoMessenger(Messenger m) throws IOException {
		wrapped = m;
		output = new AddcoDL.FOutputStream(wrapped.getOutputStream());
		input = new AddcoDL.FInputStream(wrapped.getInputStream(""));
	}

	/** Close the messenger */
	@Override
	public void close() throws IOException {
		wrapped.close();
	}

	/** Get the input stream.
	 * @param path Relative path name.
	 * @return An input stream for reading from the messenger. */
	@Override
	public InputStream getInputStream(String path) {
		return input;
	}

	/** Get the output stream */
	@Override
	public OutputStream getOutputStream(ControllerImpl c) {
		return output;
	}

	/** Drain any bytes from the input stream */
	@Override
	public void drain() throws IOException {
		wrapped.drain();
	}
}
