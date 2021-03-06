/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2019  Minnesota Department of Transportation
 * Copyright (C) 2010  AHMCT, University of California
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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.ScreenPane;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.map.Layer;
import us.mn.state.dot.tms.client.map.LayerState;
import us.mn.state.dot.tms.client.map.MapBean;
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.client.map.MapSearcher;
import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Invokable;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runQueued;
import us.mn.state.dot.tms.geo.MapVector;

/**
 * A proxy manager is a container for SONAR proxy objects. It places each
 * proxy into an appropriate style list model.
 *
 * @author Douglas Lau
 */
abstract public class ProxyManager<T extends SonarObject> {

	/** Make a menu label */
	static protected JPanel makeMenuLabel(String t) {
		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnl.setOpaque(true);
		pnl.add(new JLabel(t));
		return pnl;
	}

	/** Check if the location is set */
	static private boolean isLocationSet(MapGeoLoc loc) {
		return loc != null && !GeoLocHelper.isNull(loc.getGeoLoc());
	}

	/** User session */
	protected final Session session;

	/** Geo location manager */
	private final GeoLocManager loc_manager;

	/** Proxy descriptor */
	private final ProxyDescriptor<T> descriptor;

	/** Get the sonar type name */
	public final String getSonarType() {
		return descriptor.tname;
	}

	/** Get the proxy type cache */
	public final TypeCache<T> getCache() {
		return descriptor.cache;
	}

	/** Listener for proxy events */
	private final SwingProxyAdapter<T> listener =
		new SwingProxyAdapter<T>()
	{
		protected void proxyAddedSwing(T proxy) {
			ProxyManager.this.proxyAddedSwing(proxy);
		}
		protected void enumerationCompleteSwing(Collection<T> proxies) {
			ProxyManager.this.enumerationCompleteSwing(proxies);
			updateExtent();
		}
		protected void proxyRemovedSwing(T proxy) {
			ProxyManager.this.proxyRemovedSwing(proxy);
		}
		protected void proxyChangedSwing(T proxy, String attr) {
			ProxyManager.this.proxyChangedSwing(proxy, attr);
		}
		protected boolean checkAttributeChange(String attr) {
			return ProxyManager.this.checkAttributeChange(attr);
		}
	};

	/** Selection model */
	private final ProxySelectionModel<T> sel_mdl =
		new ProxySelectionModel<T>();

	/** Theme for drawing objects on a map layer */
	private final ProxyTheme<T> theme;

	/** Cache of MapObject to proxy */
	private final ProxyMapCache<T> map_cache = new ProxyMapCache<T>();

	/** Map layer for the proxy type */
	private final ProxyLayer<T> layer;

	/** Get the map layer */
	public ProxyLayer<T> getLayer() {
		return layer;
	}

	/** Zoom visibility threshold (0 indicates no layer for proxies) */
	private final int zoom_threshold;

	/** Default style */
	private final ItemStyle def_style;

	/** Currently selected style */
	private ItemStyle sel_style;

	/** Screen pane */
	protected ScreenPane s_pane;

	/** Set the screen pane */
	public void setScreenPane(ScreenPane sp) {
		s_pane = sp;
	}
	
	/** Get the screen pane */
	public ScreenPane getScreenPane() {
		return s_pane;
	}

	/** Create a new proxy manager.
	 * @param s Session.
	 * @param lm Location manager.
	 * @param pd Proxy descriptor.
	 * @param zt Zoom threshold.
	 * @param ds Default item style. */
	protected ProxyManager(Session s, GeoLocManager lm,
		ProxyDescriptor<T> pd, int zt, ItemStyle ds)
	{
		session = s;
		loc_manager = lm;
		descriptor = pd;
		zoom_threshold = zt;
		def_style = ds;
		sel_style = ds;
		theme = createTheme();
		layer = hasLayer() ? createLayer() : null;
	}

	/** Create a new proxy manager */
	protected ProxyManager(Session s, GeoLocManager lm,
		ProxyDescriptor<T> pd, int zt)
	{
		this(s, lm, pd, zt, ItemStyle.ALL);
	}

	/** Initialize the proxy manager. This cannot be done in the constructor
	 * because subclasses may not be fully constructed. */
	public void initialize() {
		getCache().addProxyListener(listener);
	}

	/** Dispose of the proxy manager */
	public void dispose() {
		sel_mdl.dispose();
		map_cache.dispose();
		getCache().removeProxyListener(listener);
	}

	/** Create a layer for this proxy type */
	protected ProxyLayer<T> createLayer() {
		return new ProxyLayer<T>(this);
	}

	/** Add a proxy to the manager */
	protected void proxyAddedSwing(T proxy) {
		// NOTE: this also gets called when we "watch" an
		//       object after it is selected.
		cacheMapGeoLoc(proxy);
		updateGeometry();
	}

	/** Cache a MapGeoLoc for one proxy */
	private void cacheMapGeoLoc(T proxy) {
		MapGeoLoc loc = findGeoLoc(proxy);
		if (loc != null) {
			loc.setManager(this);
			loc.doUpdate();
			map_cache.put(loc, proxy);
		}
	}

	/** Enumeraton complete */
	protected void enumerationCompleteSwing(Collection<T> proxies) {
		for (final T p: proxies) {
			runQueued(new Invokable() {
				public void invoke() {
					cacheMapGeoLoc(p);
				}
			});
		}
	}

	/** Remove a proxy from the manager */
	protected void proxyRemovedSwing(T proxy) {
		sel_mdl.removeSelected(proxy);
		map_cache.remove(proxy);
		updateGeometry();
	}

	/** Update layer geometry */
	public final void updateGeometry() {
		if (layer != null) {
			runQueued(new Invokable() {
				public void invoke() {
					layer.updateGeometry();
				}
			});
		}
	}

	/** Update layer extent */
	public final void updateExtent() {
		if (layer != null) {
			runQueued(new Invokable() {
				public void invoke() {
					layer.updateExtent();
				}
			});
		}
	}

	/** Check if an attribute change is interesting */
	private boolean checkAttributeChange(String attr) {
		return isStyleAttrib(attr);
	}

	/** Called when a proxy has been changed */
	protected void proxyChangedSwing(T proxy, String attr) {
		if (layer != null && isStyleAttrib(attr))
			layer.updateStatus();
	}

	/** Get the normal vector for the given location */
	public MapVector getNormalVector(MapGeoLoc loc) {
		return loc_manager.getNormalVector(loc);
	}

	/** Create a map tab for the managed proxies */
	public MapTab<T> createTab() {
		return null;
	}

	/** Check if user can read managed proxies */
	public boolean canRead() {
		return session.canRead(getSonarType());
	}

	/** Create a list cell renderer */
	public ListCellRenderer<T> createCellRenderer() {
		return new ProxyCellRenderer<T>(this);
	}

	/** Create a proxy JList */
	public ProxyJList<T> createList() {
		return new ProxyJList<T>(this);
	}

	/** Create a theme for this type of proxy */
	abstract protected ProxyTheme<T> createTheme();

	/** Current cell renderer size */
	private CellRendererSize m_cellSize = CellRendererSize.LARGE;

	/** Set the current cell size */
	public void setCellSize(CellRendererSize size) {
		m_cellSize = size;
	}

	/** Get the current cell size */
	public CellRendererSize getCellSize() {
		return m_cellSize;
	}

	/** Get the theme */
	public ProxyTheme<T> getTheme() {
		return theme;
	}

	/** Test if a layer is the manager's layer */
	public boolean checkLayer(Layer l) {
		return l == layer;
	}

	/** Keep a handle to this layer state. */
	private LayerState layerState;
	
	/** Create layer state for a map bean */
	public LayerState createState(MapBean mb) {
		layerState = layer.createState(mb);
		return layerState;
	}
	
	/** Return the layer state used by this manager */
	public LayerState getLayerState() {
		return layerState;
	}

	/** Get the proxy selection model */
	public ProxySelectionModel<T> getSelectionModel() {
		return sel_mdl;
	}

	/** Create a new style summary with 2 rows of buttons.
	 * @param size_btns Enable buttons to change cell size.
	 * @return A style summary for the proxy type T. */
	public StyleSummary<T> createStyleSummary(boolean size_btns) {
		return new StyleSummary<T>(this, def_style, size_btns);
	}
	
	/** Create a new style summary.
	 * @param size_btns Enable buttons to change cell size.
	 * @param sr Number of rows of style buttons.
	 * @return A style summary for the proxy type T. */
	public StyleSummary<T> createStyleSummary(boolean size_btns, int sr) {
		return new StyleSummary<T>(this, def_style, size_btns, sr);
	}

	/** Get the specified style list model */
	public StyleListModel<T> getStyleModel(String s) {
		return createStyleListModel(theme.getStyle(s));
	}

	/** Set the selected style */
	public void setSelectedStyle(ItemStyle sty) {
		sel_style = sty;
		if (layer != null)
			layer.updateStatus();
	}

	/** Get the selected style */
	public ItemStyle getSelectedStyle() {
		return sel_style;
	}

	/** Create a style list model for the given symbol */
	protected StyleListModel<T> createStyleListModel(Style sty) {
		return new StyleListModel<T>(this, sty.toString());
	}

	/** Check if a given attribute affects a proxy style */
	public boolean isStyleAttrib(String a) {
		return "styles".equals(a);
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(ItemStyle is, T proxy) {
		return false;
	}

	/** Get the ItemStyle of a proxy */
	public ItemStyle getItemStyle(T proxy) {
		Style style = getStyle(proxy);
		if (style != null)
			return ItemStyle.lookupStyle(style.toString());
		return null;
	}
	
	/** Get the style of a proxy */
	public Style getStyle(T proxy) {
		return theme.getStyle(proxy);
	}

	/** Get a style icon */
	public Icon getIcon(Style sty) {
		return theme.getLegend(sty);
	}

	/** Get an icon for a proxy */
	public Icon getIcon(T proxy) {
		return getIcon(getStyle(proxy));
	}

	/** Show the properties form for the selected proxy */
	public final void showPropertiesForm() {
		T proxy = sel_mdl.getSingleSelection();
		if (proxy != null)
			showPropertiesForm(proxy);
	}

	/** Show the properties form for the specified proxy */
	public final void showPropertiesForm(T proxy) {
		SonarObjectForm<T> form = createPropertiesForm(proxy);
		if (form != null)
			session.getDesktop().show(form);
	}

	/** Create a properties form for the specified proxy */
	private SonarObjectForm<T> createPropertiesForm(T proxy) {
		return descriptor.createPropertiesForm(proxy);
	}

	/** Create an action to display proxy table */
	public final IAction createTableAction() {
		return canRead() ? new IAction(getSonarType() + ".title") {
			protected void doActionPerformed(ActionEvent e) {
				ProxyTableForm<T> form =
					descriptor.makeTableForm();
				if (form != null)
					session.getDesktop().show(form);
			}
		} : null;
	}

	/** Show the popup menu for the selected proxy or proxies */
	public void showPopupMenu(MouseEvent e) {
		JPopupMenu popup = createPopup();
		if (popup != null) {
			popup.setInvoker(e.getComponent());
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/** Create a popup menu for the selected proxy object(s) */
	private JPopupMenu createPopup() {
		T proxy = sel_mdl.getSingleSelection();
		if (proxy != null)
			return createPopupSingle(proxy);
		else {
			int n_sel = sel_mdl.getSelectedCount();
			return (n_sel > 1) ? createPopupMulti(n_sel) : null;
		}
	}

	/** Create a popup menu for a single selection */
	protected JPopupMenu createPopupSingle(T proxy) {
		GeoLoc loc = getGeoLoc(proxy);
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
		p.addSeparator();
		fillPopupSingle(p, proxy);
		if (descriptor.has_properties) {
			if (WorkRequestAction.isConfigured())
				fillPopupWorkReq(p, proxy);
		}
		if (s_pane != null && loc != null) {
			p.add(new MapAction<T>(s_pane, proxy, loc,
				zoom_threshold));
		}
		if (descriptor.has_properties)
			p.add(new PropertiesAction<T>(this, proxy));
		return p;
	}

	/** Fill single selection popup */
	protected void fillPopupSingle(JPopupMenu p, T proxy) {
		// subclasses may override
	}

	/** Fill single selection work request popup */
	protected void fillPopupWorkReq(JPopupMenu p, T proxy) {
		GeoLoc loc = getGeoLoc(proxy);
		p.add(new WorkRequestAction<T>(proxy, loc));
		p.addSeparator();
	}

	/** Create a popup menu for multiple objects */
	protected JPopupMenu createPopupMulti(int n_selected) {
		return null;
	}

	/** Iterate through all proxy objects */
	public MapObject forEach(MapSearcher s) {
		synchronized (map_cache) {
			for (MapGeoLoc loc: map_cache) {
				if (isVisible(loc) && s.next(loc))
					return loc;
			}
		}
		return null;
	}

	/** Check if a MapGeoLoc is visible */
	private boolean isVisible(MapGeoLoc loc) {
		return isLocationSet(loc) && isStyleVisible(loc);
	}

	/** Check if a MapGeoLoc style is visible */
	private boolean isStyleVisible(MapGeoLoc loc) {
		T proxy = findProxy(loc);
		return (proxy != null) &&
		       (isStyleVisible(proxy) || sel_mdl.isSelected(proxy));
	}

	/** Check if a proxy style is visible */
	protected boolean isStyleVisible(T proxy) {
		return checkStyle(ItemStyle.ACTIVE, proxy);
	}

	/** Find the map geo location for a proxy */
	public MapGeoLoc findGeoLoc(T proxy) {
		GeoLoc loc = getGeoLoc(proxy);
		return (loc != null)
		      ? loc_manager.findMapGeoLoc(loc)
		      : null;
	}

	/** Get the GeoLoc for the specified proxy */
	abstract protected GeoLoc getGeoLoc(T proxy);

	/** Find a proxy matching the given map object */
	public T findProxy(MapObject mo) {
		return (mo instanceof MapGeoLoc)
		      ? map_cache.lookup((MapGeoLoc) mo)
		      : null;
	}

	/** Get the description of a proxy */
	public String getDescription(T proxy) {
		return proxy.getName() + " - " +
			GeoLocHelper.getLocation(getGeoLoc(proxy));
	}

	/** Check if the corresponding layer is visible.
	 * @param zoom Current map zoom level.
	 * @return True if the layer should be visible. */
	public boolean isVisible(int zoom) {
		return zoom >= zoom_threshold;
	}

	/** Check if manager has a layer to display */
	public final boolean hasLayer() {
		return canRead() && (zoom_threshold > 0);
	}
	
	/** Force a map update */
	public void updateMap() {
		s_pane.getMap().repaint();
	}
}
