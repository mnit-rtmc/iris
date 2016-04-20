/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 * List cell renderer used for combo boxes.
 *
 * @author Douglas Lau
 */
public class IListCellRenderer<T> implements ListCellRenderer<T> {

	/** Blank string */
	static private final String BLANK = " ";

	/** Cell renderer */
	private final DefaultListCellRenderer cell =
		new DefaultListCellRenderer();

	/** Create a new list cell renderer */
	public IListCellRenderer() {
		cell.setHorizontalTextPosition(SwingConstants.TRAILING);
	}

	/** Configure the renderer component */
	@Override
	public Component getListCellRendererComponent(
		JList<? extends T> list, T value, int index, boolean isSelected,
		boolean hasFocus)
	{
		cell.getListCellRendererComponent(list, asText(value), index,
			isSelected, hasFocus);
		cell.setIcon(getIcon(value));
		return cell;
	}

	/** Get a value as string */
	private String asText(T value) {
		if (value != null) {
			String v = valueToString(value);
			if (v.length() > 0)
				return v;
		}
		return BLANK;
	}

	/** Convert value to a string */
	protected String valueToString(T value) {
		return value.toString();
	}

	/** Get an icon for a value */
	protected Icon getIcon(T value) {
		return null;
	}
}
