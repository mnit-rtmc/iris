/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
package us.mn.state.dot.tms.client.alert;

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.JPopupMenu;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.IpawsAlert;
import us.mn.state.dot.tms.IpawsAlertHelper;
import us.mn.state.dot.tms.IpawsAlertDeployer;
import us.mn.state.dot.tms.IpawsAlertDeployerHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.dms.DMSManager;
import us.mn.state.dot.tms.client.map.LayerState;
import us.mn.state.dot.tms.client.map.MapBean;
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyLayer;
import us.mn.state.dot.tms.client.proxy.ProxyLayerState;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.SwingProxyAdapter;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.geo.ZoomLevel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * An (IPAWS) AlertManager is a container for SONAR IpawsAlertDeployer Objects.
 * NOTE this would need some changes to let alert tab handle other types of
 * alerts.
 *
 * @author Gordon Parikh
 */
public class AlertManager extends ProxyManager<IpawsAlertDeployer> {
	
	/** IpawsAlertDeployer cache */
	private final TypeCache<IpawsAlertDeployer> adcache;

	/** IpawsAlert cache */
	private final TypeCache<IpawsAlert> acache;
	
	/** Keep a handle to the tab */
	private AlertTab tab;
	
	/** Keep a handle to the theme */
	private AlertTheme theme;
	
	/** Keep track of the currently selected alert */
	private IpawsAlertDeployer selectedAlertDepl;
	
	/** Set the currently selected alert. */
	public void setSelectedAlert(IpawsAlertDeployer iad) {
		selectedAlertDepl = iad;
	}
	
	/** Get the currently selected alert */
	public IpawsAlertDeployer getSelectedAlert() {
		return selectedAlertDepl;
	}
	
	/** Keep track of alert editing state for communicating between various
	 *  alert components
	 */
	private boolean isEditing = false;
	
	/** Set the editing state */
	public void setEditing(boolean e) {
		isEditing = e;
	}
	
	/** Get the editing state */
	public boolean getEditing() {
		return isEditing;
	}
	
	/** Whether or not the user wants to see all DMS in the selected alert
	 *  deployer's sign group.
	 */
	private boolean showAllDms = false;
	
	/** Set the show all DMS flag. */
	public void setShowAllDms(boolean s) {
		showAllDms = s;
	}
	
	/** Get the show all DMS flag. */
	public boolean getShowAllDms() {
		return showAllDms;
	}
	
	/** Proxy listener for SONAR updates */
	private final SwingProxyAdapter<IpawsAlertDeployer> listener = 
			new SwingProxyAdapter<IpawsAlertDeployer>() {
		@Override
		protected void proxyAddedSwing(IpawsAlertDeployer iad) {
			if (tab != null)
				tab.updateStyleCounts();
		}
		
		@Override
		protected void proxyChangedSwing(IpawsAlertDeployer iad, String attr) {
			if (tab != null)
				tab.updateStyleCounts();
		}
	};
	
	/** Create a proxy descriptor. */
	static private ProxyDescriptor<IpawsAlertDeployer> descriptor(Session s) {
		return new ProxyDescriptor<IpawsAlertDeployer>(
			s.getSonarState().getIpawsDeployerCache(), false);
	}
	
	public AlertManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 10);
		
		// add the listener to the cache
		adcache = s.getSonarState().getIpawsDeployerCache();
		adcache.addProxyListener(listener);
		acache = s.getSonarState().getIpawsAlertCache();
	}
	
	/** Create an alert tab */
	public AlertTab createTab() {
		tab = new AlertTab(session, this);
		return tab;
	}

	@Override
	protected AlertTheme createTheme() {
		theme = new AlertTheme(this, session);
		return theme;
	}
	
	/** Override createLayer to let us provide a modified ProxyLayerState. */
	@Override
	protected ProxyLayer<IpawsAlertDeployer> createLayer() {
		return new ProxyLayer<IpawsAlertDeployer>(this) {
			@Override
			public LayerState createState(MapBean mb) {
				LayerState s = new ProxyLayerState<
						IpawsAlertDeployer>(this, mb) {
					@Override
					protected void doLeftClick(MouseEvent e, MapObject o) {
						// search for DMS - use the map to transform the point
						// then use the DMS manager to search
						Point2D p = map.transformPoint(e.getPoint());
						DMSManager dm = session.getDMSManager();
						selectDmsInTable(dm.findProxy(
								dm.getLayerState().search(p)));
						
						// check if they clicked out of the alert area
						System.out.println(o);
						if (o == null)
							tab.getAlertDispatcher().selectAlert(null);
					}
				};
				s.addTheme(theme);
				s.setTheme(theme);
				return s;
			}
		};
	}
	
	/** Put a custom entry in the popup menu ("Center map" will look weird,
	 *  we want zoom-to-boundary instead).
	 */
	@SuppressWarnings("serial")
	@Override
	protected JPopupMenu createPopupSingle(IpawsAlertDeployer proxy) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
		p.addSeparator();
		p.add(new IAction("alert.zoom_area") {
			@Override
			protected void doActionPerformed(ActionEvent ev) throws Exception {
				zoomToAlertArea();
			}
		});
		return p;
	}
	
	@Override
	public String getDescription(IpawsAlertDeployer proxy) {
		IpawsAlert ia = acache.lookupObject(proxy.getAlertId());
		String name = proxy.getName();
		if (name.contains("ipaws_dplr_")) {
			name = name.replace("ipaws_dplr_",
					I18N.get("ipaws_deployer") + " ");
		}
		if (ia != null)
			return name + " - " + ia.getEvent();
		return name;
	}
	
	@Override
	public boolean checkStyle(ItemStyle is, IpawsAlertDeployer proxy) {
		Integer t = checkAlertTimes(proxy);
		boolean past = IpawsAlertDeployerHelper.isPastPostAlertTime(proxy);
		if (t == null)
			// problem with the dates
			return false;
		switch (is) {
		case PENDING:
			return proxy.getDeployed() == null && !past;
		case SCHEDULED:
			// scheduled alerts are deployed (i.e. approved) but not active
			return Boolean.TRUE.equals(proxy.getDeployed())
				&& !proxy.getActive() && !past;
		case ACTIVE:
			return Boolean.TRUE.equals(proxy.getDeployed())
				&& proxy.getActive() && !past;
		case INACTIVE:
			return Boolean.FALSE.equals(proxy.getDeployed())
				&& !proxy.getActive() && !past;
		case PAST:
			return Boolean.FALSE.equals(proxy.getDeployed())
				&& !proxy.getActive() && past;
		case ALL:
			// TODO some alerts are slipping through here but not in other
			// categories...
			return true;
		default:
			return false;
		}
	}
	
	/** Check if a style is visible. All alert styles are visible (only
	 *  selected alerts are drawn).
	 */
	@Override
	protected boolean isStyleVisible(IpawsAlertDeployer proxy) {
		return true;
	}
	
	/** Update the counts in the style summary */
	public void updateStyleCounts() {
		if (tab != null)
			tab.updateStyleCounts();
	}
	
	/** Check when this alert will start relative to the current time. Returns
	 *  -1 if this alert has not yet started, 0 if the alert is currently
	 *  active, and 1 if the alert is in the past. If the time fields are not
	 *  filled, null is returned. 
	 */
	private Integer checkAlertTimes(IpawsAlertDeployer iad) {
		if (iad.getAlertStart() != null && iad.getAlertEnd() != null) {
			// check the time of the alert relative to now
			Date now = new Date();
			if (now.before(iad.getAlertStart()))
				return -1;
			else if (now.after(iad.getAlertStart())
					&& now.before(iad.getAlertEnd()))
				return 0;
			else if (now.after(iad.getAlertEnd())) {
				// if after end time, check post alert time
				long t = now.getTime() - iad.getAlertEnd().getTime();
				int mins = (int) TimeUnit.HOURS.convert(
						t, TimeUnit.MILLISECONDS);
				if (mins < iad.getPostAlertTime())
					return 0;
				return 1;
			}
			return 1;
		}
		// missing alert times - return null
		return null;
	}

	@Override
	protected GeoLoc getGeoLoc(IpawsAlertDeployer iad) {
		// return the GeoLoc for this deployer
		if (iad != null)
			return iad.getGeoLoc();
		return null;
	}
	
	/** Selected DMS (for communicating between theme and dispatcher) */
	private DMS selectedDms;
	
	/** Set the selected DMS. */
	public void setSelectedDms(DMS dms) {
		selectedDms = dms;
	}

	/** Get the selected DMS. */
	public DMS getSelectedDms() {
		return selectedDms;
	}
	
	/** Center the map on the DMS provided at the given zoom threshold. */
	public void centerMap(DMS dms, int zt) {
		ZoomLevel zoom = ZoomLevel.fromOrdinal(zt > 15 ? zt : 15);
		GeoLoc gl = dms.getGeoLoc();
		Double lat = gl.getLat();
		Double lon = gl.getLon();
		if (lat != null && lon != null)
			s_pane.setMapExtent(zoom, lat.floatValue(), lon.floatValue());
	}
	
	/** Zoom to the selected alert area, showing the entire area on the map.*/
	public void zoomToAlertArea() {
		if (selectedAlertDepl != null) {
			// get shapes representing the alert area
			IpawsAlert ia = IpawsAlertHelper.lookup(
					selectedAlertDepl.getAlertId());
			ArrayList<Shape> shapes = IpawsAlertHelper.getShapes(ia);
			
			// find the min and max x and y points
			ArrayList<Double> xPoints = new ArrayList<Double>();
			ArrayList<Double> yPoints = new ArrayList<Double>();
			for (Shape shp: shapes) {
				Rectangle2D r = shp.getBounds2D();
				xPoints.add(r.getMinX());
				xPoints.add(r.getMaxX());
				yPoints.add(r.getMinY());
				yPoints.add(r.getMaxY());
			}
			Collections.sort(xPoints);
			Collections.sort(yPoints);
			double minX = xPoints.get(0);
			double maxX = xPoints.get(xPoints.size()-1);
			double minY = yPoints.get(0);
			double maxY = yPoints.get(yPoints.size()-1);
			double w = maxX - minX;
			double h = maxY - minY;
			
			// set the map extents to the outer bounds of all polygons
			s_pane.getMap().getModel().setExtent(
					minX, minY, w, h);
		}
	}
	
	/** Select a DMS in the AlertDmsDispatcher table */
	public void selectDmsInTable(DMS d) {
		tab.getDmsDispatcher().selectDmsInTable(d);
	}
}
