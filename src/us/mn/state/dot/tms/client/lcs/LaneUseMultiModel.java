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
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LaneUseMultiHelper;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for lane-use MULTI.
 *
 * @author Douglas Lau
 */
public class LaneUseMultiModel extends ProxyTableModel<LaneUseMulti> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 6;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Indication column number */
	static protected final int COL_INDICATION = 1;

	/** Message number column number */
	static protected final int COL_MSG_NUM = 2;

	/** Width column number */
	static protected final int COL_WIDTH = 3;

	/** Height column number */
	static protected final int COL_HEIGHT = 4;

	/** Quick message column number */
	static protected final int COL_Q_MSG = 5;

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, "Name", 80));
		m.addColumn(createIndicationColumn());
		m.addColumn(createColumn(COL_MSG_NUM, "Msg #", 80));
		m.addColumn(createColumn(COL_WIDTH, "Width", 80));
		m.addColumn(createColumn(COL_HEIGHT, "Height", 80));
		m.addColumn(createColumn(COL_Q_MSG, "Quick Message", 160));
		return m;
	}

	/** Create the indication column */
	static protected TableColumn createIndicationColumn() {
		TableColumn col = createColumn(COL_INDICATION, "Indication",
			100);
		JComboBox combo = new JComboBox(
			LaneUseIndication.getDescriptions());
		col.setCellEditor(new DefaultCellEditor(combo));
		col.setCellRenderer(new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column)
			{
				return super.getTableCellRendererComponent(
					table, getIndication(value),
					isSelected, hasFocus, row, column);
			}
		});
		return col;
	}

	/** Get an indication description */
	static protected String getIndication(Object value) {
		if(value instanceof Integer) {
			return LaneUseIndication.fromOrdinal(
				(Integer)value).description;
		} else
			return null;
	}

	/** Create a new table column */
	static protected TableColumn createColumn(int col, String name, int w) {
		TableColumn c = new TableColumn(col, w);
		c.setHeaderValue(name);
		return c;
	}

	/** Create a new graphic table model */
	public LaneUseMultiModel(Session s) {
		super(s, s.getSonarState().getLcsCache().getLaneUseMultis());
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the column class */
	public Class getColumnClass(int column) {
		switch(column) {
		case COL_INDICATION:
		case COL_MSG_NUM:
		case COL_WIDTH:
		case COL_HEIGHT:
			return Integer.class;
		default:
			return String.class;
		}
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		LaneUseMulti p = getProxy(row);
		if(p == null)
			return null;
		switch(column) {
		case COL_NAME:
			return p.getName();
		case COL_INDICATION:
			return p.getIndication();
		case COL_MSG_NUM:
			return p.getMsgNum();
		case COL_WIDTH:
			return p.getWidth();
		case COL_HEIGHT:
			return p.getHeight();
		case COL_Q_MSG:
			return p.getQuickMessage();
		default:
			return null;
		}
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int col) {
		LaneUseMulti lum = getProxy(row);
		if(lum != null)
			return (col != COL_NAME) && canUpdate(lum);
		else
			return (col == COL_INDICATION) && canAdd();
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		LaneUseMulti p = getProxy(row);
		if(p == null) {
			String v = value.toString();
			if(column == COL_INDICATION)
				createObject(lookupIndication(v));
			return;
		}
		switch(column) {
		case COL_INDICATION:
			String v = value.toString();
			p.setIndication(lookupIndication(v));
			break;
		case COL_MSG_NUM:
			if(value instanceof Integer)
				p.setMsgNum((Integer)value);
			else
				p.setMsgNum(null);
			break;
		case COL_WIDTH:
			if(value instanceof Integer)
				p.setWidth((Integer)value);
			break;
		case COL_HEIGHT:
			if(value instanceof Integer)
				p.setHeight((Integer)value);
			break;
		case COL_Q_MSG:
			p.setQuickMessage(QuickMessageHelper.lookup(
				value.toString()));
			break;
		}
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

	/** Check if the user can add a proxy */
	public boolean canAdd() {
		return namespace.canAdd(user,
		       new Name(LaneUseMulti.SONAR_TYPE));
	}
}
