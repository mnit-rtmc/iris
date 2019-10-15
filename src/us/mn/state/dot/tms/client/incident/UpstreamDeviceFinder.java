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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.CorridorFinder;
import us.mn.state.dot.tms.DevicePurpose;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentHelper;
import us.mn.state.dot.tms.IncImpact;
import us.mn.state.dot.tms.IncSeverity;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;
import static us.mn.state.dot.tms.client.incident.UpstreamDevice.MAX_GAP_MI;

/**
 * Helper class to find devices upstream of an incident.
 *
 * @author Douglas Lau
 */
public class UpstreamDeviceFinder {

	/** Maximum distance from device to nearest r_node */
	static private final Distance MAX_DIST = new Distance(2.5, MILES);

	/** Maximum distance from incident to deploy tolling signs */
	static private final Distance MAX_TOLLING_DEPLOYMENT_DIST =
		new Distance(1.0, MILES);

	/** Get the maximum number of exits upstream of an incident */
	static private int maximum_exits(Incident inc) {
		IncSeverity sev = IncidentHelper.getSeverity(inc);
		return (sev != null) ? sev.maximum_range.getMaxExits() : -1;
	}

	/** Scanner for devices on a corridor */
	private class CorScanner implements Comparable<CorScanner> {

		/** Downstream location on corridor */
		private final GeoLoc loc;

		/** Number of exits after downstream location */
		private final int exits;

		/** Distance downstream of corridor */
		private final Distance dist;

		/** Flag indicating scan complete */
		private boolean scanned;

		/** Create a corridor scanner */
		private CorScanner(GeoLoc gl, int e, Distance d) {
			loc = gl;
			exits = e;
			dist = d;
			scanned = false;
		}

		/** Get the corridor name */
		private String getCorridorName() {
			String name = GeoLocHelper.getCorridorName(loc);
			return (name != null) ? name : "";
		}

		/** Scan the corridor for upstream devices */
		private void scanUpstream() {
			String name = getCorridorName();
			CorridorBase<R_Node> cb = finder.lookupCorridor(name);
			if (cb != null) {
				Float mp = cb.calculateMilePoint(loc, MAX_DIST);
				if (mp != null)
					findDevices(cb, mp, exits, dist);
			}
			scanned = true;
		}

		/** Compare with another corridor scanner */
		@Override
		public int compareTo(CorScanner other) {
			String n0 = getCorridorName();
			String n1 = other.getCorridorName();
			return n0.compareTo(n1);
		}
	}

	/** Corridor finder */
	private final CorridorFinder<R_Node> finder;

	/** The incident */
	private final Incident incident;

	/** Maximum exits for DMS */
	private final int maximum_exits;

	/** Set of all corridor scanners */
	private final TreeSet<CorScanner> scanners = new TreeSet<CorScanner>();

	/** Set of devices with exit count and distances */
	private final TreeSet<UpstreamDevice> devices =
		new TreeSet<UpstreamDevice>();

	/** Create new upstream device finder */
	public UpstreamDeviceFinder(CorridorFinder<R_Node> cf, Incident inc) {
		finder = cf;
		incident = inc;
		maximum_exits = maximum_exits(inc);
		scanners.add(new CorScanner(new IncidentLoc(inc), 0,
			new Distance(0f, MILES)));
	}

	/** Find all devices */
	public void findDevices() {
		CorScanner scanner = getScanner();
		while (scanner != null) {
			scanner.scanUpstream();
			scanner = getScanner();
		}
	}

	/** Get the next scanner */
	private CorScanner getScanner() {
		for (CorScanner scanner: scanners) {
			if (!scanner.scanned)
				return scanner;
		}
		return null;
	}

	/** Find all devices upstream of a given point on a corridor.
	 * @param cb Corridor to scan.
	 * @param mp Mile point to end scan.
	 * @param num_exits Number of exits downstream of corridor.
	 * @param dist Distance downstream of corridor. */
	private void findDevices(CorridorBase<R_Node> cb, float mp,
		int num_exits, Distance dist)
	{
		// Don't scan for LCS arrays on branched corridors
		if (num_exits == 0)
			findLCSArrays(cb, mp);
		if (num_exits <= maximum_exits) {
			findDMSs(cb, mp, num_exits, dist);
			if (num_exits < maximum_exits)
				findEntrances(cb, mp, num_exits, dist);
		}
	}

	/** Find LCS arrays */
	private void findLCSArrays(CorridorBase<R_Node> cb, float mp) {
		Iterator<LCSArray> it = LCSArrayHelper.iterator();
		while (it.hasNext()) {
			LCSArray lcs = it.next();
			GeoLoc loc = LCSArrayHelper.lookupGeoLoc(lcs);
			UpstreamDevice ed = UpstreamDevice.create(lcs, cb, mp,
				loc);
			if (ed != null)
				devices.add(ed);
		}
	}

	/** Check if a DMS is deployable for the incident */
	private boolean isDeployable(DMS dms, UpstreamDevice ed,
		boolean branched)
	{
		if (DMSHelper.isHidden(dms) ||
		    DMSHelper.isFailed(dms) ||
		   !DMSHelper.isActive(dms))
			return false;
		switch (DevicePurpose.fromOrdinal(dms.getPurpose())) {
			case GENERAL: return true;
			case TOLLING: return isTollingDeployable(ed, branched);
			default: return false;
		}
	}

	/** Check if a TOLLING sign is deployable */
	private boolean isTollingDeployable(UpstreamDevice ed,
		boolean branched)
	{
		if (branched)
			return false;
		if (ed.distance.m() > MAX_TOLLING_DEPLOYMENT_DIST.m())
			return false;
		switch (IncImpact.getImpact(incident)) {
			case lanes_blocked: return true;
			case left_lanes_blocked: return true;
			case lanes_affected: return true;
			case left_lanes_affected: return true;
			default: return false;
		}
	}

	/** Find DMS upstream of a given point on a corridor.
	 * @param cb Corridor to scan.
	 * @param mp Mile point to end scan.
	 * @param num_exits Number of exits downstream of corridor.
	 * @param dist Distance downstream of corridor. */
	private void findDMSs(CorridorBase<R_Node> cb, float mp, int num_exits,
		Distance dist)
	{
		boolean branched = (num_exits > 0);
		Iterator<DMS> dit = DMSHelper.iterator();
		while (dit.hasNext()) {
			DMS dms = dit.next();
			GeoLoc loc = dms.getGeoLoc();
			UpstreamDevice ed = UpstreamDevice.create(dms, cb, mp,
				loc);
			if (ed != null && isDeployable(dms, ed, branched)) {
				ed = ed.adjusted(num_exits, dist);
				if (ed.exits <= maximum_exits)
					devices.add(ed);
			}
		}
	}

	/** Find entrances from interchanges to other corridors */
	private void findEntrances(CorridorBase<R_Node> cb, float mp,
		int num_exits, Distance dist)
	{
		ArrayList<GeoLoc> entrances = cb.findEntrances(mp);
		for (GeoLoc loc: entrances) {
			Float p = cb.calculateMilePoint(loc);
			if (p != null && mp > p) {
				Integer e = cb.countExits(p, mp, MAX_GAP_MI);
				if (e != null) {
					// Must add at least 1 exit
					// if not on original corridor
					if (dist.m() > 0.0)
						e = Math.max(1, e);
					int exits = num_exits + e;
					if (exits <= maximum_exits) {
						Distance d = dist.add(
							new Distance(mp - p,
							MILES));
						findInterchange(loc, exits, d);
					}
				}
			}
		}
	}

	/** Find interchange exit which matches the given location */
	private void findInterchange(GeoLoc loc, int num_exits, Distance dist) {
		String name = GeoLocHelper.getLinkedCorridor(loc);
		CorridorBase<R_Node> cb = finder.lookupCorridor(name);
		GeoLoc eloc = findExit(cb, loc);
		if (eloc != null)
			scanners.add(new CorScanner(eloc, num_exits, dist));
	}

	/** Find a matching exit from specified corridor */
	private GeoLoc findExit(CorridorBase<R_Node> cb, GeoLoc loc) {
		if (cb != null && !isOppositeCorridor(cb)) {
			R_Node n = cb.findFork(loc);
			if (n != null)
				return n.getGeoLoc();
		}
		return null;
	}

	/** Check if a corridor is opposite direction of incident */
	private boolean isOppositeCorridor(CorridorBase<R_Node> cb) {
		return cb.getRoadway() == incident.getRoad() &&
		       Direction.isOpposite(cb.getRoadDir(), incident.getDir());
	}

	/** Get upstream device iterator */
	public Iterator<UpstreamDevice> iterator() {
		return devices.iterator();
	}
}
