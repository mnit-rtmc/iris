/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2022  SRF Consulting Group
 * Copyright (C) 2021       Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ndorv5;

import java.io.IOException;
import java.io.InputStream;

import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.server.comm.AsciiDeviceProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.utils.LineReader;

/**
 * A property which can be sent-to or received-from
 * an NDORv5 gate-controller.
 *
 * Note:  Code updated in August 2016 to include
 * multi-arm gate protocol referred to as v5.
 *
 * @author John L. Stanley - SRF Consulting
 * @author Douglas Lau
 */
public class GateNdorV5Property extends AsciiDeviceProperty {

	// current gate status
	// (valid after parsing a response from the gate controller)
	StatusOfGate              statusOfGate;
	StatusOfGateArmLights     gateArmLights;
	StatusOfWarningSignLights warningSign;
	int                       delay;

	/** Create a new NDOR Gate property */
	public GateNdorV5Property(String cmd) {
		super(cmd);
	}

	/** Substitute a different LineReader to deal with the
	 *  odd "<CR><LF>" (literally, those EIGHT characters)
	 *  end-of-line marker that the NDOR v5 gate-controller
	 *  sends.
	 *
	 * @param is Input stream to read.
	 * @param max_chars Maximum number of characters on a line.
	 */
	@Override
	protected LineReader newLineReader(InputStream is) throws IOException {
		return new LineReaderNdorGate(is, MAX_CHARS);
	}

	/** parse a value from the gate controller's response */
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
		if (resp.length() > 7) {
			int a = parseField('a', resp);
			int b = parseField('b', resp);
			int c = parseField('c', resp);
			int d = parseField('d', resp);
			statusOfGate  = StatusOfGate.fromOrdinal(a);
			warningSign   = StatusOfWarningSignLights.fromOrdinal(b);
			gateArmLights = StatusOfGateArmLights.fromOrdinal(c);
			delay         = d;
			return true;
		}
		return false;
	}

	/** Translate NDOR v5-gate status values to GateArmState status values */
	@SuppressWarnings("incomplete-switch")
	public GateArmState getState() {
		// communication error before state could be determined
		if (statusOfGate == null)
			return GateArmState.UNKNOWN;
		
		// gate-motion and primary faults (arm motion faults)
		switch (statusOfGate) {
			case OPEN_IN_PROGRESS:
				return GateArmState.OPENING;
			case CLOSE_IN_PROGRESS:
				return GateArmState.CLOSING;
			case TIMEOUT_STILL_CLOSED:
			case TIMEOUT_OPENING_FAILED:
			case TIMEOUT_STILL_OPENED:
			case TIMEOUT_CLOSING_FAILED:
			case GATE_NOT_CONFIGURED:
				return GateArmState.FAULT;
		}
		// secondary faults (arm-lights and gate-sign)
		if ( (gateArmLights == null || gateArmLights.isError())
				|| (warningSign == null || warningSign.isError()))
			return GateArmState.FAULT;
		// finished moving status
		switch (statusOfGate) {
			case CLOSE_COMPLETE:
				return GateArmState.CLOSED;
			case OPEN_COMPLETE:
				return GateArmState.OPEN;
		}
		// should never get here
		return GateArmState.FAULT;
	}

	/** Get fault description (or null) */
	@SuppressWarnings("incomplete-switch")
	public String getFault() {
		// communication error before state could be determined
		if (statusOfGate == null)
			return GateArmState.UNKNOWN.toString();
		
		// primary faults (arm motion faults)
		switch (statusOfGate) {
			case TIMEOUT_STILL_CLOSED:
			case TIMEOUT_OPENING_FAILED:
			case TIMEOUT_STILL_OPENED:
			case TIMEOUT_CLOSING_FAILED:
			case GATE_NOT_CONFIGURED:
				return statusOfGate.toString();
		}
		// secondary faults (arm-lights and gate-sign)
		if (gateArmLights == null)
			return StatusOfGateArmLights.LIGHTS_ERROR.toString();
		if (gateArmLights.isError())
			return gateArmLights.toString();
		if (warningSign == null)
			return StatusOfWarningSignLights.SIGN_ERROR.toString();
		if (warningSign.isError())
			return warningSign.toString();
		// no faults
		return null;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("gate:");
		sb.append(statusOfGate);
		sb.append(" gal:");
		sb.append(gateArmLights);
		sb.append(" wsl:");
		sb.append(warningSign);
		sb.append(" delay:");
		sb.append(delay);
		return sb.toString();
	}
}
