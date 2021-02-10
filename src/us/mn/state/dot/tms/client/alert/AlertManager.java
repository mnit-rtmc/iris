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
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.AlertInfo;
import us.mn.state.dot.tms.AlertInfoHelper;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.CapEvent;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.TransGeoLoc;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.map.LayerState;
import us.mn.state.dot.tms.client.map.MapBean;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyJList;
import us.mn.state.dot.tms.client.proxy.ProxyLayer;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.geo.ZoomLevel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * An AlertManager is a container for AlertInfo Objects.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class AlertManager extends ProxyManager<AlertInfo> {

	/** Keep a handle to the tab */
	private final AlertTab tab;

	/** Keep a handle to the theme */
	private AlertTheme theme;

	/** Create a proxy descriptor */
	static private ProxyDescriptor<AlertInfo> descriptor(Session s) {
		return new ProxyDescriptor<AlertInfo>(
			s.getSonarState().getAlertInfos(), false);
	}

	public AlertManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 10);
		tab = new AlertTab(session, this);
	}

	/** Create a proxy JList */
	@Override
	public ProxyJList<AlertInfo> createList() {
		ProxyJList<AlertInfo> p_list = super.createList();
		p_list.setVisibleRowCount(4);
		return p_list;
	}

	/** Create an alert tab */
	@Override
	public AlertTab createTab() {
		return tab;
	}

	@Override
	protected AlertTheme createTheme() {
		theme = new AlertTheme(session, this);
		return theme;
	}

	@Override
	protected ProxyLayer<AlertInfo> createLayer() {
		return new ProxyLayer<AlertInfo>(this) {
			@Override
			public LayerState createState(MapBean mb) {
				LayerState s = new AlertLayerState(this, mb,
					tab.getDmsDispatcher(),
					getSelectionModel());
				s.addTheme(theme);
				s.setTheme(theme);
				return s;
			}
		};
	}

	/** Put a custom entry in the popup menu ("Center map" will look weird,
	 *  we want zoom-to-boundary instead).  */
	@Override
	protected JPopupMenu createPopupSingle(AlertInfo proxy) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
		p.addSeparator();
		p.add(new IAction("alert.zoom_area") {
			@Override
			protected void doActionPerformed(ActionEvent ev) {
				theme.zoomToAlertArea(proxy);
			}
		});
		return p;
	}

	/** Get the description of a proxy */
	@Override
	public String getDescription(AlertInfo proxy) {
		StringBuilder desc = new StringBuilder();
		desc.append(proxy.getName().replace("alert_info_",
			I18N.get("alert") + " "));
		desc.append(" - " + CapEvent.fromCode(
			proxy.getEvent()).description);
		return desc.toString();
	}

	/** Check if a given attribute affects a proxy style */
	@Override
	public boolean isStyleAttrib(String a) {
		return "alertState".equals(a);
	}

	/** Check the style of the specified proxy */
	@Override
	public boolean checkStyle(ItemStyle is, AlertInfo proxy) {
		AlertState st = AlertState.fromOrdinal(proxy.getAlertState());
		switch (is) {
		case PENDING:
			return st == AlertState.PENDING;
		case ACTIVE:
			return st == AlertState.ACTIVE;
		case CLEARED:
			return st == AlertState.CLEARED;
		default:
			return true;
		}
	}

	/** Check if a style is visible.  All alert styles are visible (only
	 *  selected alerts are drawn). */
	@Override
	protected boolean isStyleVisible(AlertInfo proxy) {
		return true;
	}

	/** Get the GeoLoc for the specified proxy */
	@Override
	protected GeoLoc getGeoLoc(AlertInfo ai) {
		return (ai != null)
		      ? new TransGeoLoc(ai.getName(),
				(float) ai.getLat(),
				(float) ai.getLon())
		      : null;
	}

	/** Location mapping */
	private final HashMap<String, MapGeoLoc> locations =
		new HashMap<String, MapGeoLoc>();

	/** Find the map geo location for a proxy */
	@Override
	public MapGeoLoc findGeoLoc(AlertInfo ai) {
		if (ai != null) {
			String name = ai.getName();
			if (locations.containsKey(name))
				return locations.get(name);

			GeoLoc gl = getGeoLoc(ai);
			if (gl != null) {
				MapGeoLoc loc = new MapGeoLoc(getGeoLoc(ai));
				loc.setManager(this);
				loc.doUpdate();
				locations.put(name, loc);
				// FIXME: MapGeoLoc objects are never removed
				//        from locations HashMap
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
}
