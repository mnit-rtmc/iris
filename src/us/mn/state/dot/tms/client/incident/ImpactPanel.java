/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import java.awt.TexturePaint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Panel for incident impact.
 *
 * @author Douglas Lau
 */
public class ImpactPanel extends JPanel {

	/** Impact for one lane */
	static enum LaneImpact {
		open('.'), blocked('!'), caution('?');
		protected final char _char;
		LaneImpact(char c) {
			_char = c;
		}
		protected LaneImpact next() {
			switch(this) {
			case open:
				return blocked;
			case blocked:
				return caution;
			default:
				return open;
			}
		}
		static LaneImpact fromChar(char i) {
			for(LaneImpact li: LaneImpact.values()) {
				if(li._char == i)
					return li;
			}
			return null;
		}
	}

	/** Width of one lane */
	static protected final int LANE_WIDTH = 32;

	/** Height of one lane */
	static protected final int LANE_HEIGHT = 20;

	/** Solid stroke line */
	static protected final BasicStroke LINE_SOLID = new BasicStroke(4,
		BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	/** Impact stroke line */
	static protected final BasicStroke LINE_IMPACT = new BasicStroke(1,
		BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	/** Dashed stroke line */
	static protected final BasicStroke LINE_DASHED = new BasicStroke(4,
		BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1,
		new float[] { LANE_HEIGHT / 3, 2 * LANE_HEIGHT / 3 },
		2 * LANE_HEIGHT / 3
	);

	/** Color for blocked impact */
	static protected final Color COLOR_BLOCKED = new Color(208, 64, 64);

	/** Image for caution impact */
	static protected final BufferedImage IMAGE_CAUTION =
		new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
	static {
		IMAGE_CAUTION.setRGB(0, 0, 0xffff22);
		IMAGE_CAUTION.setRGB(1, 1, 0xffff22);
		IMAGE_CAUTION.setRGB(2, 2, 0xffff22);
		IMAGE_CAUTION.setRGB(3, 3, 0xffff22);
	}

	/** Paint for caution impact */
	static protected final TexturePaint PAINT_CAUTION = new TexturePaint(
		IMAGE_CAUTION, new Rectangle2D.Float(0, 0, 4, 4));

	/** The listeners of this model */
	protected final LinkedList<ChangeListener> listeners =
		new LinkedList<ChangeListener>();

	/** Renderer component width */
	protected int width = 0;

	/** Renderer component height */
	protected int height = 0;

	/** Lane impact array */
	protected LaneImpact[] impact = new LaneImpact[0];

	/** Set the impact */
	public void setImpact(String im) {
		LaneImpact[] imp = new LaneImpact[im.length()];
		for(int i = 0; i < imp.length; i++)
			imp[i] = LaneImpact.fromChar(im.charAt(i));
		impact = imp;
		revalidate();
		repaint();
	}

	/** Get the impact */
	public String getImpact() {
		LaneImpact[] imp = impact;
		char[] im = new char[imp.length];
		for(int i = 0; i < im.length; i++)
			im[i] = imp[i]._char;
		return new String(im);
	}

	/** Create a new roadway node renderer */
	public ImpactPanel() {
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				doClick(e.getX(), e.getY());
			}
		});
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
		drawImpact(g2);
	}

	/** Fill the roadway area */
	protected void fillRoadway(Graphics2D g) {
		g.setColor(Color.BLACK);
		int x = getLaneLine(0);
		int w = getLaneLine(impact.length) - x;
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
		int x = getLaneLine(impact.length - 1);
		g.draw(new Line2D.Double(x, height, x, 0));
	}

	/** Draw the skip stripes */
	protected void drawSkipStripes(Graphics2D g) {
		g.setStroke(LINE_DASHED);
		g.setColor(Color.WHITE);
		for(int i = 0; i < impact.length - 3; i++) {
			int x = getLaneLine(i + 2);
			g.draw(new Line2D.Double(x, 0, x, height));
		}
	}

	/** Draw the impact */
	protected void drawImpact(Graphics2D g) {
		g.setStroke(LINE_IMPACT);
		for(int i = 0; i < impact.length; i++) {
			int x = getLaneLine(i);
			drawImpact(g, x, impact[i]);
		}
	}

	/** Draw the impact for one lane */
	protected void drawImpact(Graphics2D g, int x, LaneImpact li) {
		int s = LANE_WIDTH / 2;
		x += (LANE_WIDTH - s) / 2;
		int h = (height - s) / 2;
		switch(li) {
		case open:
			g.setColor(Color.WHITE);
			g.drawRect(x, h, s, s);
			break;
		case blocked:
			g.setColor(COLOR_BLOCKED);
			g.fillRect(x, h, s, s);
			g.setColor(Color.WHITE);
			g.drawRect(x, h, s, s);
			break;
		case caution:
			g.setPaint(PAINT_CAUTION);
			g.fillRect(x, h, s, s);
			g.setColor(Color.WHITE);
			g.setPaint(null);
			g.drawRect(x, h, s, s);
			break;
		}
	}

	/** Perform a mouse click event */
	protected void doClick(final int x, final int y) {
		Dimension d = (Dimension)getSize();
		Insets insets = getInsets();
		int height = (int)d.getHeight() - insets.top - insets.bottom;
		int s = LANE_WIDTH / 2;
		int xs = insets.left + (LANE_WIDTH - s) / 2;
		int h = (height - s) / 2;
		int top = insets.top + h;
		if(y < top || y > top + s)
			return;
		for(int i = 0; i < impact.length; i++) {
			int x0 = getLaneLine(i) + xs;
			if(x >= x0 && x <= x0 + s) {
				incrementImpact(i);
				break;
			}
		}
	}

	/** Increment lane impact */
	protected void incrementImpact(int lane) {
		LaneImpact[] imp = impact;	// Avoid race
		if(lane < 0 || lane >= imp.length)
			return;
		imp[lane] = imp[lane].next();
		repaint();
		fireStateChanged();
	}

	/** Get the preferred size */
	public Dimension getPreferredSize() {
		return new Dimension(LANE_WIDTH * (impact.length + 1),
			LANE_HEIGHT * 4);
	}

	/** Get the minimum size */
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	/** Add a change listener to the model */
	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}

	/** Remove a change listener from the model */
	public void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
	}

	/** Fire a change event to all listeners */
	protected void fireStateChanged() {
		ChangeEvent ce = new ChangeEvent(this);
		for(ChangeListener l: listeners)
			l.stateChanged(ce);
	}
}
