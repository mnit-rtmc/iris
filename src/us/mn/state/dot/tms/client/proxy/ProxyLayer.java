/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import us.mn.state.dot.map.Layer;
import us.mn.state.dot.map.LayerChange;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.MapSearcher;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.tms.client.widget.IWorker;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Layer for drawing SONAR proxy objects on the map.
 *
 * @author Douglas Lau
 */
public class ProxyLayer<T extends SonarObject> extends Layer {

	/** Shape used for calculating the layer extent */
	static private final Rectangle2D EXTENT_SHAPE =
		new Rectangle2D.Float(-500, -500, 1000, 1000);

	/** Proxy manager for the layer */
	private final ProxyManager<T> manager;

	/** Get the proxy manager for the layer */
	public ProxyManager<T> getManager() {
		return manager;
	}

	/** Proxy listener */
	private final ProxyListener<T> listener = new ProxyListener<T>() {
		private boolean complete;
		public void proxyAdded(T proxy) {
			// NOTE: this also gets called when we "watch" an
			//       object after it is selected.
			if(complete)
				updateGeometry();
		}
		public void enumerationComplete() {
			complete = true;
			updateExtent();
		}
		public void proxyRemoved(T proxy) {
			updateGeometry();
		}
		public void proxyChanged(T proxy, String attrib) {
			if(manager.isStyleAttrib(attrib))
				updateStatus();
		}
	};

	/** Create a new SONAR map layer */
	public ProxyLayer(ProxyManager<T> m) {
		super(I18N.get(m.getProxyType()));
		manager = m;
	}

	/** Initialize the layer. This cannot be done in the constructor
	 * because subclasses may not be fully constructed. */
	public void initialize() {
		manager.getCache().addProxyListener(listener);
	}

	/** Dispose of the layer */
	public void dispose() {
		manager.getCache().removeProxyListener(listener);
	}

	/** Update the layer geometry */
	public void updateGeometry() {
		runSwing(new Runnable() {
			public void run() {
				fireLayerChanged(LayerChange.geometry);
			}
		});
	}

	/** Update the layer status */
	private void updateStatus() {
		runSwing(new Runnable() {
			public void run() {
				fireLayerChanged(LayerChange.status);
			}
		});
	}

	/** Update the layer extent */
	public void updateExtent() {
		IWorker<Rectangle2D> worker = new IWorker<Rectangle2D>() {
			@Override
			public Rectangle2D doInBackground() {
				ExtentCalculator calc = new ExtentCalculator();
				manager.forEach(calc, 1);
				return calc.extent;
			}
			@Override
			public void done() {
				Rectangle2D e = getResult();
				if(e != null)
					setExtent(e);
			}
		};
		worker.execute();
	}

	/** Class to calculate the extent of the layer */
	private class ExtentCalculator implements MapSearcher {
		private Rectangle2D extent = null;

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

	/** Create a new layer state */
	public LayerState createState(MapBean mb) {
		LayerState s = new ProxyLayerState(this, mb);
		s.addTheme(manager.getTheme());
		s.setTheme(manager.getTheme());
		return s;
	}
}
