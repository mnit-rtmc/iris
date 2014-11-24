/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.client.proxy.ProxyTableModel2;

/**
 * Table model for capabilities assigned to IRIS roles
 *
 * @author Douglas Lau
 */
public class RoleCapabilityModel extends ProxyTableModel2<Capability> {

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
				return canUpdate(role, "capabilities");
			}
			public void setValueAt(Capability c, Object value) {
				if (value instanceof Boolean)
					setAssigned(c, (Boolean)value);
			}
		});
		return cols;
	}

	/** Check if the given capability is assigned to the selected role */
	protected boolean isAssigned(Capability cap) {
		Role r = role;		// Avoid NPE
		if (r != null) {
			for (Capability c: r.getCapabilities())
				if (c == cap)
					return true;
		}
		return false;
	}

	/** Assign or unassign the specified capability */
	protected void setAssigned(Capability c, boolean a) {
		Role r = role;		// Avoid NPE
		if (r != null) {
			Capability[] caps = r.getCapabilities();
			if (a)
				caps = addCapability(caps, c);
			else
				caps = removeCapability(caps, c);
			r.setCapabilities(caps);
		}
	}

	/** Currently selected role */
	private Role role;

	/** Create a new role-capability table model */
	public RoleCapabilityModel(Session s) {
		super(s, s.getSonarState().getCapabilities(),
		      false,	/* has_properties */
		      false,	/* has_create */
		      false);	/* has_delete */
	}

	/** Set the capabilities for a new role */
	public void setSelectedRole(Role r) {
		role = r;
		fireTableDataChanged();
	}

	/** Update the capabilities for the specified role */
	public void updateRoleCapabilities(Role r) {
		if (r == role)
			fireTableDataChanged();
	}

	/** Add a capability to an array of capabilities */
	protected Capability[] addCapability(Capability[] caps, Capability cap){
		TreeSet<Capability> cs = new TreeSet<Capability>(comparator());
		for (Capability c: caps)
			cs.add(c);
		cs.add(cap);
		return cs.toArray(new Capability[0]);
	}

	/** Remove a capability from an array of capabilities */
	protected Capability[] removeCapability(Capability[] caps,
		Capability cap)
	{
		TreeSet<Capability> cs = new TreeSet<Capability>(comparator());
		for (Capability c: caps)
			cs.add(c);
		cs.remove(cap);
		return cs.toArray(new Capability[0]);
	}
}
