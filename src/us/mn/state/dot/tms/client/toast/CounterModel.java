/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000,2001  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import us.mn.state.dot.tms.ErrorCounter;

/**
 * CounterModel is a special table model for communication error counters
 *
 * @author Douglas Lau
 */
class CounterModel extends AbstractTableModel {

	/** Light pink color */
	static protected final Color PINK = new Color( 1.0f, 0.85f, 0.85f );

	/** Number of rows in counter models */
	static protected final int ROWS = ErrorCounter.PERIODS.length;

	/** Number of columns is counter models */
	static protected final int COLS = ErrorCounter.TYPES.length + 2;

	/** Cell values */
	protected final Object[][] values;

	/** Cell renderer component */
	protected final DefaultTableCellRenderer renderer;

	/** Get a renderer component for this model */
	public TableCellRenderer getRenderer() { return renderer; }

	/** Create a new counter model */
	public CounterModel( final int[][] c ) {
		values = new Object[ ROWS ][ COLS ];
		for( int p = 0; p < ROWS; p++ ) {
			int poll = c[0][p] + c[1][p];
			values[p][0] = ErrorCounter.PERIODS[p];
			values[p][1] = new Integer( poll );
			values[p][2] = new Integer( c[1][p] );
			float per = 0;
			if( poll > 0 ) per = 100.0f * c[1][p] / poll;
			values[p][3] = new Integer( Math.round(per) );
		}
		renderer = new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column )
			{
				Color col = Color.white;
				if( c[1][row] > 0 ) col = PINK;
				this.setBackground( col );
				return super.getTableCellRendererComponent(
					table, value, false, hasFocus,
					row, column );
			}
		};
		renderer.setHorizontalAlignment( SwingConstants.RIGHT );
	}

	/** Get the column count */
	public int getColumnCount() { return COLS; }

	/** Get the row count */
	public int getRowCount() { return ROWS; }

	/** Get the value at a specific cell */
	public Object getValueAt( int row, int column ) {
		return values[ row ][ column ];
	}

	/** Create a column model to match */
	static public TableColumnModel createColumnModel() {
		TableColumnModel colModel = new DefaultTableColumnModel();
		TableColumn column = new TableColumn( 0 );
		column.setHeaderValue( "Period" );
		colModel.addColumn( column );
		column = new TableColumn( 1 );
		column.setHeaderValue( "Poll" );
		colModel.addColumn( column );
		column = new TableColumn( 2 );
		column.setHeaderValue( "Fail" );
		colModel.addColumn( column );
		column = new TableColumn( 3 );
		column.setHeaderValue( "Percent" );
		colModel.addColumn( column );
		return colModel;
	}
}
