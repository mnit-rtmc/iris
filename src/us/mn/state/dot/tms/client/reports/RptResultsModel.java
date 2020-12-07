/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  SRF Consulting Group
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

package us.mn.state.dot.tms.client.reports;

import static us.mn.state.dot.tms.client.widget.Widgets.UI;

import java.awt.Dimension;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import us.mn.state.dot.tms.reports.RptResultItem;
import us.mn.state.dot.tms.reports.RptResults;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Report results table model
 *
 * @author Michael Janson - SRF Consulting
 * @author John L. Stanley - SRF Consulting
 */

@SuppressWarnings("serial")
public class RptResultsModel extends AbstractTableModel {
	
	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 4;

	/** Datetime column number */
	static protected final int COL_DATETIME = 0;

	/** Device name column number */
	static protected final int COL_DEV_NAME = 1;

	/** User column number */
	static protected final int COL_USER = 2;
	
	/** Report description column number */
	static protected final int COL_DESCRIPTION = 3;

	/** Maximum description length in pixels */
	private int desc_col_width;

	/** Create a new table column */
	static protected TableColumn createColumn(int column, int width,
		String headerID)
	{
		TableColumn c = new TableColumn(column, UI.scaled(width));
		c.setHeaderValue(I18N.get(headerID));
		c.setPreferredWidth(width); //

		return c;
	}

	/** Result report object */
	private RptResults reportResults;
	
	/** Create a new blank report table model */
	public RptResultsModel() {
		this(null);
	}

	/** Create a new report table model with results */
	public RptResultsModel(RptResults rpts) {
		reportResults = rpts;
		desc_col_width = calcDescriptionColumnWidth() + 10;
	}

	/** Get the column count */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}


	/** Get the row count */
	public int getRowCount() {
		return reportResults!=null ? reportResults.resultsSize() : 0;
	}

	/** Get the value at a specific cell */
	public Object getValueAt(int row, int column) {
		if(row >= 0 && row < getRowCount())
			return parseValue(reportResults.getRptResults().get(row), column);
		else
			return null;
	}
	
	/** Parse report columns */
	static protected String parseValue(RptResultItem rpt, int column) {
		switch (column) {
		case COL_DATETIME:
			return rpt.getDatetimeStr();
		case COL_DEV_NAME:
			return rpt.getName();
		case COL_USER:
			return rpt.getUsername();
		case COL_DESCRIPTION:
			return rpt.getDescription();
		default:
			return null;
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_DATETIME, 120+5,
			"report.field.datetime"));
		m.addColumn(createColumn(COL_DEV_NAME, 120+5,
			"report.field.device"));
		m.addColumn(createColumn(COL_USER,     80 +5,
			"report.field.user"));
		m.addColumn(createColumn(COL_DESCRIPTION, desc_col_width,
			"report.field.description"));
		return m;
	}

	/** Get the column class */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int column) {
		Class returnValue;
		if ((column >= 0) && (column < getColumnCount())) {
			returnValue = getValueAt(0, column).getClass();
		} else {
			returnValue = Object.class;
		}
		return returnValue;
	}
	
	//-------------------------------------------

	public int calcDescriptionColumnWidth() {
		int width = 420;  // good starting width

		if (reportResults != null) {
			RptResultItem row;
			String des;
			JLabel lbl = new JLabel();
			Dimension dim;
			int w;

			Iterator<RptResultItem> rows =
				reportResults.iterateRptResults();
			while (rows.hasNext()) {
				row = rows.next();
				des = row.getDescription();
				if (des == null)
					continue;
				lbl.setText(des);
				dim = lbl.getPreferredSize();
				if (dim != null) {
					w = dim.width;
					width = Math.max(width, w);
				}
			}
		}
		return width;
	}
}

