/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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
import java.util.HashMap;
import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.IpawsAlert;
import us.mn.state.dot.tms.IpawsAlertHelper;
import us.mn.state.dot.tms.IpawsConfig;
import us.mn.state.dot.tms.IpawsDeployer;
import us.mn.state.dot.tms.IpawsDeployerHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.TransGeoLoc;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.dms.DMSManager;
import us.mn.state.dot.tms.client.map.LayerState;
import us.mn.state.dot.tms.client.map.MapBean;
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyLayer;
import us.mn.state.dot.tms.client.proxy.ProxyLayerState;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.SwingProxyAdapter;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.geo.ZoomLevel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * An (IPAWS) AlertManager is a container for SONAR IpawsDeployer Objects.
 * NOTE this would need some changes to let alert tab handle other types of
 * alerts.
 *
 * @author Gordon Parikh
 */
public class AlertManager extends ProxyManager<IpawsDeployer> {

	/** Keep a handle to the tab */
	private AlertTab tab;

	/** Keep a handle to the theme */
	private AlertTheme theme;

	/** Keep track of the currently selected alert */
	private IpawsDeployer selectedAlertDepl;

	/** Set the currently selected alert. */
	public void setSelectedAlert(IpawsDeployer iad) {
		selectedAlertDepl = iad;
	}

	/** Get the currently selected alert */
	public IpawsDeployer getSelectedAlert() {
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

	/** Create a proxy descriptor. */
	static private ProxyDescriptor<IpawsDeployer> descriptor(Session s) {
		return new ProxyDescriptor<IpawsDeployer>(
			s.getSonarState().getIpawsDeployerCache(), false);
	}

	public AlertManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 10);
	}

	/** Create an alert tab */
	@Override
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
	protected ProxyLayer<IpawsDeployer> createLayer() {
		return new ProxyLayer<IpawsDeployer>(this) {
			@Override
			public LayerState createState(MapBean mb) {
				LayerState s = new ProxyLayerState<
						IpawsDeployer>(this, mb) {
					@Override
					protected void doLeftClick(MouseEvent e, MapObject o) {
						if (tab.getAlertDispatcher().
								getSelectedAlert() != null) {
							// search for DMS - use the map to transform the point
							// then use the DMS manager to search
							Point2D p = map.transformPoint(e.getPoint());
							DMSManager dm = session.getDMSManager();
							selectDmsInTable(dm.findProxy(
									dm.getLayerState().search(p)));

							// check if they clicked out of the alert area
							if (o == null)
								tab.getAlertDispatcher().selectAlert(null);
						}
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
	protected JPopupMenu createPopupSingle(IpawsDeployer proxy) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
		p.addSeparator();
		p.add(new IAction("alert.zoom_area") {
			@Override
			protected void doActionPerformed(ActionEvent ev) {
				zoomToAlertArea();
			}
		});
		return p;
	}

	/** Get the description of a proxy */
	@Override
	public String getDescription(IpawsDeployer proxy) {
		StringBuilder desc = new StringBuilder();
		desc.append(proxy.getName().replace("ipaws_dplr_",
			I18N.get("ipaws_deployer") + " "));
		IpawsAlert ia = IpawsAlertHelper.lookupByIdentifier(
			proxy.getAlertId());
		if (ia != null)
			desc.append(" - " + ia.getEvent());
		IpawsConfig cfg = proxy.getConfig();
		if (cfg != null)
			desc.append(" - " + cfg.getSignGroup());
		return desc.toString();
	}

	/** Check if a given attribute affects a proxy style */
	@Override
	public boolean isStyleAttrib(String a) {
		return "deployed".equals(a) || "active".equals(a) ||
		       "pastPostAlertTime".equals(a);
	}

	/** Check the style of the specified proxy */
	@Override
	public boolean checkStyle(ItemStyle is, IpawsDeployer proxy) {
		AlertState st = AlertState.fromOrdinal(proxy.getAlertState());
		switch (is) {
		case PENDING:
			return st == AlertState.PENDING;
		case SCHEDULED:
			return st == AlertState.APPROVED;
		case ACTIVE:
			return st == AlertState.DEPLOYED;
		case INACTIVE:
			return st == AlertState.INACTIVE;
		case EXPIRED:
			return st == AlertState.EXPIRED;
		case ALL:
			return true;
		default:
			return false;
		}
	}

	/** Check if a style is visible. All alert styles are visible (only
	 *  selected alerts are drawn).
	 */
	@Override
	protected boolean isStyleVisible(IpawsDeployer proxy) {
		return true;
	}

	/** Get the GeoLoc for the specified proxy */
	@Override
	protected GeoLoc getGeoLoc(IpawsDeployer iad) {
		if (iad != null) {
			IpawsAlert ia = IpawsAlertHelper.lookupByIdentifier(
				iad.getAlertId());
			if (ia != null) {
				Double lat = ia.getLat();
				Double lon = ia.getLon();
				if (lat != null && lon != null) {
					return new TransGeoLoc(iad.getName(),
						lat.floatValue(),
						lon.floatValue());
				}
			}
		}
		return null;
	}

	/** Location mapping */
	private final HashMap<String, MapGeoLoc> locations =
		new HashMap<String, MapGeoLoc>();

	/** Find the map geo location for a proxy */
	@Override
	public MapGeoLoc findGeoLoc(IpawsDeployer iad) {
		if (iad != null) {
			String name = iad.getName();
			if (locations.containsKey(name))
				return locations.get(name);

			GeoLoc gl = getGeoLoc(iad);
			if (gl != null) {
				MapGeoLoc loc = new MapGeoLoc(getGeoLoc(iad));
				loc.setManager(this);
				loc.doUpdate();
				locations.put(name, loc);
				// FIXME: MapGeoLoc objects are never removed from locations
				return loc;
			}
		}
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
			IpawsAlert ia = IpawsAlertHelper.lookupByIdentifier(
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
