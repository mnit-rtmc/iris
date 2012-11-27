/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dinrelay;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import us.mn.state.dot.tms.utils.LineReader;

/**
 * DIN relay outlet property.
 *
 * @author Douglas Lau
 */
public class OutletProperty extends DinRelayProperty {

	/** Interface to callback status */
	static public interface OutletCallback {
		void updateOutlets(boolean[] outlets);
		void complete(boolean success);
	}

	/** Maximum number of chars in response for line reader */
	static private final int MAX_RESP = 1024;

	/** Maximum number of lines to read */
	static private final int MAX_LINES = 500;

	/** Regex to match outlet state */
	static private final Pattern STATE = Pattern.compile(
		".*-- state=([a-fA-F\\d]{2}) .*");

	/** Callback interface for outlet status */
	private final OutletCallback callback;

	/** Outlet powered status */
	private final boolean[] outlets = new boolean[8];

	/** Flag to indicate state has been parsed */
	private boolean parsed = false;

	/** Get outlet state */
	public boolean[] getOutletState() {
		return outlets;
	}

	/** Create a new outlet property */
	public OutletProperty(OutletCallback oc) {
		super("index.htm");
		callback = oc;
	}

	/** Decode a QUERY response */
	public void decodeQuery(InputStream is, int drop) throws IOException {
		LineReader lr = new LineReader(is, MAX_RESP);
		String line = lr.readLine();
		for(int i = 0; line != null && i < MAX_LINES; i++) {
			DinRelayPoller.log("parsing " + line);
			Matcher m = STATE.matcher(line);
			if(m.find()) {
				setOutlets(m.group(1));
				break;
			}
			line = lr.readLine();
		}
	}

	/** Set the outlet state */
	private void setOutlets(String state) {
		try {
			int o = Integer.parseInt(state, 16);
			for(int i = 0; i < 8; i++) {
				if(((o >> i) & 1) == 1)
					outlets[i] = true;
			}
			parsed = true;
		}
		catch(NumberFormatException e) {
			// don't update the outlet state
		}
	}

	/** Complete the outlet property read */
	public void complete(boolean success) {
		if(success && parsed)
			callback.updateOutlets(outlets);
		callback.complete(success && parsed);
	}
}
