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
import us.mn.state.dot.tms.utils.MultiBuilder;
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
	private final MultiBuilder builder;

	/** Total lines on DMS */
	private final int max_lines;

	/** Line count */
	private int n_lines;

	/** Create a new incident MULTI builder */
	public IncMultiBuilder(DMS s, GeoLoc l, Distance d) {
		dms = s;
		loc = l;
		dist = d;
		builder = new MultiBuilder();
		max_lines = DMSHelper.getLineCount(dms);
		n_lines = 0;
	}

	/** Add a line to MULTI string */
	public boolean addLine(String multi, String abbrev) {
		MultiString ms = buildMulti(multi, abbrev);
		if (ms != null) {
			if (builder.toString().length() > 0) {
				if (n_lines >= max_lines) {
					builder.addPage();
					n_lines = 0;
				} else
					builder.addLine(null);
			}
			n_lines++;
			ms.parse(builder);
			return true;
		} else
			return false;
	}

	/** Build MULTI string, or abbreviation if necessary */
	private MultiString buildMulti(String multi, String abbrev) {
		MultiString res = buildMulti(multi);
		return (res != null) ? res : buildMulti(abbrev);
	}

	/** Build MULTI string, replacing [loc] tags */
	private MultiString buildMulti(String multi) {
		if (multi != null) {
			// First try to retain affixes, but strip if necessary
			MultiString ms = checkMulti(multi, true);
			return (ms != null) ? ms : checkMulti(multi, false);
		} else
			return null;
	}

	/** Check if a MULTI string fits on the DMS */
	private MultiString checkMulti(String multi, boolean retain_affixes) {
		LocMultiBuilder lmb = new LocMultiBuilder(loc, dist,
			retain_affixes);
		new MultiString(multi).parse(lmb);
		MultiString ms = lmb.toMultiString();
		return (DMSHelper.createPageOne(dms, ms) != null)
		      ? ms
		      : null;
	}

	/** Get the MULTI string */
	public MultiString toMultiString() {
		return builder.toMultiString();
	}
}
