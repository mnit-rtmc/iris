/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.io.IOException;
import java.util.Iterator;

/**
 * Graphic helper methods.
 *
 * @author Douglas Lau
 */
public class GraphicHelper extends BaseHelper {

	/** Disallow instantiation */
	private GraphicHelper() {
		assert false;
	}

	/** Lookup the graphic with the specified name */
	static public Graphic lookup(String name) {
		return (Graphic)namespace.lookupObject(Graphic.SONAR_TYPE,name);
	}

	/** Get a graphic iterator */
	static public Iterator<Graphic> iterator() {
		return new IteratorWrapper<Graphic>(namespace.iterator(
			Graphic.SONAR_TYPE));
	}

	/** Find a graphic using a graphic number */
	static public Graphic find(int g_num) {
		Iterator<Graphic> it = iterator();
		while(it.hasNext()) {
			Graphic g = it.next();
			Integer gn = g.getGNumber();
			if(gn != null && gn == g_num)
				return g;
		}
		return null;
	}

	/** Create a raster graphic */
	static public RasterGraphic createRaster(Graphic g) {
		try {
			switch(g.getBpp()) {
			case 1:
				return createBitmap(g);
			case 24:
				return createPixmap(g);
			default:
				return null;
			}
		}
		catch(IndexOutOfBoundsException e) {
			// pixel data was wrong length
			return null;
		}
		catch(IOException e) {
			// pixel data Base64 decode failed
			return null;
		}
	}

	/** Create a bitmap graphic */
	static private RasterGraphic createBitmap(Graphic g) throws IOException{
		BitmapGraphic bg = new BitmapGraphic(g.getWidth(),
			g.getHeight());
		bg.setPixels(Base64.decode(g.getPixels()));
		return bg;
	}

	/** Create a pixmap graphic */
	static private RasterGraphic createPixmap(Graphic g) throws IOException{
		PixmapGraphic pg = new PixmapGraphic(g.getWidth(),
			g.getHeight());
		pg.setPixels(Base64.decode(g.getPixels()));
		return pg;
	}
}
