/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Speed Advisory Calculator
 *
 * @author Douglas Lau
 */
public class SpeedAdvisoryCalculator {

	/** Location for advisory */
	protected final GeoLoc loc;

	/** Create a new speed advisory calculator */
	public SpeedAdvisoryCalculator(GeoLoc l) {
		loc = l;
	}

	/** Replace speed advisory tags in a MULTI string */
	public String replaceSpeedAdvisory(String multi) {
		MultiString m = new MultiString(multi);
		MultiCallback cb = new MultiCallback();
		m.parse(cb);
		if(cb.valid)
			return cb.toString();
		else
			return null;
	}

	/** MultiString for replacing speed advisory tags */
	protected class MultiCallback extends MultiString {

		protected boolean valid = true;

		/** Add a varisble speed advisory */
		public void addSpeedAdvisory() {
			Integer a = calculateSpeedAdvisory();
			if(a != null)
				addSpan(String.valueOf(a));
			else
				valid = false;
		}
	}

	/** Calculate the speed advisory */
	protected Integer calculateSpeedAdvisory() {
		String c = GeoLocHelper.getCorridorName(loc);
		if(c != null)
			return calculateSpeedAdvisory(c);
		else
			return null;
	}

	/** Calculate the speed advisory */
	protected Integer calculateSpeedAdvisory(String c) {
		Corridor cor = BaseObjectImpl.corridors.getCorridor(c);
		if(cor != null)
			return cor.calculateSpeedAdvisory(loc);
		else
			return null;
	}
}
