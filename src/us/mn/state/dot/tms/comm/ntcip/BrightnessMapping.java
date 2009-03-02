/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.ntcip;

import java.util.Map;
import java.util.TreeMap;

/**
 * A mapping of photocell values to light output values
 *
 * @author Douglas Lau
 */
public class BrightnessMapping {

	/** Mapping of photocell to light output values */
	protected final TreeMap<Integer, Integer> mapping =
		new TreeMap<Integer, Integer>();

	/** Is this a high line? */
	protected final boolean high;

	/** Create a new brightness mapping */
	public BrightnessMapping(boolean h) {
		high = h;
	}

	/** Put an entry into the mapping */
	public void put(int p, int l, boolean swap) {
		if(shouldAdd(p, l, swap)) {
			updateLower(p, l);
			updateHigher(p, l);
			mapping.put(p, l);
		}
	}

	/** Should an entry be added to the mapping? */
	protected boolean shouldAdd(int p, int l, boolean swap) {
		if(high ^ swap)
			return isAbove(p, l);
		else
			return isBelow(p, l);
	}

	/** Check if a point is above the mapping line */
	public boolean isAbove(int p, int l) {
		Integer cl = getLightOutput(p);
		if(cl != null)
			return l > cl;
		else
			return true;
	}

	/** Check if a point is below the mapping line */
	public boolean isBelow(int p, int l) {
		Integer cl = getLightOutput(p);
		if(cl != null)
			return l < cl;
		else
			return true;
	}

	/** Update lower entries if necessary */
	protected void updateLower(int p, int l) {
		Map.Entry<Integer, Integer> floor = mapping.floorEntry(p);
		while(floor != null && floor.getValue() > l) {
			Integer k = floor.getKey();
			mapping.put(k, l);
			floor = mapping.lowerEntry(k);
		}
	}

	/** Update higher entries if necessary */
	protected void updateHigher(int p, int l) {
		Map.Entry<Integer, Integer> ceil = mapping.ceilingEntry(p);
		while(ceil != null && ceil.getValue() < l) {
			Integer k = ceil.getKey();
			mapping.put(k, l);
			ceil = mapping.higherEntry(k);
		}
	}

	/** Get the light output for the given photocell value */
	public Integer getLightOutput(int photo) {
		Map.Entry<Integer, Integer> floor = mapping.floorEntry(photo);
		Map.Entry<Integer, Integer> ceil = mapping.ceilingEntry(photo);
		if(floor == null || ceil == null)
			return null;
		float x0 = floor.getKey();
		float x1 = ceil.getKey();
		float y0 = floor.getValue();
		float y1 = ceil.getValue();
		/* m => slope of line segment */
		float m;
		if(x0 != x1)
			m = (y1 - y0) / (x1 - x0);
		else
			m = 0;
		/* b => y-intercept of line segment */
		float b = y0 - x0 * m;
		return Math.round(m * photo + b);
	}

	/** Get the photocell value for the given light output */
	public Integer getPhotocell(int light) {
		Map.Entry<Integer, Integer> before = findBeforeEntry(light);
		if(before == null)
			return null;
		if(before.getValue() >= light)
			return before.getKey();
		Map.Entry<Integer, Integer> after =
			mapping.higherEntry(before.getKey());
		if(after.getValue() <= light)
			return after.getKey();
		float x0 = before.getKey();
		float x1 = after.getKey();
		float y0 = before.getValue();
		float y1 = after.getValue();
		/* m => slope of line segment */
		float m;
		if(x0 != x1)
			m = (y1 - y0) / (x1 - x0);
		else
			return before.getKey();
		/* b => y-intercept of line segment */
		float b = y0 - x0 * m;
		/* x = (y - b) / m */
		return Math.round((light - b) / m);
	}

	/** Get the last entry before the given light output value */
	protected Map.Entry<Integer, Integer> findBeforeEntry(int light) {
		Map.Entry<Integer, Integer> before = null;
		for(Map.Entry<Integer, Integer> entry: mapping.entrySet()) {
			if(entry.getValue() <= light)
				before = entry;
			else {
				if(before == null)
					before = entry;
				break;
			}
		}
		return before;
	}
}
