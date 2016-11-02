/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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
import java.util.HashMap;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for LCS within an array.
 *
 * @author Douglas Lau
 */
public class LCSTableModel extends ProxyTableModel<LCS> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<LCS> descriptor(final Session s) {
		return new ProxyDescriptor<LCS>(
			s.getSonarState().getLcsCache().getLCSs(), true
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<LCS>> createColumns() {
		ArrayList<ProxyColumn<LCS>> cols =
			new ArrayList<ProxyColumn<LCS>>(2);
		cols.add(new ProxyColumn<LCS>("lcs.lane", 36, Integer.class) {
			public Object getValueAt(LCS lcs) {
				return lcs.getLane();
			}
		});
		cols.add(new ProxyColumn<LCS>("device.name", 140) {
			public Object getValueAt(LCS lcs) {
				return lcs.getName();
			}
		});
		return cols;
	}

	/** LCS array */
	private final LCSArray lcs_array;

	/** Create a new LCS table model */
	public LCSTableModel(Session s, LCSArray la) {
		super(s, descriptor(s),
		      true,	/* has_create_delete */
		      true);	/* has_name */
		lcs_array = la;
	}

	/** Get the visible row count */
	@Override
	public int getVisibleRowCount() {
		return 12;
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(LCS proxy) {
		return proxy.getArray() == lcs_array;
	}

	/** Show the properties form for a proxy */
	@Override
	public void showPropertiesForm(LCS proxy) {
		DMS dms = DMSHelper.lookup(proxy.getName());
		if (dms != null)
			session.getDMSManager().showPropertiesForm(dms);
	}

	/** Create a new LCS */
	@Override
	public void createObject(String name) {
		int lane = getRowCount() + 1;
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("lcsArray", lcs_array);
		attrs.put("lane", new Integer(lane));
		descriptor.cache.createObject(name, attrs);
	}

	/** Check if the user can remove a proxy */
	@Override
	public boolean canRemove(LCS proxy) {
		return session.canRemove(proxy) &&
		      (getIndex(proxy) == getRowCount() - 1);
	}
}
