/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;

/**
 * Table model for gate arms.
 *
 * @author Douglas Lau
 */
public class GateArmModel extends ProxyTableModel<GateArm> {

	/** Create the columns in the model */
	@Override protected ArrayList<ProxyColumn<GateArm>> createColumns() {
		ArrayList<ProxyColumn<GateArm>> cols =
			new ArrayList<ProxyColumn<GateArm>>(2);
		cols.add(new ProxyColumn<GateArm>("gate.arm", 200) {
			public Object getValueAt(GateArm ga) {
				return ga.getName();
			}
			public boolean isEditable(GateArm ga) {
				return (ga == null) && canAdd();
			}
			public void setValueAt(GateArm ga, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		});
		cols.add(new ProxyColumn<GateArm>("location", 300) {
			public Object getValueAt(GateArm ga) {
				return GeoLocHelper.getDescription(
					ga.getGeoLoc());
			}
		});
		return cols;
	}

	/** Create a new ramp meter table model */
	public GateArmModel(Session s) {
		super(s, s.getSonarState().getGateArms());
	}

	/** Determine if a properties form is available */
	@Override public boolean hasProperties() {
		return true;
	}

	/** Create a properties form for one proxy */
	@Override protected SonarObjectForm<GateArm> createPropertiesForm(
		GateArm proxy)
	{
		return new GateArmProperties(session, proxy);
	}

	/** Get the SONAR type name */
	@Override protected String getSonarType() {
		return GateArm.SONAR_TYPE;
	}
}
