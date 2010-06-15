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

import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for IRIS roles
 *
 * @author Douglas Lau
 */
public class RoleModel extends ProxyTableModel<Role> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Role>("Name", 160) {
			public Object getValueAt(Role r) {
				return r.getName();
			}
			public boolean isEditable(Role r) {
				return r == null && canAdd();
			}
			public void setValueAt(Role r, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<Role>("Enabled", 60, Boolean.class) {
			public Object getValueAt(Role r) {
				return r.getEnabled();
			}
			public boolean isEditable(Role r) {
				return canUpdate(r);
			}
			public void setValueAt(Role r, Object value) {
				if(value instanceof Boolean)
					r.setEnabled((Boolean)value);
			}
		}
	    };
	}

	/** Role capability model */
	protected final RoleCapabilityModel rc_model;

	/** Create a new role table model */
	public RoleModel(Session s, RoleCapabilityModel rc) {
		super(s, s.getSonarState().getRoles());
		rc_model = rc;
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return Role.SONAR_TYPE;
	}

	/** Change a role in the table model */
	protected void proxyChangedSlow(Role proxy, String attrib) {
		super.proxyChangedSlow(proxy, attrib);
		if(attrib.equals("capabilities"))
			rc_model.updateRoleCapabilities(proxy);
	}

	/** Check if the user can remove a role */
	public boolean canRemove(Role r) {
		return r != null && r.getCapabilities().length == 0 &&
		       super.canRemove(r);
	}
}
