/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for IRIS users
 *
 * @author Douglas Lau
 */
public class UserModel extends ProxyTableModel<User> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<User>("User", 100) {
			public Object getValueAt(User u) {
				return u.getName();
			}
			public boolean isEditable(User u) {
				return u == null && canAdd();
			}
			public void setValueAt(User u, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<User>("Full Name", 180) {
			public Object getValueAt(User u) {
				return u.getFullName();
			}
			public boolean isEditable(User u) {
				return canUpdate(u);
			}
			public void setValueAt(User u, Object value) {
				u.setFullName(value.toString().trim());
			}
		},
		new ProxyColumn<User>("Dn", 420) {
			public Object getValueAt(User u) {
				return u.getDn();
			}
			public boolean isEditable(User u) {
				return canUpdate(u);
			}
			public void setValueAt(User u, Object value) {
				u.setDn(value.toString().trim());
			}
		}
	    };
	}

	/** User role model */
	protected final UserRoleModel rmodel;

	/** Create a new user table model */
	public UserModel(Session s, UserRoleModel r) {
		super(s, s.getSonarState().getUsers());
		rmodel = r;
	}

	/** Change a user in the table model */
	protected void proxyChangedSlow(User proxy, String attrib) {
		super.proxyChangedSlow(proxy, attrib);
		if(attrib.equals("roles"))
			rmodel.updateUserRoles(proxy);
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return User.SONAR_TYPE;
	}

	/** Check if the user can remove a user */
	public boolean canRemove(User u) {
		return u != null && u.getRoles().length == 0 &&
		       super.canRemove(u);
	}
}
