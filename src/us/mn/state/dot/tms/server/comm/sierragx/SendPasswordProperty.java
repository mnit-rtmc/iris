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
import us.mn.state.dot.tms.server.comm.AsciiDeviceProperty;

/**
 * Property to send a password and wait for an "OK" response.
 * This will also recognize a "Login incorrect" response.
 *
 * @author John L. Stanley
 */
public class SendPasswordProperty extends AsciiDeviceProperty {

	public SendPasswordProperty(String pw) {
		super(pw+"\r");
		max_chars = 200;
	}

	//--------------------------------------

	protected boolean bLoginFinished = false;

	public boolean getLoginFinished() {
		return bLoginFinished;
	}

	//--------------------------------------

	@Override
	protected boolean parseResponse(String resp) throws IOException {
		if (resp.contains("OK")) {
			bLoginFinished = true;
			return true;
		}
		if (resp.contains("Login incorrect")) {
			return true;
		}
		return false;  // keep looking
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(loginOk:");
		sb.append(bLoginFinished);
		sb.append(")");
		return sb.toString();
	}
}
