/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.sierragx;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.AsciiPromptProperty;

/**
 * Property to send nothing and wait for a "login:"
 * prompt-style response (no trailing EOL).
 *
 * @author John L. Stanley
 */
public class TestLoginModeProperty extends AsciiPromptProperty {

	public TestLoginModeProperty() {
		super("", "login: ");
		max_chars = 200;
		bGotValidResponse = true;
	}

	//--------------------------------------

	protected boolean bGotLoginPrompt = false;

	public boolean gotLoginPrompt() {
		return bGotLoginPrompt;
	}

	//--------------------------------------

	@Override
	protected boolean parseResponse(String resp) throws IOException {
		if (resp.contains("login:")) {
			bGotLoginPrompt = true;
			return true;  // found it, we're done
		}
		return false;  // keep looking
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(GotLoginPrompt:");
		sb.append(bGotLoginPrompt);
		sb.append(")");
		return sb.toString();
	}
}
