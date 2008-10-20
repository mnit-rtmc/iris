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
package us.mn.state.dot.tms.client.roads;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.DetectorManager;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;

/**
 * Table model for r_node detectors
 *
 * @author Douglas Lau
 */
public class R_NodeDetectorModel extends ProxyTableModel<Detector> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Detector name column number */
	static protected final int COL_NAME = 0;

	/** Detector label column number */
	static protected final int COL_LABEL = 1;

	/** R_Node in question */
	protected final R_Node r_node;

	/** No r_node detector model */
	protected final WrapperComboBoxModel det_model;

	/** Create a new r_node detector table model */
	public R_NodeDetectorModel(TypeCache<Detector> c, R_Node n) {
		super(c, true);
		r_node = n;
		det_model = new WrapperComboBoxModel(
			Session.det_manager_singleton.getStyleModel(
			DetectorManager.STYLE_NO_R_NODE), true);
		initialize();
	}


	/** Add a new proxy to the list model */
	protected int doProxyAdded(Detector proxy) {
		if(proxy.getR_Node() == r_node)
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Detector d = getProxy(row);
		if(d == null)
			return null;
		switch(column) {
			case COL_NAME:
				return d.getName();
			case COL_LABEL:
				return DetectorHelper.getLabel(d);
		}
		return null;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		return column == COL_NAME;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		if(column == COL_NAME) {
			if(value == null || value instanceof Detector)
				setRowDetector(row, (Detector)value);
		}
	}

	/** Set the detector at the specified row */
	protected void setRowDetector(int row, Detector nd) {
		Detector od = getProxy(row);
		if(od != nd) {
			if(od != null)
				od.setR_Node(null);
			if(nd != null)
				nd.setR_Node(r_node);
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createNameColumn());
		m.addColumn(createColumn(COL_LABEL, 140, "Label"));
		return m;
	}

	/** Create the detector name column */
	protected TableColumn createNameColumn() {
		TableColumn c = new TableColumn(COL_NAME, 80);
		c.setHeaderValue("Detector");
		c.setCellEditor(new NameCellEditor());
		return c;
	}

	/** Inner class for editing cells in the name column */
	protected class NameCellEditor extends AbstractCellEditor
		implements TableCellEditor
	{
		protected final JComboBox combo = new JComboBox(det_model);
		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column)
		{
			det_model.setSelectedItem(value);
			return combo;
		}
		public Object getCellEditorValue() {
			return combo.getSelectedItem();
		}
	}
}
