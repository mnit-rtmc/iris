/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.map.MapModel;
import us.mn.state.dot.map.TileLayer;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.camera.CameraManager;
import us.mn.state.dot.tms.client.comm.ControllerManager;
import us.mn.state.dot.tms.client.detector.DetectorManager;
import us.mn.state.dot.tms.client.dms.DMSManager;
import us.mn.state.dot.tms.client.gate.GateArmArrayManager;
import us.mn.state.dot.tms.client.incident.IncidentManager;
import us.mn.state.dot.tms.client.lcs.LCSArrayManager;
import us.mn.state.dot.tms.client.lcs.LCSIManager;
import us.mn.state.dot.tms.client.marking.LaneMarkingManager;
import us.mn.state.dot.tms.client.meter.MeterManager;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.roads.R_NodeManager;
import us.mn.state.dot.tms.client.roads.SegmentLayer;
import us.mn.state.dot.tms.client.schedule.PlanManager;
import us.mn.state.dot.tms.client.warning.WarningSignManager;
import us.mn.state.dot.tms.client.weather.WeatherSensorManager;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * A session is one IRIS login session.
 *
 * @author Douglas Lau
 */
public class Session {

	/** Session User */
	private final User user;

	/** Get the currently logged-in user */
	public User getUser() {
		return user;
	}

	/** SONAR state */
	private final SonarState state;

	/** Get the SONAR state */
	public SonarState getSonarState() {
		return state;
	}

	/** SONAR namespace */
	private final Namespace namespace;

	/** Desktop used by this session */
	private final SmartDesktop desktop;

	/** Get the desktop */
	public SmartDesktop getDesktop() {
		return desktop;
	}

	/** Client properties */
	private final Properties props;

	/** Get the client properties */
	public Properties getProperties() {
		return props;
	}

	/** Tile layer */
	private final TileLayer tile_layer;

	/** Segment layer */
	private final SegmentLayer seg_layer;

	/** Location manager */
	private final GeoLocManager loc_manager;

	/** Controller manager */
	private final ControllerManager controller_manager;

	/** Camera manager */
	private final CameraManager cam_manager;

	/** Get the camera manager */
	public CameraManager getCameraManager() {
		return cam_manager;
	}

	/** DMS manager */
	private final DMSManager dms_manager;

	/** Get the DMS manager */
	public DMSManager getDMSManager() {
		return dms_manager;
	}

	/** LCS array manager */
	private final LCSArrayManager lcs_array_manager;

	/** Get the LCS array manager */
	public LCSArrayManager getLCSArrayManager() {
		return lcs_array_manager;
	}

	/** LCS indication manager */
	private final LCSIManager lcsi_manager;

	/** Lane marking manager */
	private final LaneMarkingManager lane_marking_manager;

	/** Detector manager */
	private final DetectorManager det_manager;

	/** Get the detector manager */
	public DetectorManager getDetectorManager() {
		return det_manager;
	}

	/** R_Node manager */
	private final R_NodeManager r_node_manager;

	/** Get the r_node manager */
	public R_NodeManager getR_NodeManager() {
		return r_node_manager;
	}

	/** Warning sign manager */
	private final WarningSignManager warn_manager;

	/** Weather sensor manager */
	private final WeatherSensorManager weather_sensor_manager;

	/** Ramp meter manager */
	private final MeterManager meter_manager;

	/** Gate arm array manager */
	private final GateArmArrayManager gate_arm_manager;

	/** Incident manager */
	private final IncidentManager inc_manager;

	/** Action plan manager */
	private final PlanManager plan_manager;

	/** List of all tabs */
	private final List<MapTab> tabs = new LinkedList<MapTab>();

	/** Get a list of all tabs */
	public List<MapTab> getTabs() {
		return tabs;
	}

	/** Create a new session */
	public Session(SonarState st, SmartDesktop d, Properties p) {
		state = st;
		user = state.getUser();
		namespace = state.getNamespace();
		desktop = d;
		props = p;
		loc_manager = new GeoLocManager(this);
		r_node_manager = new R_NodeManager(this, loc_manager);
		det_manager = new DetectorManager(this, loc_manager,
			r_node_manager);
		controller_manager = new ControllerManager(this, loc_manager);
		cam_manager = new CameraManager(this, loc_manager);
		dms_manager = new DMSManager(this, loc_manager);
		lcs_array_manager = new LCSArrayManager(this, loc_manager);
		lcsi_manager = new LCSIManager(this, loc_manager);
		lane_marking_manager = new LaneMarkingManager(this,loc_manager);
		warn_manager = new WarningSignManager(this, loc_manager);
		weather_sensor_manager = new WeatherSensorManager(this,
			loc_manager);
		meter_manager = new MeterManager(this, loc_manager);
		gate_arm_manager = new GateArmArrayManager(this, loc_manager);
		inc_manager = new IncidentManager(this, loc_manager);
		plan_manager = new PlanManager(this, loc_manager);
		seg_layer = r_node_manager.getSegmentLayer();
		tile_layer = createTileLayer(props.getProperty("map.tile.url"));
	}

	/** Create the tile layer */
	private TileLayer createTileLayer(String url) {
		if(url != null)
			return new TileLayer("Base map", url, 1000);
		else
			return null;
	}

	/** Initialize the session */
	public void initialize() throws IOException, SAXException,
		ParserConfigurationException
	{
		initializeManagers();
		addTabs();
		seg_layer.start(props);
		if(tile_layer != null)
			tile_layer.initialize();
	}

	/** Initialize all the proxy managers */
	private void initializeManagers() {
		r_node_manager.initialize();
		det_manager.initialize();
		controller_manager.initialize();
		cam_manager.initialize();
		dms_manager.initialize();
		lcs_array_manager.initialize();
		lcsi_manager.initialize();
		lane_marking_manager.initialize();
		warn_manager.initialize();
		weather_sensor_manager.initialize();
		meter_manager.initialize();
		gate_arm_manager.initialize();
		inc_manager.initialize();
		plan_manager.initialize();
	}

	/** Add the tabs */
	private void addTabs() {
		if(inc_manager.canRead())
			tabs.add(inc_manager.createTab());
		if(dms_manager.canRead())
			tabs.add(dms_manager.createTab());
		if(cam_manager.canRead())
			tabs.add(cam_manager.createTab());
		if(lcs_array_manager.canRead())
			tabs.add(lcs_array_manager.createTab());
		if(meter_manager.canRead())
			tabs.add(meter_manager.createTab());
		if(gate_arm_manager.canRead())
			tabs.add(gate_arm_manager.createTab());
		if(r_node_manager.canAdd())
			tabs.add(r_node_manager.createTab());
		if(plan_manager.canRead())
			tabs.add(plan_manager.createTab());
		if(controller_manager.canRead())
			tabs.add(controller_manager.createTab());
	}

	/** Create the layer states.  The map bean and model must be seperate
	 * parameters so that the model can be built before calling setModel
	 * on the map bean.
	 * @param mb Map bean to render the layer states.
	 * @param mm Map model to contain layer states. */
	public void createLayers(MapBean mb, MapModel mm) {
		if(tile_layer != null)
			mm.addLayer(tile_layer.createState(mb));
		mm.addLayer(seg_layer.createState(mb));
		if(controller_manager.canRead())
			mm.addLayer(controller_manager.createState(mb));
		if(cam_manager.canRead())
			mm.addLayer(cam_manager.createState(mb));
		if(meter_manager.canRead())
			mm.addLayer(meter_manager.createState(mb));
		if(gate_arm_manager.canRead())
			mm.addLayer(gate_arm_manager.createState(mb));
		if(dms_manager.canRead())
			mm.addLayer(dms_manager.createState(mb));
		if(lcs_array_manager.canRead())
			mm.addLayer(lcs_array_manager.createState(mb));
		if(warn_manager.canRead())
			mm.addLayer(warn_manager.createState(mb));
		if(inc_manager.canRead())
			mm.addLayer(inc_manager.createState(mb));
		if(r_node_manager.canAdd())
			mm.addLayer(r_node_manager.createState(mb));
	}

	/** Check if the user can add an object */
	public boolean canAdd(String tname) {
		return canAdd(tname, "oname");
	}

	/** Check if the user can add an object */
	public boolean canAdd(String tname, String oname) {
		return oname != null &&
		       namespace.canAdd(new Name(tname, oname), user);
	}

	/** Check if the user can read a type */
	public boolean canRead(String tname) {
		return namespace.canRead(new Name(tname), user);
	}

	/** Check if the user can update an attribute */
	public boolean canUpdate(String tname, String aname) {
		return namespace.canUpdate(new Name(tname,"oname",aname), user);
	}

	/** Check if the user can update an attribute */
	public boolean canUpdate(String tname) {
		return canUpdate(tname, "aname");
	}

	/** Check if the user can update a proxy */
	public boolean canUpdate(SonarObject proxy) {
		return proxy != null &&
		       namespace.canUpdate(new Name(proxy), user);
	}

	/** Check if the user can update a proxy */
	public boolean canUpdate(SonarObject proxy, String aname) {
		return proxy != null &&
		       namespace.canUpdate(new Name(proxy, aname), user);
	}

	/** Check if the user can remove a proxy */
	public boolean canRemove(SonarObject proxy) {
		return proxy != null &&
		       namespace.canRemove(new Name(proxy), user);
	}

	/** Check if the user can remove a proxy */
	public boolean canRemove(String tname, String oname) {
		return namespace.canRemove(new Name(tname, oname), user);
	}

	/** Dispose of the session */
	public void dispose() {
		seg_layer.dispose();
		desktop.closeFrames();
		for(MapTab tab: tabs)
			tab.dispose();
		tabs.clear();
		plan_manager.dispose();
		r_node_manager.dispose();
		det_manager.dispose();
		gate_arm_manager.dispose();
		cam_manager.dispose();
		dms_manager.dispose();
		lcs_array_manager.dispose();
		lcsi_manager.dispose();
		lane_marking_manager.dispose();
		warn_manager.dispose();
		weather_sensor_manager.dispose();
		meter_manager.dispose();
		controller_manager.dispose();
		inc_manager.dispose();
		loc_manager.dispose();
		state.quit();
	}

	/** Get the session ID */
	public long getSessionId() {
		Connection c = state.lookupConnection();
		return c != null ? c.getSessionId() : 0;
	}
}
