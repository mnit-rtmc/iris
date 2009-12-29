/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import java.util.HashMap;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for LCS within an array.
 *
 * @author Douglas Lau
 */
public class LCSTableModel extends ProxyTableModel<LCS> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<LCS>("Lane", 36, Integer.class) {
			public Object getValueAt(LCS lcs) {
				return lcs.getLane();
			}
		},
		new ProxyColumn<LCS>("Name", 140) {
			public Object getValueAt(LCS lcs) {
				return lcs.getName();
			}
			public boolean isEditable(LCS lcs) {
				return (lcs == null) && canAdd();
			}
			public void setValueAt(LCS lcs, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0) {
					int row = getRow(lcs);
					if(row >= 0)
						createLCS(v, row + 1);
				}
			}
		}
	    };
	}

	/** Create a new LCS */
	protected void createLCS(String name, int lane) {
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("lcsArray", lcs_array);
		attrs.put("lane", new Integer(lane));
		cache.createObject(name, attrs);
	}

	/** LCS array */
	protected final LCSArray lcs_array;

	/** Create a new LCS table model */
	public LCSTableModel(Session s, LCSArray la) {
		super(s, s.getSonarState().getLcsCache().getLCSs());
		lcs_array = la;
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(LCS proxy) {
		if(proxy.getArray() == lcs_array)
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Check if the user can add an LCS */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(LCS.SONAR_TYPE,"oname"));
	}
}
