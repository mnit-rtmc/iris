/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2013  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.RasterGraphic;

/**
 * Simple glyph information structure.
 *
 * @author Douglas Lau
 */
public class GlyphInfo {

	/** Code point */
	public final int code_point;

	/** Glyph object */
	public final Glyph glyph;

	/** Graphic of glyph */
	public final Graphic graphic;

	/** Bitmap of graphic */
	public final BitmapGraphic bmap;

	/** Create a new glyph info structure */
	public GlyphInfo(int cp, Glyph g) {
		code_point = cp;
		glyph = g;
		graphic = g != null ? g.getGraphic() : null;
		bmap = g != null ? createBitmap() : null;
	}

	/** Create a new default glyph info */
	public GlyphInfo() {
		this(0, null);
	}

	/** Create a bitmap of glyph */
	private BitmapGraphic createBitmap() {
		RasterGraphic rg = GraphicHelper.createRaster(graphic);
		if(rg instanceof BitmapGraphic)
			return (BitmapGraphic)rg;
		else
			return null;
	}

	/** Test if the glyph exists */
	public boolean exists() {
		return glyph != null;
	}
}
