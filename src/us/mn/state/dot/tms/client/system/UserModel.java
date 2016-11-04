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
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for IRIS users
 *
 * @author Douglas Lau
 */
public class UserModel extends ProxyTableModel<User> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<User> descriptor(Session s) {
		return new ProxyDescriptor<User>(
			s.getSonarState().getUsers(), false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<User>> createColumns() {
		ArrayList<ProxyColumn<User>> cols =
			new ArrayList<ProxyColumn<User>>(1);
		cols.add(new ProxyColumn<User>("user", 100) {
			public Object getValueAt(User u) {
				return u.getName();
			}
		});
		return cols;
	}

	/** Create a new user table model */
	public UserModel(Session s) {
		super(s, descriptor(s), 16);
	}
}
