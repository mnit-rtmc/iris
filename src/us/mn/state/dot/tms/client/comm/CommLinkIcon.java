/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import javax.swing.Icon;

/**
 * CommLinkIcon
 *
 * @author Douglas Lau
 */
public class CommLinkIcon implements Icon {

	static protected final int WIDTH = 44;
	static protected final int HEIGHT = 24;
	static protected final float W2 = WIDTH / 2.0f;
	static protected final float H2 = HEIGHT / 2.0f;
	static protected final float W25 = WIDTH * 2.0f / 5;
	static protected final float W35 = WIDTH * 3.0f / 5;
	static protected final float H25 = HEIGHT * 2.0f / 5;
	static protected final float H35 = HEIGHT * 3.0f / 5;
	static protected final float H15 = HEIGHT * 1.0f / 5;
	static protected final float H45 = HEIGHT * 4.0f / 5;
	protected final Color color;
	protected final GeneralPath path = new GeneralPath();

	public CommLinkIcon(Color c) {
		color = c;
		path.moveTo(0, H25);
		path.lineTo(W25, H25);
		path.curveTo(W25, H15, W25, H15, W2, 0);
		path.lineTo(W2, HEIGHT);
		path.curveTo(W25, H45, W25, H45, W25, H35);
		path.lineTo(0, H35);
		path.lineTo(0, H25);

		path.moveTo(WIDTH, H25);
		path.lineTo(W35, H25);
		path.curveTo(W35, H15, W35, H15, W2, 0);
		path.lineTo(W2, HEIGHT);
		path.curveTo(W35, H45, W35, H45, W35, H35);
		path.lineTo(WIDTH, H35);
		path.lineTo(WIDTH, H25);

		path.closePath();
	}
	public int getIconHeight() {
		return HEIGHT;
	}
	public int getIconWidth() {
		return WIDTH;
	}
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(color);
		g2.fill(path);
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(1));
		g2.draw(path);
	}
}
