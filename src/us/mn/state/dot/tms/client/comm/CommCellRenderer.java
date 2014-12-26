/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer for comm status in a table cell.
 *
 * @author Douglas Lau
 */
public class CommCellRenderer extends DefaultTableCellRenderer {

	/** Get the table cell renderer component */
	@Override
	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean isSelected, boolean hasFocus,
		int row, int col)
	{
		JLabel lbl = (JLabel)super.getTableCellRendererComponent(table,
			"", isSelected, hasFocus, row, col);
		if (value instanceof CommState) {
			CommState cs = (CommState)value;
			lbl.setIcon(cs.icon);
		} else
			lbl.setIcon(null);
		return lbl;
	}
}
