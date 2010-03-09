/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.TreeSet;

/**
 * Screen display class
 *
 * @author Douglas Lau
 */
public class Screen implements Comparable {

	/** All available screens */
	static protected final Screen[] ALL_SCREENS = getAllScreens();

	/** Create a point centered on a rectangle */
	static protected Point createCenterPoint(Rectangle rect, Dimension sz) {
		int x = rect.x + rect.width / 2 - sz.width / 2;
		int y = rect.y + rect.height / 2 - sz.height / 2;
		x = Math.max(x, rect.x);
		y = Math.max(y, rect.y);
		return new Point(x, y);
	}

	/** Graphics configuration */
	protected final GraphicsConfiguration config;

	/** Bounds (with insets included) */
	protected final Rectangle bounds;

	/** Get the screen bounds */
	public Rectangle getBounds() {
		return bounds;
	}

	/** Create a display screen object */
	protected Screen(GraphicsConfiguration c, Rectangle b) {
		config = c;
		bounds = new Rectangle(b);
	}

	/** Compare the screen to another screen */
	public int compareTo(Object o) {
		Screen other = (Screen)o;
		return bounds.x - other.bounds.x;
	}

	/** Get the centered location of a rectangle with the given size */
	public Point getCenteredLocation(Container cont, Dimension sz) {
		Point loc = getLocation(cont);
		Rectangle bnd = cont.getBounds();
		Rectangle rect = new Rectangle(bounds);
		rect.translate(-loc.x, -loc.y);
		rect = rect.intersection(bounds);
		if(bnd.intersects(rect))
			return createCenterPoint(bnd.intersection(rect), sz);
		else
			return createCenterPoint(bnd, sz);
	}

	/** Center a window on the screen */
	public void centerWindow(Window w) {
		w.setLocation(createCenterPoint(bounds, w.getSize()));
	}

	/** Center a given window on its current screen */
	static public void centerOnCurrent(Window w) {
		int x = w.getX();
		int y = w.getY();
		for(Screen s: ALL_SCREENS) {
			if(s.getBounds().contains(x, y)) {
				s.centerWindow(w);
				return;
			}
		}
	}

	/** Add a screen to the list if it does not intersect other screens */
	static protected void addScreen(TreeSet<Screen> ms, Screen m) {
		for(Screen s: ms) {
			Rectangle r = s.bounds;
			if(m.bounds.intersects(r))
				return;
		}
		ms.add(m);
	}

	/** Add all screens for the specified graphics device */
	static protected void addDeviceScreens(GraphicsDevice gd,
		TreeSet<Screen> ms)
	{
		GraphicsConfiguration c = gd.getDefaultConfiguration();
		Rectangle b = c.getBounds();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(c);
		double x = b.getX() + insets.left;
		double y = b.getY() + insets.top;
		double w = b.getWidth();
		double h = b.getHeight() - (insets.top + insets.bottom);
		Rectangle r = new Rectangle();
		if(w > h * 2) {
			w /= 2.0;
			r.setRect(x, y, w - insets.left, h);
			addScreen(ms, new Screen(c, r));
			r = new Rectangle();
			r.setRect(w, y, w - insets.right, h);
			addScreen(ms, new Screen(c, r));
		} else {
			w -= insets.left + insets.right;
			r.setRect(x, y, w, h);
			addScreen(ms, new Screen(c, r));
		}
	}

	/** Get an array of all screens */
	static public Screen[] getAllScreens() {
		TreeSet<Screen> ms = new TreeSet<Screen>();
		GraphicsEnvironment g =
			GraphicsEnvironment.getLocalGraphicsEnvironment();
		for(GraphicsDevice gd: g.getScreenDevices())
			addDeviceScreens(gd, ms);
		return (Screen [])ms.toArray(new Screen[0]);
	}

	/** Get the maximized bounds for all screens */
	static public Rectangle getMaximizedBounds() {
		Rectangle b = new Rectangle();
		for(Screen s: ALL_SCREENS)
			b = b.union(s.bounds);
		return b;
	}

	/** Get the location of a component on screen */
	static public Point getLocation(Container p) {
		try {
			return p.getLocationOnScreen();
		}
		catch(IllegalComponentStateException e) {
			Point point = new Point();
			while(p != null) {
				point.translate(p.getX(), p.getY());
				p = p.getParent();
			}
			return point;
		}
	}
}
