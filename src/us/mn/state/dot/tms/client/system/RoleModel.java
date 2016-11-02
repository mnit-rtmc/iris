/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for IRIS roles.
 *
 * @author Douglas Lau
 */
public class RoleModel extends ProxyTableModel<Role> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Role> descriptor(Session s) {
		return new ProxyDescriptor<Role>(
			s.getSonarState().getRoles(), false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Role>> createColumns() {
		ArrayList<ProxyColumn<Role>> cols =
			new ArrayList<ProxyColumn<Role>>(2);
		cols.add(new ProxyColumn<Role>("role.name", 160) {
			public Object getValueAt(Role r) {
				return r.getName();
			}
		});
		cols.add(new ProxyColumn<Role>("role.enabled", 60,
			Boolean.class)
		{
			public Object getValueAt(Role r) {
				return r.getEnabled();
			}
			public boolean isEditable(Role r) {
				return canUpdate(r);
			}
			public void setValueAt(Role r, Object value) {
				if (value instanceof Boolean)
					r.setEnabled((Boolean)value);
			}
		});
		return cols;
	}

	/** Create a new role table model */
	public RoleModel(Session s) {
		super(s, descriptor(s),
		      true,	/* has_create_delete */
		      true);	/* has_name */
	}

	/** Check if the user can remove a role */
	@Override
	public boolean canRemove(Role r) {
		return super.canRemove(r) && r.getCapabilities().length == 0;
	}
}
