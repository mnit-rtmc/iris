/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cbw;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.utils.LineReader;

/**
 * Control by web relay property.
 *
 * @author Douglas Lau
 */
public class CBWProperty extends ControllerProperty {

	/** Maximum number of chars in response for line reader */
	static private final int MAX_RESP = 4096;

	/** Maximum number of lines to read */
	static private final int MAX_LINES = 500;

	/** Regex to match relay state */
	static private final Pattern STATE = Pattern.compile(
		"<relay([\\d]+)state>([01])</relay\\1state>");

	/** Relative path */
	private final String path;

	/** Get the path for a property */
	@Override
	public String getPath() {
		return path;
	}

	/** Relay powered status */
	private final boolean[] relays = new boolean[8];

	/** Get relay state */
	public boolean getRelay(int pin) {
		return relays[pin - 1];
	}

	/** Create a new CBW relay property */
	public CBWProperty(String p) {
		path = p;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("relays:");
		for (int i = 0; i < relays.length; i++) {
			if (i > 0)
				sb.append(' ');
			sb.append(relays[i]);
		}
		return sb.toString();
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		decodeXml(is);
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		decodeXml(is);
	}

	/** Decode state.xml response */
	private void decodeXml(InputStream is) throws IOException {
		boolean found = false;
		clearRelays();
		LineReader lr = new LineReader(is, MAX_RESP);
		String line = lr.readLine();
		for (int i = 0; line != null && i < MAX_LINES; i++) {
			Matcher m = STATE.matcher(line);
			while (m.find()) {
				setRelay(m.group(1), m.group(2));
				found = true;
			}
			line = lr.readLine();
		}
		if (!found)
			throw new ControllerException("NO RELAYS");
	}

	/** Clear the relays */
	private void clearRelays() {
		for (int r = 0; r < relays.length; r++)
			relays[r] = false;
	}

	/** Set one relay state */
	private void setRelay(String relay, String on) throws IOException {
		try {
			int r = Integer.parseInt(relay);
			if (r >= 1 && r <= 8) {
				switch (Integer.parseInt(on)) {
				case 0:
					relays[r - 1] = false;
					return;
				case 1:
					relays[r - 1] = true;
					return;
				default:
					break;
				}
			}
		}
		catch (NumberFormatException e) {
			throw new ControllerException("NUMBER FORMAT");
		}
		throw new ControllerException("SET RELAY");
	}
}
