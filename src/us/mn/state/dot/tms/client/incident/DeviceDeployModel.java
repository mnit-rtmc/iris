/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.DefaultListModel;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentHelper;
import us.mn.state.dot.tms.IncAdvice;
import us.mn.state.dot.tms.IncAdviceHelper;
import us.mn.state.dot.tms.IncDescriptor;
import us.mn.state.dot.tms.IncDescriptorHelper;
import us.mn.state.dot.tms.IncLocator;
import us.mn.state.dot.tms.IncLocatorHelper;
import us.mn.state.dot.tms.IncRange;
import us.mn.state.dot.tms.IncSeverity;
import us.mn.state.dot.tms.LaneConfiguration;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * DeviceDeployModel is a model of devices to deploy for an incident.
 *
 * @author Douglas Lau
 */
public class DeviceDeployModel extends DefaultListModel<Device> {

	/** Calculate a mile point for a location on a corridor */
	static private Float calculateMilePoint(CorridorBase cb, GeoLoc loc) {
		if (loc != null &&
		    loc.getRoadway() == cb.getRoadway() &&
		    loc.getRoadDir() == cb.getRoadDir())
			return cb.calculateMilePoint(loc);
		else
			return null;
	}

	/** Get an LCS array position */
	static private Position getLCSPosition(LCSArray lcs_a) {
		GeoLoc loc = LCSArrayHelper.lookupGeoLoc(lcs_a);
		return (loc != null)
		      ? new Position(loc.getLat(), loc.getLon())
		      : null;
	}

	/** Get an LCS array lane configuration */
	static private LaneConfiguration laneConfiguration(CorridorBase cb,
		LCSArray lcs_a)
	{
		Position p = getLCSPosition(lcs_a);
		return (p != null) ? cb.laneConfiguration(p) : null;
	}

	/** Create indications for an LCS array */
	static private Integer[] createIndications(CorridorBase cb,
		LCSArray lcs_a, Distance up, LcsDeployModel lcs_mdl)
	{
		LaneConfiguration cfg = laneConfiguration(cb, lcs_a);
		return (cfg != null)
		      ? lcs_mdl.createIndications(cfg, up, lcs_a)
		      : null;
	}

	/** Distance for no range */
	static private final Distance RANGE_NONE = new Distance(0);

	/** Distance for near range */
	static private final Distance RANGE_NEAR = new Distance(1.5, MILES);

	/** Distance for middle range */
	static private final Distance RANGE_MIDDLE = new Distance(5, MILES);

	/** Distance for far range */
	static private final Distance RANGE_FAR = new Distance(10, MILES);

	/** Get the maximum distance to deploy a DMS */
	static private Distance maxDistance(IncSeverity svr) {
		if (svr == null)
			return RANGE_NONE;
		switch (svr) {
		case minor:
			return RANGE_NEAR;
		case normal:
			return RANGE_MIDDLE;
		case major:
			return RANGE_FAR;
		default:
			return RANGE_NONE;
		}
	}

	/** Get the range for a distance to incident */
	static private IncRange getRange(Distance up) {
		double m = up.m();
		if (m > RANGE_FAR.m())
			return null;
		if (m > RANGE_MIDDLE.m())
			return IncRange.far;
		if (m > RANGE_NEAR.m())
			return IncRange.middle;
		else
			return IncRange.near;
	}

	/** Get the r_node type */
	static private R_NodeType getNodeType(short lt) {
		switch (LaneType.fromOrdinal(lt)) {
		case EXIT:
			return R_NodeType.EXIT;
		case MERGE:
			return R_NodeType.ENTRANCE;
		case MAINLINE:
			return R_NodeType.STATION;
		default:
			return null;
		}
	}

	/** Incident severity */
	private final IncSeverity svr;

	/** Maximum distance */
	private final Distance max_dist;

	/** Mapping of LCS array names to proposed indications */
	private final HashMap<String, Integer []> indications =
		new HashMap<String, Integer []>();

	/** Get the proposed indications for an LCS array */
	public Integer[] getIndications(String lcs_a) {
		return indications.get(lcs_a);
	}

	/** Mapping of DMS names to proposed MULTI strings */
	private final HashMap<String, MultiString> messages =
		new HashMap<String, MultiString>();

	/** Get the proposed MULTI for a DMS */
	public MultiString getMulti(String dms) {
		return messages.get(dms);
	}

	/** Mapping of DMS names to proposed page one graphics */
	private final HashMap<String, RasterGraphic> graphics =
		new HashMap<String, RasterGraphic>();

	/** Get the proposed graphics for a DMS */
	public RasterGraphic getGraphic(String dms) {
		return graphics.get(dms);
	}

	/** Create a new device deploy model */
	public DeviceDeployModel(IncidentManager man, Incident inc) {
		svr = IncidentHelper.getSeverity(inc);
		max_dist = maxDistance(svr);
		IncidentLoc loc = new IncidentLoc(inc);
		CorridorBase cb = man.lookupCorridor(loc);
		if (cb != null) {
			Float mp = cb.calculateMilePoint(loc);
			if (mp != null)
				populateList(inc, cb, mp);
		}
	}

	/** Populate list model with device deployments.
	 * @param inc Incident.
	 * @param cb Corridor where incident is located.
	 * @param mp Relative mile point of incident. */
	private void populateList(Incident inc, CorridorBase cb, float mp) {
		Position pos = new Position(inc.getLat(), inc.getLon());
		R_Node n = findNode(cb, mp, pos, inc);
		LaneConfiguration config = cb.laneConfiguration(pos);
		LcsDeployModel lcs_mdl = new LcsDeployModel(inc, config);
		TreeMap<Distance, Device> devices = findDevices(cb, mp);
		for (Distance up: devices.keySet()) {
			Device dev = devices.get(up);
			if (dev instanceof LCSArray) {
				LCSArray lcs_a = (LCSArray) dev;
				Integer[] ind = createIndications(cb, lcs_a,
					up, lcs_mdl);
				if (ind != null) {
					addElement(lcs_a);
					indications.put(lcs_a.getName(), ind);
				}
			}
			if (dev instanceof DMS) {
				DMS dms = (DMS) dev;
				MultiString ms = createMulti(inc, dms, up, n);
				if (ms != null) {
					RasterGraphic rg = createGraphic(dms,
						ms);
					if (rg != null) {
						addElement(dms);
						messages.put(dms.getName(), ms);
						graphics.put(dms.getName(), rg);
					}
				}
			}
		}
	}

	/** Find a node within 1 mile of incident (prefer pickable) */
	private R_Node findNode(CorridorBase cb, float mp, Position pos,
		Incident inc)
	{
		R_NodeType rt = getNodeType(inc.getLaneType());
		if (rt == null)
			return null;
		R_Node n = cb.findNearest(pos, rt, true);
		if (n != null) {
			Float lp = cb.calculateMilePoint(n.getGeoLoc());
			if (lp != null && Math.abs(lp - mp) < 1)
				return n;
		}
		n = cb.findNearest(pos, rt, false);
		if (n != null) {
			Float lp = cb.calculateMilePoint(n.getGeoLoc());
			if (lp != null && Math.abs(lp - mp) < 1.5)
				return n;
		}
		return null;
	}

	/** Find all devices upstream of a given point on a corridor */
	private TreeMap<Distance, Device> findDevices(CorridorBase cb,
		float mp)
	{
		TreeMap<Distance, Device> devices =
			new TreeMap<Distance, Device>();
		// Find LCS arrays
		Iterator<LCSArray> lit = LCSArrayHelper.iterator();
		while (lit.hasNext()) {
			LCSArray lcs_a = lit.next();
			GeoLoc loc = LCSArrayHelper.lookupGeoLoc(lcs_a);
			Float lp = calculateMilePoint(cb, loc);
			if (lp != null && mp > lp) {
				Distance up = new Distance(mp - lp, MILES);
				devices.put(up, lcs_a);
			}
		}
		// Find DMS
		Iterator<DMS> dit = DMSHelper.iterator();
		while (dit.hasNext()) {
			DMS dms = dit.next();
			if (DMSHelper.isHidden(dms) ||
			    DMSHelper.isFailed(dms) ||
			   !DMSHelper.isActive(dms))
				continue;
			GeoLoc loc = dms.getGeoLoc();
			Float lp = calculateMilePoint(cb, loc);
			if (lp != null && mp > lp) {
				Distance up = new Distance(mp - lp, MILES);
				devices.put(up, dms);
			}
		}
		return devices;
	}

	/** Create the MULTI string for one DMS */
	private MultiString createMulti(Incident inc, DMS dms, Distance up,
		R_Node n)
	{
		if (n == null)
			return null;
		if (up.m() > max_dist.m())
			return null;
		Set<SignGroup> groups = DmsSignGroupHelper.findGroups(dms);
		IncDescriptor dsc = IncDescriptorHelper.match(inc, groups);
		if (dsc == null)
			return null;
		IncRange rng = getRange(up);
		if (rng == null)
			return null;
		IncLocator iloc = IncLocatorHelper.match(groups, rng, false,
			n.getPickable());
		if (iloc == null)
			return null;
		IncAdvice adv = IncAdviceHelper.match(groups, rng, inc);
		if (adv == null)
			return null;
		LocMultiBuilder lmb = new LocMultiBuilder(n, up);
		new MultiString(dsc.getMulti()).parse(lmb);
		lmb.addLine(null);
		new MultiString(iloc.getMulti()).parse(lmb);
		lmb.addLine(null);
		new MultiString(adv.getMulti()).parse(lmb);
		return lmb.toMultiString();
	}

	/** Create the page one graphic for a MULTI string */
	private RasterGraphic createGraphic(DMS dms, MultiString ms) {
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
