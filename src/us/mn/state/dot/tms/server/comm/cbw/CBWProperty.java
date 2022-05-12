/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
 * Copyright (C) 2021-2022  Iteris Inc.
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
import us.mn.state.dot.tms.utils.SString;

/**
 * Control by web relay property.
 *
 * @author Douglas Lau
 * @author Deb Behera
 * @author Michael Darter
 */
public class CBWProperty extends ControllerProperty {

	/** Maximum number of chars in response for line reader */
	static private final int MAX_RESP = 4096;

	/** Maximum number of lines to read */
	static private final int MAX_LINES = 500;

	/** Regex to match relay state for previous version*/
	static private final Pattern RELAY_PREV = Pattern.compile(
		"<relay([\\d]+)state>([01])</relay\\1state>");

	/** Regex to match input state for previous version*/
	static private final Pattern INPUT_PREV = Pattern.compile(
		"<input([\\d]+)state>([01])</input\\1state>");

	/** Regex to match relay state */
	static private final Pattern RELAY = Pattern.compile(
		"<relay([\\d]+)>([01])</relay([\\d]+)>");

	/** Regex to match input state */
	static private final Pattern INPUT = Pattern.compile(
		"<digitalInput([\\d]+)>([01])</digitalInput([\\d]+)>");

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

	/** Voltage in */
	public double volt_in;

	/** Controller serial number */
	public String ctl_sn;

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

	/** Create a new CBW relay property.
	 * @param p URL path to get beacons / controller status */
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

	/** Decode a STORE response based on previous or current version.*/
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
			found |= matchRelay(line, "current") | matchInput(line, "current");
			line = lr.readLine();  // can be null
			volt_in = parseVoltage(line);
			ctl_sn = parseSn(line);
		}
		if (!found)
		{
			for (int i = 0; line != null && i < MAX_LINES; i++) {
			found |= matchRelay(line, "previous") | matchInput(line, "previous");
			line = lr.readLine();
			}            
			if (!found)
				throw new ControllerException("NO RELAYS");
		}
	}

	/** Match a relay element */
	private boolean matchRelay(String line, String version) throws ControllerException {
		boolean found = false;
		Matcher m;
		if ( version == "current" ) {
			m = RELAY.matcher(line);
		} else {
			m = RELAY_PREV.matcher(line);
		}

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
	private boolean matchInput(String line, String version) throws ControllerException {
		boolean found = false;
		Matcher m;
		if ( version == "current" ) {
			m = INPUT.matcher(line);
		} else {
			m = INPUT_PREV.matcher(line);
		}

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

	/** Extract voltage from XML */
	private double parseVoltage(String line) {
		if (!SString.safe(line).startsWith("<vin>"))
			return volt_in;
		String volts = SString.extractMiddle(line, "<vin>", "</vin>");
		volts = (volts.isEmpty() ? "0" : volts);
		return Double.parseDouble(volts);
	}

	/** Extract device SN from XML */
	private String parseSn(String line) {
		if (!SString.safe(line).startsWith("<serialNumber>"))
			return ctl_sn;
		String sn = SString.extractMiddle(line, "<serialNumber>", "</serialNumber>");
		return (!sn.isEmpty() ? sn : ctl_sn);
	}
}
