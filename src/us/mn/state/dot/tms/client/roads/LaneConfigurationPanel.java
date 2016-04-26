/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import us.mn.state.dot.tms.LaneConfiguration;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A lane configuration panel can draw configuration of a roadway, including
 * all lanes, shoulders and skip stripes.  Non-opaque child components can be
 * added to draw devices or other graphical features over the lanes.
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
	static private final BasicStroke LINE_SOLID = new BasicStroke(
		UI.scaled(2), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

	/** Dashed stroke line */
	private final Stroke line_dashed;

	/** Pixel width of each lane */
	private final int l_width;

	/** Flag to draw labels */
	private final boolean labels;

	/** Lane configuration */
	private LaneConfiguration config;

	/** Create a lane configuration panel.
	 * @param w Width of each lane.
	 * @param l True if labels should be drawn */
	public LaneConfigurationPanel(int w, boolean l) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		l_width = w;
		labels = l;
		float s = w / 3.0f;
		line_dashed = new BasicStroke(UI.scaled(2),
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1,
			new float[] { s, 2 * s }, 2 * s);
		clear();
	}

	/** Is the component opaque? */
	@Override
	public boolean isOpaque() {
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
	@Override
	public void paintComponent(Graphics g) {
		clearGraphics(g);
		Graphics g2 = createGraphics(g);
		if (g2 instanceof Graphics2D)
			paint2D((Graphics2D) g2);
	}

	/** Clear the graphics */
	private void clearGraphics(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	/** Create graphics clipped to insets */
	private Graphics createGraphics(Graphics g) {
		Insets insets = getInsets();
		int width = getWidth() - insets.left - insets.right;
		int height = getHeight() - insets.top - insets.bottom;
		return g.create(insets.left, insets.top, width, height);
	}

	/** Paint the panel */
	private void paint2D(Graphics2D g) {
		Dimension d = getSize();
		int height = (int) d.getHeight();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		if (config.getLanes() > 0) {
			fillShoulders(g, height);
			fillLanes(g, height);
			drawLines(g, height);
		}
		if (labels) {
			drawLeft(g, height);
			drawRight(g, height);
		}
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
		g.setStroke(line_dashed);
		for (int i = config.leftShift + 1; i < config.rightShift; i++) {
			x = getX(i);
			g.drawLine(x, 0, x, height);
		}
	}

	/** Create a glyph vector */
	private GlyphVector createGlyphVector(Graphics2D g, String txt) {
		Font f = getFont();
		Font fd = f.deriveFont(2f * f.getSize2D());
		return fd.createGlyphVector(g.getFontRenderContext(), txt);
	}

	/** Draw the left side text */
	private void drawLeft(Graphics2D g, int height) {
		GlyphVector gv = createGlyphVector(g,
			I18N.get("location.left"));
		Rectangle2D rect = gv.getVisualBounds();
		int x = 0;
		int y = (height + (int) rect.getHeight()) / 2;
		g.setColor(Color.BLACK);
		g.drawGlyphVector(gv, x, y);
	}

	/** Draw the right side text */
	private void drawRight(Graphics2D g, int height) {
		GlyphVector gv = createGlyphVector(g,
			I18N.get("location.right"));
		Rectangle2D rect = gv.getVisualBounds();
		int x = getWidth() - (int) rect.getWidth() - 4;
		int y = (height + (int) rect.getHeight()) / 2;
		g.setColor(Color.BLACK);
		g.drawGlyphVector(gv, x, y);
	}

	/** Get the X pixel value of a shift.
	 * @param shift Lane shift.
	 * @return X pixel value at lane shift. */
	private int getX(int shift) {
		return 3 + shift * (l_width + 6);
	}
}
