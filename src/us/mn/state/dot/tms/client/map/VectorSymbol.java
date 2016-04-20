/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A vector symbol is draws a Shape with a specific Style.
 *
 * @author Douglas Lau
 */
public class VectorSymbol implements Symbol {

	/** Size of legend icons */
	static private final int lsize = UI.scaled(20);

	/** Transparent white */
	static private final Color TRANS_WHITE = new Color(1, 1, 1, 0.4f);

	/** Transparent white */
	static private final Color TRANSPARENT = new Color(1, 1, 1, 0.75f);

	/** Create an ellipse around the given shape */
	static private Shape createEllipse(Shape s) {
		Rectangle2D r = s.getBounds2D();
		return new Ellipse2D.Double(
			r.getCenterX() - r.getWidth(),
			r.getCenterY() - r.getHeight(),
			r.getWidth() * 2,
			r.getHeight() * 2
		);
	}

	/** Marker shape */
	private final Marker marker;

	/** Scaled shape */
	private Shape shape;

	/** Get scaled shape */
	protected Shape getShape() {
		return shape;
	}

	/** Map scale */
	private float scale = 1;

	/** Create a new vector symbol */
	public VectorSymbol(Marker m) {
		marker = m;
		shape = m;
	}

	/** Set the map scale */
	@Override
	public void setScale(float s) {
		setScale(s, marker);
	}

	/** Set the map scale */
	protected final void setScale(float s, Marker m) {
		scale = s;
		AffineTransform at = new AffineTransform();
		at.setToScale(s, s);
		shape = m.createTransformedShape(at);
	}

	/** Get shape for a map object */
	private Shape getShape(MapObject mo) {
		Shape shp = mo.getShape();
		return (shp != null) ? shp : shape;
	}

	/** Get outline shape for a map object */
	private Shape getOutlineShape(MapObject mo) {
		Shape shp = mo.getOutlineShape();
		return (shp != null) ? shp : shape;
	}

	/** Draw the symbol */
	@Override
	public void draw(Graphics2D g, MapObject mo, Style sty) {
		if (sty != null) {
			AffineTransform trans = mo.getTransform();
			if (trans != null)
				g.transform(trans);
			draw(g, getShape(mo), getOutlineShape(mo), sty);
		}
	}

	/** Draw the symbol */
	protected void draw(Graphics2D g, Shape shp, Shape o_shp, Style sty) {
		if (shp != null && sty.fill_color != null) {
			g.setColor(sty.fill_color);
			g.fill(shp);
		}
		if (o_shp != null && sty.outline != null) {
			g.setColor(sty.outline.color);
			g.setStroke(sty.outline.getStroke(scale));
			g.draw(o_shp);
		}
	}

	/** Draw a selected symbol */
	@Override
	public void drawSelected(Graphics2D g, MapObject mo, Style sty) {
		if (sty != null)
			drawSelected(g, mo, getShape(mo), sty);
	}

	/** Draw a selected symbol */
	private void drawSelected(Graphics2D g, MapObject mo, Shape shp,
		Style sty)
	{
		g.transform(mo.getTransform());
		g.setColor(TRANS_WHITE);
		g.fill(shp);
		Outline outline = Outline.createSolid(TRANSPARENT, 4);
		g.setColor(TRANSPARENT);
		g.setStroke(outline.getStroke(scale));
		g.draw(createEllipse(shp));
	}

	/** Hit-test map object */
	@Override
	public boolean hit(Point2D p, MapObject mo) {
		Shape shp = getShape(mo);
		AffineTransform t = mo.getInverseTransform();
		Point2D ip = t.transform(p, null);
		return shp.contains(ip);
	}

	/** Get the legend icon */
	@Override
	public Icon getLegend(Style sty) {
		return new LegendIcon(sty);
	}

	/** Inner class for icon displayed on the legend */
	private class LegendIcon implements Icon {

		/** Legend style */
		private final Style sty;

		/** Transform to draw the legend */
		private final AffineTransform transform;

		/** Create a new legend icon */
		protected LegendIcon(Style s) {
			sty = s;
			Rectangle2D b = marker.getBounds2D();
			double x = b.getX() + b.getWidth() / 2;
			double y = b.getY() + b.getHeight() / 2;
			double scale = (lsize - 2) /
				Math.max(b.getWidth(), b.getHeight());
			transform = new AffineTransform();
			transform.translate(lsize / 2, lsize / 2);
			transform.scale(scale, -scale);
			transform.translate(-x, -y);
		}	

		/** Paint the icon onto the given component */
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			setScale(1);
			Graphics2D g2 = (Graphics2D) g;
			AffineTransform t = g2.getTransform();
			g2.translate(x, y);
			g2.transform(transform);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
			draw(g2, marker, marker, sty);
			g2.setTransform(t);
		}

		/** Get the icon width */
		@Override
		public int getIconWidth() {
			return lsize;
		}

		/** Get the icon height */
		@Override
		public int getIconHeight() {
			return lsize;
		}
	}
}
