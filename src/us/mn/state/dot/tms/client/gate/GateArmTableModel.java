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
import java.util.HashMap;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmArray;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for gate arms within an array.
 *
 * @author Douglas Lau
 */
public class GateArmTableModel extends ProxyTableModel<GateArm> {

	/** Create the columns in the model */
	@Override protected ArrayList<ProxyColumn<GateArm>> createColumns() {
		ArrayList<ProxyColumn<GateArm>> cols =
			new ArrayList<ProxyColumn<GateArm>>(2);
		cols.add(new ProxyColumn<GateArm>("gate.arm.index", 36,
			Integer.class)
		{
			public Object getValueAt(GateArm ga) {
				return ga.getIdx();
			}
		});
		cols.add(new ProxyColumn<GateArm>("device.name", 140) {
			public Object getValueAt(GateArm ga) {
				return ga.getName();
			}
			public boolean isEditable(GateArm ga) {
				return (ga == null) && canAdd();
			}
			public void setValueAt(GateArm ga,int row,Object value){
				String v = value.toString().trim();
				if(v.length() > 0)
					createGateArm(v, row + 1);
			}
		});
		return cols;
	}

	/** Create a new gate arm */
	private void createGateArm(String name, int idx) {
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("ga_array", ga_array);
		attrs.put("idx", new Integer(idx));
		cache.createObject(name, attrs);
	}

	/** Gate arm array */
	private final GateArmArray ga_array;

	/** Create a new gate arm table model */
	public GateArmTableModel(Session s, GateArmArray ga) {
		super(s, s.getSonarState().getGateArms());
		ga_array = ga;
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(GateArm proxy) {
		if(proxy.getGaArray() == ga_array)
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Get the SONAR type name */
	@Override protected String getSonarType() {
		return GateArm.SONAR_TYPE;
	}
}
