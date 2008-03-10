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
package us.mn.state.dot.tms;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * TMS is the remote interface to the TMS (Traffic Management System)
 *
 * @author Douglas Lau
 */
public interface TMS extends Remote {

	/** Get the communication line list */
	public IndexedList getLineList() throws RemoteException;

	/** Get the node group list */
	public IndexedList getNodeGroupList() throws RemoteException;

	/** Get the roadway list */
	public RoadwayList getRoadwayList() throws RemoteException;

	/** Get the detector list */
	public IndexedList getDetectorList() throws RemoteException;

	/** Get the station map */
	public StationMap getStationMap() throws RemoteException;

	/** Get the segment map */
	public SegmentMap getSegmentMap() throws RemoteException;

	/** Get the r_node map */
	public R_NodeMap getR_NodeMap() throws RemoteException;

	/** Get the timing plan list */
	public TimingPlanList getTimingPlanList() throws RemoteException;

	/** Get the ramp meter list */
	public RampMeterList getRampMeterList() throws RemoteException;

	/** Get the dynamic message sign list */
	public DMSList getDMSList() throws RemoteException;

	/** Get the warning sign list */
	public SortedList getWarningSignList() throws RemoteException;

	/** Get the available device list */
	public SortedList getDeviceList() throws RemoteException;

	/** Get the camera list */
	public SortedList getCameraList() throws RemoteException;

	/** Get the tour list */
	public SortedList getTourList() throws RemoteException;

	/** Get the lane control signal list */
	public SortedList getLCSList() throws RemoteException;

	/** Get a TMS object by its object ID */
	public TMSObject getObject(int oid) throws RemoteException;
}
