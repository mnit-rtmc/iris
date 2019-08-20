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

import java.util.Iterator;
import java.util.TreeSet;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.CorridorFinder;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentHelper;
import us.mn.state.dot.tms.IncSeverity;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.R_Node;

/**
 * Helper class to find devices upstream of an incident.
 *
 * @author Douglas Lau
 */
public class UpstreamDeviceFinder {

	/** Get the maximum number of exits upstream of an incident */
	static private int maximum_exits(Incident inc) {
		IncSeverity sev = IncidentHelper.getSeverity(inc);
		return (sev != null) ? sev.maximum_range.getExits() : -1;
	}

	/** Set of devices with exit count and distances */
	private final TreeSet<UpstreamDevice> devices =
		new TreeSet<UpstreamDevice>();

	/** Corridor finder */
	private final CorridorFinder<R_Node> finder;

	/** Maximum exits for DMS */
	private final int maximum_exits;

	/** Incident location */
	private final IncidentLoc iloc;

	/** Create new upstream device finder */
	public UpstreamDeviceFinder(CorridorFinder<R_Node> cf, Incident inc) {
		finder = cf;
		maximum_exits = maximum_exits(inc);
		iloc = new IncidentLoc(inc);
	}

	/** Find all devices */
	public void findDevices() {
		devices.clear();
		CorridorBase<R_Node> cb = finder.lookupCorridor(iloc);
		if (cb != null) {
			Float mp = cb.calculateMilePoint(iloc);
			if (mp != null)
				findDevices(cb, mp);
		}
	}

	/** Find all devices upstream of a given point on a corridor */
	private void findDevices(CorridorBase<R_Node> cb, float mp) {
		findLCSArrays(cb, mp);
		if (maximum_exits >= 0)
			findDMSs(cb, mp, 0);
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

	/** Find DMS upstream of a given point on a corridor */
	private void findDMSs(CorridorBase<R_Node> cb, float mp, int num_exits){
		Iterator<DMS> dit = DMSHelper.iterator();
		while (dit.hasNext()) {
			DMS dms = dit.next();
			// FIXME: filter out HOT lane signs
			if (DMSHelper.isHidden(dms) ||
			    DMSHelper.isFailed(dms) ||
			   !DMSHelper.isActive(dms))
				continue;
			GeoLoc loc = dms.getGeoLoc();
			UpstreamDevice ed = UpstreamDevice.create(dms, cb, mp,
				loc);
			if (ed != null && ed.exits + num_exits <= maximum_exits)
				devices.add(ed);
		}
		// FIXME: scan for freeway entrances
	}

	/** Get upstream device iterator */
	public Iterator<UpstreamDevice> iterator() {
		return devices.iterator();
	}
}
