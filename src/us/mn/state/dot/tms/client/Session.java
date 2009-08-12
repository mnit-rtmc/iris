/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import us.mn.state.dot.map.Layer;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.Theme;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tdxml.TdxmlException;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.StationHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.camera.CameraManager;
import us.mn.state.dot.tms.client.camera.CameraTab;
import us.mn.state.dot.tms.client.camera.VideoMenu;
import us.mn.state.dot.tms.client.detector.DetectorManager;
import us.mn.state.dot.tms.client.dms.DMSManager;
import us.mn.state.dot.tms.client.dms.DMSTab;
import us.mn.state.dot.tms.client.incidents.IncidentTab;
import us.mn.state.dot.tms.client.lcs.LaneUseMenu;
import us.mn.state.dot.tms.client.lcs.LcsTab;
import us.mn.state.dot.tms.client.lcs.LCSArrayManager;
import us.mn.state.dot.tms.client.lcs.LCSIManager;
import us.mn.state.dot.tms.client.meter.RampMeterTab;
import us.mn.state.dot.tms.client.meter.MeterManager;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.roads.FreewayTheme;
import us.mn.state.dot.tms.client.roads.R_NodeManager;
import us.mn.state.dot.tms.client.roads.RoadwayTab;
import us.mn.state.dot.tms.client.roads.SegmentLayer;
import us.mn.state.dot.tms.client.security.UserManager;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.warning.WarningSignManager;

// agency specific imports
import us.mn.state.dot.tms.client.incidents.TmsIncidentLayer;
import us.mn.state.dot.tms.client.incidents.D10IncidentLayer;

/**
 * A session is one IRIS login session.
 *
 * @author Douglas Lau
 */
public class Session {

	/** Hide the named layer */
	static protected void hideLayer(List<LayerState> lstates, String name) {
		for(LayerState ls: lstates) {
			if(ls.getLayer().getName().equals(name))
				ls.setVisible(false);
		}
	}

	/** Session User */
	protected final User user;

	/** Get the currently logged-in user */
	public User getUser() {
		return user;
	}

	/** SONAR state */
	protected final SonarState state;

	/** Get the SONAR state */
	public SonarState getSonarState() {
		return state;
	}

	/** SONAR namespace */
	protected final Namespace namespace;

	/** Desktop used by this session */
	protected final SmartDesktop desktop;

	/** Get the desktop */
	public SmartDesktop getDesktop() {
		return desktop;
	}

	/** Client properties */
	protected final Properties props;

	/** Get the client properties */
	public Properties getProperties() {
		return props;
	}

	/** Message logger */
	protected final Logger logger;

	/** Get the logger */
	public Logger getLogger() {
		return logger;
	}

	/** View menu */
	protected final ViewMenu v_menu;

	/** Get the view menu */
	public ViewMenu getViewMenu() {
		return v_menu;
	}

	/** Base layers */
	protected final List<Layer> baseLayers;

	/** Segment layer */
	protected final SegmentLayer seg_layer;

	/** Incident layer */
	protected final TmsIncidentLayer incLayer;

	/** Location manager */
	protected final GeoLocManager loc_manager;

	/** Camera manager */
	protected final CameraManager cam_manager;

	/** Get the camera manager */
	public CameraManager getCameraManager() {
		return cam_manager;
	}

	/** DMS manager */
	protected final DMSManager dms_manager;

	/** Get the DMS manager */
	public DMSManager getDMSManager() {
		return dms_manager;
	}

	/** LCS array manager */
	protected final LCSArrayManager lcs_array_manager;

	/** LCS indication manager */
	protected final LCSIManager lcsi_manager;

	/** Get the LCS indication manager */
	public LCSIManager getLCSIManager() {
		return lcsi_manager;
	}

	/** Detector manager */
	protected final DetectorManager det_manager;

	/** Get the detector manager */
	public DetectorManager getDetectorManager() {
		return det_manager;
	}

	/** R_Node manager */
	protected final R_NodeManager r_node_manager;

	/** Warning sign manager */
	protected final WarningSignManager warn_manager;

	/** Get the warning sign manager */
	public WarningSignManager getWarnManager() {
		return warn_manager;
	}

	/** Ramp meter manager */
	protected final MeterManager meter_manager;

	/** Get the ramp meter manager */
	public MeterManager getMeterManager() {
		return meter_manager;
	}

	/** List of all tabs */
	protected final List<MapTab> tabs = new LinkedList<MapTab>();

	/** Get a list of all tabs */
	public List<MapTab> getTabs() {
		return tabs;
	}

	/** Create a new session */
	public Session(UserManager um, SmartDesktop d, Properties p, Logger l,
		List<Layer> bl) throws TdxmlException, IOException
	{
		user = um.getUser();
		state = um.getSonarState();
		namespace = state.getNamespace();
		desktop = d;
		props = p;
		logger = l;
		baseLayers = bl;
		v_menu = new ViewMenu(this);
		loc_manager = new GeoLocManager(state.getGeoLocs());
		cam_manager = new CameraManager(this,
			state.getCamCache().getCameras(), loc_manager);
		dms_manager = new DMSManager(this,state.getDmsCache().getDMSs(),
			loc_manager);
		lcs_array_manager = new LCSArrayManager(this, loc_manager);
		lcsi_manager = new LCSIManager(this, loc_manager);
		det_manager = new DetectorManager(
			state.getDetCache().getDetectors(), loc_manager);
		r_node_manager = new R_NodeManager(this,
			state.getDetCache().getR_Nodes(), loc_manager);
		warn_manager = new WarningSignManager(this,
			state.getWarningSigns(), loc_manager);
		meter_manager = new MeterManager(this,
			state.getRampMeters(), loc_manager);
		incLayer = createIncidentLayer();
		seg_layer = r_node_manager.getSegmentLayer();
		addTabs();
	}

	/** Create the incident layer */
	protected TmsIncidentLayer createIncidentLayer() throws TdxmlException,
		IOException
	{
		String i_loc = props.getProperty("tdxml.incident.url");
		if(i_loc != null) {
			URL u = new URL(i_loc);
			if(SystemAttrEnum.INCIDENT_CALTRANS_ENABLE.getBoolean())
				return new D10IncidentLayer(u, logger);
			else
				return new TmsIncidentLayer(u, logger);
		} else
			return null;
	}

	/** Add the tabs */
	protected void addTabs() throws IOException {
		if(canUpdate(DMS.SONAR_TYPE, "messageNext"))
			addDMSTab();
		if(canUpdate(RampMeter.SONAR_TYPE, "rateNext"))
			addMeterTab();
		if(incLayer != null)
			addIncidentTab();
		if(canUpdate(LCSArray.SONAR_TYPE, "indicationsNext"))
			addLcsTab();
		if(namespace.canRead(user, new Name(Camera.SONAR_TYPE)))
			addCameraTab();
		if(namespace.canAdd(user, new Name(R_Node.SONAR_TYPE, "oname")))
			addRoadwayTab();
	}

	/** Add the DMS tab */
	protected void addDMSTab() {
		v_menu.addDMSItems();
		List<LayerState> lstates = createLayers();
		hideLayer(lstates, meter_manager.getProxyType());
		hideLayer(lstates, lcs_array_manager.getProxyType());
		tabs.add(new DMSTab(this, dms_manager, lstates));
	}

	/** Create the layer states */
	protected List<LayerState> createLayers() {
		List<LayerState> lstates = createBaseLayers();
		if(seg_layer != null)
			lstates.add(seg_layer.createState());
		if(namespace.canRead(user, new Name(Camera.SONAR_TYPE)))
			lstates.add(cam_manager.getLayer().createState());
		if(incLayer != null)
			lstates.add(incLayer.createState());
		if(canUpdate(RampMeter.SONAR_TYPE, "rateNext"))
			lstates.add(meter_manager.getLayer().createState());
		if(canUpdate(DMS.SONAR_TYPE, "messageNext"))
			lstates.add(dms_manager.getLayer().createState());
		if(canUpdate(LCSArray.SONAR_TYPE, "indicationsNext"))
			lstates.add(lcs_array_manager.getLayer().createState());
		if(canUpdate(WarningSign.SONAR_TYPE, "deployed"))
			lstates.add(warn_manager.getLayer().createState());
		return lstates;
	}

	/** Create the base layer states */
	protected List<LayerState> createBaseLayers() {
		LinkedList<LayerState> lstates = new LinkedList<LayerState>();
		for(Layer l: baseLayers)
			lstates.add(l.createState());
		return lstates;
	}

	/** Add the meter tab */
	protected void addMeterTab() throws IOException {
		v_menu.addMeterItem();
		List<LayerState> lstates = createLayers();
		for(LayerState ls: lstates) {
			for(Theme t: ls.getThemes()) {
				if(t instanceof FreewayTheme)
					ls.setTheme(t);
			}
		}
		hideLayer(lstates, dms_manager.getProxyType());
		hideLayer(lstates, lcs_array_manager.getProxyType());
		hideLayer(lstates, warn_manager.getProxyType());
		tabs.add(new RampMeterTab(this, meter_manager, lstates));
	}

	/** Add the incident tab */
	protected void addIncidentTab() {
		List<LayerState> lstates = createLayers();
		hideLayer(lstates, meter_manager.getProxyType());
		hideLayer(lstates, dms_manager.getProxyType());
		hideLayer(lstates, lcs_array_manager.getProxyType());
		hideLayer(lstates, warn_manager.getProxyType());
		tabs.add(new IncidentTab(incLayer, lstates));
	}

	/** Add the LCS tab */
	protected void addLcsTab() throws IOException {
		v_menu.add(new LaneUseMenu(this));
		List<LayerState> lstates = createLayers();
		hideLayer(lstates, "Water");
		hideLayer(lstates, "County Road");
		hideLayer(lstates, "C.S.A.H.");
		hideLayer(lstates, "Highway");
		hideLayer(lstates, "Freeway");
		hideLayer(lstates, meter_manager.getProxyType());
		hideLayer(lstates, dms_manager.getProxyType());
		hideLayer(lstates, warn_manager.getProxyType());
		tabs.add(new LcsTab(this, lcs_array_manager, lstates));
	}

	/** Add the camera tab */
	protected void addCameraTab() {
		v_menu.add(new VideoMenu(this));
		List<LayerState> lstates = createLayers();
		hideLayer(lstates, meter_manager.getProxyType());
		hideLayer(lstates, dms_manager.getProxyType());
		hideLayer(lstates, lcs_array_manager.getProxyType());
		hideLayer(lstates, warn_manager.getProxyType());
		tabs.add(new CameraTab(cam_manager, lstates, props, logger,
			state, user));
	}

	/** Add the roadway tab */
	protected void addRoadwayTab() {
		List<LayerState> lstates = createBaseLayers();
		tabs.add(new RoadwayTab(this, r_node_manager, lstates));
	}

	/** Check if the user can update an attribute */
	protected boolean canUpdate(String tname, String aname) {
		return namespace.canUpdate(user, new Name(tname,"oname",aname));
	}

	/** Dispose of the session */
	public void dispose() {
		desktop.closeFrames();
		for(MapTab tab: tabs)
			tab.dispose();
		if(incLayer != null)
			incLayer.dispose();
	}
}
