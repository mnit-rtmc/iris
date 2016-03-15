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
	static private final Pattern RELAY = Pattern.compile(
		"<relay([\\d]+)state>([01])</relay\\1state>");

	/** Regex to match input state */
	static private final Pattern INPUT = Pattern.compile(
		"<input([\\d]+)state>([01])</input\\1state>");

	/** Relative path */
	private final String path;

	/** Get the path for a property */
	@Override
	public String getPath() {
		return path;
	}

	/** Relay powered status */
	private final boolean[] relays = new boolean[8];

	/** Input status */
	private final boolean[] inputs = new boolean[8];

	/** Get relay state */
	public boolean getRelay(int pin) {
		return relays[pin - 1];
	}

	/** Set relay state */
	private void setRelay(int pin, boolean s) {
		relays[pin - 1] = s;
	}

	/** Get input state */
	public boolean getInput(int pin) {
		return inputs[pin - 1];
	}

	/** Set input state */
	private void setInput(int pin, boolean s) {
		inputs[pin - 1] = s;
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
		sb.append(",inputs:");
		for (int i = 0; i < inputs.length; i++) {
			if (i > 0)
				sb.append(' ');
			sb.append(inputs[i]);
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
		clearValues();
		LineReader lr = new LineReader(is, MAX_RESP);
		String line = lr.readLine();
		for (int i = 0; line != null && i < MAX_LINES; i++) {
			found |= matchRelay(line) | matchInput(line);
			line = lr.readLine();
		}
		if (!found)
			throw new ControllerException("NO RELAYS");
	}

	/** Match a relay element */
	private boolean matchRelay(String line) throws ControllerException {
		boolean found = false;
		Matcher m = RELAY.matcher(line);
		while (m.find()) {
			int pin = parsePin(m.group(1));
			boolean v = parseBool(m.group(2));
			try {
				setRelay(pin, v);
			}
			catch (IndexOutOfBoundsException e) {
				throw new ControllerException("INVALID RELAY");
			}
			found = true;
		}
		return found;
	}

	/** Match an input element */
	private boolean matchInput(String line) throws ControllerException {
		boolean found = false;
		Matcher m = INPUT.matcher(line);
		while (m.find()) {
			int pin = parsePin(m.group(1));
			boolean v = parseBool(m.group(2));
			try {
				setInput(pin, v);
			}
			catch (IndexOutOfBoundsException e) {
				throw new ControllerException("INVALID INPUT");
			}
			found = true;
		}
		return found;
	}

	/** Clear the relays and inputs */
	private void clearValues() {
		for (int r = 0; r < relays.length; r++)
			relays[r] = false;
		for (int r = 0; r < inputs.length; r++)
			inputs[r] = false;
	}

	/** Parse pin number */
	private int parsePin(String value) throws ControllerException {
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e) {
			throw new ControllerException("INVALID PIN");
		}
	}

	/** Parse a boolean value */
	private boolean parseBool(String value) throws ControllerException {
		try {
			switch (Integer.parseInt(value)) {
			case 0: return false;
			case 1: return true;
			default: break;
			}
		}
		catch (NumberFormatException e) { }
		throw new ControllerException("INVALID BOOL");
	}
}
