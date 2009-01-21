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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import us.mn.state.dot.map.Layer;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.Theme;
import us.mn.state.dot.tdxml.TdxmlException;
import us.mn.state.dot.trafmap.BaseLayers;
import us.mn.state.dot.trafmap.FreewayTheme;
import us.mn.state.dot.trafmap.RwisLayer;
import us.mn.state.dot.trafmap.StationLayer;
import us.mn.state.dot.trafmap.ViewLayer;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.tms.client.camera.CameraManager;
import us.mn.state.dot.tms.client.camera.CameraTab;
import us.mn.state.dot.tms.client.dms.DMSManager;
import us.mn.state.dot.tms.client.dms.DMSTab;
import us.mn.state.dot.tms.client.incidents.IncidentTab;
import us.mn.state.dot.tms.client.rwis.RwisTab;
import us.mn.state.dot.tms.client.lcs.LcsTab;
import us.mn.state.dot.tms.client.meter.RampMeterTab;
import us.mn.state.dot.tms.client.meter.MeterManager;
import us.mn.state.dot.tms.client.roads.R_NodeManager;
import us.mn.state.dot.tms.client.roads.RoadwayTab;
import us.mn.state.dot.tms.client.sonar.GeoLocManager;
import us.mn.state.dot.tms.client.security.IrisPermission;
import us.mn.state.dot.tms.client.security.IrisUser;
import us.mn.state.dot.tms.client.toast.DetectorManager;
import us.mn.state.dot.tms.client.warning.WarningSignManager;

// agency specific imports
import us.mn.state.dot.tms.client.incidents.TmsIncidentLayer;
import us.mn.state.dot.tms.client.incidents.D10IncidentLayer;
import us.mn.state.dot.tms.client.rwis.D10RwisLayer;

/**
 * A session is one IRIS login session.
 *
 * @author Douglas Lau
 */
public class Session {

	/** Contains information about the current connection */
	protected final TmsConnection tmsConnection;

	/** Client properties */
	protected final Properties props;

	/** Message logger */
	protected final Logger logger;

	/** Base layers */
	protected final List<Layer> baseLayers;

	/** View layer */
	protected final ViewLayer vlayer;

	/** Station layer */
	protected final StationLayer gpoly;

	/** Incident layer */
	protected final TmsIncidentLayer incLayer;

	/** RWIS layer */
	protected final RwisLayer rwisLayer;

	/** Location manager */
	protected final GeoLocManager loc_manager;

	/** Camera manager */
	protected final CameraManager cam_manager;

	/** DMS manager */
	protected final DMSManager dms_manager;

	/** Detector manager */
	protected final DetectorManager det_manager;

	/** R_Node manager */
	protected final R_NodeManager r_node_manager;

	/** Warning sign manager */
	protected final WarningSignManager warn_manager;

	/** Ramp meter manager */
	protected final MeterManager meter_manager;

	/** FIXME: this is a hack */
	static public CameraManager cam_manager_singleton;
	static public DMSManager dms_manager_singleton;
	static public DetectorManager det_manager_singleton;
	static public WarningSignManager warn_manager_singleton;

	/** List of all tabs */
	protected final List<IrisTab> tabs = new LinkedList<IrisTab>();

	/** Get a list of all tabs */
	public List<IrisTab> getTabs() {
		return tabs;
	}

	/** Create the base layer states */
	protected List<LayerState> createBaseLayers() {
		LinkedList<LayerState> lstates = new LinkedList<LayerState>();
		for(Layer l: baseLayers)
			lstates.add(l.createState());
		return lstates;
	}

	/** Create the station layer */
	protected StationLayer createStationLayer(final SonarState st)
		throws IOException, TdxmlException
	{
		StationLayer layer = new StationLayer(props, logger);
		layer.setLabels(new StationLayer.Labeller() {
			public String getLabel(String sid) throws IOException {
				Station s = st.lookupStation(sid);
				if(s != null)
					return s.getLabel();
				else
					return null;
			}
		});
		return layer;
	}

	/** Add the DMS tab */
	protected void addDMSTab() {
		List<LayerState> lstates = createBaseLayers();
		lstates.add(gpoly.createState());
		lstates.add(cam_manager.getLayer().createState());
		lstates.add(incLayer.createState());
		if(rwisLayer != null)
			lstates.add(rwisLayer.createState());
		lstates.add(warn_manager.getLayer().createState());
		tabs.add(new DMSTab(dms_manager, lstates, vlayer,
			tmsConnection));
	}

	/** Add the meter tab */
	protected void addMeterTab() throws IOException {
		List<LayerState> lstates = createBaseLayers();
		LayerState gpolyState = gpoly.createState();
		for(Theme t: gpolyState.getThemes()) {
			if(t instanceof FreewayTheme)
				gpolyState.setTheme(t);
		}
		lstates.add(gpolyState);
		tabs.add(new RampMeterTab(meter_manager, lstates, vlayer,
			tmsConnection));
	}

	/** Add the incident tab */
	protected void addIncidentTab() {
		tabs.add(new IncidentTab(incLayer));
	}

	/** Add the rwis tab */
	protected void addRwisTab() {
		tabs.add(new RwisTab(rwisLayer));
	}

	/** Add the LCS tab */
	protected void addLcsTab() throws IOException {
		tabs.add(new LcsTab(tmsConnection));
	}

	/** Add the camera tab */
	protected void addCameraTab(final SonarState st, IrisUser user) {
		tabs.add(new CameraTab(cam_manager, props, logger, st, user));
	}

	/** Add the roadway tab */
	protected void addRoadwayTab() {
		List<LayerState> lstates = createBaseLayers();
		tabs.add(new RoadwayTab(r_node_manager, lstates, vlayer,
			tmsConnection));
	}

	/** Create a new session */
	public Session(TmsConnection tc, SonarState st, Properties p, Logger l)
		throws TdxmlException, IOException
	{
		tmsConnection = tc;
		props = p;
		logger = l;
		baseLayers = new BaseLayers().getLayers();
		gpoly = createStationLayer(st);

		// Create agency specific incident layer
		if(SystemAttributeHelper.isAgencyCaltransD10()) {
			incLayer = new D10IncidentLayer(props, logger);
			rwisLayer = new D10RwisLayer(props, logger);
		} else {
			incLayer = new TmsIncidentLayer(props, logger);
			rwisLayer = null;
		}

		loc_manager = new GeoLocManager(st.getGeoLocs());
		cam_manager = new CameraManager(tmsConnection, st.getCameras(),
			loc_manager);
		cam_manager_singleton = cam_manager;
		dms_manager = new DMSManager(tmsConnection, st.getDMSs(),
			loc_manager);
		dms_manager_singleton = dms_manager;
		det_manager = new DetectorManager(tmsConnection,
			st.getDetectors(), loc_manager);
		det_manager_singleton = det_manager;
		r_node_manager = new R_NodeManager(tmsConnection,
			st.getR_Nodes(), loc_manager);
		warn_manager = new WarningSignManager(tmsConnection,
			st.getWarningSigns(), loc_manager);
		warn_manager_singleton = warn_manager;
		meter_manager = new MeterManager(tmsConnection,
			st.getRampMeters(), loc_manager);
		vlayer = new ViewLayer();
		IrisUser user = tmsConnection.getUser();
		if(user.hasPermission(IrisPermission.DMS_TAB))
			addDMSTab();
		if(user.hasPermission(IrisPermission.METER_TAB))
			addMeterTab();
		if(user.hasPermission(IrisPermission.MAIN_TAB))
			addIncidentTab();
		if(user.hasPermission(IrisPermission.RWIS_TAB))
			addRwisTab();
		if(user.hasPermission(IrisPermission.LCS_TAB))
			addLcsTab();
		addCameraTab(st, user);
		if(user.hasPermission(IrisPermission.ADMINISTRATOR))
			addRoadwayTab();
	}

	/** Dispose of the session */
	public void dispose() {
		for(IrisTab tab: tabs)
			tab.dispose();
		gpoly.dispose();
		incLayer.dispose();
		if(rwisLayer != null)
			rwisLayer.dispose();
	}
}
