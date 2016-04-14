/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.map;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import javax.swing.Icon;

/**
 * A tile symbol draws tiles on a map.
 *
 * @author Douglas Lau
 */
public class TileSymbol implements Symbol {

	/** Set the map scale */
	@Override
	public void setScale(float scale) {
		// ignore
	}

	/** Draw the symbol */
	@Override
	public void draw(Graphics2D g, MapObject mo, Style sty) {
		if (mo instanceof TileMapObject) {
			TileMapObject tmo = (TileMapObject) mo;
			g.setTransform(tmo.getTransform());
			g.drawImage(tmo.getImage(), 0, 0, null);
		}
	}

	/** Draw the selected symbol */
	@Override
	public void drawSelected(Graphics2D g, MapObject mo, Style sty) {
		// cannot select tiles
	}

	/** Hit-test map object */
	@Override
	public boolean hit(Point2D p, MapObject mo) {
		return false;
	}

	/** Get the legend icon */
	@Override
	public Icon getLegend(Style sty) {
		return null;
	}
}
