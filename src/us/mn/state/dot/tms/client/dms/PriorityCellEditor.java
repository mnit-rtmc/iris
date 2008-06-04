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
package us.mn.state.dot.tms.client.dms;

import java.awt.Component;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;

/**
 * Cell editor for sign text priority.
 *
 * @author Douglas Lau
 */
public class PriorityCellEditor extends AbstractCellEditor
	implements TableCellEditor
{
	/** Model for priority cells */
	protected final SpinnerNumberModel model =
		new SpinnerNumberModel(50, 1, 99, 1);

	/** Spinner for editing priority values */
	protected final JSpinner spinner = new JSpinner(model);

	/** Get the priority cell editor component */
	public Component getTableCellEditorComponent(JTable table,
		Object value, boolean isSelected, int row, int column)
	{
		if(value != null) {
			Short p = (Short)value;
			model.setValue(p.intValue());
		}
		return spinner;
	}

	/** Get the value of the cell editor */
	public Object getCellEditorValue() {
		return model.getValue();
	}
}
