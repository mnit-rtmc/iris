/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2022  Minnesota Department of Transportation
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

import java.awt.image.BufferedImage;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.PixmapGraphic;
import us.mn.state.dot.tms.RasterGraphic;

/**
 * Helper class to make a BufferedImage from a Graphic.
 *
 * @author Douglas Lau
 */
public class GraphicImage {

	/** Create an image of a graphic */
	static public BufferedImage create(Graphic g) {
		RasterGraphic rg = GraphicHelper.createRaster(g);
		if (rg instanceof BitmapGraphic)
			return createBitmap((BitmapGraphic) rg);
		if (rg instanceof PixmapGraphic)
			return createPixmap((PixmapGraphic) rg);
		else
			return null;
	}

	/** Create a bitmap image */
	static private BufferedImage createBitmap(BitmapGraphic bg) {
		BufferedImage im = new BufferedImage(bg.getWidth(),
			bg.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < bg.getHeight(); y++) {
			for (int x = 0; x < bg.getWidth(); x++) {
				if (bg.isTransparent(x, y))
					im.setRGB(x, y, 0xFF000000);
			}
		}
		return im;
	}

	/** Create a pixmap image */
	static private BufferedImage createPixmap(PixmapGraphic pg) {
		BufferedImage im = new BufferedImage(pg.getWidth(),
			pg.getHeight(), BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < pg.getHeight(); y++) {
			for (int x = 0; x < pg.getWidth(); x++)
				im.setRGB(x, y, pg.getPixel(x, y).rgb());
		}
		return im;
	}

	/** Do not allow instantiation */
	private GraphicImage() { }
}
