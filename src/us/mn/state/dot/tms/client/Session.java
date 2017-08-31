/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
 * Copyright (C) 2014  AHMCT, University of California
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.beacon.BeaconManager;
import us.mn.state.dot.tms.client.camera.CameraManager;
import us.mn.state.dot.tms.client.comm.ControllerManager;
import us.mn.state.dot.tms.client.dms.DMSManager;
import us.mn.state.dot.tms.client.gate.GateArmArrayManager;
import us.mn.state.dot.tms.client.incident.IncidentManager;
import us.mn.state.dot.tms.client.lcs.LCSArrayManager;
import us.mn.state.dot.tms.client.lcs.LCSIManager;
import us.mn.state.dot.tms.client.map.MapBean;
import us.mn.state.dot.tms.client.map.MapModel;
import us.mn.state.dot.tms.client.map.TileLayer;
import us.mn.state.dot.tms.client.marking.LaneMarkingManager;
import us.mn.state.dot.tms.client.meter.MeterManager;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.roads.R_NodeManager;
import us.mn.state.dot.tms.client.schedule.PlanManager;
import us.mn.state.dot.tms.client.toll.TagReaderManager;
import us.mn.state.dot.tms.client.weather.WeatherSensorManager;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * A session is one IRIS login session.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class Session {

	/** Session User */
	private final User user;

	/** Get the currently logged-in user */
	public User getUser() {
		return user;
	}

	/** "Edit" mode */
	private boolean edit_mode = false;

	/** Set the edit mode */
	public void setEditMode(boolean m) {
		edit_mode = m;
		fireEditModeChange();
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

	/** Location manager */
	private final GeoLocManager loc_manager;

	/** List of proxy managers */
	private final LinkedList<ProxyManager<?>> managers;

	/** R_Node manager */
	private final R_NodeManager r_node_manager;

	/** Get the r_node manager */
	public R_NodeManager getR_NodeManager() {
		return r_node_manager;
	}

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

	/** Mapping of all tabs */
	private final HashMap<String, MapTab> all_tabs =
		new HashMap<String, MapTab>();

	/** Tile layer */
	private final TileLayer tile_layer;

	/** Listeners for edit mode changes */
	private final LinkedList<EditModeListener> listeners =
		new LinkedList<EditModeListener>();

	/** Create a new session */
	public Session(SonarState st, SmartDesktop d, Properties p)
		throws Exception
	{
		state = st;
		user = state.getUser();
		namespace = state.getNamespace();
		desktop = d;
		props = p;
		loc_manager = new GeoLocManager(this);
		r_node_manager = new R_NodeManager(this, loc_manager, p);
		cam_manager = new CameraManager(this, loc_manager);
		dms_manager = new DMSManager(this, loc_manager);
		lcs_array_manager = new LCSArrayManager(this, loc_manager);
		managers = new LinkedList<ProxyManager<?>>();
		managers.add(r_node_manager);
		managers.add(new ControllerManager(this, loc_manager));
		managers.add(cam_manager);
		managers.add(new MeterManager(this, loc_manager));
		managers.add(new GateArmArrayManager(this, loc_manager));
		managers.add(dms_manager);
		managers.add(lcs_array_manager);
		managers.add(new LCSIManager(this, loc_manager));
		managers.add(new LaneMarkingManager(this,loc_manager));
		managers.add(new BeaconManager(this, loc_manager));
		managers.add(new TagReaderManager(this, loc_manager));
		managers.add(new WeatherSensorManager(this, loc_manager));
		managers.add(new IncidentManager(this, loc_manager));
		managers.add(new PlanManager(this, loc_manager));
		tile_layer = createTileLayer(props.getProperty("map.tile.url"));
	}

	/** Create the tile layer */
	private TileLayer createTileLayer(String url) {
		if (url != null)
			return new TileLayer("Base map", url, 1000);
		else
			return null;
	}

	/** Initialize the session */
	public void initialize() throws Exception {
		initializeManagers();
		createTabs();
		if (tile_layer != null)
			tile_layer.initialize();
	}

	/** Initialize all the proxy managers */
	private void initializeManagers() {
		loc_manager.initialize();
		for (ProxyManager<?> man: managers)
			man.initialize();
	}

	/** Create all map tabs in all_tabs mapping */
	private void createTabs() {
		for (ProxyManager<?> man: managers) {
			if (man.canRead()) {
				MapTab<?> tab = man.createTab();
				if (tab != null) {
					tab.initialize();
					all_tabs.put(tab.getTabId(), tab);
				}
			}
		}
	}

	/** Get a list of tabs in the order specified by properties */
	public List<MapTab> getTabs() {
		LinkedList<MapTab> tabs = new LinkedList<MapTab>();
		for (String t : UserProperty.getTabList(props)) {
			MapTab tab = all_tabs.get(t);
			if (tab != null)
				tabs.add(tab);
		}
		return tabs;
	}

	/** Lookup a map tab by text ID */
	public MapTab lookupTab(String tid) {
		return all_tabs.get(tid);
	}

	/** Create the layer states.  The map bean and model must be seperate
	 * parameters so that the model can be built before calling setModel
	 * on the map bean.
	 * @param mb Map bean to render the layer states.
	 * @param mm Map model to contain layer states. */
	public void createLayers(MapBean mb, MapModel mm) {
		if (tile_layer != null)
			mm.addLayer(tile_layer.createState(mb));
		for (ProxyManager<?> man: managers) {
			if (man.hasLayer())
				mm.addLayer(man.createState(mb));
		}
	}

	/** Create an action to create a proxy table form */
	public IAction createTableAction(String tname) {
		for (ProxyManager<?> man: managers) {
			if (tname.equals(man.getSonarType()))
				return man.createTableAction();
		}
		return null;
	}

	/** Check if the user is permitted to add an object, regardless of
	 * EDIT mode.
	 * @param tname Type name of object to add.
	 * @param oname Name of object to add.
	 * @return true if user can add the object */
	public boolean isAddPermitted(String tname, String oname) {
		return canWrite(new Name(tname, oname), true);
	}

	/** Check if the user is permitted to add an object, regardless of
	 * EDIT mode.
	 * @param tname Type name of object to add.
	 * @return true if user can add the object */
	public boolean isAddPermitted(String tname) {
		return canWrite(tname, "oname", true);
	}

	/** Check if the user can read a type */
	public boolean canRead(String tname) {
		return namespace.canRead(new Name(tname), user);
	}

	/** Check if the user can write an attribute.
	 * @param name Name of object/attribute to check.
	 * @param can_edit Flag to allow editing.
	 * @return true if user can write the attribute */
	private boolean canWrite(Name name, boolean can_edit) {
		return can_edit && namespace.canWrite(name, user);
	}

	/** Check if the user can write an attribute.
	 * @param tname Type name of attribute to check.
	 * @param aname Name of attribute to check.
	 * @param can_edit Flag to allow editing.
	 * @return true if user can write the attribute */
	private boolean canWrite(String tname, String aname, boolean can_edit) {
		return canWrite(new Name(tname, "oname", aname), can_edit);
	}

	/** Check if the user can write an attribute.
	 * @param tname Type name of attribute to check.
	 * @param can_edit Flag to allow editing.
	 * @return true if user can write the attribute */
	private boolean canWrite(String tname, boolean can_edit) {
		return canWrite(tname, "aname", can_edit);
	}

	/** Check if the user can write a proxy attribute.
	 * @param proxy Proxy object to check.
	 * @param can_edit Flag to allow editing.
	 * @return true if user can write the attribute */
	private boolean canWrite(SonarObject proxy, boolean can_edit) {
		return proxy != null && canWrite(new Name(proxy), can_edit);
	}

	/** Check if the user can write a proxy attribute.
	 * @param proxy Proxy object to check.
	 * @param aname Name of attribute to check.
	 * @param can_edit Flag to allow editing.
	 * @return true if user can write the attribute */
	private boolean canWrite(SonarObject proxy, String aname,
		boolean can_edit)
	{
		return proxy != null &&
		       canWrite(new Name(proxy, aname), can_edit);
	}

	/** Check if the user can write an attribute.
	 * @param tname Type name of attribute to check.
	 * @param aname Name of attribute to check.
	 * @return true if user can write the attribute */
	public boolean canWrite(String tname, String aname) {
		return canWrite(tname, aname, edit_mode);
	}

	/** Check if the user can write an attribute.
	 * @param tname Type name of attribute to check.
	 * @return true if user can write the attribute */
	public boolean canWrite(String tname) {
		return canWrite(tname, edit_mode);
	}

	/** Check if the user can write a proxy attribute.
	 * @param proxy Proxy object to check.
	 * @return true if user can write the attribute */
	public boolean canWrite(SonarObject proxy) {
		return canWrite(proxy, edit_mode);
	}

	/** Check if the user can write a proxy attribute.
	 * @param proxy Proxy object to check.
	 * @param aname Name of attribute to check.
	 * @return true if user can write the attribute */
	public boolean canWrite(SonarObject proxy, String aname) {
		return canWrite(proxy, aname, edit_mode);
	}

	/** Check if the user is permitted to update an attribute, regardless of
	 * EDIT mode.
	 * @param tname Type name of attribute to check.
	 * @param aname Name of attribute to check.
	 * @return true if user can write the attribute */
	public boolean isUpdatePermitted(String tname, String aname) {
		return canWrite(new Name(tname, "oname", aname), true);
	}

	/** Check if the user is permitted to update an attribute, regardless of
	 * EDIT mode.
	 * @param tname Type name of attribute to check.
	 * @return true if user can write the attribute */
	public boolean isUpdatePermitted(String tname) {
		return canWrite(tname, true);
	}

	/** Check if the user is permitted to update a proxy attribute,
	 * regardless of EDIT mode.
	 * @param proxy Proxy object to check.
	 * @return true if user can write the attribute */
	public boolean isUpdatePermitted(SonarObject proxy) {
		return canWrite(proxy, true);
	}

	/** Check if the user is permitted to update a proxy attribute,
	 * regardless of EDIT mode.
	 * @param proxy Proxy object to check.
	 * @param aname Name of attribute to check.
	 * @return true if user can write the attribute */
	public boolean isUpdatePermitted(SonarObject proxy, String aname) {
		return canWrite(proxy, aname, true);
	}

	/** Dispose of the session */
	public void dispose() {
		listeners.clear();
		desktop.dispose();
		for (MapTab tab: all_tabs.values())
			tab.dispose();
		all_tabs.clear();
		for (ProxyManager<?> man: managers)
			man.dispose();
		managers.clear();
		loc_manager.dispose();
		state.quit();
	}

	/** Get the session ID */
	public long getSessionId() {
		Connection c = state.lookupConnection();
		return c != null ? c.getSessionId() : 0;
	}

	/** Add an edit mode listener */
	public void addEditModeListener(EditModeListener l) {
		listeners.add(l);
	}

	/** Remove an edit mode listener */
	public void removeEditModeListener(EditModeListener l) {
		listeners.remove(l);
	}

	/** Fire an edit mode change event */
	private void fireEditModeChange() {
		for (EditModeListener l: listeners)
			l.editModeChanged();
	}
}
