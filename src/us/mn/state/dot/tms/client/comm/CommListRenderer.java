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
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * Renderer for comm status in a list.
 *
 * @author Douglas Lau
 */
public class CommListRenderer extends DefaultListCellRenderer {

	/** Get cell value as text */
	static private String valueText(Object value) {
		if (value instanceof CommState)
			return value.toString();
		else
			return " ";
	}

	/** Get the table cell renderer component */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean hasFocus)
	{
		JLabel lbl = (JLabel)super.getListCellRendererComponent(list,
			valueText(value), index, isSelected, hasFocus);
		if (value instanceof CommState) {
			CommState cs = (CommState)value;
			lbl.setIcon(cs.icon);
		} else
			lbl.setIcon(null);
		return lbl;
	}
}
