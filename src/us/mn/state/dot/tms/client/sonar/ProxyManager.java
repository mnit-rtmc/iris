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

import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.MapSearcher;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;

/**
 * A proxy manager is a container for SONAR proxy objects. It places each
 * proxy into an appropriate style list model.
 *
 * @author Douglas Lau
 */
abstract public class ProxyManager<T extends SonarObject>
	implements ProxyListener<T>
{
	/** Name of list model containing all objects */
	static public final String STYLE_ALL = "All";

	/** Make a menu label */
	static protected Box makeMenuLabel(String id) {
		Box b = Box.createHorizontalBox();
		b.add(Box.createHorizontalStrut(6));
		b.add(Box.createHorizontalGlue());
		b.add(new JLabel(id));
		b.add(Box.createHorizontalGlue());
		b.add(Box.createHorizontalStrut(6));
		return b;
	}

	/** Proxy type cache */
	protected final TypeCache<T> cache;

	/** Geo location manager */
	protected final GeoLocManager loc_manager;

	/** Selection model */
	protected final ProxySelectionModel<T> s_model =
		new ProxySelectionModel<T>();

	/** Styled theme for drawing objects on a map layer */
	protected final StyledTheme theme;

	/** Map of symbol names to style list models */
	protected final Map<String, StyleListModel<T>> models =
		new HashMap<String, StyleListModel<T>>();

	/** Mapping from MapObject identityHashCode to proxy objects.  This is
	 * an optimization cache to help findProxy run fast. */
	protected final HashMap<Integer, T> map_proxies =
		new HashMap<Integer, T>();

	/** Map layer for the proxy type */
	protected final SonarLayer<T> layer;

	/** Create a new proxy manager */
	protected ProxyManager(TypeCache<T> c, GeoLocManager lm) {
		cache = c;
		loc_manager = lm;
		theme = createTheme();
		for(Symbol s: theme.getSymbols()) {
			StyleListModel<T> slm = createStyleListModel(s);
			if(slm != null)
				models.put(s.getLabel(), slm);
		}
		layer = createLayer();
	}

	/** Create a style list model for the given symbol */
	protected StyleListModel<T> createStyleListModel(Symbol s) {
		return new StyleListModel<T>(this, s.getLabel(), s.getLegend());
	}

	/** Create a layer for this proxy type */
	protected SonarLayer<T> createLayer() {
		return new SonarLayer<T>(this);
	}

	/** Initialize the proxy manager. This cannot be done in the constructor
	 * because subclasses may not be fully constructed. */
	public void initialize() {
		cache.addProxyListener(this);
		for(StyleListModel<T> model: models.values())
			model.initialize();
		layer.initialize();
	}

	/** Dispose of the proxy manager */
	public void dispose() {
		layer.dispose();
		for(StyleListModel<T> model: models.values())
			model.dispose();
		s_model.dispose();
		models.clear();
		cache.removeProxyListener(this);
	}

	/** Called when a proxy has been added */
	public void proxyAdded(final T proxy) {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				proxyAddedSlow(proxy);
			}
		}.addToScheduler();
	}

	/** Add a proxy to the manager */
	protected void proxyAddedSlow(T proxy) {
		MapGeoLoc loc = findGeoLoc(proxy);
		if(loc != null) {
			int i = System.identityHashCode(loc);
			synchronized(map_proxies) {
				map_proxies.put(i, proxy);
			}
		}
	}

	/** Called when proxy enumeration is complete */
	public void enumerationComplete() {
		// not interested
	}

	/** Called when a proxy has been removed */
	public void proxyRemoved(T proxy) {
		// FIXME: this will leak when proxy objects are removed.
		// Not easy to fix, since proxy objects die before we can
		// get the corresponding MapGeoLoc to remove.
	}

	/** Called when a proxy attribute has changed */
	public void proxyChanged(T proxy, String a) {
		// not interested
	}

	/** Get the proxy type name */
	abstract public String getProxyType();

	/** Get the proxy type cache */
	public TypeCache<T> getCache() {
		return cache;
	}

	/** Create a styled theme for this type of proxy */
	abstract protected StyledTheme createTheme();

	/** Get the theme */
	public StyledTheme getTheme() {
		return theme;
	}

	/** Create a map layer for the proxy type */
	public SonarLayer<T> getLayer() {
		return layer;
	}

	/** Get the proxy selection model */
	public ProxySelectionModel<T> getSelectionModel() {
		return s_model;
	}

	/** Create a new style summary for this proxy type */
	public StyleSummary<T> createStyleSummary() {
		return new StyleSummary<T>(this);
	}

	/** Get the specified style list model */
	public StyleListModel<T> getStyleModel(String s) {
		return models.get(s);
	}

	/** Get an array of all styles */
	public String[] getStyles() {
		LinkedList<String> styles = new LinkedList<String>();
		for(Symbol s: theme.getSymbols())
			styles.add(s.getLabel());
		return (String[])styles.toArray(new String[0]);
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, T proxy) {
		return false;
	}

	/** Show the properties form for the selected proxy */
	abstract public void showPropertiesForm();

	/** Show the popup menu for the selected proxy or proxies */
	public void showPopupMenu(MouseEvent e) {
		JPopupMenu popup = createPopup();
		if(popup != null) {
			popup.setInvoker(e.getComponent());
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/** Create a popup menu for the selected proxy object(s) */
	abstract protected JPopupMenu createPopup();

	/** Iterate through all proxy objects */
	public MapObject forEach(final MapSearcher s) {
		T result = cache.findObject(new Checker<T>() {
			public boolean check(T proxy) {
				MapGeoLoc loc = findGeoLoc(proxy);
				return isLocationSet(loc) && s.next(loc);
			}
		});
		if(result != null)
			return findGeoLoc(result);
		else
			return null;
	}

	/** Check if the location is set */
	static protected boolean isLocationSet(MapGeoLoc loc) {
		return loc != null && !GeoLocHelper.isNull(loc.getGeoLoc());
	}

	/** Find the map geo location for a proxy */
	public MapGeoLoc findGeoLoc(T proxy) {
		GeoLoc loc = getGeoLoc(proxy);
		if(loc != null)
			return loc_manager.findMapGeoLoc(loc);
		else
			return null;
	}

	/** Get the GeoLoc for the specified proxy */
	abstract protected GeoLoc getGeoLoc(T proxy);

	/** Find a proxy matching the given map object */
	public T findProxy(MapObject o) {
		int i = System.identityHashCode(o);
		synchronized(map_proxies) {
			return map_proxies.get(i);
		}
	}

	/** Get the description of a proxy */
	public String getDescription(T proxy) {
		return proxy.getName() + " - " +
			GeoLocHelper.getDescription(getGeoLoc(proxy));
	}
}
