/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.g4;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Setup information property contains configuration options.
 *
 * @author Douglas Lau
 */
public class SetupInfoProperty extends G4Property {

	/** Byte offsets from beginning of information data */
	static private final int OFF_NEW_ID = 0;
	static private final int OFF_ZONES = 2;
	static private final int OFF_MSG_PERIOD = 3;
	static private final int OFF_SENSITIVITY = 5;
	static private final int OFF_COMP = 6;
	static private final int OFF_PORT_1 = 7;
	static private final int OFF_PORT_2 = 9;
	static private final int OFF_STAT_FLAGS = 11;
	static private final int OFF_DATE = 12;

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] data = new byte[0];
		os.write(formatRequest(QualCode.SETUP_QUERY, c.getDrop(),data));
	}

	/** Decode a QUERY response */
	@Override public void decodeQuery(InputStream is, int drop)
		throws IOException
	{
		parseFrame(is, drop);
	}

	/** Parse the data from one frame.
	 * @param qual Qualifier code.
	 * @param data Data packet. */
	@Override protected void parseData(QualCode qual, byte[] data)
		throws IOException
	{
		switch(qual) {
		case SETUP:
			parseSetup(data);
			break;
		default:
			super.parseData(qual, data);
		}
	}

	/** Encode a STORE request */
	@Override public void encodeStore(OutputStream os, int drop)
		throws IOException
	{
		byte[] data = new byte[15];
		format16(data, OFF_NEW_ID, drop);
		format8(data, OFF_ZONES, n_zones);
		format16(data, OFF_MSG_PERIOD, msg_period);
		format8(data, OFF_SENSITIVITY, sensitivity);
		format8(data, OFF_COMP, msg_comp.getCode());
		format16(data, OFF_PORT_1, port_1.getCode());
		format16(data, OFF_PORT_2, port_2.getCode());
		format8(data, OFF_STAT_FLAGS, stat_flags.getFlags());
		formatDate(data, OFF_DATE, date);
		os.write(formatRequest(QualCode.SETUP, drop, data));
	}

	/** Format a configuration date */
	private void formatDate(byte[] data, int pos, long d) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(d);
		format8(data, pos, cal.get(Calendar.DATE));
		format8(data, pos + 1, cal.get(Calendar.MONTH) + 1);
		format8(data, pos + 2, cal.get(Calendar.YEAR) % 100);
	}

	/** Decode a STORE response */
	@Override public void decodeStore(InputStream is, int drop)
		throws IOException
	{
		parseFrame(is, drop);
	}

	/** New RTMS sensor ID */
	private int new_id;

	/** Number of zones */
	private int n_zones;

	/** Message period (seconds) */
	private int msg_period;

	/** Get the message period (seconds) */
	public int getPeriod() {
		return msg_period;
	}

	/** Set the message period (seconds) */
	public void setPeriod(int p) {
		msg_period = p;
	}

	/** Sensitivity setting */
	private int sensitivity;

	/** Extra frame composition */
	private StatComposition msg_comp;

	/** Get the extra frame composition */
	public StatComposition getComp() {
		return msg_comp;
	}

	/** Set the extra frame composition */
	public void setComp(StatComposition c) {
		msg_comp = c;
	}

	/** Serial port 1 config */
	private PortConfig port_1;

	/** Get the config for serial port 1 */
	public PortConfig getPort1() {
		return port_1;
	}

	/** Set the config for serial port 1 */
	public void setPort1(PortConfig pc) {
		port_1 = pc;
	}

	/** Serial port 2 config */
	private PortConfig port_2;

	/** Get the config for serial port 2 */
	public PortConfig getPort2() {
		return port_2;
	}

	/** Set the config for serial port 2 */
	public void setPort2(PortConfig pc) {
		port_2 = pc;
	}

	/** Status flags */
	private StatusFlags stat_flags;

	/** Get the status flags */
	public StatusFlags getStatusFlags() {
		return stat_flags;
	}

	/** Set the status flags */
	public void setStatusFlags(StatusFlags f) {
		stat_flags = f;
	}

	/** Configuration date */
	private long date;

	/** Set the configuration date */
	public void setDate(long d) {
		date = d;
	}

	/** Parse setup information data */
	private void parseSetup(byte[] data) throws ParsingException {
		if(data.length != 15)
			throw new ParsingException("INVALID SETUP LENGTH");
		new_id = parse16(data, OFF_NEW_ID);
		n_zones = parse8(data, OFF_ZONES);
		msg_period = parse16(data, OFF_MSG_PERIOD);
		sensitivity = parse8(data, OFF_SENSITIVITY);
		msg_comp = new StatComposition(parse8(data, OFF_COMP));
		if(msg_comp.getClassCount() == 0)
			throw new ParsingException("INVALID COMP");
		port_1 = new PortConfig(1, parse16(data, OFF_PORT_1));
		port_2 = new PortConfig(2, parse16(data, OFF_PORT_2));
		stat_flags = new StatusFlags(parse8(data, OFF_STAT_FLAGS));
		date = parseDate(data, OFF_DATE);
	}

	/** Parse configuration date */
	private long parseDate(byte[] data, int pos) {
		int day = parse8(data, pos);
		int month = parse8(data, pos + 1);
		int year = 2000 + parse8(data, pos + 2);
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}

	/** Get a string representation of the statistical property */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id:");
		sb.append(new_id);
		sb.append(" zones:");
		sb.append(n_zones);
		sb.append(" period:");
		sb.append(msg_period);
		sb.append(" sen:");
		sb.append(sensitivity);
		sb.append(" comp:");
		sb.append(msg_comp);
		sb.append(" port_1:");
		sb.append(port_1);
		sb.append(" port_2:");
		sb.append(port_2);
		sb.append(" flags:");
		sb.append(stat_flags);
		sb.append(" date:");
		sb.append(TimeSteward.dateShortString(date));
		return sb.toString();
	}
}
