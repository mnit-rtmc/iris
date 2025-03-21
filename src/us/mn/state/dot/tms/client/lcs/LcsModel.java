/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.util.ArrayList;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for LCS arrays.
 *
 * @author Douglas Lau
 */
public class LcsModel extends ProxyTableModel<Lcs> {

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Lcs>> createColumns() {
		ArrayList<ProxyColumn<Lcs>> cols =
			new ArrayList<ProxyColumn<Lcs>>(2);
		cols.add(new ProxyColumn<Lcs>("lcs", 120) {
			public Object getValueAt(Lcs l) {
				return l.getName();
			}
		});
		cols.add(new ProxyColumn<Lcs>("location", 300) {
			public Object getValueAt(Lcs l) {
				return GeoLocHelper.getLocation(l.getGeoLoc());
			}
		});
		return cols;
	}

	/** Create a new LCS array table model */
	public LcsModel(Session s) {
		super(s, LcsManager.descriptor(s), 16);
	}
}
