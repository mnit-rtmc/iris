/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2023  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Cell renderer used for message lines.
 *
 * @author Douglas Lau
 */
public class MsgLineCellRenderer implements ListCellRenderer<MsgLine> {

	/** Blank string (must contain a space to be visible on combo box) */
	static private final String BLANK = " ";

	/** Highlight color for special rank levels */
	static private final Color HIGHLIGHT = new Color(204, 204, 255);

	/** Get a message line as text */
	static private String asText(MsgLine value) {
		if (value != null) {
			String v = new MultiString(value.getMulti()).asText();
			if (v.length() > 0)
				return v;
		}
		return BLANK;
	}

	/** Cell renderer */
	private final DefaultListCellRenderer cell =
		new DefaultListCellRenderer();

	/** Create a new message line cell renderer */
	public MsgLineCellRenderer() {
		cell.setHorizontalAlignment(SwingConstants.CENTER);
	}

	/** Configure the renderer component for a message line */
	@Override
	public Component getListCellRendererComponent(
		JList<? extends MsgLine> list, MsgLine value, int index,
		boolean isSelected, boolean cellHasFocus)
	{
		cell.setBackground(null);
		cell.getListCellRendererComponent(list, asText(value), index,
			isSelected, cellHasFocus);
		if (value != null && (value.getRank() != 50) && !isSelected)
			cell.setBackground(HIGHLIGHT);
		return cell;
	}
}
