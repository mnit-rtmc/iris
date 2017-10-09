/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
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
import java.io.InputStream;

import us.mn.state.dot.tms.server.comm.AsciiDeviceProperty;
import us.mn.state.dot.tms.utils.LineReader;

/**
 * A property used for a send-expect style exchange
 * with a device where the expected response does not
 * have an EOL character at the end of the response.
 *
 * @author John L. Stanley - SRF Consulting
 */
abstract public class AsciiPromptProperty extends AsciiDeviceProperty {

	/** String the substitute reader will look for
	 *  at the end of a line */
	protected final String prompt;

	/** Create a new prompt style property */
	public AsciiPromptProperty(String xsend, String xprompt) {
		super(xsend);
		prompt = xprompt;
	}

	/** Substitute a different LineReader.
	 *
	 * @param is Input stream to read.
	 * @param max_chars Maximum number of characters on a line.
	 */
	@Override
	protected LineReader newLineReader(InputStream is, int max_chars)
			throws IOException {
		return new PromptReader(is, max_chars, prompt);
	}
}
