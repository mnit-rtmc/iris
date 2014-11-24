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
package us.mn.state.dot.tms.client.widget;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * ITable is a simple JTable extension which adds a setVisibleRowCount method
 *
 * @author Douglas Lau
 */
public class ITable extends JTable {

	/** Table model */
	private final ITableModel model;

	/** Create a new ITable */
	public ITable(ITableModel m) {
		model = m;
	}

	/** Set the visible row count */
	public void setVisibleRowCount(int c) {
		int h = getRowHeight();
		Dimension d = new Dimension(getPreferredSize().width,
			getRowHeight() * c);
		setPreferredScrollableViewportSize(d);
	}

	/** Get tooltip text for a mouse event */
	@Override
	public String getToolTipText(MouseEvent e) {
		Point p = e.getPoint();
		int v_row = rowAtPoint(p);
		int v_col = columnAtPoint(p);
		if (v_row >= 0 && v_col >= 0) {
			int row = convertRowIndexToModel(v_row);
			int col = convertColumnIndexToModel(v_col);
			return model.getToolTipText(row, col);
		} else
			return null;
	}

	/** Clear the selection */
	@Override
	public void clearSelection() {
		TableCellEditor editor = getCellEditor();
		if (editor != null)
			editor.cancelCellEditing();
		super.clearSelection();
	}
}
