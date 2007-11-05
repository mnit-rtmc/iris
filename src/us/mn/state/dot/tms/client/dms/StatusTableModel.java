/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.dms;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import us.mn.state.dot.tms.StatusTable;

/**
 * StatusTableModel is a special table model for IRIS StatusTable objects
 *
 * @author Douglas Lau
 */
class StatusTableModel extends AbstractTableModel {

	/** Status table */
	protected final StatusTable stat;

	/** Cell renderer component */
	protected final DefaultTableCellRenderer renderer;

	/** Get a renderer component for this model */
	public TableCellRenderer getRenderer() { return renderer; }

	/** Create a new counter model */
	public StatusTableModel(StatusTable t) {
		stat = t;
		renderer = new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column)
			{
				this.setBackground(new Color(
					stat.getBackgroundAt(row, column)));
				return super.getTableCellRendererComponent(
					table, value, false, hasFocus,
					row, column);
			}
		};
	}

	/** Get the column count */
	public int getColumnCount() { return stat.getColumnCount(); }

	/** Get the row count */
	public int getRowCount() { return stat.getRowCount(); }

	/** Get the value at a specific cell */
	public Object getValueAt(int row, int column) {
		return stat.getValueAt(row, column);
	}

	/** Create a column model to match */
	public TableColumnModel createColumnModel() {
		TableColumnModel model = new DefaultTableColumnModel();
		for(int i = 0; i < stat.getColumnCount(); i++) {
			TableColumn column = new TableColumn(i);
			column.setHeaderValue(stat.getColumnName(i));
			model.addColumn(column);
		}
		return model;
	}
}
