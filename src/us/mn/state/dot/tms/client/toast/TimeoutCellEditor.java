/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2011  Minnesota Department of Transportation
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

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableCellEditor;

/**
 * Editor for timeout values in a table cell.
 *
 * @author Douglas Lau
 */
public class TimeoutCellEditor extends AbstractCellEditor
	implements TableCellEditor
{
	/** Spinner model */
	protected final SpinnerNumberModel model =
		new SpinnerNumberModel(0, 0, 8000, 50);

	/** Spinner component */
	protected final JSpinner spinner = new JSpinner(model);

	/** Get a table cell editor component */
	public Component getTableCellEditorComponent(JTable table, Object value,
		boolean isSelected, int row, int column)
	{
		spinner.setValue(value);
		return spinner;
	}

	/** Get the cell editor value */
	public Object getCellEditorValue() {
		return spinner.getValue();
	}
}
