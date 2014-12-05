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

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.DefaultCellEditor;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LaneUseMultiHelper;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for lane-use MULTI.
 *
 * @author Douglas Lau
 */
public class LaneUseMultiModel extends ProxyTableModel<LaneUseMulti> {

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
				return lum.getIndication();
			}
			public boolean isEditable(LaneUseMulti lum) {
				return canUpdate(lum);
			}
			public void setValueAt(LaneUseMulti lum, Object value) {
				String v = value.toString();
				int ind = lookupIndication(v);
				if (lum != null)
					lum.setIndication(ind);
			}
			protected TableCellRenderer createCellRenderer() {
				return new IndicationCellRenderer();
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(
					LaneUseIndication.getDescriptions());
				return new DefaultCellEditor(combo);
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
		super(s, s.getSonarState().getLcsCache().getLaneUseMultis(),
		      false,	/* has_properties */
		      true,	/* has_create_delete */
		      false);	/* has_name */
	}

	/** Lookup a lane-use indication */
	private int lookupIndication(String desc) {
		for (LaneUseIndication lui: LaneUseIndication.values()) {
			if (desc.equals(lui.description))
				return lui.ordinal();
		}
		return 0;
	}

	/** Create a new lane-use MULTI */
	@Override
	public void createObject(String n) {
		// Ignore name given to us
		String name = createUniqueName();
		if (name != null)
			cache.createObject(name);
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

	/** Get the SONAR type name */
	@Override
	protected String getSonarType() {
		return LaneUseMulti.SONAR_TYPE;
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

	/** Indication cell renderer */
	static protected class IndicationCellRenderer
		extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			return super.getTableCellRendererComponent(table,
				getIndication(value), isSelected, hasFocus,
				row, column);
		}
	}

	/** Get an indication description */
	static protected String getIndication(Object value) {
		if (value instanceof Integer) {
			return LaneUseIndication.fromOrdinal(
				(Integer)value).description;
		} else
			return null;
	}
}
