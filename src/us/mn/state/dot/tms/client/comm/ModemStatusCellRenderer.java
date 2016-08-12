/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import us.mn.state.dot.tms.ModemState;

/**
 * Renderer for modem status status in a table cell.
 *
 * @author Michael Janson
 */
public class ModemStatusCellRenderer extends DefaultTableCellRenderer {

	/** Get the renderer component using same modem status
	 * color coding as in task bar */
	@Override
	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean isSelected, boolean hasFocus,
		int row, int column)
	{
		JLabel label = (JLabel)super.getTableCellRendererComponent(
			table, "", isSelected, hasFocus, row, column);
		
		label.setForeground(Color.WHITE);
		label.setText(value.toString());
		
		if (value == ModemState.online || value == ModemState.connecting){
			label.setBackground(Color.YELLOW);
			label.setForeground(Color.BLACK);
		}
		else if (value == ModemState.offline)
			label.setBackground(Color.BLUE);
		else
			label.setBackground(Color.GRAY);
		
		return label;
	}
	
}
