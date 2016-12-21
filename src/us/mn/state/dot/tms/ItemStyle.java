/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.util.HashMap;
import java.util.LinkedList;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Item style enumeration.
 *
 * @author Douglas Lau
 */
public enum ItemStyle {
	/* Generic item styles */
	ALL,
	ACTIVE,
	/* Generic device styles */
	AVAILABLE,
	DEPLOYED,
	MAINTENANCE,
	FAILED,
	INACTIVE,
	NO_CONTROLLER,
	HIDDEN,
	/* Incident styles */
	CLEARED,
	CRASH,
	STALL,
	ROADWORK,
	HAZARD,
	UNCONFIRMED,
	/* DMS styles */
	SCHEDULED,
	AWS_CONTROLLED,
	AWS_DEPLOYED,
	/* Camera styles */
	PLAYLIST,
	UNPUBLISHED,
	/* Ramp meter styles */
	LOCKED,
	METERING,
	QUEUE_EXISTS,
	QUEUE_FULL,
	/* Gate arm styles */
	CLOSED,
	OPEN,
	MOVING,
	/* Plan styles */
	DMS,
	BEACON,
	LANE,
	METER,
	TIME;

	/** Get a string representation of the item style */
	@Override
	public String toString() {
		return I18N.get("item.style." +
			name().toLowerCase().replace('_', '.'));
	}

	/** Get the bit for a style */
	public long bit() {
		return 1L << ordinal();
	}

	/** Check if the style bit is set */
	public boolean checkBit(long bits) {
		return (bits & bit()) != 0;
	}

	/** Hash map of all styles */
	static private final HashMap<String, ItemStyle> ALL_STYLES =
		new HashMap<String, ItemStyle>();

	/** Initialize hash map of all styles */
	static {
		for(ItemStyle is: ItemStyle.values())
			ALL_STYLES.put(is.toString(), is);
	}

	/** Lookup a item style from a string description */
	static public ItemStyle lookupStyle(String style) {
		return ALL_STYLES.get(style);
	}

	/** Get an array of item styles from a bit set */
	static public ItemStyle[] toStyles(long bits) {
		LinkedList<ItemStyle> styles = new LinkedList<ItemStyle>();
		for(ItemStyle is: ItemStyle.values()) {
			if(is.checkBit(bits))
				styles.add(is);
		}
		return styles.toArray(new ItemStyle[0]);
	}

	/** Get the bits for a set of styles */
	static public long toBits(ItemStyle... styles) {
		long bits = 0;
		for(ItemStyle is: styles)
			bits |= is.bit();
		return bits;
	}
}
