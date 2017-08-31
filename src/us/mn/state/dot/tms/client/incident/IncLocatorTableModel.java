/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.IncLocator;
import us.mn.state.dot.tms.IncRange;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Table model for incident locators.
 *
 * @author Douglas Lau
 */
public class IncLocatorTableModel extends ProxyTableModel<IncLocator> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<IncLocator> descriptor(Session s) {
		return new ProxyDescriptor<IncLocator>(
			s.getSonarState().getIncCache().getIncLocators(),
			false,	/* has_properties */
			true,	/* has_create_delete */
			false	/* has_name */
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<IncLocator>> createColumns() {
		ArrayList<ProxyColumn<IncLocator>> cols =
			new ArrayList<ProxyColumn<IncLocator>>(5);
		cols.add(new ProxyColumn<IncLocator>("dms.group", 108) {
			public Object getValueAt(IncLocator loc) {
				return loc.getSignGroup();
			}
			public boolean isEditable(IncLocator loc) {
				return canWrite(loc);
			}
			public void setValueAt(IncLocator loc, Object value) {
				String v = value.toString().trim();
				SignGroup sg = SignGroupHelper.lookup(v);
				if (sg != null)
					loc.setSignGroup(sg);
			}
		});
		cols.add(new ProxyColumn<IncLocator>("incident.range", 96) {
			public Object getValueAt(IncLocator loc) {
				return IncRange.fromOrdinal(loc.getRange());
			}
			public boolean isEditable(IncLocator loc) {
				return canWrite(loc);
			}
			public void setValueAt(IncLocator loc, Object value) {
				if (value instanceof IncRange) {
					IncRange r = (IncRange) value;
					loc.setRange(r.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<IncRange> cbx = new JComboBox
					<IncRange>(IncRange.values());
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<IncLocator>("incident.branched", 64,
			Boolean.class)
		{
			public Object getValueAt(IncLocator loc) {
				return loc.getBranched();
			}
			public boolean isEditable(IncLocator loc) {
				return canWrite(loc);
			}
			public void setValueAt(IncLocator loc, Object value) {
				if (value instanceof Boolean) {
					Boolean p = (Boolean) value;
					loc.setBranched(p);
				}
			}
		});
		cols.add(new ProxyColumn<IncLocator>("r_node.pickable", 64,
			Boolean.class)
		{
			public Object getValueAt(IncLocator loc) {
				return loc.getPickable();
			}
			public boolean isEditable(IncLocator loc) {
				return canWrite(loc);
			}
			public void setValueAt(IncLocator loc, Object value) {
				if (value instanceof Boolean) {
					Boolean p = (Boolean) value;
					loc.setPickable(p);
				}
			}
		});
		cols.add(new ProxyColumn<IncLocator>("dms.multi.string",
			512)
		{
			public Object getValueAt(IncLocator loc) {
				return loc.getMulti();
			}
			public boolean isEditable(IncLocator loc) {
				return canWrite(loc);
			}
			public void setValueAt(IncLocator loc, Object value){
				loc.setMulti(new MultiString(value.toString())
					.normalize());
			}
		});
		return cols;
	}

	/** Create a new table model.
	 * @param s Session */
	public IncLocatorTableModel(Session s) {
		super(s, descriptor(s), 12, 20);
	}

	/** Get a proxy comparator */
	@Override
	protected Comparator<IncLocator> comparator() {
		return new Comparator<IncLocator>() {
			public int compare(IncLocator loc0, IncLocator loc1) {
				SignGroup sg0 = loc0.getSignGroup();
				SignGroup sg1 = loc1.getSignGroup();
				int c = sg0.getName().compareTo(sg1.getName());
				if (c != 0)
					return c;
				int r0 = loc0.getRange();
				int r1 = loc1.getRange();
				if (r0 != r1)
					return r0 - r1;
				return loc0.getName().compareTo(loc1.getName());
			}
		};
	}

	/** Create a new incident locator */
	public void create(SignGroup sg) {
		String name = createUniqueName();
		if (name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("sign_group", sg);
			descriptor.cache.createObject(name, attrs);
		}
	}

	/** Create a unique incident locator name */
	private String createUniqueName() {
		for (int uid = 1; uid <= 99999; uid++) {
			String n = String.format("iloc_%05d", uid);
			if (descriptor.cache.lookupObject(n) == null)
				return n;
		}
		return null;
	}
}
