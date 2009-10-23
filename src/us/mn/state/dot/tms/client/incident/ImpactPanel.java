/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import javax.swing.JPanel;

/**
 * Panel for incident impact.
 *
 * @author Douglas Lau
 */
public class ImpactPanel extends JPanel {

	/** Width of one lane */
	static protected final int LANE_WIDTH = 32;

	/** Height of one lane */
	static protected final int LANE_HEIGHT = 20;

	/** Solid stroke line */
	static protected final BasicStroke LINE_SOLID = new BasicStroke(4,
		BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	/** Dashed stroke line */
	static protected final BasicStroke LINE_DASHED = new BasicStroke(4,
		BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1,
		new float[] { LANE_HEIGHT / 3, 2 * LANE_HEIGHT / 3 },
		2 * LANE_HEIGHT / 3
	);

	/** Renderer component width */
	protected int width = 0;

	/** Renderer component height */
	protected int height = 0;

	/** Number of lanes */
	protected int n_lanes;

	/** Create a new roadway node renderer */
	public ImpactPanel() {
		n_lanes = 4;
	}

	/** Get the lane line (left) for the given lane */
	static protected int getLaneLine(int lane) {
		return LANE_WIDTH * lane + LANE_WIDTH / 2;
	}

	/** Paint the component */
	public void paintComponent(Graphics g) {
		Dimension d = (Dimension)getSize();
		Insets insets = getInsets();
		width = (int)d.getWidth() - insets.left - insets.right;
		height = (int)d.getHeight() - insets.top - insets.bottom;
		Graphics2D g2 = (Graphics2D)g.create(insets.left, insets.top,
			width, height);
		g2.setColor(getBackground());
		g2.fillRect(0, 0, width, height);
		fillRoadway(g2);
		drawYellowLine(g2);
		drawSkipStripes(g2);
		drawWhiteLine(g2);
	}

	/** Fill the roadway area */
	protected void fillRoadway(Graphics2D g) {
		g.setColor(Color.BLACK);
		int x = getLaneLine(0);
		int w = getLaneLine(2 + n_lanes) - x;
		g.fillRect(x, 0, w, height);
	}

	/** Draw the yellow lines */
	protected void drawYellowLine(Graphics2D g) {
		g.setStroke(LINE_SOLID);
		g.setColor(Color.YELLOW);
		int x = getLaneLine(1);
		g.draw(new Line2D.Double(x, 0, x, height));
	}

	/** Draw the white line */
	protected void drawWhiteLine(Graphics2D g) {
		g.setStroke(LINE_SOLID);
		g.setColor(Color.WHITE);
		int x = getLaneLine(n_lanes + 1);
		g.draw(new Line2D.Double(x, height, x, 0));
	}

	/** Draw the skip stripes */
	protected void drawSkipStripes(Graphics2D g) {
		g.setStroke(LINE_DASHED);
		g.setColor(Color.WHITE);
		for(int i = 0; i < n_lanes - 1; i++) {
			int x = getLaneLine(i + 2);
			g.draw(new Line2D.Double(x, 0, x, height));
		}
	}

	/** Get the preferred size */
	public Dimension getPreferredSize() {
		return new Dimension(LANE_WIDTH * (n_lanes + 3),
			LANE_HEIGHT * 4);
	}

	/** Get the minimum size */
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
}
