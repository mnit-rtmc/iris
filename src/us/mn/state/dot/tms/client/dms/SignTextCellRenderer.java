/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2013  Minnesota Department of Transportation
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

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignText;

/**
 * Cell renderer used for sign text.
 *
 * @author Douglas Lau
 */
public class SignTextCellRenderer extends BasicComboBoxRenderer {

	/** Highlight color for special rank levels */
	static protected final Color HIGHLIGHT = new Color(204, 204, 255);

	/** Configure the renderer component for a sign text message */
	public Component getListCellRendererComponent(JList list,
		Object value, int index, boolean isSelected,
		boolean cellHasFocus)
	{
		String v = "";
		short rank = 50;
		if(value instanceof SignText) {
			SignText t = (SignText)value;
			v = new MultiString(t.getMulti()).asText();
			rank = t.getRank();
		}
		JLabel r = (JLabel)super.getListCellRendererComponent(
			list, v, index, isSelected, cellHasFocus);
		r.setHorizontalAlignment(SwingConstants.CENTER);
		if(rank != 50 && !isSelected)
			r.setBackground(HIGHLIGHT);
		return r;
	}
}
