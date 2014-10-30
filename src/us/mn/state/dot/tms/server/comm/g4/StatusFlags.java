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
 * Sensor status flags.
 *
 * @author Douglas Lau
 */
public class StatusFlags {

	/** Status flag bits */
	static private final int FLAG_FIFO = 1 << 7;
	static private final int FLAG_DUAL_LOOP = 1 << 6;
	static private final int FLAG_6_FT = 1 << 5;
	static private final int FLAG_HIGH_Z = 1 << 4;
	static private final int FLAG_MEMORY = 1 << 3;
	static private final int FLAG_STAMP = 1 << 2;
	static private final int FLAG_CLOSURE = 1 << 1;
	static private final int FLAG_MPH = 1 << 0;

	/** Status flags */
	private final int flags;

	/** Get the status flags */
	public int getFlags() {
		return flags;
	}

	/** Create status flags */
	public StatusFlags(int f) {
		flags = f;
	}

	/** Create status flags */
	public StatusFlags(boolean f, boolean dl, boolean sf, boolean z,
		boolean m, boolean ts, boolean c, boolean mph)
	{
		int fs = 0;
		if (f)
			fs |= FLAG_FIFO;
		if (dl)
			fs |= FLAG_DUAL_LOOP;
		if (sf)
			fs |= FLAG_6_FT;
		if (z)
			fs |= FLAG_HIGH_Z;
		if (m)
			fs |= FLAG_MEMORY;
		if (ts)
			fs |= FLAG_STAMP;
		if (c)
			fs |= FLAG_CLOSURE;
		if (mph)
			fs |= FLAG_MPH;
		flags = fs;
	}

	/** Test if a flag is set */
	private boolean isFlagSet(int f) {
		return (flags & f) == f;
	}

	/** Test if FIFO flag is set */
	public boolean isFifo() {
		return isFlagSet(FLAG_FIFO);
	}

	/** Test if dual loop flag is set */
	public boolean isDualLoop() {
		return isFlagSet(FLAG_DUAL_LOOP);
	}

	/** Test if six foot emulation flag is set */
	public boolean isSixFoot() {
		return isFlagSet(FLAG_6_FT);
	}

	/** Test if high Z flag is set */
	public boolean isHighZ() {
		return isFlagSet(FLAG_HIGH_Z);
	}

	/** Test if memory flag is set */
	public boolean isMemory() {
		return isFlagSet(FLAG_MEMORY);
	}

	/** Test if time stamp flag is set */
	public boolean isStamp() {
		return isFlagSet(FLAG_STAMP);
	}

	/** Test if contact closure flag is set */
	public boolean isClosure() {
		return isFlagSet(FLAG_CLOSURE);
	}

	/** Test if mph flag is set */
	public boolean isMph() {
		return isFlagSet(FLAG_MPH);
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isFifo())
			sb.append("fifo,");
		if (isDualLoop())
			sb.append("2loop,");
		if (isSixFoot())
			sb.append("6ft,");
		if (isHighZ())
			sb.append("hiZ,");
		if (isMemory())
			sb.append("mem,");
		if (isStamp())
			sb.append("stamp,");
		if (isClosure())
			sb.append("closure,");
		if (isMph())
			sb.append("mph,");
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
