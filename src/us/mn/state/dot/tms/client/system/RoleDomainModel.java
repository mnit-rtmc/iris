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
import java.util.TreeSet;
import us.mn.state.dot.sonar.Domain;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for domains assigned to IRIS roles.
 *
 * @author Douglas Lau
 */
public class RoleDomainModel extends ProxyTableModel<Domain> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Domain> descriptor(Session s) {
		return new ProxyDescriptor<Domain>(
			s.getSonarState().getDomains(),
			false,	/* has_properties */
			false,	/* has_create_delete */
			false	/* has_name */
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Domain>> createColumns() {
		ArrayList<ProxyColumn<Domain>> cols =
			new ArrayList<ProxyColumn<Domain>>(2);
		cols.add(new ProxyColumn<Domain>("domain", 120) {
			public Object getValueAt(Domain d) {
				return d.getName();
			}
		});
		cols.add(new ProxyColumn<Domain>("domain.assigned", 80,
			Boolean.class)
		{
			public Object getValueAt(Domain d) {
				return isAssigned(d);
			}
			public boolean isEditable(Domain d) {
				return canWrite(role, "domains");
			}
			public void setValueAt(Domain d, Object value) {
				if (value instanceof Boolean)
					setAssigned(d, (Boolean) value);
			}
		});
		return cols;
	}

	/** Check if the given domain is assigned to the role */
	private boolean isAssigned(Domain dom) {
		if (role != null) {
			for (Domain d: role.getDomains())
				if (d == dom)
					return true;
		}
		return false;
	}

	/** Assign or unassign the specified domain */
	private void setAssigned(Domain d, boolean a) {
		if (role != null) {
			Domain[] doms = role.getDomains();
			if (a)
				doms = addDomain(doms, d);
			else
				doms = removeDomain(doms, d);
			role.setDomains(doms);
		}
	}

	/** Role for associated domains */
	private final Role role;

	/** Create a new role-domain table model */
	public RoleDomainModel(Session s, Role r) {
		super(s, descriptor(s), 16);
		role = r;
	}

	/** Add a domain to an array of domains */
	private Domain[] addDomain(Domain[] doms, Domain dom) {
		TreeSet<Domain> dset = new TreeSet<Domain>(comparator());
		for (Domain d: doms)
			dset.add(d);
		dset.add(dom);
		return dset.toArray(new Domain[0]);
	}

	/** Remove a domain from an array of domains */
	private Domain[] removeDomain(Domain[] doms, Domain dom) {
		TreeSet<Domain> dset = new TreeSet<Domain>(comparator());
		for (Domain d: doms)
			dset.add(d);
		dset.remove(dom);
		return dset.toArray(new Domain[0]);
	}
}
