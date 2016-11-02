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

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LaneUseMultiHelper;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for lane-use MULTI.
 *
 * @author Douglas Lau
 */
public class LaneUseMultiModel extends ProxyTableModel<LaneUseMulti> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<LaneUseMulti> descriptor(Session s) {
		return new ProxyDescriptor<LaneUseMulti>(
			s.getSonarState().getLcsCache().getLaneUseMultis(),
			false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<LaneUseMulti>> createColumns() {
		ArrayList<ProxyColumn<LaneUseMulti>> cols =
			new ArrayList<ProxyColumn<LaneUseMulti>>(6);
		cols.add(new ProxyColumn<LaneUseMulti>("device.name", 80) {
			public Object getValueAt(LaneUseMulti lum) {
				return lum.getName();
			}
		});
		cols.add(new ProxyColumn<LaneUseMulti>(
			"lane.use.multi.indication", 100)
		{
			public Object getValueAt(LaneUseMulti lum) {
				return LaneUseIndication.fromOrdinal(
					lum.getIndication());
			}
			public boolean isEditable(LaneUseMulti lum) {
				return canUpdate(lum);
			}
			public void setValueAt(LaneUseMulti lum, Object value) {
				if (value instanceof LaneUseIndication) {
					LaneUseIndication v =
						(LaneUseIndication) value;
					lum.setIndication(v.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<LaneUseIndication> cbx =
					new JComboBox<LaneUseIndication>(
					LaneUseIndication.values());
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<LaneUseMulti>("lane.use.multi.msg", 80,
			Integer.class)
		{
			public Object getValueAt(LaneUseMulti lum) {
				return lum.getMsgNum();
			}
			public boolean isEditable(LaneUseMulti lum) {
				return canUpdate(lum);
			}
			public void setValueAt(LaneUseMulti lum, Object value) {
				if (value instanceof Integer)
					lum.setMsgNum((Integer)value);
				else
					lum.setMsgNum(null);
			}
		});
		cols.add(new ProxyColumn<LaneUseMulti>("graphic.width", 80,
			Integer.class)
		{
			public Object getValueAt(LaneUseMulti lum) {
				return lum.getWidth();
			}
			public boolean isEditable(LaneUseMulti lum) {
				return canUpdate(lum);
			}
			public void setValueAt(LaneUseMulti lum, Object value) {
				if (value instanceof Integer)
					lum.setWidth((Integer)value);
			}
		});
		cols.add(new ProxyColumn<LaneUseMulti>("graphic.height", 80,
			Integer.class)
		{
			public Object getValueAt(LaneUseMulti lum) {
				return lum.getHeight();
			}
			public boolean isEditable(LaneUseMulti lum) {
				return canUpdate(lum);
			}
			public void setValueAt(LaneUseMulti lum, Object value) {
				if (value instanceof Integer)
					lum.setHeight((Integer)value);
			}
		});
		cols.add(new ProxyColumn<LaneUseMulti>("dms.quick.message",160){
			public Object getValueAt(LaneUseMulti lum) {
				return lum.getQuickMessage();
			}
			public boolean isEditable(LaneUseMulti lum) {
				return canUpdate(lum);
			}
			public void setValueAt(LaneUseMulti lum, Object value) {
				lum.setQuickMessage(QuickMessageHelper.lookup(
					value.toString()));
			}
		});
		return cols;
	}

	/** Create a new graphic table model */
	public LaneUseMultiModel(Session s) {
		super(s, descriptor(s),
		      true,	/* has_create_delete */
		      false);	/* has_name */
	}

	/** Create a new lane-use MULTI */
	@Override
	public void createObject(String n) {
		// Ignore name given to us
		String name = createUniqueName();
		if (name != null)
			descriptor.cache.createObject(name);
	}

	/** Create a unique LaneUseMulti name */
	private String createUniqueName() {
		for (int uid = 1; uid <= 256; uid++) {
			String n = "LUM_" + uid;
			if (LaneUseMultiHelper.lookup(n) == null)
				return n;
		}
		assert false;
		return null;
	}

	/** Get the row height */
	@Override
	public int getRowHeight() {
		return 22;
	}

	/** Get the visible row count */
	@Override
	public int getVisibleRowCount() {
		return 10;
	}
}
