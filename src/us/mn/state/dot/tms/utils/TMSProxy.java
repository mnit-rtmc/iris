/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import us.mn.state.dot.tms.DMSList;
import us.mn.state.dot.tms.DetectorList;
import us.mn.state.dot.tms.Login;
import us.mn.state.dot.tms.MeteringHolidayList;
import us.mn.state.dot.tms.RampMeterList;
import us.mn.state.dot.tms.RoadwayList;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.SystemPolicy;
import us.mn.state.dot.tms.TMS;
import us.mn.state.dot.tms.TMSObject;

/**
 * Client-side proxy for the TMS server object.
 *
 * @author Douglas Lau
 */
public class TMSProxy {

	/** The status level at which the proxy is fully initialized. */
	public final static int MAX_STATUS = 21;

	/** Remote TMS */
	protected final TMS tms;

	/** System-wide policy */
	protected final SystemPolicy policy;

	/** Get the system-wide policy */
	public SystemPolicy getPolicy() {
		return policy;
	}

	/** Remote roadway list */
	protected final RemoteListModel roadways;

	/** Get the remote roadway list */
	public RemoteListModel getRoadways() { return roadways; }

	/** Remote freeway list */
	protected final RemoteListModel freeways;

	/** Get the remote freeway list */
	public RemoteListModel getFreeways() { return freeways; }

	/** Communication line list */
	protected final RemoteListModel lines;

	/** Get the communication line list */
	public RemoteListModel getLines() { return lines; }

	/** Node group list */
	protected final RemoteListModel groups;

	/** Get the node group list */
	public RemoteListModel getGroups() { return groups; }

	/** Detector list */
	protected final RemoteListModel detectors;

	/** Get the detector list */
	public RemoteListModel getDetectors() { return detectors; }

	/** Available detector list */
	protected final RemoteListModel available;

	/** Get the available detector list */
	public RemoteListModel getAvailable() { return available; }

	/** Free mainline detectors */
	protected final RemoteListModel mainFree;

	/** Get the free mainline detector list */
	public RemoteListModel getMainFree() { return mainFree; }

	/** Free green count detector list */
	protected final RemoteListModel greenFree;

	/** Get the free green count detector list */
	public RemoteListModel getGreenFree() { return greenFree; }

	/** Station list */
	protected final RemoteListModel stations;

	/** Get the station list */
	public RemoteListModel getStations() { return stations; }

	/** Segment list */
	protected final RemoteListModel segments;

	/** Get the segment list */
	public RemoteListModel getSegments() {
		return segments;
	}

	/** R_Node list */
	protected final RemoteListModel r_nodes;

	/** Get the r_node list */
	public RemoteListModel getR_Nodes() {
		return r_nodes;
	}

	/** Timing plan list */
	protected final RemoteListModel plans;

	/** Get the timing plan list */
	public RemoteListModel getTimingPlans() { return plans; }

	/** Ramp meter list */
	protected final RampMeterList meter_list;

	/** Get the ramp meter list */
	public RampMeterList getMeterList() {
		return meter_list;
	}

	/** Ramp meter list model */
	protected RemoteListModel rampMeters;

	/** Get the ramp meter list model
	 * @deprecated Call getMeterList instead */
	public RemoteListModel getRampMeters() {
		return rampMeters;
	}

	/** Set the ramp meter list model
	 * @deprecated */
	public void setRampMeters(RemoteListModel m) {
		rampMeters = m;
	}

	/** Available ramp meter list */
	protected final RemoteListModel availableMeters;

	/** Get the available ramp meter list */
	public RemoteListModel getAvailableMeters() { return availableMeters; }

	/** Metering holiday list */
	protected final MeteringHolidayList holidays;

	/** Get the metering holiday list */
	public MeteringHolidayList getMeteringHolidayList() { return holidays; }

	/** Dynamic message sign list */
	protected final DMSList dms_list;

	/** Get the dynamic message sign list */
	public DMSList getDMSList() {
		return dms_list;
	}

	/** Dynamic message sign list */
	protected RemoteListModel dms_model;

	/** Get the dynamic message sign list model
	 * @deprecated Use getDMSList instead */
	public RemoteListModel getDMSListModel() {
		return dms_model;
	}

	/** Set the dynamic message sign list model
	 * @deprecated */
	public void setDMSListModel(RemoteListModel m) {
		dms_model = m;
	}

	/** Warning sign list */
	protected final RemoteListModel warn_signs;

	/** Get the warning sign list */
	public RemoteListModel getWarningSignList() { return warn_signs; }

	/** Available device list */
	protected final RemoteListModel devices;

	/** Get the available device list */
	public RemoteListModel getDevices() { return devices; }

	/** Camera list */
	protected final SortedList camera_list;

	/** Get the camera list */
	public SortedList getCameraList() {
		return camera_list;
	}

	/** Tour list */
	protected final RemoteListModel tour_list;

	/** Get the tour list */
	public RemoteListModel getTourList() {
		return tour_list;
	}

	/** Camera list model */
	protected RemoteListModel cameras;

	/** Get the camera list model
	 * @deprecated */
	public RemoteListModel getCameras() { return cameras; }

	/** Set the camera list model
	 * @deprecated */
	public void setCameras(RemoteListModel c) {
		cameras = c;
	}

	/** Lane Control Signal list */
	protected final RemoteListModel lcss;

	/** Get the lane control signal list */
	public RemoteListModel getLCSList() { return lcss; }

	/** Create a new TMS proxy */
	public TMSProxy(String server, String user) throws RemoteException,
		NotBoundException, MalformedURLException
	{
		Login l = (Login)Naming.lookup("//" + server + "/login");
		tms = l.login(user);
		policy = tms.getPolicy();
		roadways = new RemoteListModel(tms.getRoadwayList());
		RoadwayList roads = (RoadwayList)roadways.getList();
		freeways = new RemoteListModel(roads.getFreewayList());
		lines = new RemoteListModel(tms.getLineList());
		groups = new RemoteListModel(tms.getNodeGroupList());
		DetectorList dets = (DetectorList)tms.getDetectorList();
		detectors = new RemoteListModel(dets);
		available = new RemoteListModel(dets.getAvailableList());
		mainFree = new RemoteListModel(dets.getMainFreeList());
		greenFree = new RemoteListModel(dets.getGreenFreeList());
		stations = new RemoteListModel(tms.getStationList());
		segments = new RemoteListModel(tms.getSegmentMap());
		r_nodes = new RemoteListModel(tms.getR_NodeMap());
		meter_list = tms.getRampMeterList();
		availableMeters = new RemoteListModel(
			meter_list.getAvailableList());
		holidays = tms.getMeteringHolidayList();
		dms_list = tms.getDMSList();
		plans = new RemoteListModel(tms.getTimingPlanList());
		warn_signs = new RemoteListModel(tms.getWarningSignList());
		devices = new RemoteListModel(tms.getDeviceList());
		camera_list = tms.getCameraList();
		tour_list = new RemoteListModel(tms.getTourList());
		lcss = new RemoteListModel(tms.getLCSList());
	}

	/** Dispose of all proxied lists */
	public void dispose() {
		roadways.dispose();
		freeways.dispose();
		lines.dispose();
		groups.dispose();
		detectors.dispose();
		available.dispose();
		mainFree.dispose();
		greenFree.dispose();
		stations.dispose();
		availableMeters.dispose();
		devices.dispose();
		tour_list.dispose();
	}

	/** Get a TMSObject */
	public TMSObject getTMSObject(int vaultOID){
		try{
			return tms.getObject(vaultOID);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
