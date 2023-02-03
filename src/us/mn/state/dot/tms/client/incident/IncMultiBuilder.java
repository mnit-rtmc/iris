/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2023  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.SignConfigHelper;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.TextRect;

/**
 * MULTI string builder for incidents.
 *
 * @author Douglas Lau
 */
public class IncMultiBuilder {

	/** Text rectangle */
	private final TextRect tr;

	/** Location of incident */
	private final GeoLoc loc;

	/** Distance upstream of incident */
	private final Distance dist;

	/** Location MULTI builder */
	private final MultiBuilder builder;

	/** Total lines on text rectangle */
	private final int max_lines;

	/** Line count */
	private int n_lines;

	/** Create a new incident MULTI builder */
	public IncMultiBuilder(DMS dms, GeoLoc l, Distance d) {
		// FIXME: find best pattern, then use first fillable rectangle
		tr = SignConfigHelper.textRect(dms.getSignConfig());
		loc = l;
		dist = d;
		builder = new MultiBuilder();
		max_lines = (tr != null) ? tr.getLineCount() : 0;
		n_lines = 0;
	}

	/** Add a line to MULTI string */
	public boolean addLine(String multi) {
		MultiString ms = buildMulti(multi);
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

	/** Build MULTI string, replacing [loc] tags */
	private MultiString buildMulti(String multi) {
		if (tr != null && multi != null) {
			// First try to retain affixes, but strip if necessary
			MultiString ms = checkLine(multi, true);
			return (ms != null) ? ms : checkLine(multi, false);
		} else
			return null;
	}

	/** Check if a MULTI line fits on the text rectangle */
	private MultiString checkLine(String ms, boolean retain_affixes) {
		LocMultiBuilder lmb = new LocMultiBuilder(loc, dist,
			retain_affixes);
		new MultiString(ms).parse(lmb);
		// Don't try abbreviating if we're retaining affixes
		String multi = tr.checkLine(lmb.toString(), !retain_affixes);
		return (multi != null) ? new MultiString(multi) : null;
	}

	/** Get the MULTI as a String */
	@Override
	public String toString() {
		return builder.toString();
	}

	/** Get the MULTI string */
	public MultiString toMultiString() {
		return builder.toMultiString();
	}
}
