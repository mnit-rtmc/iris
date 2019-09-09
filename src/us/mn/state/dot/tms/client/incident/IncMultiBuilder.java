/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * MULTI string builder for incidents.
 *
 * @author Douglas Lau
 */
public class IncMultiBuilder {

	/** Sign to deploy */
	private final DMS dms;

	/** Location of incident */
	private final GeoLoc loc;

	/** Distance upstream of incident */
	private final Distance dist;

	/** Location MULTI builder */
	private final LocMultiBuilder builder;

	/** Create a new incident MULTI builder */
	public IncMultiBuilder(DMS s, GeoLoc l, Distance d) {
		dms = s;
		loc = l;
		dist = d;
		builder = new LocMultiBuilder(loc, d);
	}

	/** Add a line to MULTI string */
	public boolean addLine(String multi, String abbrev) {
		String ms = checkMulti(multi, abbrev);
		if (ms != null) {
			if (builder.toString().length() > 0)
				builder.addLine(null);
			new MultiString(ms).parse(builder);
			return true;
		} else
			return false;
	}

	/** Check if MULTI string or abbreviation will fit */
	private String checkMulti(String multi, String abbrev) {
		return checkMulti(multi)
		      ? multi
		      : (checkMulti(abbrev) ? abbrev : null);
	}

	/** Check if MULTI string will fit on a DMS */
	private boolean checkMulti(String multi) {
		if (multi != null) {
			LocMultiBuilder lmb = new LocMultiBuilder(loc, dist);
			new MultiString(multi).parse(lmb);
			MultiString ms = lmb.toMultiString();
			return (DMSHelper.createPageOne(dms, ms) != null);
		} else
			return false;
	}

	/** Get the MULTI string */
	public MultiString toMultiString() {
		return builder.toMultiString();
	}
}
