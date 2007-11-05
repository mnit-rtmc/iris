/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.lcs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;
import javax.swing.JPanel;
import us.mn.state.dot.tms.LCSModule;

/**
 * GUI for displaying one module of a LaneControlSignal.
 *
 * @author    <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @author Douglas Lau
 */
public class LcsModule extends JPanel implements Icon {

	/** Border around LCS shapes */
	static protected final float SHAPE_BORDER = 0.05f;

	/** String to display for error status */
	static protected final String ERROR_STRING = "?";

	/** Shape to use for representing an error condition */
	static protected final Shape ERROR_SHAPE;
	static {
		Font font = new Font("Serif", Font.PLAIN, 24);
		FontRenderContext frc = new FontRenderContext(
			new AffineTransform(), false, false);
		GlyphVector vec = font.createGlyphVector(frc, ERROR_STRING);
		Shape s = vec.getGlyphOutline(0);
		Rectangle2D rect = s.getBounds2D();
		AffineTransform a = new AffineTransform();
		a.translate(SHAPE_BORDER, SHAPE_BORDER);
		a.scale((1 - 2 * SHAPE_BORDER) / rect.getWidth(),
			(1 - 2 * SHAPE_BORDER) / rect.getHeight());
		a.translate(-rect.getX(), -rect.getY());
		ERROR_SHAPE = a.createTransformedShape(s);
	}

	/** Shape to draw a red X */
	static protected final Shape CROSS_SHAPE;
	static {
		GeneralPath path = new GeneralPath();
		path.moveTo(SHAPE_BORDER, SHAPE_BORDER);
		path.lineTo(1 - SHAPE_BORDER, 1 - SHAPE_BORDER);
		path.moveTo(SHAPE_BORDER, 1 - SHAPE_BORDER);
		path.lineTo(1 - SHAPE_BORDER, SHAPE_BORDER);
		CROSS_SHAPE = path;
	}

	/** Shape to draw an arrow */
	static protected final Shape ARROW_SHAPE;
	static {
		GeneralPath path = new GeneralPath();
		path.moveTo(0.5f, SHAPE_BORDER);
		path.lineTo(0.5f, 1 - SHAPE_BORDER);
		path.moveTo(SHAPE_BORDER, 0.5f);
		path.lineTo(0.5f, 1 - SHAPE_BORDER);
		path.lineTo(1 - SHAPE_BORDER, 0.5f);
		ARROW_SHAPE = path;
	}

	/** Stroke for drawing symbol shadows */
	protected final BasicStroke shadow;

	/** Stroke for drawing symbols */
	protected final BasicStroke stroke;

	/** State of module */
	protected int state = LCSModule.ERROR;

	/** Create a new LCS module */
	public LcsModule(int size) {
		this(size, LCSModule.DARK);
	}

	/**
	 * Constructor for the LcsModule object
	 *
	 * @param size Sise to make the module.
	 * @param signal  Current signal state
	 */
	public LcsModule(int size, int signal) {
		setBackground(Color.BLACK);
		state = signal;
		if(size > 0) {
			setPreferredSize(new Dimension(size, size));
			setMinimumSize(new Dimension(size, size));
			setSize(size, size);
		}
		shadow = new BasicStroke(5f / size, BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_MITER);
		stroke = new BasicStroke(3f / size, BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_MITER);
	}

	/** Set the signal */
	public void setSignal(int signal) {
		state = signal;
	}

	/** Get the signal */
	public int getSignal() {
		return state;
	}

	/** Get the icon's height */
	public int getIconHeight() {
		return getSize().height;
	}

	/** Get the icon's width */
	public int getIconWidth() {
		return this.getSize().width;
	}

	/** Paint the LCS module */
	public void paintComponent(Graphics g) {
		Dimension size = getSize();
		g.setColor(getBackground());
		g.fillRect(0, 0, size.width, size.height);
		paintIcon(null, g, 0, 0);
	}

	/**
	 * Paint the icon at the specified location.
	 *
	 * @param component  The component to paint on.
	 * @param g          The Graphics object to paint with.
	 * @param x          The x coordinate at which to paint.
	 * @param y          The y coordinate at which to paint.
	 */
	public void paintIcon(Component component, Graphics g, int x, int y) {
		Dimension size = getSize();
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		g2.translate(x, y);
		g2.scale(size.getWidth(), size.getHeight());
		g2.setStroke(shadow);
		g2.setColor(Color.BLACK);
		switch(state) {
			case LCSModule.GREEN:
				g2.draw(ARROW_SHAPE);
				g2.setStroke(stroke);
				g2.setColor(Color.GREEN);
				g2.draw(ARROW_SHAPE);
				break;
			case LCSModule.YELLOW:
				g2.draw(ARROW_SHAPE);
				g2.setStroke(stroke);
				g2.setColor(Color.YELLOW);
				g2.draw(ARROW_SHAPE);
				break;
			case LCSModule.RED:
				g2.draw(CROSS_SHAPE);
				g2.setStroke(stroke);
				g2.setColor(Color.RED);
				g2.draw(CROSS_SHAPE);
				break;
			case LCSModule.ERROR:
				g2.setColor(Color.GRAY);
				g2.fill(ERROR_SHAPE);
				break;
		}
	}
}
