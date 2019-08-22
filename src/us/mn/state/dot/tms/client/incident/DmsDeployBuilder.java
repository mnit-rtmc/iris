/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncAdvice;
import us.mn.state.dot.tms.IncAdviceHelper;
import us.mn.state.dot.tms.IncDescriptor;
import us.mn.state.dot.tms.IncDescriptorHelper;
import us.mn.state.dot.tms.IncLocator;
import us.mn.state.dot.tms.IncLocatorHelper;
import us.mn.state.dot.tms.IncRange;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * DmsDeployBuilder is a builder for incident deployed DMS messages.
 *
 * @author Douglas Lau
 */
public class DmsDeployBuilder {

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
		String name = GeoLocHelper.getCorridorName(iloc);
		CorridorBase cb = finder.lookupCorridor(name);
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
		R_Node n = cb.findNearest(pos, checker, true);
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
	public MultiString createMulti(DMS dms, UpstreamDevice ud) {
		Distance up = ud.distance;
		IncRange rng = ud.range();
		if (null == rng)
			return null;
		IncDescriptor dsc = IncDescriptorHelper.match(inc);
		if (null == dsc)
			return null;
		IncLocator iloc = IncLocatorHelper.match(rng, false, picked);
		if (null == iloc)
			return null;
		IncAdvice adv = IncAdviceHelper.match(rng, inc);
		if (null == adv)
			return null;
		String mdsc = checkMulti(dms, dsc.getMulti(), dsc.getAbbrev(),
			up, loc);
		if (null == mdsc)
			return null;
		String mloc = checkMulti(dms, iloc.getMulti(), iloc.getAbbrev(),
			up, loc);
		if (null == mloc)
			return null;
		String madv = checkMulti(dms, adv.getMulti(), adv.getAbbrev(),
			up, loc);
		if (null == madv)
			return null;
		LocMultiBuilder lmb = new LocMultiBuilder(loc, up);
		new MultiString(mdsc).parse(lmb);
		lmb.addLine(null);
		new MultiString(mloc).parse(lmb);
		lmb.addLine(null);
		new MultiString(madv).parse(lmb);
		return lmb.toMultiString();
	}

	/** Check if MULTI string or abbreviation will fit on a DMS */
	private String checkMulti(DMS dms, String ms, String abbrev,
		Distance up, GeoLoc loc)
	{
		String res = checkMulti(dms, ms, up, loc);
		return (res != null) ? res : checkMulti(dms, abbrev, up, loc);
	}

	/** Check if MULTI string will fit on a DMS */
	private String checkMulti(DMS dms, String ms, Distance up, GeoLoc loc) {
		if (null == ms)
			return null;
		LocMultiBuilder lmb = new LocMultiBuilder(loc, up);
		new MultiString(ms).parse(lmb);
		MultiString multi = lmb.toMultiString();
		return (createGraphic(dms, multi) != null) ? ms : null;
	}

	/** Create the page one graphic for a MULTI string */
	public RasterGraphic createGraphic(DMS dms, MultiString ms) {
		try {
			RasterGraphic[] pixmaps = DMSHelper.createPixmaps(dms,
				ms);
			return pixmaps[0];
		}
		catch (Exception e) {
			// could be IndexOutOfBounds or InvalidMessage
			return null;
		}
	}
}
