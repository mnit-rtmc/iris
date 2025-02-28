/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * A TestFileMessenger is a class which reads a file from the filesystem.
 *
 * @author Douglas Lau
 */
public class TestFileMessenger extends Messenger {

	/** Create a test file messenger.
	 * @param u URI of file. */
	static protected TestFileMessenger create(URI u) throws IOException {
		return new TestFileMessenger(u.toURL());
	}

	/** URL to read */
	private final URL url;

	/** Create a new test file messenger.
	 * @param url The URL of the file to read. */
	private TestFileMessenger(URL url) {
		this.url = url;
	}

	/** Close the messenger */
	@Override
	public void close() {
		// nothing to do
	}

	/** Get the input stream */
	@Override
	public InputStream getInputStream(String p) throws IOException {
		return new FileInputStream(url.getPath());
	}

	/** Get the output stream */
	@Override
	public OutputStream getOutputStream(ControllerImpl c) {
		// File messengers don't have output streams
		return null;
	}

	/** Drain any bytes from the input stream */
	@Override
	public void drain() {
		// not needed
	}
}
