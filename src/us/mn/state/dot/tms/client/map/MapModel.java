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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.event.EventListenerList;
import us.mn.state.dot.tms.geo.ZoomLevel;

/**
 * The map model is a collection of LayerStates and the current extent of the
 * map.
 *
 * @author Douglas Lau
 */
public class MapModel {

	/** Listeners that for layer change events */
	private final EventListenerList listeners = new EventListenerList();

	/** Add a layer changed listener */
	public void addLayerChangeListener(LayerChangeListener l) {
		listeners.add(LayerChangeListener.class, l);
	}

	/** Remove a layer changed listener */
	public void removeLayerChangeListener(LayerChangeListener l) {
		listeners.remove(LayerChangeListener.class, l);
	}

	/** Notify all listeners of a layer change */
	private void fireLayerChanged(LayerChangeEvent e) {
		Object[] list = listeners.getListenerList();
		for(int i = list.length - 1; i >= 0; i -= 2) {
			Object l = list[i];
			if(l instanceof LayerChangeListener)
				((LayerChangeListener)l).layerChanged(e);
		}
	}

	/** Notify all listeners of a map model layer change.
	 * @param reason Reason for layer change.
	 *               Should only be extent. */
	protected void fireLayerChanged(final LayerChange reason) {
		fireLayerChanged(new LayerChangeEvent(MapModel.this, reason));
	}

	/** Listener for layer changed events from a layer state */
	private final LayerChangeListener listener =
		new LayerChangeListener()
	{
		public void layerChanged(LayerChangeEvent e) {
			fireLayerChanged(e);
		}
	};

	/** List of all layers */
	protected final LinkedList<LayerState> lstates =
		new LinkedList<LayerState>();

	/** Add a new layer to the map model */
	public void addLayer(LayerState ls) {
		lstates.add(ls);
		ls.addLayerChangeListener(listener);
	}

	/** Remove a layer from the map model */
	public void removeLayer(LayerState ls) {
		lstates.remove(ls);
		ls.removeLayerChangeListener(listener);
	}

	/** Get the list of layer states in the map model */
	public List<LayerState> getLayers() {
		return lstates;
	}

	/** Get a list iterator for layers */
	public ListIterator<LayerState> getLayerIterator() {
		return lstates.listIterator(lstates.size());
	}

	/** Current center in user coordinates */
	protected final Point2D center = new Point2D.Double();

	/** Get the current center in user coordinates */
	public Point2D getCenter() {
		return center;
	}

	/** Set the center in user coordinates.
	 * @param c New center */
	public void setCenter(Point2D c) {
		setExtent(c, zoom);
	}

	/** Current zoom level */
	protected ZoomLevel zoom = ZoomLevel.ZERO;

	/** Zoom in from the current extent.
	 * @param p Point in user coordinates. */
	public void zoomIn(Point2D p) {
		ZoomLevel zl = ZoomLevel.fromOrdinal(zoom.ordinal() + 1);
		if(zl == null)
			zl = zoom;
		double x = center.getX() + (p.getX() - center.getX()) / 2;
		double y = center.getY() + (p.getY() - center.getY()) / 2;
		Point2D.Double c = new Point2D.Double(x, y);
		setExtent(c, zl);
	}

	/** Zoom out from the current extent.
	 * @param p Point in user coordinates. */
	public void zoomOut(Point2D p) {
		ZoomLevel zl = ZoomLevel.fromOrdinal(zoom.ordinal() - 1);
		if(zl == null)
			zl = zoom;
		double x = center.getX() - (p.getX() - center.getX());
		double y = center.getY() - (p.getY() - center.getY());
		Point2D.Double c = new Point2D.Double(x, y);
		setExtent(c, zl);
	}

	/** Get the zoom level */
	public ZoomLevel getZoomLevel() {
		return zoom;
	}

	/** Current extent (bounding box) */
	protected final Rectangle2D extent = new Rectangle2D.Double();

	/** Get the extent of the map model */
	public Rectangle2D getExtent() {
		return extent;
	}

	/**
	 * Set the bounding box for display
	 * @param x the new x-coordinate for the map.
	 * @param y the new y-coordinate for the map.
	 * @param width the new width for the map.
	 * @param height the new height for the map.
	 */
	public void setExtent(double x, double y, double width, double height) {
		setExtent(new Rectangle2D.Double(x, y, width, height));
	}

	/** Set the map extent */
	protected void setExtent(Rectangle2D e) {
		Rectangle2D le = getLayerExtent();
		if(le != null)
			Rectangle2D.intersect(e, le, e);
		Point2D c = new Point2D.Double(e.getCenterX(), e.getCenterY());
		ZoomLevel zl = ZoomLevel.lookup(getScale(e));
		setExtent(c, zl);
	}

	/** Set the map extent */
	public void setExtent(Point2D c, ZoomLevel zl) {
		center.setLocation(c.getX(), c.getY());
		zoom = zl;
		Rectangle2D ne = calculateExtent(c, zl);
		if(!ne.equals(extent)) {
			extent.setRect(ne);
			fireLayerChanged(LayerChange.extent);
		}
	}

	/** Get the scale for a given extent */
	static protected double getScale(Rectangle2D e) {
		double s = Math.min(e.getWidth(), e.getHeight());
		// FIXME: 600 pixels is generic window size
		return s / 600;
	}

	/** Calculate extent */
	static protected Rectangle2D calculateExtent(Point2D c, ZoomLevel zl) {
		// FIXME: 600 pixels is generic window size
		double half = 300 * zl.scale;
		return new Rectangle2D.Double(c.getX() - half, c.getY() - half,
			2 * half, 2 * half);
	}

	/** Get the full extent of all layers */
	public Rectangle2D getLayerExtent() {
		Rectangle2D le = null;
		for(LayerState ls: lstates) {
			Rectangle2D e = ls.getExtent();
			if(le == null) {
				le = new Rectangle2D.Double();
				le.setRect(e);
			} else
				Rectangle2D.union(le, e, le);
		}
		return le;
	}

	/** Dispose of the map model */
	public void dispose() {
		while(!lstates.isEmpty()) {
			LayerState ls = lstates.pop();
			ls.removeLayerChangeListener(listener);
			ls.dispose();
		}
	}
}
