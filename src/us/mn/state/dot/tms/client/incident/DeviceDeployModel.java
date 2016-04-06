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

	/** Mapping of LCS array names to proposed indications */
	private final HashMap<String, Integer []> indications =
		new HashMap<String, Integer []>();

	/** Get the proposed indications for an LCS array */
	public Integer[] getIndications(String lcs_array) {
		return indications.get(lcs_array);
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
		LcsDeployModel lcs_mdl = new LcsDeployModel(inc);
		Position pos = new Position(inc.getLat(), inc.getLon());
		LaneConfiguration config = cb.laneConfiguration(pos);
		TreeMap<Distance, LCSArray> devices = findDevices(cb, mp);
		int shift = config.leftShift;
		for (Distance up: devices.keySet()) {
			LCSArray lcs_array = devices.get(up);
			int l_shift = lcs_array.getShift() - shift;
			Integer[] ind = lcs_mdl.createIndications(up, lcs_array,
				l_shift, config.getLanes());
			if (ind != null) {
				addElement(lcs_array);
				indications.put(lcs_array.getName(), ind);
			}
		}
	}

	/** Find all devices upstream of a given point on a corridor */
	private TreeMap<Distance, LCSArray> findDevices(CorridorBase cb,
		float mp)
	{
		TreeMap<Distance, LCSArray> devices =
			new TreeMap<Distance, LCSArray>();
		Iterator<LCSArray> lit = LCSArrayHelper.iterator();
		while (lit.hasNext()) {
			LCSArray lcs_array = lit.next();
			GeoLoc loc = LCSArrayHelper.lookupGeoLoc(lcs_array);
			if (loc != null &&
			    loc.getRoadway() == cb.getRoadway() &&
			    loc.getRoadDir() == cb.getRoadDir())
			{
				Float lp = cb.calculateMilePoint(loc);
				if (lp != null && mp > lp) {
					Distance up = new Distance(mp - lp,
						Distance.Units.MILES);
					devices.put(up, lcs_array);
				}
			}
		}
		return devices;
	}
}
