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

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

/**
 * Layer state is the rendering state for one layer on a map. Multiple layer
 * states can share the same underlying layer for separate map components.
 * 
 * The layer state can be made invisible and can listen for mouse actions.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
abstract public class LayerState {

	/** Empty selection special case (for equality comparisons) */
	static private final MapObject[] NO_SELECTION = new MapObject[0];

	/** Map bean for rendering */
	protected final MapBean map;

	/** Layer this state is for */
	private final Layer layer;

	/** Get the layer */
	public Layer getLayer() {
		return layer;
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
		for (int i = list.length - 1; i >= 0; i -= 2) {
			Object l = list[i];
			if (l instanceof LayerChangeListener)
				((LayerChangeListener) l).layerChanged(e);
		}
	}

	/** Notify all listeners of a layer state change.
	 * @param reason Reason for layer change.
	 *               Should be visibility, theme or selection. */
	protected void fireLayerChanged(final LayerChange reason) {
		fireLayerChanged(new LayerChangeEvent(LayerState.this, reason));
	}

	/** Listener for layer changed events from layer */
	private final LayerChangeListener listener =
		new LayerChangeListener()
	{
		public void layerChanged(LayerChangeEvent e) {
			if (isVisible())
				fireLayerChanged(e);
		}
	};

	/** List of available themes */
	private final List<Theme> themes = new LinkedList<Theme>();

	/** Current theme */
	private Theme theme;

	/** Currently selected map objects */
	private MapObject[] selections = NO_SELECTION;

	/** Visibility flag (null means automatic) */
	private Boolean visible = null;

	/** Create a new layer state.
	 * @param layer Layer this state is based upon.
	 * @param mb Map bean for rendering. */
	protected LayerState(Layer layer, MapBean mb) {
		this(layer, mb, null, null);
	}

	/** Create a new layer state.
	 * @param layer Layer this state is based upon.
	 * @param mb Map bean for rendering.
	 * @param theme Theme used to paint the layer. */
	protected LayerState(Layer layer, MapBean mb, Theme theme) {
		this(layer, mb, theme, null);
	}

	/** Create a new layer state.
	 * @param layer Layer this state is based upon.
	 * @param mb Map bean for rendering.
	 * @param theme Theme used to paint the layer.
	 * @param visible The visible tri-state. */
	protected LayerState(Layer layer, MapBean mb, Theme theme,
		Boolean visible)
	{
		this.layer = layer;
		map = mb;
		this.theme = theme;
		this.visible = visible;
		layer.addLayerChangeListener(listener);
	}

	/** Dispose of the layer state */
	public void dispose() {
		layer.removeLayerChangeListener(listener);
		themes.clear();
	}

	/** Add a theme to this layer state */
	public void addTheme(Theme t) {
		themes.add(t);
	}

	/** Get a list of all themes for this layer state */
	public List<Theme> getThemes() {
		return themes;
	}

	/** Set the theme */
	public void setTheme(Theme t) {
		if (t != theme) {
			theme = t;
			fireLayerChanged(LayerChange.theme);
		}
	}

	/** Get the theme */
	public Theme getTheme() {
		return theme;
	}

	/** Set the selected map objects */
	public void setSelections(MapObject[] s) {
		if (s != selections) {
			selections = s;
			fireLayerChanged(LayerChange.selection);
		}
	}

	/** Clear the selected map objects */
	public void clearSelections() {
		setSelections(NO_SELECTION);
	}

	/** Get the selected map objects */
	public MapObject[] getSelections() {
		return selections;
	}

	/** Get the layer extent */
	public Rectangle2D getExtent() {
		return layer.getExtent();
	}

	/** Call the specified callback for each map object in the layer */
	abstract public MapObject forEach(MapSearcher s);

	/** Paint the layer */
	public void paint(final Graphics2D g) {
		if (isVisible()) {
			final AffineTransform t = g.getTransform();
			theme.setScale(getScale());
			forEach(new MapSearcher() {
				public boolean next(MapObject mo) {
					theme.draw(g, mo);
					g.setTransform(t);
					return false;
				}
			});
		}
	}

	/** Paint the selections for the layer */
	public void paintSelections(Graphics2D g) {
		if (isVisible()) {
			AffineTransform t = g.getTransform();
			theme.setScale(getScale());
			MapObject[] sel = selections;
			for (MapObject mo: sel) {
				theme.drawSelected(g, mo);
				g.setTransform(t);
			}
		}
	}

	/** Get the current map scale */
	protected float getScale() {
		return (float) map.getScale();
	}

	/** Get the visibility flag */
	public boolean isVisible() {
		// Sub-classes can override automatic visibility
		return (visible != null) ? visible : true;
	}

	/** Get the visibility tri-state */
	public Boolean getVisible() {
		return visible;
	}

	/** Set the visibility of the layer.
	 * @param v New visibility; true == visible, false == invisible,
	 *          null means automatic. */
	public void setVisible(Boolean v) {
		boolean pv = isVisible();
		visible = v;
		if (pv != isVisible())
			fireLayerChanged(LayerChange.visibility);
	}

	/** Get the appropriate tool tip text for the specified point */
	public String getTip(Point2D p) {
		if (isSearchable()) {
			MapObject mo = search(p);
			if (mo != null)
				return theme.getTip(mo);
		}
		return null;
	}

	/** Search the layer for a map object containing the given point */
	public MapObject search(final Point2D p) {
		theme.setScale(getScale());
		return forEach(new MapSearcher() {
			public boolean next(MapObject mo) {
				return theme.hit(p, mo);
			}
		});
	}

	/** Process a mouse click for the layer */
	public boolean doMouseClicked(MouseEvent e, Point2D p) {
		if (isSearchable()) {
			MapObject mo = search(p);
			if (SwingUtilities.isLeftMouseButton(e))
				doLeftClick(e, mo);
			if (SwingUtilities.isRightMouseButton(e))
				doRightClick(e, mo);
			if (mo != null)
				return true;
		}
		return false;
	}

	/** Process a left click on a map object */
	protected void doLeftClick(MouseEvent e, MapObject mo) {}

	/** Process a right click on a map object */
	protected void doRightClick(MouseEvent e, MapObject mo) {}

	/** Check if the layer is currently searchable */
	protected boolean isSearchable() {
		return isVisible() && layer.isSearchable();
	}
}
