/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.gps;

import java.util.ArrayList;
import us.mn.state.dot.tms.Gps;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for GPS
 *
 * @author Douglas Lau
 */
public class GpsModel extends ProxyTableModel<Gps> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Gps> descriptor(Session s) {
		return new ProxyDescriptor<Gps>(
			s.getSonarState().getGpses(),
			false,  /* has_properties */
			true,   /* has_create_delete */
			false   /* has_name */
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Gps>> createColumns() {
		ArrayList<ProxyColumn<Gps>> cols =
			new ArrayList<ProxyColumn<Gps>>(2);
		cols.add(new ProxyColumn<Gps>("gps", 60) {
			public Object getValueAt(Gps g) {
				return g.getName();
			}
		});
		cols.add(new ProxyColumn<Gps>("gps.loc", 100) {
			public Object getValueAt(Gps g) {
				return g.getGeoLoc();
			}
		});
		return cols;
	}

	/** Create a new GPS table model */
	public GpsModel(Session s) {
		super(s, descriptor(s), 16, 20);
	}

	/** Create an object with the given name */
	@Override
	public void createObject(String n) {
		String name = createUniqueName();
		if (name != null)
			descriptor.cache.createObject(name);
	}

	/** Create a unique GPS name */
	private String createUniqueName() {
		for (int uid = 1; uid <= 9999; uid++) {
			String n = "gps_" + uid;
			if (descriptor.cache.lookupObject(n) == null)
				return n;
		}
		assert false;
		return null;
	}
}
