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
import java.util.TreeMap;
import javax.swing.DefaultListModel;
import us.mn.state.dot.geokit.Position;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.LaneConfiguration;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.units.Distance;

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

	/** Mapping of LCS array names to proposed indications */
	private final HashMap<String, Integer []> indications =
		new HashMap<String, Integer []>();

	/** Get the proposed indications for an LCS array */
	public Integer[] getIndications(String lcs_a) {
		return indications.get(lcs_a);
	}

	/** Create a new device deploy model */
	public DeviceDeployModel(IncidentManager man, Incident inc) {
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
		LaneConfiguration config = cb.laneConfiguration(pos);
		LcsDeployModel lcs_mdl = new LcsDeployModel(inc, config);
		TreeMap<Distance, Device> devices = findDevices(cb, mp);
		int shift = config.leftShift;
		for (Distance up: devices.keySet()) {
			Device dev = devices.get(up);
			if (dev instanceof LCSArray) {
				LCSArray lcs_a = (LCSArray) dev;
				int l_shift = lcs_a.getShift() - shift;
				Integer[] ind = lcs_mdl.createIndications(up,
					lcs_a, l_shift);
				if (ind != null) {
					addElement(lcs_a);
					indications.put(lcs_a.getName(), ind);
				}
			}
			if (dev instanceof DMS) {
				DMS dms = (DMS) dev;
				System.err.println("dms: " + dms + ", " + up);
				// FIXME
			}
		}
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
				Distance up = new Distance(mp - lp,
					Distance.Units.MILES);
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
				Distance up = new Distance(mp - lp,
					Distance.Units.MILES);
				devices.put(up, dms);
			}
		}
		return devices;
	}
}
