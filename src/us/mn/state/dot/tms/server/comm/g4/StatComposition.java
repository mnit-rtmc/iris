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
package us.mn.state.dot.tms.server.comm.g4;

/**
 * Code defining composition of a statistical message.
 *
 * @author Douglas Lau
 */
public class StatComposition {

	/** Flags for extra frames in statistical message */
	static private final int FLAG_GAP = 1 << 7;
	static private final int FLAG_HEADWAY = 1 << 6;
	static private final int FLAG_SPEED85 = 1 << 2;

	/** Vehicle class is determined by bits 5, 4, 3, 1 and 0 */
	static private final int MASK_CLASS = (1 << 5) | (1 << 4) | (1 << 3) |
		(1 << 1) | (1 << 0);

	/** Bit pattern for 2 vehicle classes */
	static private final int CLASSES_2 = (1 << 3);

	/** Bit pattern for 4 vehicle classes */
	static private final int CLASSES_4 = (1 << 5) | (1 << 4) | (1 << 3);

	/** Bit pattern for 6 vehicle classes */
	static private final int CLASSES_6 = MASK_CLASS;

	/** Statistical message composition code */
	private final int comp;

	/** Create a new statistical composition code */
	public StatComposition(int c) {
		comp = c;
	}

	/** Test if a flag is set */
	private boolean isFlagSet(int flag) {
		return (comp & flag) == flag;
	}

	/** Test if a statistical message contains a GAP frame */
	public boolean hasGap() {
		return isFlagSet(FLAG_GAP);
	}

	/** Test if a statistical message contains a HEADWAY frame */
	public boolean hasHeadway() {
		return isFlagSet(FLAG_HEADWAY);
	}

	/** Test if a statistical message contains a SPEED85 frame */
	public boolean hasSpeed85() {
		return isFlagSet(FLAG_SPEED85);
	}

	/** Get the count of vehicle classes */
	public int getClassCount() {
		int c = comp & MASK_CLASS;
		switch(c) {
		case CLASSES_2:
			return 2;
		case CLASSES_4:
			return 4;
		case CLASSES_6:
			return 6;
		default:
			return 0;
		}
	}

	/** Get a string representation */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(hasGap())
			sb.append("gap,");
		if(hasHeadway())
			sb.append("headway,");
		if(hasSpeed85())
			sb.append("speed85,");
		sb.append(getClassCount());
		sb.append("_classes");
		return sb.toString();
	}
}
