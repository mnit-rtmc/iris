/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.util.ArrayList;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for sign groups.
 *
 * @author Douglas Lau
 */
public class SignGroupModel extends ProxyTableModel<SignGroup> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<SignGroup> descriptor(Session s) {
		return new ProxyDescriptor<SignGroup>(
			s.getSonarState().getDmsCache().getSignGroups(),
			false,  /* has_properties */
			true,   /* has_create_delete */
			true    /* has_name */
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<SignGroup>> createColumns() {
		ArrayList<ProxyColumn<SignGroup>> cols =
			new ArrayList<ProxyColumn<SignGroup>>(2);
		cols.add(new ProxyColumn<SignGroup>("dms.group", 120) {
			public Object getValueAt(SignGroup sg) {
				return sg.getName();
			}
		});
		return cols;
	}

	/** Create a new sign group table model */
	public SignGroupModel(Session s) {
		super(s, descriptor(s), 16);
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(SignGroup sg) {
		return !sg.getLocal();
	}

	/** Create an object */
	@Override
	public void createObject(String name) {
		descriptor.cache.createObject(name);
	}
}
