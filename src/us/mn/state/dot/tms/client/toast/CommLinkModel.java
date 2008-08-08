/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.awt.Color;
import java.awt.Component;
import java.util.LinkedList;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for comm links
 *
 * @author Douglas Lau
 */
public class CommLinkModel extends ProxyTableModel<CommLink> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 6;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Description column number */
	static protected final int COL_DESCRIPTION = 1;

	/** URL column number */
	static protected final int COL_URL = 2;

	/** Link connection status */
	static protected final int COL_STATUS = 3;

	/** Protocol column number */
	static protected final int COL_PROTOCOL = 4;

	/** Timeout column number */
	static protected final int COL_TIMEOUT = 5;

	/** List of all possible protocol selections */
	static protected final LinkedList<String> PROTOCOLS =
		new LinkedList<String>();
	static {
		for(String p: CommLink.PROTOCOLS)
			PROTOCOLS.add(p);
	}

	/** Create a new comm link table model */
	public CommLinkModel(TypeCache<CommLink> c) {
		super(c, true);
		initialize();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		CommLink c = getProxy(row);
		if(c == null)
			return null;
		switch(column) {
			case COL_NAME:
				return c.getName();
			case COL_DESCRIPTION:
				return c.getDescription();
			case COL_URL:
				return c.getUrl();
			case COL_STATUS:
				return c.getStatus();
			case COL_PROTOCOL:
				return PROTOCOLS.get(c.getProtocol());
			case COL_TIMEOUT:
				return c.getTimeout();
			default:
				return null;
		}
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(column == COL_STATUS)
			return false;
		if(isLastRow(row))
			return column == COL_NAME;
		else
			return column != COL_NAME;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		String v = value.toString().trim();
		CommLink c = getProxy(row);
		switch(column) {
			case COL_NAME:
				if(v.length() > 0)
					cache.createObject(v);
				break;
			case COL_DESCRIPTION:
				c.setDescription(v);
				break;
			case COL_URL:
				c.setUrl(v);
				break;
			case COL_PROTOCOL:
				c.setProtocol(Short.valueOf(
					(short)PROTOCOLS.indexOf(value)));
				break;
			case COL_TIMEOUT:
				c.setTimeout((Integer)value);
				break;
		}
	}

	/** Create the status column */
	protected TableColumn createStatusColumn() {
		TableColumn c = new TableColumn(COL_STATUS, 44);
		c.setHeaderValue("Status");
		c.setCellRenderer(new StatusCellRenderer());
		return c;
	}

	/** Create the protocol column */
	protected TableColumn createProtocolColumn() {
		TableColumn c = new TableColumn(COL_PROTOCOL, 140);
		c.setHeaderValue("Protocol");
		JComboBox combo = new JComboBox(PROTOCOLS.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the timeout column */
	protected TableColumn createTimeoutColumn() {
		TableColumn c = new TableColumn(COL_TIMEOUT, 60);
		c.setHeaderValue("Timeout");
		c.setCellEditor(new TimeoutEditor());
		return c;
	}

	/** Renderer for link status in a table cell */
	public class StatusCellRenderer extends DefaultTableCellRenderer {
		protected final Icon ok = new CommLinkIcon(Color.BLUE);
		protected final Icon fail = new CommLinkIcon(Color.GRAY);
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			JLabel label =
				(JLabel)super.getTableCellRendererComponent(
				table, "", isSelected, hasFocus, row,
				column);
			if(value == null)
				label.setIcon(null);
			else if("".equals(value))
				label.setIcon(ok);
			else
				label.setIcon(fail);
			return label;
		}
	}

	/** Editor for timeout values in a table cell */
	public class TimeoutEditor extends AbstractCellEditor
		implements TableCellEditor
	{
		protected final SpinnerNumberModel model =
			new SpinnerNumberModel(0, 0, 8000, 50);
		protected final JSpinner spinner = new JSpinner(model);

		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column)
		{
			spinner.setValue(value);
			return spinner;
		}
		public Object getCellEditorValue() {
			return spinner.getValue();
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 90, "Comm Link"));
		m.addColumn(createColumn(COL_DESCRIPTION, 220, "Description"));
		m.addColumn(createColumn(COL_URL, 280, "URL"));
		m.addColumn(createStatusColumn());
		m.addColumn(createProtocolColumn());
		m.addColumn(createTimeoutColumn());
		return m;
	}
}
