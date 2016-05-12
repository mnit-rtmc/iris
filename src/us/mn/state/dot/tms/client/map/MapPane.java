/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * This class can be used to generate map graphics when access to the graphics
 * subsystem is not available.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class MapPane implements LayerChangeListener {

	/** Minimum width/height of map pane */
	static private final int MIN_SIZE = 1;

	/** Buffer for map */
	private BufferedImage screenBuffer;

	/** Dirty flag */
	private boolean dirty = true;

	/** Transform from world to screen coordinates */
	private final AffineTransform transform = new AffineTransform();

	/** Transform from screen to world coordinates */
	private AffineTransform inverseTransform = new AffineTransform();

	/** Background color of map */
	private Color background = Color.GRAY;

	/** Map bean */
	private final MapBean mapbean;

	/** Create a new map pane */
	public MapPane(MapBean b) {
		mapbean = b;
		setSize(new Dimension(MIN_SIZE, MIN_SIZE));
	}

	/** Set the pixel size of the map panel */
	public void setSize(Dimension d) {
		screenBuffer = createImage(d.width, d.height);
		rescale();
		dirty = true;
	}

	/** Create a buffered image of the specified size */
	private BufferedImage createImage(int width, int height) {
		return new BufferedImage(
			Math.max(width, MIN_SIZE),
			Math.max(height, MIN_SIZE),
			BufferedImage.TYPE_INT_RGB);
	}

	/** Get the size of the map */
	public Dimension getSize() {
		BufferedImage bi = screenBuffer;	// Avoid race
		return new Dimension(bi.getWidth(), bi.getHeight());
	}

	/** Dispose of the map pane */
	public void dispose() {
		// nothing to do
	}

	/** Change the scale of the map panel */
	private void rescale() {
		BufferedImage bi = screenBuffer;	// Avoid race
		int height = bi.getHeight();
		int width = bi.getWidth();
		// scale is pixels per meter
		double scale = 1 / mapbean.getScale();
		Point2D center = mapbean.getModel().getCenter();
		transform.setToTranslation(
			width / 2 - center.getX() * scale,
			height / 2 + center.getY() * scale
		);
		transform.scale(scale, -scale);
		try {
			inverseTransform = transform.createInverse();
		}
		catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
	}

	/** Set the background color of the map */
	public void setBackground(Color color) {
		background = color;
	}

	/** Get the current image for the map panel */
	public BufferedImage getImage() {
		BufferedImage bi = screenBuffer;
		if (dirty) {
			drawImage(bi);
			dirty = false;
		}
		return bi;
	}

	/** Draw the map image */
	private void drawImage(BufferedImage bi) {
		Graphics2D g = bi.createGraphics();
		g.setBackground(background);
		g.clearRect(0, 0, bi.getWidth(), bi.getHeight());
		g.transform(transform);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		for (LayerState s: mapbean.getLayers())
			s.paint(g);
		g.dispose();
	}

	/** Get the buffered image */
	public BufferedImage getBufferedImage() {
		return screenBuffer;
	}

	/** Map model has changed */
	@Override
	public void layerChanged(LayerChangeEvent ev) {
		switch (ev.getReason()) {
		case selection:
			return;
		case model:
		case extent:
			rescale();
		default:
			dirty = true;
		}
	}

	/** Get the transform from world to screen coordinates */
	public AffineTransform getTransform() {
		return transform;
	}

	/** Get the transform from screen to world coordinates */
	public AffineTransform getInverseTransform() {
		return inverseTransform;
	}

	/** Get the pixel scale */
	public double getScale() {
		return mapbean.getScale();
	}
}
