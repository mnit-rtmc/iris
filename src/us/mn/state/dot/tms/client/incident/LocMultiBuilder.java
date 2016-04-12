/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.LocModifier;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.MultiBuilder;

/**
 * LocMultiBuilder builds a MULTI string with an incident locator.
 *
 * @author Douglas Lau
 */
public class LocMultiBuilder extends MultiBuilder {

	/** Replace affixes */
	static private String replaceAffixes(String r) {
		if (r.startsWith("U.S."))
			return "HWY" + r.substring(4);
		if (r.startsWith("T.H."))
			return "HWY" + r.substring(4);
		if (r.startsWith("C.S.A.H."))
			return "CTY" + r.substring(8);
		if (r.startsWith("I-"))
			return r.substring(2);
		return r;
	}

	/** Strip affixes */
	static private String stripAffixes(String r) {
		if (r.startsWith("U.S."))
			return r.substring(4);
		if (r.startsWith("T.H."))
			return r.substring(4);
		if (r.startsWith("C.S.A.H."))
			return r.substring(8);
		if (r.startsWith("I-"))
			return r.substring(2);
		if (r.endsWith(" AVE"))
			return r.substring(0, r.length() - 4);
		if (r.endsWith(" BLVD"))
			return r.substring(0, r.length() - 5);
		if (r.endsWith(" DR"))
			return r.substring(0, r.length() - 3);
		if (r.endsWith(" HWY"))
			return r.substring(0, r.length() - 4);
		if (r.endsWith(" LN"))
			return r.substring(0, r.length() - 3);
		if (r.endsWith(" PKWY"))
			return r.substring(0, r.length() - 5);
		if (r.endsWith(" PL"))
			return r.substring(0, r.length() - 3);
		if (r.endsWith(" RD"))
			return r.substring(0, r.length() - 3);
		if (r.endsWith(" ST"))
			return r.substring(0, r.length() - 3);
		if (r.endsWith(" TR"))
			return r.substring(0, r.length() - 3);
		if (r.endsWith(" WAY"))
			return r.substring(0, r.length() - 4);
		return r;
	}

	/** Get direction text */
	static private String dirText(Direction dir) {
		switch (dir) {
		case NORTH:
			return "NORTH";
		case SOUTH:
			return "SOUTH";
		case EAST:
			return "EAST";
		case WEST:
			return "WEST";
		default:
			return "";
		}
	}

	/** Get modifier text */
	static private String modText(LocModifier mod) {
		switch (mod) {
		case NORTH_OF:
			return "N OF";
		case SOUTH_OF:
			return "S OF";
		case EAST_OF:
			return "E OF";
		case WEST_OF:
			return "W OF";
		default:
			return "AT";
		}
	}

	/** Location of r_node */
	private final GeoLoc loc;

	/** Distance upstream of incident */
	private final Distance up;

	/** Create a new location MULTI builder */
	public LocMultiBuilder(R_Node n, Distance u) {
		loc = n.getGeoLoc();
		up = u;
	}

	/** Add an incident locator */
	@Override
	public void addLocator(String code) {
		if ("rn".equals(code))
			addRoadway();
		else if ("rd".equals(code))
			addRoadDir();
		else if ("md".equals(code))
			addModifier();
		else if ("xn".equals(code))
			addCrossStreet();
		else if ("xa".equals(code))
			addCrossAbbrev();
		else if ("mi".equals(code))
			addMiles();
	}

	/** Add roadway name */
	private void addRoadway() {
		addSpan(replaceAffixes(loc.getRoadway().getName()
			.toUpperCase()));
	}

	/** Add roadway direction */
	private void addRoadDir() {
		addSpan(dirText(Direction.fromOrdinal(loc.getRoadDir())));
	}

	/** Add cross-modifier */
	private void addModifier() {
		addSpan(modText(LocModifier.fromOrdinal(loc.getCrossMod())));
	}

	/** Add cross-street name */
	private void addCrossStreet() {
		addSpan(replaceAffixes(loc.getCrossStreet().getName()
			.toUpperCase()));
	}

	/** Add cross-street abbreviation */
	private void addCrossAbbrev() {
		addSpan(stripAffixes(loc.getCrossStreet().getName()
			.toUpperCase()));
	}

	/** Add miles upstream */
	private void addMiles() {
		addSpan(String.valueOf(up.round(Distance.Units.MILES)));
	}
}
