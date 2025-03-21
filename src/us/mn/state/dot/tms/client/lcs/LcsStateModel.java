/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsIndication;
import us.mn.state.dot.tms.LcsState;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for LCS states.
 *
 * @author Douglas Lau
 */
public class LcsStateModel extends ProxyTableModel<LcsState> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<LcsState> descriptor(final Session s) {
		return new ProxyDescriptor<LcsState>(
			s.getSonarState().getLcsCache().getLcsStates(),
			false,  /* has_properties */
			true,   /* has_create_delete */
			false   /* has_name */
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<LcsState>> createColumns() {
		ArrayList<ProxyColumn<LcsState>> cols =
			new ArrayList<ProxyColumn<LcsState>>(4);
		cols.add(new ProxyColumn<LcsState>("lcs.lane", 36,
			Integer.class)
		{
			public Object getValueAt(LcsState ls) {
				return ls.getLane();
			}
			public boolean isEditable(LcsState ls) {
				return canWrite(ls);
			}
			public void setValueAt(LcsState ls, Object val) {
				if (val instanceof Integer)
					ls.setLane((Integer) val);
			}
		});
		cols.add(new ProxyColumn<LcsState>("lcs.indication", 100) {
			public Object getValueAt(LcsState ls) {
				return LcsIndication.fromOrdinal(
					ls.getIndication());
			}
			public boolean isEditable(LcsState ls) {
				return canWrite(ls);
			}
			public void setValueAt(LcsState ls, Object val) {
				if (val instanceof LcsIndication) {
					ls.setIndication(
						((LcsIndication) val).ordinal()
					);
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<LcsIndication> cbx =
					new JComboBox<LcsIndication>(
						LcsIndication.values()
					);
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<LcsState>("msg.pattern", 160) {
			public Object getValueAt(LcsState ls) {
				return ls.getMsgPattern();
			}
			public boolean isEditable(LcsState ls) {
				return canWrite(ls);
			}
			public void setValueAt(LcsState ls, Object val) {
				ls.setMsgPattern(
					MsgPatternHelper.lookup(val.toString())
				);
			}
		});
		cols.add(new ProxyColumn<LcsState>("lcs.msg", 80, Integer.class)
		{
			public Object getValueAt(LcsState ls) {
				return ls.getMsgNum();
			}
			public boolean isEditable(LcsState ls) {
				return canWrite(ls);
			}
			public void setValueAt(LcsState ls, Object val) {
				ls.setMsgNum((val instanceof Integer)
					? (Integer) val
					: null);
			}
		});
		return cols;
	}

	/** LCS array */
	private final Lcs lcs;

	/** Create a new LCS state model */
	public LcsStateModel(Session s, Lcs l) {
		super(s, descriptor(s), 12);
		lcs = l;
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(LcsState ls) {
		return ls.getLcs() == lcs;
	}

	/** Create a new LCS state */
	@Override
	public void createObject(String name) {
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("lcs", lcs);
		attrs.put("lane", Integer.valueOf(1));
		attrs.put("indication", Integer.valueOf(0));
		descriptor.cache.createObject(name, attrs);
	}

	/** Check if the user can remove a proxy */
	@Override
	public boolean canRemove(LcsState proxy) {
		return session.canWrite(proxy) &&
		      (getIndex(proxy) == getRowCount() - 1);
	}
}
