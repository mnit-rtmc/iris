/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
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
	static private final int OFF_ZONES = 11;
	static private final int OFF_COMP = 12;
	static private final int OFF_PERIOD = 13;
	static private final int OFF_VOLT = 15;
	static private final int OFF_HEALTH = 16;

	/** Convert KPH to MPH */
	static private int kphToMph(int kph) {
		return (int)Math.round(.621371192 * (double)kph);
	}

	/** Status flags for statistical data header */
	static private final int STAT_FLAG_MPH = 1 << 0;
	static private final int STAT_FLAG_CLOSURE = 1 << 1;
	static private final int STAT_FLAG_STAMP = 1 << 2;
	static private final int STAT_FLAG_MEMORY = 1 << 3;
	static private final int STAT_FLAG_HIGH_Z = 1 << 4;
	static private final int STAT_FLAG_6_FT = 1 << 5;
	static private final int STAT_FLAG_DUAL_LOOP = 1 << 6;
	static private final int STAT_FLAG_FIFO = 1 << 7;

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
	private final int[][] vol_class = new int[MAX_LANES][VehicleClass.size];

	/** Get the volume for the specified vehicle class.
	 * @param vcls Vehicle class. */
	public int[] getVolClass(VehicleClass vcls) {
		if(vcls != VehicleClass.SMALL) {
			int[] vol = new int[MAX_LANES];
			for(int i = 0; i < MAX_LANES; i++)
				vol[i] = vol_class[i][vcls.ordinal()];
			return vol;
		} else
			return getSmallClass();
	}

	/** Get the volume for SMALL vehicle class.  This is the extra volume
	 * not included in vehicle classes 1 through 5. */
	private int[] getSmallClass() {
		int[] vol = new int[MAX_LANES];
		for(int i = 0; i < MAX_LANES; i++) {
			int v = volume[i];
			for(int j = 1; j < VehicleClass.size; j++) {
				int vc = vol_class[i][j];
				if(vc > 0)
					v -= vc;
			}
			if(v >= 0)
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
		for(int i = 0; i < MAX_LANES; i++) {
			volume[i] = MISSING_DATA;
			scans[i] = MISSING_DATA;
			speed[i] = MISSING_DATA;
			gap[i] = MISSING_DATA;
			headway[i] = MISSING_DATA;
			speed85[i] = MISSING_DATA;
			for(int j = 0; j < VehicleClass.size; j++)
				vol_class[i][j] = MISSING_DATA;
		}
	}

	/** Encode a QUERY request */
	public void encodeQuery(OutputStream os, int drop) throws IOException {
		byte[] data = new byte[1];
		data[0] = 0;	// reserved for future use
		os.write(formatRequest(QualCode.STAT_POLL, drop, data));
	}

	/** Message number (0 - 255) */
	private int msg_num = MISSING_DATA;

	/** Memory page where data is stored */
	private int page_num = MISSING_DATA;

	/** Status flags */
	private int stat_flags = MISSING_DATA;

	/** Test if a status flag is set */
	private boolean isStatFlagSet(int flag) {
		return (stat_flags & flag) == flag;
	}

	/** Low 4 bits are zone count; bit 6 is mounting (0: side-fired,
	 * 1: forward) */
	private int n_zones = MISSING_DATA;

	/** Extra frame composition.  Bit flags, 7:gap, 6:headway, 54310:class,
	 * 2:speed85. */
	private int msg_comp = MISSING_DATA;

	/** Message period (seconds) */
	private int msg_period = MISSING_DATA;

	/** Voltage (tenths of volts) */
	private int volt = MISSING_DATA;

	/** Sensor health (currently always 0x10) */
	private int health = MISSING_DATA;

	/** Flag indicating a footer was received */
	private boolean footer = false;

	/** Decode a QUERY response */
	@Override public void decodeQuery(InputStream is, int drop)
		throws IOException
	{
		while(!footer)
			parseFrame(is, drop);
	}

	/** Parse the data from one frame.
	 * @param qual Qualifier code.
	 * @param data Data packet. */
	@Override protected void parseData(QualCode qual, byte[] data)
		throws IOException
	{
		switch(qual) {
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
			parseC(VehicleClass.REGULAR, data);
			break;
		case C2:
			parseC(VehicleClass.MEDIUM, data);
			break;
		case C3:
			parseC(VehicleClass.LARGE, data);
			break;
		case C4:
			parseC(VehicleClass.TRUCK, data);
			break;
		case C5:
			parseC(VehicleClass.EXTRA_LARGE, data);
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
		if(data.length != 22)
			throw new ParsingException("INVALID HEADER LENGTH");
		msg_num = parse8(data, OFF_MSG_NUM);
		page_num = parse16(data, OFF_PAGE_NUM);
		stat_flags = parse8(data, OFF_STAT_FLAGS);
		// NOTE: time stamp consumes 7 bytes here
		n_zones = parse8(data, OFF_ZONES);
		msg_comp = parse8(data, OFF_COMP);
		msg_period = parse16(data, OFF_PERIOD);
		if(msg_period != period)
			throw new ParsingException("INVALID PERIOD");
		volt = parse8(data, OFF_VOLT);
		health = parse8(data, OFF_HEALTH);
		// NOTE: last 5 bytes are "spare"; for future use
	}

	/** Parse statistical volume data */
	private void parseVolume(byte[] data) throws ParsingException {
		if(data.length != n_zones * 2)
			throw new ParsingException("INVALID VOLUME LENGTH");
		for(int i = 0; i < n_zones; i++) {
			int val = parse16(data, i * 2);
			if(val >= 0 && val <= MAX_VOLUME)
				volume[i] = val;
		}
	}

	/** Parse statistical occupancy data */
	private void parseOccupancy(byte[] data) throws ParsingException {
		if(data.length != n_zones * 2)
			throw new ParsingException("INVALID OCCUPANCY LENGTH");
		for(int i = 0; i < n_zones; i++) {
			int val = parse16(data, i * 2);
			if(val >= 0 && val <= MAX_SCANS)
				scans[i] = val;
		}
	}

	/** Parse statistical speed data */
	private void parseSpeed(byte[] data) throws ParsingException {
		if(data.length != n_zones * 2)
			throw new ParsingException("INVALID SPEED LENGTH");
		for(int i = 0; i < n_zones; i++) {
			int val = parse16(data, i * 2);
			if(val >= 0 && val <= MAX_SPEED)
				speed[i] = asMph(val);
		}
	}

	/** Parse statistical gap data */
	private void parseGap(byte[] data) throws ParsingException {
		if(data.length != n_zones * 2)
			throw new ParsingException("INVALID GAP LENGTH");
		for(int i = 0; i < n_zones; i++) {
			int val = parse16(data, i * 2);
			if(val >= 0 && val <= MAX_GAP)
				gap[i] = val;
		}
	}

	/** Get a speed sample in mph units */
	private int asMph(int val) {
		if(stat_flags < 0)
			return MISSING_DATA;
		if(isStatFlagSet(STAT_FLAG_MPH))
			return val;
		else
			return kphToMph(val);
	}

	/** Parse statistical vehicle class data */
	private void parseC(VehicleClass vcls, byte[] data)
		throws ParsingException
	{
		if(data.length != n_zones * 2)
			throw new ParsingException("INVALID C LENGTH");
		int j = vcls.ordinal();
		for(int i = 0; i < n_zones; i++) {
			int val = parse16(data, i * 2);
			if(val >= 0 && val <= MAX_VOLUME)
				vol_class[i][j] = val;
		}
	}

	/** Parse statistical headway data */
	private void parseHeadway(byte[] data) throws ParsingException {
		if(data.length != n_zones * 2)
			throw new ParsingException("INVALID HEADWAY LENGTH");
		for(int i = 0; i < n_zones; i++) {
			int val = parse16(data, i * 2);
			if(val >= 0 && val <= MAX_HEADWAY)
				headway[i] = val;
		}
	}

	/** Parse statistical speed 85 data */
	private void parseSpeed85(byte[] data) throws ParsingException {
		if(data.length != n_zones * 2)
			throw new ParsingException("INVALID SPEED_85 LENGTH");
		for(int i = 0; i < n_zones; i++) {
			int val = parse16(data, i * 2);
			if(val >= 0 && val <= MAX_SPEED)
				speed85[i] = asMph(val);
		}
	}

	/** Parse statistical footer data */
	private void parseStatFooter(byte[] data) throws ParsingException {
		if(data.length != 1)
			throw new ParsingException("INVALID FOOTER LENGTH");
		if(parse8(data, OFF_MSG_NUM) != msg_num)
			throw new ParsingException("INVALID FOOTER MSG #");
		footer = true;
	}
}
