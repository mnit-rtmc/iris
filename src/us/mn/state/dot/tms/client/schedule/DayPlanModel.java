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
package us.mn.state.dot.tms.client.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for day plans.
 *
 * @author Douglas Lau
 */
public class DayPlanModel extends ProxyTableModel<DayPlan> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<DayPlan> descriptor(Session s) {
		return new ProxyDescriptor<DayPlan>(
			s.getSonarState().getDayPlans(),
			false,  /* has_properties */
			true,   /* has_create_delete */
			true    /* has_name */
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<DayPlan>> createColumns() {
		ArrayList<ProxyColumn<DayPlan>> cols =
			new ArrayList<ProxyColumn<DayPlan>>(2);
		cols.add(new ProxyColumn<DayPlan>("action.plan.day", 120) {
			public Object getValueAt(DayPlan p) {
				return p.getName();
			}
		});
		cols.add(new ProxyColumn<DayPlan>("day.plan.holidays", 60,
			Boolean.class)
		{
			public Object getValueAt(DayPlan p) {
				return p.getHolidays();
			}
		});
		return cols;
	}

	/** Create a new day plan table model */
	public DayPlanModel(Session s) {
		super(s, descriptor(s), 12);
	}

	/** Create a new day plan */
	public void createObject(String name, boolean h) {
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("holidays", h);
		descriptor.cache.createObject(name, attrs);
	}
}
