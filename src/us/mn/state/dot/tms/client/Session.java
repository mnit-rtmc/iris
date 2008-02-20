/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
import java.rmi.RemoteException;
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
import us.mn.state.dot.trafmap.StationLayer;
import us.mn.state.dot.trafmap.ViewLayer;
import us.mn.state.dot.tms.StationList;
import us.mn.state.dot.tms.StationSegment;
import us.mn.state.dot.tms.client.camera.CameraHandler;
import us.mn.state.dot.tms.client.camera.CameraTab;
import us.mn.state.dot.tms.client.dms.DMSHandler;
import us.mn.state.dot.tms.client.dms.DMSTab;
import us.mn.state.dot.tms.client.incidents.IncidentTab;
import us.mn.state.dot.tms.client.incidents.TmsIncidentLayer;
import us.mn.state.dot.tms.client.lcs.LcsTab;
import us.mn.state.dot.tms.client.meter.RampMeterTab;
import us.mn.state.dot.tms.client.proxy.TmsMapLayer;
import us.mn.state.dot.tms.client.roads.R_NodeHandler;
import us.mn.state.dot.tms.client.roads.R_NodeLayer;
import us.mn.state.dot.tms.client.roads.RoadwayTab;
import us.mn.state.dot.tms.client.security.IrisPermission;
import us.mn.state.dot.tms.client.security.IrisUser;
import us.mn.state.dot.tms.client.warning.WarningSignHandler;

// agency specific imports
//import gov.ca.dot.d10.tms.client.incidents.D10IncidentLayer;

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

	/** Camera layer */
	protected final TmsMapLayer camLayer;

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
	protected StationLayer createStationLayer() throws IOException,
		TdxmlException
	{
		StationLayer layer = new StationLayer(props, logger);
		final StationList s_list = (StationList)(tmsConnection.getProxy(
			).getStations().getList());
		layer.setLabels(new StationLayer.Labeller() {
			public String getLabel(Integer i) throws IOException {
				StationSegment s = s_list.getElement(i);
				if(s != null)
					return s.getLabel();
				else
					return null;
			}
		});
		return layer;
	}

	/** Add the DMS tab */
	protected void addDMSTab() throws RemoteException {
		TmsMapLayer dmsLayer = DMSHandler.createLayer(tmsConnection);
		Layer warnLayer = WarningSignHandler.createLayer(tmsConnection);
		List<LayerState> lstates = createBaseLayers();
		lstates.add(gpoly.createState());
		lstates.add(camLayer.createState());
		lstates.add(incLayer.createState());
		lstates.add(dmsLayer.createState());
		lstates.add(warnLayer.createState());
		tabs.add(new DMSTab(lstates, vlayer,
			(DMSHandler)dmsLayer.getHandler()));
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
		tabs.add(new RampMeterTab(lstates, vlayer, tmsConnection));
	}

	/** Add the incident tab */
	protected void addIncidentTab() throws RemoteException {
		tabs.add(new IncidentTab(incLayer));
	}

	/** Add the LCS tab */
	protected void addLcsTab() throws IOException {
		tabs.add(new LcsTab(tmsConnection));
	}

	/** Add the camera tab */
	protected void addCameraTab() throws RemoteException {
		tabs.add(new CameraTab((CameraHandler)camLayer.getHandler(),
			tmsConnection.isAdmin(), props));
	}

	/** Add the roadway tab */
	protected void addRoadwayTab() throws RemoteException {
		List<LayerState> lstates = createBaseLayers();
		R_NodeLayer layer = R_NodeHandler.createLayer(tmsConnection);
		lstates.add(layer.createState());
		tabs.add(new RoadwayTab(layer,
			(R_NodeHandler)layer.getHandler(), lstates, vlayer));
	}

	/** Create a new session */
	public Session(TmsConnection tc, SonarState st, Properties p, Logger l)
		throws TdxmlException, IOException
	{
		tmsConnection = tc;
		props = p;
		logger = l;
		baseLayers = new BaseLayers().getLayers();
		gpoly = createStationLayer();

		// create D10 agency specific incident layer
		//incLayer = new D10IncidentLayer(props, logger,
		//    st.getSystemPolicy());

		// create Mn/DOT incident layer
		incLayer = new TmsIncidentLayer(props, logger,
		    st.getSystemPolicy());

		camLayer = CameraHandler.createLayer(tmsConnection);
		vlayer = new ViewLayer();
		IrisUser user = tmsConnection.getUser();
		if(user.hasPermission(IrisPermission.DMS_TAB))
			addDMSTab();
		if(user.hasPermission(IrisPermission.METER_TAB))
			addMeterTab();
		if(user.hasPermission(IrisPermission.MAIN_TAB))
			addIncidentTab();
		if(user.hasPermission(IrisPermission.LCS_TAB))
			addLcsTab();
		addCameraTab();
		if(user.hasPermission(IrisPermission.ADMINISTRATOR))
			addRoadwayTab();
	}

	/** Dispose of the session */
	public void dispose() {
		for(IrisTab tab: tabs)
			tab.dispose();
		gpoly.dispose();
		incLayer.dispose();
	}
}
