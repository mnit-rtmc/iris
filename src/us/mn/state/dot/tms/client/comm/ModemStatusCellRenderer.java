/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
 * Copyright (C) 2015  SRF Consulting Group
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

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import us.mn.state.dot.tms.ModemState;

/**
 * Renderer for modem status status in a table cell.
 *
 * @author Michael Janson
 * @author Douglas Lau
 */
public class ModemStatusCellRenderer extends DefaultTableCellRenderer {

	/** Get the background color for a modem state */
	static private Color modemBackground(ModemState ms) {
		switch (ms) {
		case connecting:
			return Color.ORANGE;
		case online:
			return Color.YELLOW;
		case offline:
			return Color.BLUE;
		default:
			return Color.GRAY;
		}
	}

	/** Get the foreground color for a modem state */
	static private Color modemForeground(ModemState ms) {
		switch (ms) {
		case connecting:
		case online:
			return Color.BLACK;
		default:
			return Color.WHITE;
		}
	}

	/** Get cell renderer component with modem status color coding */
	@Override
	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean isSelected, boolean hasFocus,
		int row, int column)
	{
		JLabel lbl = (JLabel) super.getTableCellRendererComponent(
			table, value, isSelected, hasFocus, row, column);
		if (value instanceof ModemState) {
			ModemState ms = (ModemState) value;
			lbl.setBackground(modemBackground(ms));
			lbl.setForeground(modemForeground(ms));
		}
		return lbl;
	}
}
