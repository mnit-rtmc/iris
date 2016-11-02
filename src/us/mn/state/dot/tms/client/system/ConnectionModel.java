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
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for IRIS connections
 *
 * @author Douglas Lau
 */
public class ConnectionModel extends ProxyTableModel<Connection> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Connection> descriptor(Session s) {
		return new ProxyDescriptor<Connection>(
			s.getSonarState().getConnections(), false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Connection>> createColumns() {
		ArrayList<ProxyColumn<Connection>> cols =
			new ArrayList<ProxyColumn<Connection>>(2);
		cols.add(new ProxyColumn<Connection>("connection.peer", 140) {
			public Object getValueAt(Connection c) {
				return c.getName();
			}
		});
		cols.add(new ProxyColumn<Connection>("user", 80) {
			public Object getValueAt(Connection c) {
				User u = c.getUser();
				if(u != null)
					return u.getName();
				else
					return null;
			}
		});
		return cols;
	}

	/** Create a new connection table model */
	public ConnectionModel(Session s) {
		super(s, descriptor(s),
		      false,	/* has_create_delete */
		      false);	/* has_name */
	}
}
