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
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import javax.swing.Icon;

/**
 * ControllerIcon
 *
 * @author Douglas Lau
 */
public class ControllerIcon implements Icon {

	static protected final int WIDTH = 44;
	static protected final int HEIGHT = 24;
	static protected final float W2 = WIDTH / 2.0f;
	static protected final float H2 = HEIGHT / 2.0f;
	static protected final float W2_5 = WIDTH * 2.0f / 5;
	static protected final float W3_5 = WIDTH * 3.0f / 5;
	static protected final float H1_5 = HEIGHT / 5.0f;
	static protected final float H2_5 = HEIGHT * 2.0f / 5;
	static protected final float H3_5 = HEIGHT * 3.0f / 5;
	static protected final float H4_5 = HEIGHT * 4.0f / 5;
	static protected final float H10 = HEIGHT / 10.0f;
	static protected final float H12 = HEIGHT / 12.0f;
	static protected final float H20 = HEIGHT / 20.0f;
	static protected final float H24 = HEIGHT / 24.0f;
	protected final Color color;
	protected final GeneralPath path =
		new GeneralPath(GeneralPath.WIND_EVEN_ODD);

	public ControllerIcon(Color c) {
		color = c;
		path.moveTo(0, H1_5);
		path.lineTo(WIDTH - H20, H1_5);
		path.lineTo(WIDTH - H20, H4_5);
		path.lineTo(0, H4_5);
		path.lineTo(0, H1_5);
		path.moveTo(W2_5 - H24, H1_5 + H20);
		path.lineTo(W3_5 + H24, H1_5 + H20);
		path.lineTo(W3_5 + H24, H2_5);
		path.lineTo(W2_5 - H24, H2_5);
		path.lineTo(W2_5 - H24, H1_5 + H20);
		addCircle(W2_5, H3_5 - H10);
		addCircle(W2, H3_5 - H10);
		addCircle(W3_5, H3_5 - H10);
		addCircle(W2_5, H3_5);
		addCircle(W2, H3_5);
		addCircle(W3_5, H3_5);
		addCircle(W2_5, H3_5 + H10);
		addCircle(W2, H3_5 + H10);
		addCircle(W3_5, H3_5 + H10);
		path.closePath();
	}
	protected void addCircle(float w, float h) {
		Arc2D.Float arc = new Arc2D.Float(w - H24, h - H24,
			H12, H12, 0, 360, Arc2D.OPEN);
		path.append(arc, false);
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
		g2.setStroke(new BasicStroke(0.8f));
		g2.draw(path);
	}
}
