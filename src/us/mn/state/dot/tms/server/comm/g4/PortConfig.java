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

/**
 * Serial port configuration.
 *
 * @author Douglas Lau
 */
public class PortConfig {

	/** Flags for port configuration */
	static private final int FLAG_X3 = 1 << 15;
	static private final int FLAG_HIGH_OCC = 1 << 14;
	static private final int FLAG_RS4XX = 1 << 13;
	static private final int FLAG_MODE_VEHICLE = 1 << 12;
	static private final int FLAG_MODE_NORMAL = 1 << 11;
	static private final int FLAG_MODE_STAT = 1 << 10;
	static private final int FLAG_MODE_POLLED = 1 << 9;
	static private final int FLAG_RTS_CTS = 1 << 8;

	/** Sensor mode mask */
	static private final int MASK_MODE = FLAG_MODE_VEHICLE |
		FLAG_MODE_NORMAL | FLAG_MODE_STAT | FLAG_MODE_POLLED;

	/** Sensor mode */
	static public enum Mode {
		NORMAL(FLAG_MODE_NORMAL | FLAG_MODE_STAT),
		STAT(FLAG_MODE_STAT),
		POLLED(FLAG_MODE_POLLED),
		PER_VEHICLE(FLAG_MODE_VEHICLE | FLAG_MODE_POLLED),
		SPIDER(0);
		private final int flags;
		private Mode(int f) {
			flags = f;
		}
		static public Mode fromFlags(int f) {
			for(Mode m: Mode.values()) {
				if(m.flags == f)
					return m;
			}
			return NORMAL;
		}
	}

	/** Baud rate mask */
	static private final int MASK_BAUD = 0xFF;

	/** Serial port baud rates */
	static public enum Baud {
		INVALID,	/* 0 */
		B2400,		/* 1 */
		B4800,		/* 2 */
		B9600,		/* 3 */
		B14400,		/* 4 */
		B19200,		/* 5 */
		B38400,		/* 6 */
		B57600,		/* 7 */
		B115200;	/* 8 */
		static public Baud fromOrdinal(int o) {
			for(Baud b: Baud.values()) {
				if(b.ordinal() == o)
					return b;
			}
			return INVALID;
		}
	}

	/** Port number (1 or 2) */
	private final int port;

	/** Get the port number */
	public int getPort() {
		return port;
	}

	/** Configuration code */
	private final int config;

	/** Get the configuration code */
	public int getCode() {
		return config;
	}

	/** Create a new serial port configuration */
	public PortConfig(int p, int c) {
		port = p;
		config = c;
	}

	/** Creat a new serial port configuration */
	public PortConfig(int p, Mode m, boolean rs4xx, boolean rtscts,
		Baud baud)
	{
		port = p;
		int c = FLAG_HIGH_OCC;
		c |= m.flags;
		if(rs4xx)
			c |= FLAG_RS4XX;
		if(rtscts)
			c |= FLAG_RTS_CTS;
		c |= (baud.ordinal() & MASK_BAUD);
		config = c;
	}

	/** Test if a flag is set */
	private boolean isFlagSet(int flag) {
		return (config & flag) == flag;
	}

	/** Test if X3 protocol is set (not G4) */
	public boolean isX3() {
		return isFlagSet(FLAG_X3);
	}

	/** Test if high occupancy is set (always set for G4) */
	public boolean isHighOccupancy() {
		return isFlagSet(FLAG_HIGH_OCC);
	}

	/** Get port type (rs232, rs485 or rs422) */
	public String getPortType() {
		if(isRS4xx()) {
			if(port == 1)
				return "rs485";
			else
				return "rs422";
		} else
			return "rs232";
	}

	/** Test if serial port config is RS4xx */
	public boolean isRS4xx() {
		return isFlagSet(FLAG_RS4XX);
	}

	/** Get the sensor mode */
	public Mode getMode() {
		int f = config & MASK_MODE;
		return Mode.fromFlags(f);
	}

	/** Test if RTS/CTS is enabled */
	public boolean isRTSCTS() {
		return isFlagSet(FLAG_RTS_CTS);
	}

	/** Get the baud rate */
	public Baud getBaudRate() {
		int b = config & MASK_BAUD;
		return Baud.fromOrdinal(b);
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isX3())
			sb.append("X3,");
		else
			sb.append("G4,");
		if (isHighOccupancy())
			sb.append("high_occ,");
		sb.append(getMode());
		sb.append(',');
		sb.append(getPortType());
		sb.append(',');
		if (isRTSCTS())
			sb.append("rts_cts,");
		sb.append(getBaudRate());
		return sb.toString();
	}
}
