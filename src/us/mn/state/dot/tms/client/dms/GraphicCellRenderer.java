/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2018  Minnesota Department of Transportation
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

import java.awt.Component;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.Graphic;

/**
 * Table cell renderer for graphics.
 *
 * @author Douglas Lau
 */
public class GraphicCellRenderer implements TableCellRenderer {

	/** Create an image */
	static private BufferedImage createImage(Object value) {
		return (value instanceof Graphic)
		      ? GraphicImage.create((Graphic) value)
		      : null;
	}

	/** Image icon */
	private final ImageIcon icon = new ImageIcon();

	/** Get a component to render the graphic */
	@Override
	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean isSelected, boolean hasFocus, int row,
		int column)
	{
		BufferedImage im = createImage(value);
		if (im != null) {
			icon.setImage(im);
			return new JLabel(icon);
		} else
			return new JLabel();
	}
}
