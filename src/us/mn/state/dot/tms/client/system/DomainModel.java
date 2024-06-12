/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Domain;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for IRIS domains.
 *
 * @author Douglas Lau
 */
public class DomainModel extends ProxyTableModel<Domain> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Domain> descriptor(Session s) {
		return new ProxyDescriptor<Domain>(
			s.getSonarState().getDomains(), false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Domain>> createColumns() {
		ArrayList<ProxyColumn<Domain>> cols =
			new ArrayList<ProxyColumn<Domain>>(3);
		cols.add(new ProxyColumn<Domain>("domain.name", 120) {
			public Object getValueAt(Domain d) {
				return d.getName();
			}
		});
		cols.add(new ProxyColumn<Domain>("domain.block", 240) {
			public Object getValueAt(Domain d) {
				return d.getBlock();
			}
			public boolean isEditable(Domain d) {
				return canWrite(d);
			}
			public void setValueAt(Domain d, Object value) {
				String b = value.toString().trim();
				d.setBlock(b);
			}
		});
		cols.add(new ProxyColumn<Domain>("domain.enabled", 60,
			Boolean.class)
		{
			public Object getValueAt(Domain d) {
				return d.getEnabled();
			}
			public boolean isEditable(Domain d) {
				return canWrite(d);
			}
			public void setValueAt(Domain d, Object value) {
				if (value instanceof Boolean)
					d.setEnabled((Boolean) value);
			}
		});
		return cols;
	}

	/** Create a new domain table model */
	public DomainModel(Session s) {
		super(s, descriptor(s), 16);
	}

	/** Create an object with the given name */
	@Override
	public void createObject(String name) {
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("block", "0.0.0.0/0");
		descriptor.cache.createObject(name, attrs);
	}
}
