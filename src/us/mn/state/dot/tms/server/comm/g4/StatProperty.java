/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2014  Minnesota Department of Transportation
 * Copyright (C) 2012  Iteris Inc.
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
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * A statistical property encapsulates traffic data for one binning interval.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class StatProperty extends G4Property {

	/** Maximum number of lanes */
	static private final int MAX_LANES = 12;

	/** Maximum volume for one statistical data sample */
	static private final int MAX_VOLUME = 0xFE00;

	/** Maximum number of scans for one statistical sample */
	static protected final int MAX_SCANS = 1000;

	/** Maximum speed for one statistical sample (kph or mph) */
	static private final int MAX_SPEED = 200;

	/** Maximum gap for one statistical sample (tenths of second) */
	static private final int MAX_GAP = 36000;

	/** Maximum headway for one statistical sample (tenths of second) */
	static private final int MAX_HEADWAY = MAX_GAP;

	/** Byte offsets from beginning of stat header data */
	static private final int OFF_MSG_NUM = 0;
	static private final int OFF_PAGE_NUM = 1;
	static private final int OFF_STAT_FLAGS = 3;
	static private final int OFF_STAMP = 4;
	static private final int OFF_ZONES = 11;
	static private final int OFF_COMP = 12;
	static private final int OFF_PERIOD = 13;
	static private final int OFF_VOLT = 15;
	static private final int OFF_HEALTH = 16;

	/** Convert KPH to MPH */
	static private int kphToMph(int kph) {
		return (int)Math.round(.621371192 * (double)kph);
	}

	/** Binning period (seconds) */
	private final int period;

	/** Volume data */
	private final int[] volume = new int[MAX_LANES];

	/** Get the volume for all lanes */
	public int[] getVolume() {
		return volume;
	}

	/** Scan data (0 - 1000) */
	private final int[] scans = new int[MAX_LANES];

	/** Get the scans for all lanes */
	public int[] getScans() {
		return scans;
	}

	/** Speed data (MPH) */
	private final int[] speed = new int[MAX_LANES];

	/** Get the speeds for all lanes */
	public int[] getSpeed() {
		return speed;
	}

	/** Gap data (tenths of second) */
	private final int[] gap = new int[MAX_LANES];

	/** Get the gap data for all lanes (tenths of second) */
	public int[] getGap() {
		return gap;
	}

	/** Headway data (tenths of second) */
	private final int[] headway = new int[MAX_LANES];

	/** Get the headway data for all lanes */
	public int[] getHeadway() {
		return headway;
	}

	/** Speed data 85% (MPH) */
	private final int[] speed85 = new int[MAX_LANES];

	/** Get the 85% speeds for all lanes.  This indicates 85% of vehicles
	 * are at or below this speed (mph). */
	public int[] getSpeed85() {
		return speed85;
	}

	/** Volume data for class (regular vehicles) */
	private final int[][] vol_class = new int[MAX_LANES][G4VehClass.size];

	/** Get the volume for the specified vehicle class.
	 * @param vcls Vehicle class. */
	public int[] getVolume(G4VehClass vcls) {
		if (vcls != G4VehClass.SMALL) {
			int[] vol = new int[MAX_LANES];
			for (int i = 0; i < MAX_LANES; i++)
				vol[i] = vol_class[i][vcls.ordinal()];
			return vol;
		} else
			return getSmallClass();
	}

	/** Get the volume for SMALL vehicle class.  This is the extra volume
	 * not included in vehicle classes 1 through 5. */
	private int[] getSmallClass() {
		int[] vol = new int[MAX_LANES];
		for (int i = 0; i < MAX_LANES; i++) {
			int v = volume[i];
			for (int j = 1; j < G4VehClass.size; j++) {
				int vc = vol_class[i][j];
				if (vc > 0)
					v -= vc;
			}
			if (v >= 0)
				vol[i] = v;
			else
				vol[i] = MISSING_DATA;
		}
		return vol;
	}

	/** Create a new statistical property.
	 * @param p Binning period (seconds). */
	public StatProperty(int p) {
		period = p;
		for (int i = 0; i < MAX_LANES; i++) {
			volume[i] = MISSING_DATA;
			scans[i] = MISSING_DATA;
			speed[i] = MISSING_DATA;
			gap[i] = MISSING_DATA;
			headway[i] = MISSING_DATA;
			speed85[i] = MISSING_DATA;
			for (int j = 0; j < G4VehClass.size; j++)
				vol_class[i][j] = MISSING_DATA;
		}
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] data = new byte[1];
		data[0] = 0;	// reserved for future use
		os.write(formatRequest(QualCode.STAT_POLL, c.getDrop(), data));
	}

	/** Message number (0 - 255) */
	private int msg_num = MISSING_DATA;

	/** Memory page where data is stored */
	private int page_num = MISSING_DATA;

	/** Status flags */
	private StatusFlags stat_flags = new StatusFlags(0);

	/** Time stamp */
	private long stamp = TimeSteward.currentTimeMillis();

	/** Time stamp */
	public long getStamp() {
		return stamp;
	}

	/** Low 4 bits are zone count; bit 6 is mounting (0: side-fired,
	 * 1: forward) */
	private int n_zones = MISSING_DATA;

	/** Get the zone count */
	public int getZones() {
		return n_zones & 0x0F;
	}

	/** Extra frame composition */
	private StatComposition msg_comp = new StatComposition(0);

	/** Message period (seconds) */
	private int msg_period = MISSING_DATA;

	/** Voltage (tenths of volts) */
	private int volt = MISSING_DATA;

	/** Sensor health (currently always 0x10) */
	private int health = MISSING_DATA;

	/** Flag indicating a footer was received */
	private boolean footer = false;

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		int drop = c.getDrop();
		while (!footer)
			parseFrame(is, drop);
	}

	/** Parse the data from one frame.
	 * @param qual Qualifier code.
	 * @param data Data packet. */
	@Override protected void parseData(QualCode qual, byte[] data)
		throws IOException
	{
		switch (qual) {
		case STAT_HEADER:
			parseStatHeader(data);
			break;
		case VOLUME:
			parseVolume(data);
			break;
		case OCCUPANCY:
			parseOccupancy(data);
			break;
		case SPEED:
			parseSpeed(data);
			break;
		case GAP:
			parseGap(data);
			break;
		case C1:
			parseC(G4VehClass.REGULAR, data);
			break;
		case C2:
			parseC(G4VehClass.MEDIUM, data);
			break;
		case C3:
			parseC(G4VehClass.LARGE, data);
			break;
		case C4:
			parseC(G4VehClass.TRUCK, data);
			break;
		case C5:
			parseC(G4VehClass.EXTRA_LARGE, data);
			break;
		case HEADWAY:
			parseHeadway(data);
			break;
		case SPEED_85:
			parseSpeed85(data);
			break;
		case STAT_FOOTER:
			parseStatFooter(data);
			break;
		default:
			super.parseData(qual, data);
		}
	}

	/** Parse statistical header data */
	private void parseStatHeader(byte[] data) throws ParsingException {
		if (data.length != 22)
			throw new ParsingException("INVALID HEADER LENGTH");
		msg_num = parse8(data, OFF_MSG_NUM);
		page_num = parse16(data, OFF_PAGE_NUM);
		stat_flags = new StatusFlags(parse8(data, OFF_STAT_FLAGS));
		stamp = parseStamp(data, OFF_STAMP);
		n_zones = parse8(data, OFF_ZONES);
		msg_comp = new StatComposition(parse8(data, OFF_COMP));
		if (msg_comp.getClassCount() == 0)
			throw new ParsingException("INVALID COMP");
		msg_period = parse16(data, OFF_PERIOD);
		if (msg_period != period)
			throw new ParsingException("INVALID PERIOD");
		volt = parse8(data, OFF_VOLT);
		health = parse8(data, OFF_HEALTH);
		// NOTE: last 5 bytes are "spare"; for future use
	}

	/** Parse statistical volume data */
	private void parseVolume(byte[] data) throws ParsingException {
		if (data.length != getZones() * 2)
			throw new ParsingException("INVALID VOLUME LENGTH");
		for (int i = 0; i < getZones(); i++) {
			int val = parse16(data, i * 2);
			if (val >= 0 && val <= MAX_VOLUME)
				volume[i] = val;
		}
	}

	/** Parse statistical occupancy data */
	private void parseOccupancy(byte[] data) throws ParsingException {
		if (data.length != getZones() * 2)
			throw new ParsingException("INVALID OCCUPANCY LENGTH");
		for (int i = 0; i < getZones(); i++) {
			int val = parse16(data, i * 2);
			if (val >= 0 && val <= MAX_SCANS)
				scans[i] = val;
		}
	}

	/** Parse statistical speed data */
	private void parseSpeed(byte[] data) throws ParsingException {
		if (data.length != getZones() * 2)
			throw new ParsingException("INVALID SPEED LENGTH");
		for (int i = 0; i < getZones(); i++) {
			int val = parse16(data, i * 2);
			if (val >= 0 && val <= MAX_SPEED)
				speed[i] = asMph(val);
		}
	}

	/** Parse statistical gap data */
	private void parseGap(byte[] data) throws ParsingException {
		if (data.length != getZones() * 2)
			throw new ParsingException("INVALID GAP LENGTH");
		for (int i = 0; i < getZones(); i++) {
			int val = parse16(data, i * 2);
			if (val >= 0 && val <= MAX_GAP)
				gap[i] = val;
		}
	}

	/** Get a speed sample in mph units */
	private int asMph(int val) {
		if (stat_flags.isMph())
			return val;
		else
			return kphToMph(val);
	}

	/** Parse statistical vehicle class data */
	private void parseC(G4VehClass vcls, byte[] data)
		throws ParsingException
	{
		if (data.length != getZones() * 2)
			throw new ParsingException("INVALID C LENGTH");
		int j = vcls.ordinal();
		for (int i = 0; i < getZones(); i++) {
			int val = parse16(data, i * 2);
			if (val >= 0 && val <= MAX_VOLUME)
				vol_class[i][j] = val;
		}
	}

	/** Parse statistical headway data */
	private void parseHeadway(byte[] data) throws ParsingException {
		if (data.length != getZones() * 2)
			throw new ParsingException("INVALID HEADWAY LENGTH");
		for (int i = 0; i < getZones(); i++) {
			int val = parse16(data, i * 2);
			if (val >= 0 && val <= MAX_HEADWAY)
				headway[i] = val;
		}
	}

	/** Parse statistical speed 85 data */
	private void parseSpeed85(byte[] data) throws ParsingException {
		if (data.length != getZones() * 2)
			throw new ParsingException("INVALID SPEED_85 LENGTH");
		for (int i = 0; i < getZones(); i++) {
			int val = parse16(data, i * 2);
			if (val >= 0 && val <= MAX_SPEED)
				speed85[i] = asMph(val);
		}
	}

	/** Parse statistical footer data */
	private void parseStatFooter(byte[] data) throws ParsingException {
		if (data.length != 1)
			throw new ParsingException("INVALID FOOTER LENGTH");
		if (parse8(data, OFF_MSG_NUM) != msg_num)
			throw new ParsingException("INVALID FOOTER MSG #");
		footer = true;
	}

	/** Get a string representation of the statistical property */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("msg#:");
		sb.append(msg_num);
		sb.append(" flags:");
		sb.append(stat_flags);
		sb.append(" ");
		sb.append(new Date(stamp));
		sb.append(" zones:");
		sb.append(getZones());
		sb.append(" comp:");
		sb.append(msg_comp);
		sb.append(" period:");
		sb.append(msg_period);
		sb.append(" volts:");
		sb.append(volt);
		sb.append(' ');
		sb.append(arrayStr("volume", volume));
		sb.append(arrayStr("scans", scans));
		sb.append(arrayStr("speed", speed));
		sb.append(arrayStr("gap", gap));
		sb.append(arrayStr("headway", headway));
		sb.append(arrayStr("speed85", speed85));
		sb.append(arrayStr("c1", getVolume(G4VehClass.REGULAR)));
		sb.append(arrayStr("c2", getVolume(G4VehClass.MEDIUM)));
		sb.append(arrayStr("c3", getVolume(G4VehClass.LARGE)));
		sb.append(arrayStr("c4", getVolume(G4VehClass.TRUCK)));
		sb.append(arrayStr("c5", getVolume(G4VehClass.EXTRA_LARGE)));
		return sb.toString().trim();
	}

	/** Get a string representation of a data array */
	static private String arrayStr(String lbl, int[] a) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < MAX_LANES; i++) {
			if (a[i] != MISSING_DATA)
				sb.append(a[i]);
			sb.append(',');
		}
		while (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',')
			sb.deleteCharAt(sb.length() - 1);
		if (sb.length() > 0)
			return lbl + ':' + sb.toString() + ' ';
		else
			return "";
	}
}
