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

import java.awt.Component;
import java.util.HashMap;
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
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<LaneUseMulti>("Name", 80) {
			public Object getValueAt(LaneUseMulti lum) {
				return lum.getName();
			}
		},
		new ProxyColumn<LaneUseMulti>("Indication", 100) {
			public Object getValueAt(LaneUseMulti lum) {
				return lum.getIndication();
			}
			public boolean isEditable(LaneUseMulti lum) {
				if(lum != null)
					return canUpdate(lum);
				else
					return canAdd();
			}
			public void setValueAt(LaneUseMulti lum, Object value) {
				String v = value.toString();
				int ind = lookupIndication(v);
				if(lum != null)
					lum.setIndication(ind);
				else
					createObject(ind);
			}
			protected TableCellRenderer createCellRenderer() {
				return new IndicationCellRenderer();
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(
					LaneUseIndication.getDescriptions());
				return new DefaultCellEditor(combo);
			}
		},
		new ProxyColumn<LaneUseMulti>("Msg #", 80, Integer.class) {
			public Object getValueAt(LaneUseMulti lum) {
				return lum.getMsgNum();
			}
			public boolean isEditable(LaneUseMulti lum) {
				return canUpdate(lum);
			}
			public void setValueAt(LaneUseMulti lum, Object value) {
				if(value instanceof Integer)
					lum.setMsgNum((Integer)value);
				else
					lum.setMsgNum(null);
			}
		},
		new ProxyColumn<LaneUseMulti>("Width", 80, Integer.class) {
			public Object getValueAt(LaneUseMulti lum) {
				return lum.getWidth();
			}
			public boolean isEditable(LaneUseMulti lum) {
				return canUpdate(lum);
			}
			public void setValueAt(LaneUseMulti lum, Object value) {
				if(value instanceof Integer)
					lum.setWidth((Integer)value);
			}
		},
		new ProxyColumn<LaneUseMulti>("Height", 80, Integer.class) {
			public Object getValueAt(LaneUseMulti lum) {
				return lum.getHeight();
			}
			public boolean isEditable(LaneUseMulti lum) {
				return canUpdate(lum);
			}
			public void setValueAt(LaneUseMulti lum, Object value) {
				if(value instanceof Integer)
					lum.setHeight((Integer)value);
			}
		},
		new ProxyColumn<LaneUseMulti>("Quick Message", 160) {
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
		}
	    };
	}

	/** Create a new graphic table model */
	public LaneUseMultiModel(Session s) {
		super(s, s.getSonarState().getLcsCache().getLaneUseMultis());
	}

	/** Lookup a lane-use indication */
	protected int lookupIndication(String desc) {
		for(LaneUseIndication lui: LaneUseIndication.values()) {
			if(desc.equals(lui.description))
				return lui.ordinal();
		}
		return 0;
	}

	/** Create a new lane-use MULTI */
	protected void createObject(int ind) {
		String name = createUniqueName();
		if(name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("indication", ind);
			cache.createObject(name, attrs);
		}
	}

	/** Create a unique LaneUseMulti name */
	protected String createUniqueName() {
		for(int uid = 1; uid <= 256; uid++) {
			String n = "LUM_" + uid;
			if(LaneUseMultiHelper.lookup(n) == null)
				return n;
		}
		assert false;
		return null;
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return LaneUseMulti.SONAR_TYPE;
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
		if(value instanceof Integer) {
			return LaneUseIndication.fromOrdinal(
				(Integer)value).description;
		} else
			return null;
	}
}
