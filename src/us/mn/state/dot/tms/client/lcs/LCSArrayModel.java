/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.util.ArrayList;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for LCS arrays.
 *
 * @author Douglas Lau
 */
public class LCSArrayModel extends ProxyTableModel<LCSArray> {

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<LCSArray>> createColumns() {
		ArrayList<ProxyColumn<LCSArray>> cols =
			new ArrayList<ProxyColumn<LCSArray>>(2);
		cols.add(new ProxyColumn<LCSArray>("lcs_array", 200) {
			public Object getValueAt(LCSArray a) {
				return a.getName();
			}
		});
		cols.add(new ProxyColumn<LCSArray>("location", 300) {
			public Object getValueAt(LCSArray a) {
				return LCSArrayHelper.lookupLocation(a);
			}
		});
		return cols;
	}

	/** Create a new LCS array table model */
	public LCSArrayModel(Session s) {
		super(s, LCSArrayManager.descriptor(s),
		      true,	/* has_create_delete */
		      true);	/* has_name */
	}
}
