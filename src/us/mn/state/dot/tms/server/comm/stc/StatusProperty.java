/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.stc;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Statuc property reads the gate arm status.
 *
 * @author Douglas Lau
 */
public class StatusProperty extends STCProperty {

	/** Byte offsets from beginning of status response */
	static private final int OFF_COMMAND = 1;
	static private final int OFF_OPERATOR = 3;
	static private final int OFF_FAULTS = 5;
	static private final int OFF_BATTERY = 6;
	static private final int OFF_AC_PRESENT = 7;
	static private final int OFF_OPEN_LIMIT = 8;
	static private final int OFF_CLOSE_LIMIT = 9;
	static private final int OFF_PARTIAL_OPEN_LIMIT = 10;
	static private final int OFF_EXIT_LOOP = 11;
	static private final int OFF_INNER_OBSTRUCTION_LOOP = 12;
	static private final int OFF_OUTER_OBSTRUCTION_LOOP = 13;
	static private final int OFF_RESET_SHADOW_LOOP = 14;
	static private final int OFF_RELAY_1 = 15;
	static private final int OFF_RELAY_2 = 16;
	static private final int OFF_RELAY_3 = 17;
	static private final int OFF_OPEN_TOO_LONG = 18;
	static private final int OFF_TAILGATER = 19;
	static private final int OFF_LOITERING = 20;

	/** Encode a QUERY request */
	@Override public void encodeQuery(OutputStream os, int drop)
		throws IOException
	{
		byte[] data = new byte[1];
		data[0] = 'S';
		os.write(formatRequest(drop, data));
	}

	/** Decode a QUERY response */
	@Override public void decodeQuery(InputStream is, int drop)
		throws IOException
	{
		parseFrame(is, drop);
	}

	/** Parse a received message */
	@Override protected void parseMessage(byte[] msg, int len)
		throws IOException
	{
		if(msg[0] != 'S')
			throw new ParsingException("INVALID MESSAGE:" + msg[0]);
		if(len != 21)
			throw new ParsingException("INVALID LENGTH:" + len);
		int c = parseAsciiHex2(msg, OFF_COMMAND);
		command_state = CommandStatus.fromOrdinal(c);
		if(command_state == null)
			throw new ParsingException("INVALID COMMAND:" + c);
		int o = parseAsciiHex2(msg, OFF_OPERATOR);
		operator_state = OperatorStatus.fromOrdinal(o);
		if(operator_state == null)
			throw new ParsingException("INVALID OPERATOR:" + o);
		faults = parseBoolean(msg, OFF_FAULTS);
		battery = parseAsciiHex1(msg, OFF_BATTERY);
		ac_present = parseBoolean(msg, OFF_AC_PRESENT);
		open_limit = parseBoolean(msg, OFF_OPEN_LIMIT);
		close_limit = parseBoolean(msg, OFF_CLOSE_LIMIT);
		partial_open_limit = parseBoolean(msg, OFF_PARTIAL_OPEN_LIMIT);
		exit_loop = parseBoolean(msg, OFF_EXIT_LOOP);
		inner_obstruction_loop = parseBoolean(msg,
			OFF_INNER_OBSTRUCTION_LOOP);
		outer_obstruction_loop = parseBoolean(msg,
			OFF_OUTER_OBSTRUCTION_LOOP);
		reset_shadow_loop = parseBoolean(msg, OFF_RESET_SHADOW_LOOP);
		relay_1 = parseBoolean(msg, OFF_RELAY_1);
		relay_2 = parseBoolean(msg, OFF_RELAY_2);
		relay_3 = parseBoolean(msg, OFF_RELAY_3);
		open_too_long = parseBoolean(msg, OFF_OPEN_TOO_LONG);
		tailgater = parseBoolean(msg, OFF_TAILGATER);
		loitering = parseBoolean(msg, OFF_LOITERING);
	}

	/** Command status */
	private CommandStatus command_state;

	/** Operator status */
	private OperatorStatus operator_state;

	/** Faults present flag */
	private boolean faults;

	/** Battery state */
	private int battery;

	/** AC present */
	private boolean ac_present;

	/** Open limit */
	private boolean open_limit;

	/** Close limit */
	private boolean close_limit;

	/** Partial open limit */
	private boolean partial_open_limit;

	/** Exit loop */
	private boolean exit_loop;

	/** Inner obstruction loop */
	private boolean inner_obstruction_loop;

	/** Outer obstruction loop */
	private boolean outer_obstruction_loop;

	/** Reset / shadow loop */
	private boolean reset_shadow_loop;

	/** Relay #1 */
	private boolean relay_1;

	/** Relay #2 */
	private boolean relay_2;

	/** Relay #3 */
	private boolean relay_3;

	/** Open too long */
	private boolean open_too_long;

	/** Tailgater */
	private boolean tailgater;

	/** Loitering */
	private boolean loitering;

	/** Get a string representation */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("command_status:");
		sb.append(command_state);
		sb.append(" operator_status:");
		sb.append(operator_state);
		sb.append(" faults:");
		sb.append(faults);
		sb.append(" battery:");
		sb.append(battery);
		sb.append(" AC_present:");
		sb.append(ac_present);
		sb.append(" open_limit:");
		sb.append(open_limit);
		sb.append(" close_limit:");
		sb.append(close_limit);
		sb.append(" partial_open_limit:");
		sb.append(partial_open_limit);
		sb.append(" exit_loop:");
		sb.append(exit_loop);
		sb.append(" inner_obstruction_loop:");
		sb.append(inner_obstruction_loop);
		sb.append(" outer_obstruction_loop:");
		sb.append(outer_obstruction_loop);
		sb.append(" reset_shadow_loop:");
		sb.append(reset_shadow_loop);
		sb.append(" relay_1:");
		sb.append(relay_1);
		sb.append(" relay_2:");
		sb.append(relay_2);
		sb.append(" relay_3:");
		sb.append(relay_3);
		sb.append(" open_too_long:");
		sb.append(open_too_long);
		sb.append(" tailgater:");
		sb.append(tailgater);
		sb.append(" loitering:");
		sb.append(loitering);
		return sb.toString();
	}

	/** Get the gate arm state */
	public GateArmState getState() {
		if(hasFaults())
			return GateArmState.FAULT;
		else if(isOpening())
			return GateArmState.OPENING;
		else if(isClosing())
			return GateArmState.CLOSING;
		else if(isOpen())
			return GateArmState.OPEN;
		else if(isClosed())
			return GateArmState.CLOSED;
		else
			return GateArmState.UNKNOWN;
	}

	/** Test if the gate arm has faults */
	private boolean hasFaults() {
		return faults ||
		       CommandStatus.isFault(command_state) ||
		       OperatorStatus.isFault(operator_state);
	}

	/** Test if the gate arm is opening */
	private boolean isOpening() {
		return CommandStatus.isOpening(command_state);
	}

	/** Test if the gate arm is closing */
	private boolean isClosing() {
		return CommandStatus.isClosing(command_state);
	}

	/** Test if the gate arm is open */
	private boolean isOpen() {
		return open_limit && CommandStatus.isOpen(command_state);
	}

	/** Test if the gate arm is closed */
	private boolean isClosed() {
		return close_limit && CommandStatus.isClosed(command_state);
	}

	/** Get the maintenance status */
	public String getMaintStatus() {
		OperatorStatus os = operator_state;
		if(OperatorStatus.isFault(os))
			return os.toString();
		else {
			CommandStatus cs = command_state;
			if(CommandStatus.isNormal(cs))
				return "";
			else
				return cs.toString();
		}
	}
}
