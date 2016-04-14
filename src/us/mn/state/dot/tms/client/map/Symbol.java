/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
 * A symbol is a graphical representaion of a map object.
 *
 * @author Douglas Lau
 */
public interface Symbol {

	/** Set the map scale */
	void setScale(float scale);

	/** Draw the symbol */
	void draw(Graphics2D g, MapObject mo, Style sty);

	/** Draw the selected symbol */
	void drawSelected(Graphics2D g, MapObject mo, Style sty);

	/** Hit-test map object */
	boolean hit(Point2D p, MapObject mo);

	/** Get the legend icon */
	Icon getLegend(Style sty);
}
