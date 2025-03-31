/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.ndotbeacon;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import us.mn.state.dot.tms.BeaconState;
import us.mn.state.dot.tms.server.comm.AsciiDeviceProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.utils.LineReader;

/**
 * A property to handle a command/response exchange
 * with a Nebraska (NDOT) beacon-controller.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class NdotBeaconProperty extends AsciiDeviceProperty {

	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss  ");
	public void log(String msg) {
		String myDateString = sdf.format(new Date());
		System.out.println(myDateString+msg);
	}

	// current beacon state/status values
	// (valid after parsing a response from the NDOT beacon controller)
	NdotBeaconState  beaconState;
	NdotBeaconStatus primaryStatus;
	NdotBeaconStatus secondaryStatus;

	/** Create a new NDOT Beacon property */
	public NdotBeaconProperty(String cmd) {
		super(cmd);
	}

	/** Substitute a different LineReader to deal with
	 *  the odd "<CR><LF>" (EIGHT characters) EOL
	 *  marker that the NDOR beacon-controller sends.
	 *
	 * @param is Input stream to read.
	 * @param max_chars Maximum number of characters on a line.
	 */
	@Override
	protected LineReader newLineReader(InputStream is) throws IOException {
		return new LineReaderNdotBeacon(is, MAX_CHARS);
	}

	/** parse a value from the beacon controller's response */
	private int parseField(char ch, String resp) throws IOException {
		int len = resp.length();
		for (int i = 0; (i < len); ++i) {
			if (resp.charAt(i) == ch) {
				int ret = 0;
				char x;
				for (++i; (i < len); ++i) {
					x = resp.charAt(i);
					if (('0' <= x) && (x <= '9'))
						ret = (ret * 10) + (x - '0');
					else
						return ret;
				}
			}
		}
		throw new ParsingException(
			"INVALID RESPONSE "+ch+": \"" + resp + "\"");
	}

	/** Parse response from controller **/
	@Override
	protected boolean parseResponse(String resp) throws IOException {
		log("Recv: "+resp);
		if (resp.length() > 7) {
			int a = parseField('a', resp);
			int b = parseField('b', resp);
			int c = parseField('c', resp);
			beaconState     = NdotBeaconState.fromOrdinal(a);
			primaryStatus   = NdotBeaconStatus.fromOrdinal(b);
			secondaryStatus = NdotBeaconStatus.fromOrdinal(c);
			log("Stat: "+toString());
			return true;
		}
		return false;
	}

	/** Translate NDOT-beacon-status values to IRIS BeaconState values */
	public BeaconState getState() {
		if (!gotValidResponse())
			return BeaconState.UNKNOWN;
		// current state
		NdotBeaconStatus desiredStatus;
		BeaconState      desiredResult;
		BeaconState      incompleteResult;
		switch (beaconState) {
			case BEACON_DISABLED:
				desiredStatus    = NdotBeaconStatus.LIGHTS_OFF;
				desiredResult    = BeaconState.DARK;
				incompleteResult = BeaconState.DARK_REQ;
				break;
			case BEACON_ENABLED:
				desiredStatus    = NdotBeaconStatus.LIGHTS_ON;
				desiredResult    = BeaconState.FLASHING;
				incompleteResult = BeaconState.FLASHING_REQ;
				break;
			default:
				return BeaconState.UNKNOWN;
		}
		if ((primaryStatus == desiredStatus) || (secondaryStatus == desiredStatus))
		{
			if ((primaryStatus == secondaryStatus)
			 || primaryStatus.isError()
			 || secondaryStatus.isError())
				return desiredResult;
		}
		return incompleteResult;
	}

	/** Get FAULT description (or null) */
	public String getFaultStatus() {
		if (!gotValidResponse())
			return "COMM_ERROR";
		if (primaryStatus.isError()) {
			if (primaryStatus == secondaryStatus)
				return "BOTH_ERROR";
			return "PRIMARY_ERROR";
		}
		if (secondaryStatus.isError())
			return "SECONDARY_ERROR";
		return null;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("control:");
		sb.append(beaconState);
		sb.append(", primary:");
		sb.append(primaryStatus);
		sb.append(", secondary:");
		sb.append(secondaryStatus);
		return sb.toString();
	}
}
