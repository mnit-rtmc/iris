/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2026  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.ndotgate;

import java.io.IOException;
import java.io.InputStream;

import us.mn.state.dot.tms.GateArmError;
import us.mn.state.dot.tms.GateArmFlag;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.server.comm.AsciiDeviceProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.utils.LineReader;

/**
 * A property which can be sent-to or received-from
 * an NDORv5, NDOTv6 or NDOTv7 gate-controller.
 *
 * Note:  Code updated in August 2016 to include
 *  multi-arm gate protocol referred to as v5.
 * Note2:  Updated in January 2026 to include the
 *  v6 extension that included the North Dakota
 *  MODBUS error-reporting extension and shorter
 *  gate-fault message strings.
 * Note3:  Updated in October 2026 to include the
 *  NDOTv7 status-flags field 'f' (gate override).
 *
 * @author John L. Stanley - SRF Consulting
 * @author Douglas Lau
 */
public class NdotGateProperty extends AsciiDeviceProperty {

	/** Error flags field 'e': MODBUS error */
	static private final int E_MODBUS_ERROR = 0x01;

	/** All error flags bits known to IRIS */
	static private final int E_KNOWN_BITS = E_MODBUS_ERROR;

	/** Status flags field 'f': gate override in process */
	static private final int F_GATE_OVERRIDE = 0x01;

	/** All status flags bits known to IRIS */
	static private final int F_KNOWN_BITS = F_GATE_OVERRIDE;

	// current gate status
	// (valid after parsing a response from the gate controller)
	StatusOfGate              statusOfGate;
	StatusOfGateArmLights     gateArmLights;
	StatusOfWarningSignLights warningSign;
	int                       delay;
	int                       errorFlags;
	int                       statusFlags;

	/** Close commanded and warning delay running (set by IRIS, not
	 * parsed).  An "open" arm is reported as closing. */
	boolean                   closeDelay;

	/** Create a new NDOT Gate property */
	public NdotGateProperty(String cmd) {
		super(cmd);
	}

	/** Substitute a different LineReader to deal with the
	 *  odd "<CR><LF>" (literally, those EIGHT characters)
	 *  end-of-line marker that some NDORv5 gate-controllers
	 *  send.
	 *
	 * @param is Input stream to read.
	 * @param max_chars Maximum number of characters on a line.
	 */
	@Override
	protected LineReader newLineReader(InputStream is) throws IOException {
		return new NdotGateLineReader(is, MAX_CHARS);
	}

	/** parse a value from the gate controller's response */
	private int parseField(char ch, String resp) throws IOException {
		int len = resp.length();
		int ret = -1;
		for (int i = 0; (i < len); ++i) {
			if (resp.charAt(i) == ch) {
				ret = 0;
				char x;
				for (++i; (i < len); ++i) {
					x = resp.charAt(i);
					if (!Character.isDigit(x))
						break;
					ret = (ret * 10) + (x - '0');
				}
				return ret;
			}
		}
		// fields 'e' and 'f' are optional
		if ((ch == 'e' || ch == 'f') && (ret == -1))
			return 0;
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
			int e = parseField('e', resp);
			int f = parseField('f', resp);
			statusOfGate  = StatusOfGate.fromOrdinal(a);
			warningSign   = StatusOfWarningSignLights.fromOrdinal(b);
			gateArmLights = StatusOfGateArmLights.fromOrdinal(c);
			delay         = d;
			errorFlags    = e;
			statusFlags   = f;
			return true;
		}
		return false;
	}

	/** Translate NDOT gate status values to IRIS GateArmState status values */
	@SuppressWarnings("incomplete-switch")
	public GateArmState getState() {
		// communication error before state could be determined
		if (statusOfGate == null)
			return GateArmState.UNKNOWN;

		// errorFlags (MODBUS) faults, even while the arm is moving
		if (errorFlags > 0)
			return GateArmState.FAULT;
		// gate-motion and primary faults (arm motion faults)
		switch (statusOfGate) {
			case OPEN_IN_PROGRESS:
				return GateArmState.OPENING;
			case CLOSE_IN_PROGRESS:
				return GateArmState.CLOSING;
			case TIMEOUT_STILL_CLOSED:
			case TIMEOUT_OPEN_FAILED:
			case TIMEOUT_STILL_OPEN:
			case TIMEOUT_CLOSE_FAILED:
			case GATE_NOT_CONFIGURED:
				return GateArmState.FAULT;
		}
		// secondary faults (arm-lights and gate-sign)
		if (isLightsError() || isSignError())
			return GateArmState.FAULT;
		// finished moving status
		switch (statusOfGate) {
			case CLOSE_COMPLETE:
				return GateArmState.CLOSED;
			case OPEN_COMPLETE:
				return closeDelay
				      ? GateArmState.CLOSING
				      : GateArmState.OPEN;
		}
		// should never get here
		return GateArmState.FAULT;
	}

	/** Check if the arm lights report an error (or are unknown) */
	private boolean isLightsError() {
		return gateArmLights == null || gateArmLights.isError();
	}

	/** Check if the warning sign reports an error (or is unknown) */
	private boolean isSignError() {
		return warningSign == null || warningSign.isError();
	}

	/** Check if the MODBUS error flag is set */
	private boolean isModbusError() {
		return (errorFlags & E_MODBUS_ERROR) != 0;
	}

	/** Check if the gate override flag is set */
	public boolean isGateOverride() {
		return (statusFlags & F_GATE_OVERRIDE) != 0;
	}

	/** Check if an open arm is waiting to close: either a close
	 * warning delay is running, or a gate override is waiting before
	 * closing on its own */
	private boolean isCloseDelay() {
		return statusOfGate == StatusOfGate.OPEN_COMPLETE
		    && (closeDelay || isGateOverride());
	}

	/** Get fault description (or null).
	 * Descriptions fit the 12-character dispatcher label. */
	@SuppressWarnings("incomplete-switch")
	public String getFault() {
		// communication error before state could be determined
		if (statusOfGate == null)
			return GateArmState.UNKNOWN.toString();

		boolean mb = isModbusError();
		// primary faults (arm motion faults)
		switch (statusOfGate) {
			case TIMEOUT_STILL_CLOSED:
				return mb ? "MB_STK_CLSD" : "STUCK_CLOSED";
			case TIMEOUT_OPEN_FAILED:
				return mb ? "MB_OPEN_FAIL" : "OPEN_FAILED";
			case TIMEOUT_STILL_OPEN:
				return mb ? "MB_STK_OPEN" : "STUCK_OPEN";
			case TIMEOUT_CLOSE_FAILED:
				return mb ? "MB_CLS_FAIL" : "CLOSE_FAILED";
			case GATE_NOT_CONFIGURED:
				return "NOT_CONFIG";
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
		// tertiary faults (NDOTv6 errorFlags)
		if (mb) {
			switch (statusOfGate) {
				case CLOSE_COMPLETE:
					return "MB_CLOSED";
				case OPEN_COMPLETE:
					return isCloseDelay()
					      ? "MB_CLS_DELAY"
					      : "MB_OPEN";
				case OPEN_IN_PROGRESS:
					return "MB_OPENING";
				case CLOSE_IN_PROGRESS:
					return "MB_CLOSING";
			}
			return "MB_ERROR";
		}
		if (errorFlags > 0)
			return "ERRORFLAGS="+errorFlags;
		// no faults
		return null;
	}

	/** Get the IRIS gate arm error flags (GateArmError bits) */
	@SuppressWarnings("incomplete-switch")
	public int getGateArmErrors() {
		// communication error before state could be determined
		if (statusOfGate == null)
			return GateArmError.GATE_ERROR_COMM.bit();

		int bits = 0;
		switch (statusOfGate) {
			case TIMEOUT_STILL_CLOSED:
				bits |= GateArmError.GATE_ERROR_STILL_CLOSED.bit();
				break;
			case TIMEOUT_OPEN_FAILED:
				bits |= GateArmError.GATE_ERROR_OPEN_FAILED.bit();
				break;
			case TIMEOUT_STILL_OPEN:
				bits |= GateArmError.GATE_ERROR_STILL_OPEN.bit();
				break;
			case TIMEOUT_CLOSE_FAILED:
				bits |= GateArmError.GATE_ERROR_CLOSE_FAILED.bit();
				break;
			case GATE_NOT_CONFIGURED:
				bits |= GateArmError.GATE_ERROR_CONFIG.bit();
				break;
			case UNKNOWN_4:
			case UNKNOWN_5:
				bits |= GateArmError.GATE_ERROR_OTHER.bit();
				break;
		}
		if (isModbusError())
			bits |= GateArmError.GATE_ERROR_MODBUS.bit();
		if (isLightsError())
			bits |= GateArmError.GATE_ERROR_LIGHTS.bit();
		if (isSignError())
			bits |= GateArmError.GATE_ERROR_SIGN.bit();
		if ((errorFlags & ~E_KNOWN_BITS) != 0)
			bits |= GateArmError.GATE_ERROR_OTHER.bit();
		return bits;
	}

	/** Get the IRIS gate arm status flags (GateArmFlag bits).
	 * A position flag is only set when the controller reports an
	 * actual arm position (never for a timeout). */
	@SuppressWarnings("incomplete-switch")
	public int getGateArmFlags() {
		// communication error before state could be determined
		if (statusOfGate == null)
			return 0;

		int bits = 0;
		if (isGateOverride())
			bits |= GateArmFlag.GATE_FLAG_OVERRIDE.bit();
		switch (statusOfGate) {
			case CLOSE_COMPLETE:
				bits |= GateArmFlag.GATE_FLAG_CLOSED.bit();
				break;
			case OPEN_IN_PROGRESS:
				bits |= GateArmFlag.GATE_FLAG_OPENING.bit();
				break;
			case OPEN_COMPLETE:
				bits |= GateArmFlag.GATE_FLAG_OPEN.bit();
				if (isCloseDelay())
					bits |= GateArmFlag.GATE_FLAG_CLOSE_DELAY.bit();
				break;
			case CLOSE_IN_PROGRESS:
				bits |= GateArmFlag.GATE_FLAG_CLOSING.bit();
				break;
		}
		if ((statusFlags & ~F_KNOWN_BITS) != 0)
			bits |= GateArmFlag.GATE_FLAG_OTHER.bit();
		return bits;
	}

	/** Check if the arm is moving or a gate override is in progress
	 * (the arm has not reached a final position) */
	public boolean isMoving() {
		return (statusOfGate == StatusOfGate.OPEN_IN_PROGRESS)
		    || (statusOfGate == StatusOfGate.CLOSE_IN_PROGRESS)
		    || ((statusOfGate != null) && isGateOverride());
	}

	/** Check if the controller reported a motion timeout or
	 * configuration error */
	@SuppressWarnings("incomplete-switch")
	public boolean isMotionTimeout() {
		if (statusOfGate != null) {
			switch (statusOfGate) {
				case TIMEOUT_STILL_CLOSED:
				case TIMEOUT_OPEN_FAILED:
				case TIMEOUT_STILL_OPEN:
				case TIMEOUT_CLOSE_FAILED:
				case GATE_NOT_CONFIGURED:
					return true;
			}
		}
		return false;
	}

	/** Get the arm position at rest (CLOSED, OPEN or null) */
	@SuppressWarnings("incomplete-switch")
	public GateArmState getRestPosition() {
		if (statusOfGate != null) {
			switch (statusOfGate) {
				case CLOSE_COMPLETE:
					return GateArmState.CLOSED;
				case OPEN_COMPLETE:
					return GateArmState.OPEN;
			}
		}
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
		sb.append(" errorFlags:");
		sb.append(errorFlags);
		sb.append(" statusFlags:");
		sb.append(statusFlags);
		return sb.toString();
	}
}
