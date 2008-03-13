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

import java.rmi.RemoteException;

/**
 * Detector for traffic data sampling
 *
 * @author Douglas Lau
 */
public interface Detector extends Device {

	/** Get the detector index */
	public int getIndex() throws RemoteException;

	/** Undefined lane type */
	public short NONE = 0;

	/** Mainline lane type */
	public short MAINLINE = 1;

	/** Auxiliary lane type */
	public short AUXILIARY = 2;

	/** Collector/Distributor lane type */
	public short CD_LANE = 3;

	/** Reversible lane type */
	public short REVERSIBLE = 4;

	/** Merge lane type */
	public short MERGE = 5;

	/** Queue detector lane type */
	public short QUEUE = 6;

	/** Exit lane type */
	public short EXIT = 7;

	/** Meter bypass (HOV) lane type */
	public short BYPASS = 8;

	/** Passage lane type */
	public short PASSAGE = 9;

	/** Velocity (mainline) lane type */
	public short VELOCITY = 10;

	/** Omnibus (ok, bus) lane type */
	public short OMNIBUS = 11;

	/** Green count lane type */
	public short GREEN = 12;

	/** Wrong way (exit) lane type */
	public short WRONG_WAY = 13;

	/** High-Occupancy-Vehicle (HOV) lane type */
	short HOV = 14;

	/** High Occupancy / Toll (HOT) lane type */
	short HOT = 15;

	/** Lane class constant strings */
	public String[] LANE_TYPE = {
		" ", "Mainline", "Auxiliary", "CD Lane", "Reversible",
		"Merge", "Queue", "Exit", "Bypass", "Passage", "Velocity",
		"Omnibus", "Green", "Wrong Way", "HOV", "HOT"
	};

	/** Lane strings to use for detector names */
	public String[] LANE_SUFFIX = {
		"", "",
		"A", "CD", "R", "M", "Q", "X", "B", "P", "V", "O", "G", "Y",
		"H", "HT"
	};

	/** Number of samples in 3 minutes */
	public int SAMPLE_3_MINUTES = 6;

	/** Number of samples in 20 minutes */
	public int SAMPLE_20_MINUTES = 40;

	/** Number of samples in 30 minutes */
	public int SAMPLE_30_MINUTES = 60;

	/** Number of samples in 4 hours */
	public int SAMPLE_4_HOURS = 480;

	/** Number of samples in 8 hours */
	public int SAMPLE_8_HOURS = 960;

	/** Number of samples in 12 hours */
	public int SAMPLE_12_HOURS = 1440;

	/** Number of samples in 1 day */
	public int SAMPLE_1_DAY = 2880;

	/** Number of samples in 3 days */
	public int SAMPLE_3_DAYS = SAMPLE_1_DAY * 3;

	/** 30-second samples to check for "no hit" failure, by type */
	public int[] SAMPLE_THRESHOLD = {
		0,
		SAMPLE_4_HOURS,	// mainline
		SAMPLE_1_DAY,	// auxiliary
		SAMPLE_4_HOURS,	// CD lane
		SAMPLE_3_DAYS,	// reversible
		SAMPLE_12_HOURS,// merge
		SAMPLE_12_HOURS,// queue
		SAMPLE_8_HOURS,	// exit
		SAMPLE_3_DAYS,	// bypass
		SAMPLE_12_HOURS,// passage
		SAMPLE_4_HOURS,	// velocity
		SAMPLE_3_DAYS,	// omnibus
		SAMPLE_3_DAYS,	// green
		SAMPLE_8_HOURS,	// wrong way
		SAMPLE_8_HOURS,	// HOV
		SAMPLE_8_HOURS	// HOT
	};

	/** Set the lane type */
	public void setLaneType( short t ) throws TMSException,
		RemoteException;

	/** Get the lane type */
	public short getLaneType() throws RemoteException;

	/** Is this a station detector? (mainline, non-HOV) */
	public boolean isStation() throws RemoteException;

	/** Set the lane number */
	public void setLaneNumber( short laneNumber ) throws TMSException,
		RemoteException;

	/** Get the lane number */
	public short getLaneNumber() throws RemoteException;

	/** Set the abandoned status */
	public void setAbandoned( boolean abandoned ) throws TMSException,
		RemoteException;

	/** Get the abandoned status */
	public boolean isAbandoned() throws RemoteException;

	/** Set the Force Fail status */
	public void setForceFail( boolean forceFail ) throws TMSException,
		RemoteException;

	/** Get the Force Fail status */
	public boolean getForceFail() throws RemoteException;

	/** Set the average field length */
	public void setFieldLength( float field ) throws TMSException,
		RemoteException;

	/** Get the average field length */
	public float getFieldLength() throws RemoteException;

	/** Get the String representation of this detector.*/
	public String getLabel() throws RemoteException;

	/** Get the detector label */
	public String getLabel(boolean statName) throws RemoteException;

	/** Set the fake detector */
	public void setFakeDetector(String f) throws TMSException,
		RemoteException;

	/** Get the fake detector */
	public String getFakeDetector() throws RemoteException;

	/** Get the station which contains this detector */
	public Station getStation() throws RemoteException;
}
