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

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;

/**
 * Renderer for comm status in a table cell.
 *
 * @author Douglas Lau
 */
public class CommCellRenderer extends DefaultTableCellRenderer {

	/** Color to display inactive controllers */
	static private final Color COLOR_INACTIVE = new Color(0, 0, 0, 32);

	/** Color to display available devices */
	static private final Color COLOR_AVAILABLE = new Color(96, 96, 255);

	/** Icon for OK status controllers */
	static private final Icon OK = new ControllerIcon(COLOR_AVAILABLE);

	/** Icon for failed status controllers */
	static private final Icon FAIL = new ControllerIcon(Color.GRAY);

	/** Icon for inactive status controllers */
	static private final Icon INACTIVE = new ControllerIcon(COLOR_INACTIVE);

	/** Get the table cell renderer component */
	@Override
	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean isSelected, boolean hasFocus,
		int row, int col)
	{
		JLabel label = (JLabel)super.getTableCellRendererComponent(
			table, "", isSelected, hasFocus, row, col);
		if (value instanceof Controller) {
			Controller c = (Controller)value;
			if (ControllerHelper.isFailed(c))
				label.setIcon(FAIL);
			else if (ControllerHelper.isActive(c))
				label.setIcon(OK);
			else
				label.setIcon(INACTIVE);
		} else
			label.setIcon(null);
		return label;
	}
}
