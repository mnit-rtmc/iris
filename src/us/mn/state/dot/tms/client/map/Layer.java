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

import java.awt.geom.Rectangle2D;
import javax.swing.event.EventListenerList;

/**
 * A layer is a grouping of similar MapObjects.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
abstract public class Layer {

	/** Layer name */
	private final String name;

	/** Extent of layer */
	private final Rectangle2D extent = new Rectangle2D.Double();

	/** Create a new layer */
	public Layer(String n) {
		name = n;
	}

	/** Get the name of the layer */
	public String getName() {
		return name;
	}

	/** Get the extent of the layer */
	public Rectangle2D getExtent() {
		return extent;
	}

	/** Set the extent of the layer */
	public void setExtent(Rectangle2D e) {
		extent.setRect(e);
		fireLayerChanged(LayerChange.extent);
	}

	/** Listeners that listen to this layer state */
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

	/** Notify all listeners of a layer change.
	 * @param reason Reason for layer change.
	 *               Should be extent, geometry or status. */
	protected void fireLayerChanged(final LayerChange reason) {
		fireLayerChanged(new LayerChangeEvent(Layer.this, reason));
	}

	/** Create a new layer state */
	abstract public LayerState createState(MapBean mb);

	/** Check if the layer is searchable */
	public boolean isSearchable() {
		return true;
	}
}
