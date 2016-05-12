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
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.ListIterator;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

/**
 * The MapBean class is a container for a MapPane which allows the pane to be
 * scrolled and zoomed.  It has several convenience methods giving access to
 * the internal MapPane.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @see us.mn.state.dot.tms.client.map.MapPane
 */
public class MapBean extends JComponent {

	/** Cursor for panning the map */
	static private final Cursor PAN_CURSOR;
	static {
		ImageIcon i = new ImageIcon(MapBean.class.getResource(
                        "/images/pan.png"));
		PAN_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(
                        i.getImage(), new Point(6, 6), "Pan");
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

	/** When map changes, the map model updates all change listeners */
	private final LayerChangeListener listener =
		new LayerChangeListener()
	{
		@Override
		public void layerChanged(LayerChangeEvent ev) {
			fireLayerChanged(ev);
			mapPane.layerChanged(ev);
			repaint();
		}
	};

	/** Map model */
	private MapModel model = new MapModel();

	/** Set the map model */
	public void setModel(MapModel m) {
		model.removeLayerChangeListener(listener);
		model = m;
		model.addLayerChangeListener(listener);
		listener.layerChanged(new LayerChangeEvent(MapBean.this,
			LayerChange.model));
	}

	/** Get the map model */
	public MapModel getModel() {
		return model;
	}

	/** MapPane that will create the map */
	private final MapPane mapPane;

	/** MapBean reference for PanState inner class */
	private final MapBean map;

	/** Current panning state */
	private PanState pan = null;

	/** Current point selector */
	private PointSelector pselect = new NullPointSelector();

	/** Create a new map */
	public MapBean(boolean a) {
		map = this;
		mapPane = new MapPane(this);
		mapPane.setBackground(getBackground());
		model.addLayerChangeListener(listener);
		setOpaque(true);
		setDoubleBuffered(false);
		setToolTipText(" ");
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				rescale();
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				doMouseClicked(e);
			}
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e))
					startPan(e.getPoint());
			}
			public void mouseReleased(MouseEvent e) {
				finishPan(e.getPoint());
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				doPan(e.getPoint());
			}
		});
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				Point2D p = transformPoint(e.getPoint());
				if (e.getWheelRotation() < 0)
					zoomIn(p);
				else
					zoomOut(p);
			}
		});
	}

	/** Set the background color */
	public void setBackground(Color c) {
		super.setBackground(c);
		mapPane.setBackground(c);
	}

	/** Set point selector.
	 * @param ps New point selector (null for none). */
	public void setPointSelector(PointSelector ps) {
		final PointSelector ops = pselect;
		pselect = (ps != null) ? ps : new NullPointSelector();
		ops.finish();
		setCursor();
	}

	/** Select a point with the mouse pointer */
	private boolean selectPoint(Point2D p) {
		boolean sel = pselect.selectPoint(p);
		setPointSelector(null);
		return sel;
	}

	/** Set the mouse cursor */
	private void setCursor() {
		if (pselect instanceof NullPointSelector)
			setCursor(null);
		else
			setCursor(Cursor.getPredefinedCursor(
				Cursor.CROSSHAIR_CURSOR));
	}

	/** Process a mouse click event */
	private void doMouseClicked(MouseEvent e) {
		boolean consumed = false;
		Point2D p = transformPoint(e.getPoint());
		if (selectPoint(p))
			return;
		ListIterator<LayerState> it = model.getLayerIterator();
		while (it.hasPrevious()) {
			LayerState s = it.previous();
			if (consumed)
				s.clearSelections();
			else
				consumed = s.doMouseClicked(e, p);
		}
	}

	/** Get a list of the layers contained by this Map */
	public List<LayerState> getLayers() {
		return model.getLayers();
	}

	/** Transform a point from screen to world coordinates */
	public Point2D transformPoint(Point p) {
		PanState ps = pan;
		if (ps != null)
			p = ps.start;
		AffineTransform t = mapPane.getInverseTransform();
		return t.transform(p, null);
	}

	/** Get the size of a pixel in world coordinates */
	public double getScale() {
		return model.getZoomLevel().scale;
	}

	/** Get the tooltip text for the given mouse event */
	public String getToolTipText(MouseEvent e) {
		Point2D p = transformPoint(e.getPoint());
		ListIterator<LayerState> it = model.getLayerIterator();
		while (it.hasPrevious()) {
			LayerState t = it.previous();
			String tip = t.getTip(p);
			if (tip != null)
				return tip;
		}
		return null;
	}

	/** Create a tooltip for the map */
	public JToolTip createToolTip() {
		return new MapToolTip();
	}

	/** Set the center */
	private void setCenter(double x, double y) {
		Point2D.Double c = new Point2D.Double(x, y);
		model.setCenter(c);
	}

	/** State of map panning action */
	private class PanState {
		private final Point start;
		private Image buffer;
		private AffineTransform transform;
		private int xpan, ypan;

		private PanState(Point s) {
			start = s;
		}

		private boolean isStarted() {
			return buffer != null;
		}

		private void initialize() {
			setCursor(PAN_CURSOR);
			buffer = mapPane.getBufferedImage();
			AffineTransform t = mapPane.getTransform();
			transform = AffineTransform.getScaleInstance(
				t.getScaleX(), t.getScaleY());
			xpan = 0;
			ypan = 0;
		}

		/** Set the X and Y pan values */
		private void setPan(Point2D end) {
			xpan = (int) (end.getX() - start.getX());
			ypan = (int) (end.getY() - start.getY());
		}

		/** Drag the map pan */
		private void drag(Point p) {
			if (!isStarted())
				initialize();
			setPan(p);
			repaint();
		}

		/** Render the panning map */
		private void renderMap(Graphics2D g) {
			Rectangle bounds = getBounds();
			g.drawImage(buffer, xpan, ypan, map);
			g.setColor(getBackground());
			if (xpan >= 0)
				g.fillRect(0, 0, xpan, bounds.height);
			else { 
				g.fillRect(bounds.width + xpan, 0,
					-xpan, bounds.height);
			}
			if (ypan >= 0)
				g.fillRect(0, 0, bounds.width, ypan);
			else { 
				g.fillRect(0, bounds.height + ypan,
					 bounds.width,-ypan);
			}
		}

		/** Finish panning the map */
		private void finish(Point2D end) {
			setPan(end);
			Point p = new Point(xpan, ypan);
			try {
				transform.inverseTransform(p, p);
			}
			catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
			setCursor();
			Point2D center = model.getCenter();
			setCenter(center.getX() - p.getX(),
			          center.getY() - p.getY());
		}
	}

	/** Start a pan of the map */
	private void startPan(Point p) {
		pan = new PanState(p);
	}

	/** Pan the map */
	private void doPan(Point p) {
		if (pan != null)
			pan.drag(p);
	}

	/** Finish panning the map */
	private void finishPan(Point2D end) {
		if (pan != null) {
			if (pan.isStarted())
				pan.finish(end);
			pan = null;
		}
	}

	/** Zoom in or out from the current extent. */
	public void zoom(boolean zoomin) {
		Point2D center = model.getCenter();
		if (zoomin)
			zoomIn(center);
		else
			zoomOut(center);
	}

	/** Zoom in on the map.
	 * @param p Point in user coordinates. */
	private void zoomIn(final Point2D p) {
		model.zoomIn(p);
	}

	/** Zoom out on the map.
	 * @param p Point in user coordinates. */
	private void zoomOut(final Point2D p) {
		model.zoomOut(p);
	}

	/** Called when the map is resized or the extent is changed */
	private void rescale() {
		mapPane.setSize(getSize());
		if (isShowing())
			repaint();
	}

	/** Render the map */
	private void renderMap(Graphics2D g) {
		Image image = mapPane.getImage();
		if (image != null)
			g.drawImage(image, 0, 0, this);
		paintSelections(g);
	}

	/** Paint the current selections */
	private void paintSelections(Graphics2D g) {
		g.transform(mapPane.getTransform());
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		ListIterator<LayerState> li = model.getLayerIterator();
		while (li.hasPrevious())
			li.previous().paintSelections(g);
	}

	/** Paint the map component */
	public void paintComponent(Graphics g) {
		if (pan != null && pan.isStarted())
			pan.renderMap((Graphics2D)g);
		else
			renderMap((Graphics2D)g);
	}

	/** Dispose of the map */
	public void dispose() {
		mapPane.dispose();
		model.removeLayerChangeListener(listener);
		model.dispose();
	}
}
