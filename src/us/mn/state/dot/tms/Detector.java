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
	int getIndex() throws RemoteException;

	/** Undefined lane type */
	short NONE = 0;

	/** Mainline lane type */
	short MAINLINE = 1;

	/** Auxiliary lane type */
	short AUXILIARY = 2;

	/** Collector/Distributor lane type */
	short CD_LANE = 3;

	/** Reversible lane type */
	short REVERSIBLE = 4;

	/** Merge lane type */
	short MERGE = 5;

	/** Queue detector lane type */
	short QUEUE = 6;

	/** Exit lane type */
	short EXIT = 7;

	/** Meter bypass (HOV) lane type */
	short BYPASS = 8;

	/** Passage lane type */
	short PASSAGE = 9;

	/** Velocity (mainline) lane type */
	short VELOCITY = 10;

	/** Omnibus (ok, bus) lane type */
	short OMNIBUS = 11;

	/** Green count lane type */
	short GREEN = 12;

	/** Wrong way (exit) lane type */
	short WRONG_WAY = 13;

	/** High-Occupancy-Vehicle (HOV) lane type */
	short HOV = 14;

	/** High Occupancy / Toll (HOT) lane type */
	short HOT = 15;

	/** Lane class constant strings */
	String[] LANE_TYPE = {
		" ", "Mainline", "Auxiliary", "CD Lane", "Reversible",
		"Merge", "Queue", "Exit", "Bypass", "Passage", "Velocity",
		"Omnibus", "Green", "Wrong Way", "HOV", "HOT"
	};

	/** Lane strings to use for detector names */
	String[] LANE_SUFFIX = {
		"", "",
		"A", "CD", "R", "M", "Q", "X", "B", "P", "V", "O", "G", "Y",
		"H", "HT"
	};

	/** Number of samples in 3 minutes */
	int SAMPLE_3_MINUTES = 6;

	/** Number of samples in 20 minutes */
	int SAMPLE_20_MINUTES = 40;

	/** Number of samples in 30 minutes */
	int SAMPLE_30_MINUTES = 60;

	/** Number of samples in 4 hours */
	int SAMPLE_4_HOURS = 480;

	/** Number of samples in 8 hours */
	int SAMPLE_8_HOURS = 960;

	/** Number of samples in 12 hours */
	int SAMPLE_12_HOURS = 1440;

	/** Number of samples in 1 day */
	int SAMPLE_1_DAY = 2880;

	/** Number of samples in 3 days */
	int SAMPLE_3_DAYS = SAMPLE_1_DAY * 3;

	/** 30-second samples to check for "no hit" failure, by type */
	int[] SAMPLE_THRESHOLD = {
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
	void setLaneType(short t) throws TMSException, RemoteException;

	/** Get the lane type */
	short getLaneType() throws RemoteException;

	/** Is this a station detector? (mainline, non-HOV) */
	boolean isStation() throws RemoteException;

	/** Set the lane number */
	void setLaneNumber(short laneNumber) throws TMSException,
		RemoteException;

	/** Get the lane number */
	short getLaneNumber() throws RemoteException;

	/** Set the abandoned status */
	void setAbandoned(boolean abandoned) throws TMSException,
		RemoteException;

	/** Get the abandoned status */
	boolean isAbandoned() throws RemoteException;

	/** Set the Force Fail status */
	void setForceFail(boolean forceFail) throws TMSException,
		RemoteException;

	/** Get the Force Fail status */
	boolean getForceFail() throws RemoteException;

	/** Set the average field length */
	void setFieldLength(float field) throws TMSException, RemoteException;

	/** Get the average field length */
	float getFieldLength() throws RemoteException;

	/** Get the String representation of this detector */
	String getLabel() throws RemoteException;

	/** Get the detector label */
	String getLabel(boolean statName) throws RemoteException;

	/** Set the fake detector */
	void setFakeDetector(String f) throws TMSException, RemoteException;

	/** Get the fake detector */
	String getFakeDetector() throws RemoteException;

	/** Get the station which contains this detector */
	Station getStation() throws RemoteException;
}
