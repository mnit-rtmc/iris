/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.clearguide;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.Messenger;

/**
 * ClearGuide messenger, which blocks the superclass from opening the
 * URL specified in the comm link, which instead is opened and closed
 * as part of ClearGuideProperty.
 * @author Michael Darter
 */
public class ClearGuideMessenger extends Messenger {

	/** Constructor */
	protected ClearGuideMessenger(URI s, String u, int rt, int nrd)
		throws IOException
	{
		// nothing to do
	}

	/** Close the messenger */
	@Override
	public void close() throws IOException {
		// nothing to do
	}

	/** Get the input stream
	 * @param path Relative path name.
	 * @return An input stream for reading from the messenger. */
	@Override
	public InputStream getInputStream(String path) throws IOException {
		// nothing to do
		return null;
	}

	/** Get an output stream for the specified controller */
	@Override
	public OutputStream getOutputStream(ControllerImpl ci)
		throws IOException
	{
		// nothing to do
		return null;
	}

	/** Drain any bytes from the input stream */
	@Override
	public void drain() {
		// nothing to do
	}
}
