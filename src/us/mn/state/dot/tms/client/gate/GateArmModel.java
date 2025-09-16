/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.gate;

import java.util.ArrayList;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for gate arms.
 *
 * @author Douglas Lau
 */
public class GateArmModel extends ProxyTableModel<GateArm> {

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<GateArm>> createColumns() {
		ArrayList<ProxyColumn<GateArm>> cols =
			new ArrayList<ProxyColumn<GateArm>>(2);
		cols.add(new ProxyColumn<GateArm>("gate_arm", 200) {
			public Object getValueAt(GateArm ga) {
				return ga.getName();
			}
		});
		cols.add(new ProxyColumn<GateArm>("location", 300) {
			public Object getValueAt(GateArm ga) {
				return GeoLocHelper.getLocation(ga.getGeoLoc());
			}
		});
		return cols;
	}

	/** Create a new gate arm table model */
	public GateArmModel(Session s) {
		super(s, GateArmManager.descriptor(s), 16);
	}
}
