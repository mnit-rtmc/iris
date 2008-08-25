/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.sonar;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.SwingUtilities;
import us.mn.state.dot.map.DynamicLayer;
import us.mn.state.dot.map.Layer;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.MapSearcher;
import us.mn.state.dot.map.event.LayerChangedEvent;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;

/**
 * Layer for drawing SONAR objects on the map.
 *
 * @author Douglas Lau
 */
public class SonarLayer<T extends SonarObject> extends Layer
	implements DynamicLayer, ProxyListener<T>
{
	/** Shape used for calculating the layer extent */
	static protected final Rectangle2D EXTENT_SHAPE =
		new Rectangle2D.Float(-50, -50, 100, 100);

	/** Proxy manager for the layer */
	protected final ProxyManager<T> manager;

	/** Proxy type cache */
	protected final TypeCache<T> cache;

	/** Get the proxy manager for the layer */
	public ProxyManager<T> getManager() {
		return manager;
	}

	/** Create a new SONAR map layer */
	public SonarLayer(ProxyManager<T> m) {
		super(m.getProxyType());
		manager = m;
		cache = manager.getCache();
	}

	/** Initialize the layer. This cannot be done in the constructor
	 * because subclasses may not be fully constructed. */
	public void initialize() {
		cache.addProxyListener(this);
	}

	/** Dispose of the layer */
	public void dispose() {
		cache.removeProxyListener(this);
	}

	/** Add a new proxy to the layer */
	public void proxyAdded(T proxy) {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				updateExtent();
				notifyLayerChanged(LayerChangedEvent.DATA);
			}
		}.addToScheduler();
	}

	/** Remove a proxy from the model */
	public void proxyRemoved(T proxy) {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				updateExtent();
				notifyLayerChanged(LayerChangedEvent.DATA);
			}
		}.addToScheduler();
	}

	/** Change a proxy in the model */
	public void proxyChanged(T proxy, String attrib) {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				// Can an attribute change affect the layer?
				notifyLayerChanged(LayerChangedEvent.DATA);
			}
		}.addToScheduler();
	}

	/** Update the layer extent */
	protected void updateExtent() {
		ExtentCalculator calc = new ExtentCalculator();
		manager.forEach(calc);
		if(calc.extent != null)
			extent.setRect(calc.extent);
	}

	/** Class to calculate the extent of the layer */
	protected class ExtentCalculator implements MapSearcher {
		protected Rectangle2D extent = null;

		public boolean next(MapObject o) {
			AffineTransform t = o.getTransform();
			Rectangle2D b = t.createTransformedShape(
				EXTENT_SHAPE).getBounds2D();
			if(extent == null) {
				extent = new Rectangle2D.Double();
				extent.setRect(b);
			} else
				extent.add(b);
			return false;
		}
	}

	/** Iterate through all shapes in the layer */
	public MapObject forEach(MapSearcher s) {
		return manager.forEach(s);
	}

	/** Notify listeners that the layer has changed */
	protected void notifyLayerChanged(final int reason) {
		final LayerChangedEvent e = new LayerChangedEvent(this, reason);
		Runnable notifier = new Runnable() {
			public void run() {
				notifyLayerChangedListeners(e);
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			notifier.run();
		else
			SwingUtilities.invokeLater(notifier);
	}

	/** Notify the renderer of the layer has changed */
	public void notifyRendererChanged() {
		notifyLayerChanged(LayerChangedEvent.DATA);
	}

	/** Create a new layer state */
	public LayerState createState() {
		LayerState s = new SonarLayerState(this);
		s.addTheme(manager.getTheme());
		s.setTheme(manager.getTheme());
		return s;
	}
}
