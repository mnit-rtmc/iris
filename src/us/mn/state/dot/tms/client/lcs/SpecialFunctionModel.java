/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2005  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.awt.Color;
import java.awt.Component;
import java.rmi.RemoteException;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import us.mn.state.dot.tms.LCSModule;

/**
 * SpecialFunctionModel is a special table model for setting up special function
 * of a 170 for LCSModules.
 *
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
abstract public class SpecialFunctionModel extends AbstractTableModel {

	/** Number of rows in special function model */
	protected final int ROWS;

	/** Number of columns in special function model */
	protected final int COLS = 4; // A column each for Red, Yellow and Green

	/** Cell values */
	protected final Object[][] values;

	/** Cell renderer component */
	protected final DefaultTableCellRenderer renderer;

	/** Get a renderer component for this model */
	public TableCellRenderer getRenderer() { return renderer; }

	/** LCSModules that are represented by this model */
	protected LCSModule[] modules;

	/** Create a new SpecialFunctionModel */
	public SpecialFunctionModel(LCSModule[] modules)
		throws RemoteException
	{
		this.modules = modules;
		ROWS = modules.length;
		values = new Object[ROWS][COLS];
		renderer = new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column)
			{
				JComponent comp = (JComponent)super.
					getTableCellRendererComponent(table,
					value, false, hasFocus, row, column);
				if(column == 0) {
					// FIXME: find a better way to do this
					comp.setBackground(
						javax.swing.plaf.metal.MetalLookAndFeel.getControl());
					comp.setBorder(
						new javax.swing.plaf.metal.MetalBorders.TableHeaderBorder());
				} else {
					this.setBackground(Color.WHITE);
				}
				return comp;
			}
		};
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
	}

	/** Get the column count */
	public int getColumnCount() { return COLS; }

	/** Get the row count */
	public int getRowCount() { return ROWS; }

	/** Get the value at a specific cell */
	public Object getValueAt(int row, int column) {
		if(row < ROWS && column < COLS)
			return values[row][column];
		else
			return null;
	}

	/** Returns true if the cell is not in the lanes column */
	public boolean isCellEditable(int row, int col) {
		if(col > 0)
			return true;
		return false;
	}

	/** Returns true if the cell is not in the lanes column */
	public boolean isCellSelectable(int row, int col) {
		if(col > 0)
			return true;
		return false;
	}

	/** Create a column model to match */
	static public TableColumnModel createColumnModel() {
		TableColumnModel colModel = new DefaultTableColumnModel();
		TableColumn column = new TableColumn(0);
		column.setHeaderValue("Lane");
		colModel.addColumn(column);
		column = new TableColumn(1);
		column.setHeaderValue("Green");
		colModel.addColumn(column);
		column = new TableColumn(2);
		column.setHeaderValue("Yellow");
		colModel.addColumn(column);
		column = new TableColumn(3);
		column.setHeaderValue("Red");
		colModel.addColumn(column);
		return colModel;
	}
}
