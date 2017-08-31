/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2017  Minnesota Department of Transportation
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
import java.util.TreeSet;
import us.mn.state.dot.sonar.Capability;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for capabilities assigned to IRIS roles
 *
 * @author Douglas Lau
 */
public class RoleCapabilityModel extends ProxyTableModel<Capability> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Capability> descriptor(Session s) {
		return new ProxyDescriptor<Capability>(
			s.getSonarState().getCapabilities(),
			false,	/* has_properties */
			false,	/* has_create_delete */
			false	/* has_name */
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Capability>> createColumns() {
		ArrayList<ProxyColumn<Capability>> cols =
			new ArrayList<ProxyColumn<Capability>>(2);
		cols.add(new ProxyColumn<Capability>("capability", 120) {
			public Object getValueAt(Capability c) {
				return c.getName();
			}
		});
		cols.add(new ProxyColumn<Capability>("capability.assigned", 80,
			Boolean.class)
		{
			public Object getValueAt(Capability c) {
				return isAssigned(c);
			}
			public boolean isEditable(Capability c) {
				return canWrite(role, "capabilities");
			}
			public void setValueAt(Capability c, Object value) {
				if (value instanceof Boolean)
					setAssigned(c, (Boolean)value);
			}
		});
		return cols;
	}

	/** Check if the given capability is assigned to the role */
	private boolean isAssigned(Capability cap) {
		if (role != null) {
			for (Capability c: role.getCapabilities())
				if (c == cap)
					return true;
		}
		return false;
	}

	/** Assign or unassign the specified capability */
	private void setAssigned(Capability c, boolean a) {
		if (role != null) {
			Capability[] caps = role.getCapabilities();
			if (a)
				caps = addCapability(caps, c);
			else
				caps = removeCapability(caps, c);
			role.setCapabilities(caps);
		}
	}

	/** Role for associated capabilities */
	private final Role role;

	/** Create a new role-capability table model */
	public RoleCapabilityModel(Session s, Role r) {
		super(s, descriptor(s), 16);
		role = r;
	}

	/** Add a capability to an array of capabilities */
	private Capability[] addCapability(Capability[] caps, Capability cap) {
		TreeSet<Capability> cs = new TreeSet<Capability>(comparator());
		for (Capability c: caps)
			cs.add(c);
		cs.add(cap);
		return cs.toArray(new Capability[0]);
	}

	/** Remove a capability from an array of capabilities */
	private Capability[] removeCapability(Capability[] caps,
		Capability cap)
	{
		TreeSet<Capability> cs = new TreeSet<Capability>(comparator());
		for (Capability c: caps)
			cs.add(c);
		cs.remove(cap);
		return cs.toArray(new Capability[0]);
	}
}
