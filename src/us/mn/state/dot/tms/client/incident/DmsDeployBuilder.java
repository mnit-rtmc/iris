/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2020  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.CorridorFinder;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncAdvice;
import us.mn.state.dot.tms.IncAdviceHelper;
import us.mn.state.dot.tms.IncDescriptor;
import us.mn.state.dot.tms.IncDescriptorHelper;
import us.mn.state.dot.tms.IncLocator;
import us.mn.state.dot.tms.IncLocatorHelper;
import us.mn.state.dot.tms.IncRange;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * DmsDeployBuilder is a builder for incident deployed DMS messages.
 *
 * @author Douglas Lau
 */
public class DmsDeployBuilder {

	/** Get cleared incident advice MULTI string */
	static private String clearedAdviceMulti() {
		return SystemAttrEnum.INCIDENT_CLEAR_ADVICE_MULTI.getString();
	}

	/** Get the r_node type checker */
	static private R_NodeType.Checker getChecker(short lto) {
		final LaneType lt = LaneType.fromOrdinal(lto);
		return new R_NodeType.Checker() {
			public boolean check(R_NodeType nt) {
				switch (lt) {
				case EXIT:
					return R_NodeType.EXIT == nt;
				case MERGE:
					return R_NodeType.ENTRANCE == nt;
				case MAINLINE:
					return R_NodeType.STATION == nt
					    || R_NodeType.INTERSECTION == nt;
				default:
					return false;
				}
			}
		};
	}

	/** Incident to deploy */
	private final Incident inc;

	/** Flag to indicate an r_node was picked for incident location */
	private final boolean picked;

	/** Location to use for incident */
	private final GeoLoc loc;

	/** Create a new incident deployed DMS message builder */
	public DmsDeployBuilder(CorridorFinder finder, Incident inc) {
		this.inc = inc;
		R_Node n = pickNode(finder);
		picked = (n != null);
		loc = (picked) ? n.getGeoLoc() : new IncidentLoc(inc);
	}

	/** Pick a node within 1 mile of incident */
	private R_Node pickNode(CorridorFinder finder) {
		IncidentLoc iloc = new IncidentLoc(inc);
		CorridorBase cb = finder.lookupCorridor(iloc);
		if (cb != null) {
			Float mp = cb.calculateMilePoint(iloc);
			if (mp != null)
				return pickNode(cb, mp);
		}
		return null;
	}

	/** Pick a node within 1 mile of incident */
	private R_Node pickNode(CorridorBase cb, float mp) {
		Position pos = new Position(inc.getLat(), inc.getLon());
		R_NodeType.Checker checker = getChecker(inc.getLaneType());
		R_Node n = cb.pickNearest(pos, checker);
		if (n != null) {
			Float lp = cb.calculateMilePoint(n.getGeoLoc());
			if (lp != null && Math.abs(lp - mp) < 1)
				return n;
		}
		return null;
	}

	/** Create the MULTI string for one DMS.
	 * @param dms Possible sign to deploy.
	 * @param ud Upstream device.
	 * @return MULTI string for DMS, or null. */
	public String createMulti(DMS dms, UpstreamDevice ud, boolean cleared) {
		boolean branched = !isCorridorSame(dms);
		Distance dist = ud.distance;
		IncRange rng = ud.range(picked);
		if (null == rng)
			return null;
		IncMultiBuilder builder = new IncMultiBuilder(dms, loc, dist);
		// Add incident descriptor line
		IncDescriptor idsc = IncDescriptorHelper.match(inc);
		if (null == idsc || !builder.addLine(idsc.getMulti()))
			return null;
		// Add incident locator line
		IncLocator iloc = IncLocatorHelper.match(rng, branched, picked);
		if (null == iloc || !builder.addLine(iloc.getMulti()))
			return null;
		// Add incident advice line
		if (cleared) {
			if (!builder.addLine(clearedAdviceMulti()))
				return null;
		} else {
			IncAdvice iadv = IncAdviceHelper.match(rng, inc);
			if (null == iadv || !builder.addLine(iadv.getMulti()))
				return null;
		}
		return builder.toString();
	}

	/** Check if a DMS is on same corridor as incident */
	private boolean isCorridorSame(DMS dms) {
		GeoLoc loc = dms.getGeoLoc();
		return loc.getRoadway() == inc.getRoad()
                    && loc.getRoadDir() == inc.getDir();
	}
}
