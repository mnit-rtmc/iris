/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import us.mn.state.dot.tms.LaneConfiguration;

/**
 * Lane configuration panel.
 *
 * @author Douglas Lau
 */
public class LaneConfigurationPanel extends JPanel {

	/** Color of lanes */
	static private final Color LANE_COLOR = Color.GRAY;

	/** Color of shoulders */
	static private final Color SHOULDER_COLOR = Color.LIGHT_GRAY;

	/** Fractions for shoulder gradient paint */
	static private final float[] GRAD_FRACS = new float[] { 0.3f, 0.7f };

	/** Solid stroke line */
	static private final BasicStroke LINE_SOLID = new BasicStroke(2,
		BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

	/** Dashed stroke line */
	static private final Stroke LINE_DASHED = new BasicStroke(2,
		BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1,
		new float[] { 8, 16 }, 16);

	/** Pixel width of each lane */
	private final int l_width;

	/** Lane configuration */
	private LaneConfiguration config;

	/**
	 * Create a lane configuration panel.
	 * @param w Width of each lane. 
	 */
	public LaneConfigurationPanel(int w) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		l_width = w;
		clear();
	}

	/** Is the component opaque? */
	@Override public boolean isOpaque() {
		return true;
	}

	/** Clear the lane configuration */
	public void clear() {
		setConfiguration(new LaneConfiguration(0, 0));
	}

	/** Set new lane configuration */
	public void setConfiguration(LaneConfiguration lc) {
		config = lc;
		repaint();
	}

	/** Paint the panel */
	public void paintComponent(Graphics g) {
		clearComponent(g);
		if(config.getLanes() > 0)
			paint2D((Graphics2D)g);
	}

	/** Clear the component panel */
	private void clearComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	/** Paint the panel */
	private void paint2D(Graphics2D g) {
		final Stroke s = g.getStroke();
		Dimension d = getSize();
		int height = (int)d.getHeight();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		fillShoulders(g, height);
		fillLanes(g, height);
		drawLines(g, height);
		g.setStroke(s);
	}

	/** Fill the shoulders */
	private void fillShoulders(Graphics2D g, int height) {
		int x0 = getX(config.leftShift - 1);
		int x1 = getX(config.leftShift);
		int x2 = getX(config.rightShift);
		int x3 = getX(config.rightShift + 1);
		g.setPaint(new LinearGradientPaint(x0, 0, x1, 0, GRAD_FRACS,
			new Color[] { getBackground(), SHOULDER_COLOR }));
		int w = (l_width + 6);
		g.fillRect(x0, 0, w, height);
		g.setPaint(new LinearGradientPaint(x2, 0, x3, 0, GRAD_FRACS,
			new Color[] { SHOULDER_COLOR, getBackground() }));
		g.fillRect(x2, 0, w, height);
	}

	/** Fill the lanes */
	private void fillLanes(Graphics2D g, int height) {
		g.setColor(LANE_COLOR);
		int x = getX(config.leftShift);
		int w = config.getLanes() * (l_width + 6);
		g.fillRect(x, 0, w, height);
	}

	/** Draw the lane lines */
	private void drawLines(Graphics2D g, int height) {
		g.setStroke(LINE_SOLID);
		g.setColor(Color.YELLOW);
		int x = getX(config.leftShift);
		g.drawLine(x, 0, x, height);
		g.setColor(Color.WHITE);
		x = getX(config.rightShift);
		g.drawLine(x, 0, x, height);
		g.setStroke(LINE_DASHED);
		for(int i = config.leftShift + 1; i < config.rightShift; i++) {
			x = getX(i);
			g.drawLine(x, 0, x, height);
		}
	}

	/** Get the X pixel value of a lane */
	private int getX(int l) {
		return 3 + l * (l_width + 6);
	}
}
