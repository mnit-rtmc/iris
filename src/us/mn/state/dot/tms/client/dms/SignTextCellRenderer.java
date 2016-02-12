/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignText;

/**
 * Cell renderer used for sign text.
 *
 * @author Douglas Lau
 */
public class SignTextCellRenderer implements ListCellRenderer<SignText> {

	/** Highlight color for special rank levels */
	static private final Color HIGHLIGHT = new Color(204, 204, 255);

	/** Cell renderer */
	private final DefaultListCellRenderer cell =
		new DefaultListCellRenderer();

	/** Create a new sign text cell renderer */
	public SignTextCellRenderer() {
		cell.setHorizontalAlignment(SwingConstants.CENTER);
	}

	/** Configure the renderer component for a sign text message */
	@Override
	public Component getListCellRendererComponent(
		JList<? extends SignText> list, SignText value, int index,
		boolean isSelected, boolean cellHasFocus)
	{
		String v = "";
		if (value != null) {
			v = new MultiString(value.getMulti()).asText();
			if (value.getRank() != 50 && !isSelected)
				cell.setBackground(HIGHLIGHT);
			else
				cell.setBackground(null);
		} else
			cell.setBackground(null);
		return cell.getListCellRendererComponent(list, v, index,
			isSelected, cellHasFocus);
	}
}
