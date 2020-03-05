/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.RoadAffixHelper;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.MultiBuilder;

/**
 * LocMultiBuilder builds a MULTI string with an incident locator.
 *
 * @author Douglas Lau
 */
public class LocMultiBuilder extends MultiBuilder {

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
			return "NORTH OF";
		case SOUTH_OF:
			return "SOUTH OF";
		case EAST_OF:
			return "EAST OF";
		case WEST_OF:
			return "WEST OF";
		default:
			return "AT";
		}
	}

	/** Location of incident */
	private final GeoLoc loc;

	/** Distance upstream of incident */
	private final Distance up;

	/** Flag to retain allowed road affixes */
	private final boolean retain_affixes;

	/** Create a new location MULTI builder */
	public LocMultiBuilder(GeoLoc l, Distance u, boolean ra) {
		loc = l;
		up = u;
		retain_affixes = ra;
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
		String s = loc.getRoadway().getName().toUpperCase();
		addSpan(RoadAffixHelper.replace(s, retain_affixes));
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
		Road xstreet = loc.getCrossStreet();
		if (xstreet != null) {
			String s = xstreet.getName().toUpperCase();
			addSpan(RoadAffixHelper.replace(s, retain_affixes));
		}
	}

	/** Add cross-street abbreviation */
	private void addCrossAbbrev() {
		Road xstreet = loc.getCrossStreet();
		if (xstreet != null) {
			String s = xstreet.getName().toUpperCase();
			addSpan(RoadAffixHelper.replace(s, false));
		}
	}

	/** Add miles upstream */
	private void addMiles() {
		addSpan(String.valueOf(up.round(Distance.Units.MILES)));
	}
}
