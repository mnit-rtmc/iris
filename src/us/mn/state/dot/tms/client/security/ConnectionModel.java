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
package us.mn.state.dot.tms.client.security;

import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for IRIS connections
 *
 * @author Douglas Lau
 */
public class ConnectionModel extends ProxyTableModel<Connection> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Connection>("Host:Port", 140) {
			public Object getValueAt(Connection c) {
				return c.getName();
			}
		},
		new ProxyColumn<Connection>("User", 180) {
			public Object getValueAt(Connection c) {
				User u = c.getUser();
				if(u != null)
					return u.getName();
				else
					return null;
			}
		},
	    };
	}

	/** Create a new connection table model */
	public ConnectionModel(Session s) {
		super(s, s.getSonarState().getConnections());
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		return super.getRowCount() - 1;
	}
}
