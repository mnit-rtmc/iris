/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerListModel;
import javax.swing.table.TableCellEditor;
import static us.mn.state.dot.tms.CommLink.VALID_PERIODS;

/**
 * Editor for poll period in a table cell.
 *
 * @author Douglas Lau
 */
public class PollPeriodCellEditor extends AbstractCellEditor
	implements TableCellEditor
{
	/** Spinner component */
	private final JSpinner spinner;

	/** Create a new poll period cell editor */
	public PollPeriodCellEditor() {
		spinner = new JSpinner(new SpinnerListModel(VALID_PERIODS));
	}

	/** Get a table cell editor component */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
		boolean isSelected, int row, int column)
	{
		spinner.setValue(value);
		return spinner;
	}

	/** Get the cell editor value */
	@Override
	public Object getCellEditorValue() {
		return spinner.getValue();
	}
}
