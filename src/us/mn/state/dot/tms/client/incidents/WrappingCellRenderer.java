/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.incidents;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

/**
 * ListCellRenderer that allows contents to wrap.
 *
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 */
public class WrappingCellRenderer extends JTextArea implements
	ListCellRenderer
{
	static protected final Color COLOR_SELECTED = new Color(153, 153, 203);
	static protected final Color COLOR_BACKGROUND = Color.WHITE;

	protected final Border emptyBorder = BorderFactory.createEmptyBorder();
	protected final Border selectedBorder = BorderFactory.createLineBorder(
		Color.BLACK);

	/** Create a new wrapping cell renderer */
	public WrappingCellRenderer() {
		setOpaque(true);
		setWrapStyleWord(true);
		setLineWrap(true);
		setTabSize(4);
	}

	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus)
	{
		setText("--" + value);
		if(isSelected)
			setBackground(COLOR_SELECTED);
		else
			setBackground(COLOR_BACKGROUND);
		if(cellHasFocus)
			setBorder(selectedBorder);
		else
			setBorder(emptyBorder);
		return this;
	}
}
