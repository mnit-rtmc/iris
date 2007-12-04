/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import javax.swing.Icon;

/**
 * A pixel icon draws a lit or unlit pixel as an icon.
 *
 * @author Douglas Lau
 */
public class PixelIcon implements Icon {

	/** Diameter of pixel */
	static protected final int SIZE = 20;

	/** On/off state of the pixel */
	protected final boolean state;

	/** Circle to draw the pixel */
	protected final Ellipse2D circle = new Ellipse2D.Float(0, 0, SIZE,
		SIZE);

	/** Create a pixel icon */
	public PixelIcon(boolean s) {
		state = s;
	}

	/** Get the height of the icon */
	public int getIconHeight() {
		return SIZE;
	}

	/** Get the width of the icon */
	public int getIconWidth() {
		return SIZE;
	}

	/** Paint the icon onto a component */
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		if(state)
			g2.setColor(Color.YELLOW);
		else
			g2.setColor(Color.GRAY);
		g2.fill(circle);
	}
}
