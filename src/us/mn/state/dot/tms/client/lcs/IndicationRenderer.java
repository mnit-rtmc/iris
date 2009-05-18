/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import us.mn.state.dot.tms.LaneUseIndication;

/**
 * Renderer for LCS indications in a list.
 *
 * @author Douglas Lau
 */
public class IndicationRenderer extends DefaultListCellRenderer {

	/** Size of renderer in pixels */
	protected final int pixels;

	/** Actual label widget to render indications */
	protected final JLabel cell = new JLabel();

	/** Create a new indication renderer */
	public IndicationRenderer(int p) {
		pixels = p;
		cell.setOpaque(true);
		setPreferredSize(new Dimension(pixels, pixels));
	}

	/** Get a configured cell renderer for an LCS indication */
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus)
	{
		if(value instanceof LaneUseIndication) {
			LaneUseIndication li = (LaneUseIndication)value;
			cell.setIcon(IndicationIcon.create(pixels, li));
		}
		Component c = super.getListCellRendererComponent(list, value,
			index, isSelected, cellHasFocus);
		cell.setBackground(c.getBackground());
		return cell;
	}
}
