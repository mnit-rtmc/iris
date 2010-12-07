/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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

import java.util.TreeSet;
import us.mn.state.dot.sonar.Capability;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for capabilities assigned to IRIS roles
 *
 * @author Douglas Lau
 */
public class RoleCapabilityModel extends ProxyTableModel<Capability> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Capability>("Capability", 120) {
			public Object getValueAt(Capability c) {
				return c.getName();
			}
		},
		new ProxyColumn<Capability>("Assigned", 80, Boolean.class) {
			public Object getValueAt(Capability c) {
				return isAssigned(c);
			}
			public boolean isEditable(Capability c) {
				return canUpdateRoleCapabilities();
			}
			public void setValueAt(Capability c, Object value) {
				if(value instanceof Boolean)
					setAssigned(c, (Boolean)value);
			}
		}
	    };
	}

	/** Check if the given capability is assigned to the selected role */
	protected boolean isAssigned(Capability cap) {
		Role r = role;		// Avoid NPE
		if(r != null) {
			for(Capability c: r.getCapabilities())
				if(c == cap)
					return true;
		}
		return false;
	}

	/** Assign or unassign the specified capability */
	protected void setAssigned(Capability c, boolean a) {
		Role r = role;		// Avoid NPE
		if(r != null) {
			Capability[] caps = r.getCapabilities();
			if(a)
				caps = addCapability(caps, c);
			else
				caps = removeCapability(caps, c);
			r.setCapabilities(caps);
		}
	}

	/** Currently selected role */
	protected Role role;

	/** Create a new role-capability table model */
	public RoleCapabilityModel(Session s) {
		super(s, s.getSonarState().getCapabilities());
	}

	/** Set the capabilities for a new role */
	public void setSelectedRole(Role r) {
		role = r;
		fireTableDataChanged();
	}

	/** Update the capabilities for the specified role */
	public void updateRoleCapabilities(Role r) {
		if(r == role)
			fireTableDataChanged();
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		return super.getRowCount() - 1;
	}

	/** Add a capability to an array of capabilities */
	protected Capability[] addCapability(Capability[] caps, Capability cap){
		TreeSet<Capability> cap_set = createProxySet();
		for(Capability c: caps)
			cap_set.add(c);
		cap_set.add(cap);
		return cap_set.toArray(new Capability[0]);
	}

	/** Remove a capability from an array of capabilities */
	protected Capability[] removeCapability(Capability[] caps,
		Capability cap)
	{
		TreeSet<Capability> cap_set = createProxySet();
		for(Capability c: caps)
			cap_set.add(c);
		cap_set.remove(cap);
		return cap_set.toArray(new Capability[0]);
	}

	/** Check if the user can update role capabilities */
	protected boolean canUpdateRoleCapabilities() {
		return session.canUpdate(role, "capabilities");
	}
}
