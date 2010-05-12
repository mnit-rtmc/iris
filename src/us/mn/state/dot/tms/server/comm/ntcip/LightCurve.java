/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.util.Map;
import java.util.TreeMap;

/**
 * A curve of light output to photocell samples.
 *
 * @author Douglas Lau
 */
public class LightCurve {

	/** Mapping of photocell to light output values */
	protected final TreeMap<Integer, Integer> mapping =
		new TreeMap<Integer, Integer>();

	/** Put an entry into the mapping */
	public void put(int p, int l) {
		updateLower(p, l);
		updateHigher(p, l);
		mapping.put(p, l);
	}

	/** Update lower entries if necessary.  This prevents the curve from
	 * having a negative slope. */
	protected void updateLower(int p, int l) {
		Map.Entry<Integer, Integer> floor = mapping.floorEntry(l);
		while(floor != null && floor.getValue() > l) {
			Integer k = floor.getKey();
			mapping.put(k, l);
			floor = mapping.lowerEntry(k);
		}
	}

	/** Update higher entries if necessary.  This prevents the curve from
	 * having a negative slope. */
	protected void updateHigher(int p, int l) {
		Map.Entry<Integer, Integer> ceil = mapping.ceilingEntry(l);
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
		if(x1 > x0)
			m = (y1 - y0) / (x1 - x0);
		else
			m = 0;
		/* b => y-intercept of line segment */
		float b = y0 - x0 * m;
		return Math.round(m * photo + b);
	}
}
