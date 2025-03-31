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
package us.mn.state.dot.tms.client.system;

import java.util.ArrayList;
import java.util.HashMap;
import us.mn.state.dot.tms.EventConfig;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for event configs.
 *
 * @author Douglas Lau
 */
public class EventConfigModel extends ProxyTableModel<EventConfig> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<EventConfig> descriptor(Session s) {
		return new ProxyDescriptor<EventConfig>(
			s.getSonarState().getEventConfigs(),
			false, /* has_properties */
			false, /* has_create_delete */
			false  /* has_name */
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<EventConfig>> createColumns() {
		ArrayList<ProxyColumn<EventConfig>> cols =
			new ArrayList<ProxyColumn<EventConfig>>(4);
		cols.add(new ProxyColumn<EventConfig>("event.config.name", 140) {
			public Object getValueAt(EventConfig ec) {
				return ec.getName();
			}
		});
		cols.add(new ProxyColumn<EventConfig>(
			"event.config.enable.store", 60, Boolean.class)
		{
			public Object getValueAt(EventConfig ec) {
				return ec.getEnableStore();
			}
			public boolean isEditable(EventConfig ec) {
				return canWrite(ec);
			}
			public void setValueAt(EventConfig ec, Object value) {
				if (value instanceof Boolean)
					ec.setEnableStore((Boolean) value);
			}
		});
		cols.add(new ProxyColumn<EventConfig>(
			"event.config.enable.purge", 60, Boolean.class)
		{
			public Object getValueAt(EventConfig ec) {
				return ec.getEnablePurge();
			}
			public boolean isEditable(EventConfig ec) {
				return canWrite(ec);
			}
			public void setValueAt(EventConfig ec, Object value) {
				if (value instanceof Boolean)
					ec.setEnablePurge((Boolean) value);
			}
		});
		cols.add(new ProxyColumn<EventConfig>("event.config.purge.days",
			90, Integer.class)
		{
			public Object getValueAt(EventConfig ec) {
				return ec.getPurgeDays();
			}
			public boolean isEditable(EventConfig ec) {
				return canWrite(ec);
			}
			public void setValueAt(EventConfig ec, Object value) {
				if (value instanceof Integer)
					ec.setPurgeDays((Integer) value);
			}
		});
		return cols;
	}

	/** Create a new event config table model */
	public EventConfigModel(Session s) {
		super(s, descriptor(s), 16);
	}
}
