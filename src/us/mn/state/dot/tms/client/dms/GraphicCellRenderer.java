/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.PixmapGraphic;
import us.mn.state.dot.tms.RasterGraphic;

/**
 * Table cell renderer for graphics.
 *
 * @author Douglas Lau
 */
public class GraphicCellRenderer implements TableCellRenderer {

	/** Create an image */
	static private BufferedImage createImage(Object value) {
		if (value instanceof Graphic) {
			RasterGraphic rg = GraphicHelper.createRaster(
				(Graphic)value);
			if (rg instanceof BitmapGraphic)
				return createBitmapImage((BitmapGraphic)rg);
			if (rg instanceof PixmapGraphic)
				return createPixmapImage((PixmapGraphic)rg);
		}
		return null;
	}

	/** Create a bitmap image */
	static private BufferedImage createBitmapImage(BitmapGraphic bg) {
		BufferedImage im = new BufferedImage(bg.getWidth(),
			bg.getHeight(), BufferedImage.TYPE_INT_RGB);
		final int rgb = 0xFFFFFF;
		for (int y = 0; y < bg.getHeight(); y++) {
			for (int x = 0; x < bg.getWidth(); x++) {
				if (bg.getPixel(x, y).isLit())
					im.setRGB(x, y, rgb);
			}
		}
		return im;
	}

	/** Create a pixmap image */
	static private BufferedImage createPixmapImage(PixmapGraphic pg) {
		BufferedImage im = new BufferedImage(pg.getWidth(),
			pg.getHeight(), BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < pg.getHeight(); y++) {
			for (int x = 0; x < pg.getWidth(); x++)
				im.setRGB(x, y, pg.getPixel(x, y).rgb());
		}
		return im;
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
