/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import javax.swing.SwingUtilities;
import us.mn.state.dot.map.DynamicLayer;
import us.mn.state.dot.map.Layer;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.MapSearcher;
import us.mn.state.dot.map.event.LayerChangedEvent;
import us.mn.state.dot.tms.client.TmsSelectionModel;

/**
 * Layer for drawing TMS objects on the map.
 *
 * @author Douglas Lau
 */
public class TmsMapLayer extends Layer implements DynamicLayer {

	/** Proxy handler for the layer */
	protected final ProxyHandler handler;

	/** Get the proxy handler for the layer */
	public ProxyHandler getHandler() {
		return handler;
	}

	/** Model describing the selected object */
	protected final TmsSelectionModel selectionModel;

	/** Create a new TMS map layer */
	public TmsMapLayer(ProxyHandler h) {
		super(h.getProxyType());
		handler = h;
		handler.addRefreshListener(new RefreshListener() {
			public void dataChanged() {
				updateExtent();
				notifyLayerChanged(LayerChangedEvent.DATA);
			}
		});
		selectionModel = handler.getSelectionModel();
	}

	/** Create a new layer state */
	public LayerState createState() {
		LayerState s = new TmsMapLayerState(this);
		s.addTheme(handler.getTheme());
		s.setTheme(handler.getTheme());
		return s;
	}

	/** Dispose of the layer */
	public void dispose() {
		handler.dispose();
	}

	/** Update the layer extent */
	protected void updateExtent() {
		Rectangle2D e = null;
		Rectangle2D shape = new Rectangle2D.Float(-50, -50, 100, 100);
		Map<Object, TmsMapProxy> proxies = handler.getProxies();
		synchronized(proxies) {
			for(TmsMapProxy o: proxies.values()) {
				if(o.hasLocation()) {
					AffineTransform t = o.getTransform();
					Rectangle2D b =
						t.createTransformedShape(
						shape).getBounds2D();
					if(e == null) {
						e = new Rectangle2D.Double();
						e.setRect(b);
					} else
						e.add(b);
				}
			}
		}
		if(e != null)
			extent.setRect(e);
	}

	/** Iterate through all shapes */
	public MapObject forEach(MapSearcher s) {
		Map<Object, TmsMapProxy> proxies = handler.getProxies();
		synchronized(proxies) {
			for(TmsMapProxy o: proxies.values()) {
				if(s.next(o))
					return o;
			}
		}
		return null;
	}

	/** Notify listeners that the layer has changed */
	protected void notifyLayerChanged(final int reason) {
		final TmsMapLayer layer = this;
		Runnable notifier = new Runnable() {
			public void run() {
				notifyLayerChangedListeners(
					new LayerChangedEvent(layer, reason));
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
}
