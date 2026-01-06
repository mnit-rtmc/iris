/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2026  Minnesota Department of Transportation
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
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer for connected status in a table cell.
 *
 * @author Douglas Lau
 */
public class ConnectedCellRenderer extends DefaultTableCellRenderer {

	/** Get the renderer component */
	@Override
	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean isSelected, boolean hasFocus,
		int row, int column)
	{
		JLabel lbl = (JLabel) super.getTableCellRendererComponent(
			table, "", isSelected, hasFocus, row, column);
		Color c = Color.BLACK;
		if (value instanceof ConnectState)
			c = ((ConnectState) value).color;
		lbl.setIcon(new CommLinkIcon(c));
		return lbl;
	}
}
