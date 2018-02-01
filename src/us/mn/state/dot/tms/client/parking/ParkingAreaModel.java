/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.parking;

import java.util.ArrayList;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ParkingArea;
import us.mn.state.dot.tms.ParkingAreaHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for parking areas.
 *
 * @author Douglas Lau
 */
public class ParkingAreaModel extends ProxyTableModel<ParkingArea> {

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<ParkingArea>> createColumns() {
		ArrayList<ProxyColumn<ParkingArea>> cols =
			new ArrayList<ProxyColumn<ParkingArea>>(2);
		cols.add(new ProxyColumn<ParkingArea>("parking_area", 120) {
			public Object getValueAt(ParkingArea pa) {
				return pa.getName();
			}
		});
		cols.add(new ProxyColumn<ParkingArea>("location", 300) {
			public Object getValueAt(ParkingArea pa) {
				return GeoLocHelper.getDescription(
					pa.getGeoLoc());
			}
		});
		return cols;
	}

	/** Create a new parking area table model */
	public ParkingAreaModel(Session s) {
		super(s, ParkingAreaManager.descriptor(s), 12);
	}

	/** Create a new parking area */
	@Override
	public void createObject(String n) {
		// Ignore name given to us
		String name = createUniqueName();
		if (name != null)
			descriptor.cache.createObject(name);
	}

	/** Create a unique parking area name */
	private String createUniqueName() {
		for (int i = 1; i <= 9999; i++) {
			String n = "pa" + i;
			if (ParkingAreaHelper.lookup(n) == null)
				return n;
		}
		assert false;
		return null;
	}
}
