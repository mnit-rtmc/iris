/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  SRF Consulting Group
 * Copyright (C) 2018  Minnesota Department of Transportation
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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import javax.swing.JPanel;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.RasterGraphic;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Renders a static graphic for a DMS.
 *
 * @author Michael Janson
 * @author Douglas Lau
 */
public class StaticGraphicPanel extends JPanel {

	/** Raster graphic to paint */
	private RasterGraphic graphic;

	/** Create a new static graphic panel.
	 * @param h Pixel height of image.
	 * @param w Pixel width of image. */
	public StaticGraphicPanel(int h, int w) {
		super(true);
		setSizes(h, w);
	}

	/** Set the image size */
	private void setSizes(int height, int width) {
		Dimension d = UI.dimension(width, height);
		setMinimumSize(d);
		setPreferredSize(d);
	}

	/** Set the graphic displayed */
	public void setGraphic(RasterGraphic rg) {
		graphic = rg;
		repaint();
	}

	/** Paint this on the screen */
	@Override
	public void paintComponent(Graphics g) {
		doPaint((Graphics2D) g, graphic);
	}

	/** Paint the static image onto a graphics context */
	private void doPaint(Graphics2D g, RasterGraphic rg) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		if (rg != null) {
			AffineTransform t = createTransform(rg);
			if (t != null) {
				g.transform(t);
				paintPixels(g, rg);
			}
		}
	}

	/** Create a transform to paint the graphic */
	private AffineTransform createTransform(RasterGraphic rg) {
		int wp = rg.getWidth();
		int hp = rg.getHeight();
		if (wp > 0 && hp > 0) {
			double sx = getWidth() / wp;
			double sy = getHeight() / hp;
			double scale = Math.min(sx, sy);
			double tx = wp * (sx - scale) / 2;
			double ty = hp * (sy - scale) / 2;
			AffineTransform t = new AffineTransform();
			t.translate(tx, ty);
			t.scale(scale, scale);
			return t;
		} else
			return null;
	}

	/** Paint the static image */
	private void paintPixels(Graphics2D g, RasterGraphic rg) {
		for (int y = 0; y < rg.getHeight(); y++) {
			for (int x = 0; x < rg.getWidth(); x++) {
				DmsColor clr = rg.getPixel(x, y);
				g.setColor(clr.color);
				g.fillRect(x, y, 1, 1);
			}
		}
	}
}
