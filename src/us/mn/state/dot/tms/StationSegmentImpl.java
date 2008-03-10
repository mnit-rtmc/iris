/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2007  Minnesota Department of Transportation
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

import java.io.PrintWriter;
import java.rmi.RemoteException;
import us.mn.state.dot.vault.FieldMap;

/**
 * A station segment is a mainline station on a freeway segment list.
 *
 * @author Douglas Lau
 */
public class StationSegmentImpl extends SegmentImpl implements StationSegment,
	Constants
{
	/** ObjectVault table name */
	static public final String tableName = "station_segment";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Default speed limit */
	static public final int DEFAULT_SPEED_LIMIT = 55;

	/** Minimum freeway speed limit */
	static public final int MINIMUM_SPEED_LIMIT = 45;

	/** Maximum freeway speed limit */
	static public final int MAXIMUM_SPEED_LIMIT = 75;

        /**
         * Create a new mainline station segment.
         * @param left flag for left-side ramps
         * @param delta change in number of mainline lanes
         * @param cdDelta change in number of collector-distributor lanes
         */
	public StationSegmentImpl(boolean left, int delta, int cdDelta)
		throws RemoteException
	{
		super(left, delta, cdDelta);
	}

	/** Create a mainline station segment from an ObjectVault field map */
	protected StationSegmentImpl(FieldMap fields) throws RemoteException {
		super(fields);
	}

	/** Initialize the transient state */
	public void initTransients() throws TMSException {
		super.initTransients();
		if(index.intValue() == 0)
			index = null;
	}

	/** Get a String representation of the station */
	public String toString() {
		StringBuffer buffer = new StringBuffer().append(index);
		while(buffer.length() < 4) buffer.insert(0, ' ');
		buffer.append("  ").append(getLabel());
		return buffer.toString();
	}

	/** Get the station label */
	public String getLabel() {
		DetectorImpl[] dets = detectors;
		if(dets.length < 1) return "UNASSIGNED";
		return dets[0].getLabel(true);
	}

	/** Test if a detector is valid for the segment type */
	protected boolean validateDetector(DetectorImpl det) {
		return det.isMainline() &&
			(det.getLaneType() != Detector.CD_LANE);
	}

	/** Staiton index */
	protected Integer index = null;

	/** Get the station index */
	public Integer getIndex() {
		Integer i = index;	// Avoid client races
		if(i == null || i.intValue() < 1) return null;
		else return i;
	}

	/** Set the station index */
	public synchronized void setIndex(Integer i) throws TMSException {
		if(i != null) {
			if(i.equals(index)) return;
			if(i.intValue() <= 0) throw new ChangeVetoException(
				"Station index must be positive");
		} else if(index == null) return;
		store.update(this, "index", i);
		index = i;
	}

	/** Station speed limit */
	protected int speed_limit = DEFAULT_SPEED_LIMIT;

	/** Get the station speed limit */
	public int getSpeedLimit() { return speed_limit; }

	/** Set the station speed limit */
	public synchronized void setSpeedLimit(int l) throws TMSException {
		if(l == speed_limit) return;
		if(l < MINIMUM_SPEED_LIMIT || l > MAXIMUM_SPEED_LIMIT) throw
			new ChangeVetoException("Invalid speed limit");
		store.update(this, "speed_limit", l);
		speed_limit = l;
	}

	/** Is this station active? */
	public boolean isActive() {
		return detectors.length > 0;
	}

	/** Current average station volume */
	protected transient float volume = MISSING_DATA;

	/** Get the average station volume
	 * @deprecated */
	public float getVolume() { return volume; }

	/** Current average station occupancy */
	protected transient float occupancy = MISSING_DATA;

	/** Get the average station occupancy
	 * @deprecated */
	public float getOccupancy() { return occupancy; }

	/** Current average station flow */
	protected transient int flow = MISSING_DATA;

	/** Get the average station flow */
	public int getFlow() { return flow; }

	/** Current average station speed */
	protected transient int speed = MISSING_DATA;

	/** Get the average station speed */
	public int getSpeed() { return speed; }

	/** Calculate the current station data */
	public void calculateData() {
		DetectorImpl[] dets = detectors;
		float t_volume = 0;
		int n_volume = 0;
		float t_occ = 0;
		int n_occ = 0;
		float t_flow = 0;
		int n_flow = 0;
		float t_speed = 0;
		int n_speed = 0;
		for(int i = 0; i < dets.length; i++) {
			DetectorImpl det = dets[i];
			if(det.getForceFail() || !det.isStation()) continue;
			float f = det.getVolume();
			if(f != MISSING_DATA) {
				t_volume += f;
				n_volume++;
			}
			f = det.getOccupancy();
			if(f != MISSING_DATA) {
				t_occ += f;
				n_occ++;
			}
			f = det.getFlow();
			if(f != MISSING_DATA) {
				t_flow += f;
				n_flow++;
			}
			f = det.getSpeed();
			if(f != MISSING_DATA) {
				t_speed += f;
				n_speed++;
			}
		}
		if(n_volume > 0) volume = t_volume / n_volume;
		else volume = MISSING_DATA;
		if(n_occ > 0) occupancy = t_occ / n_occ;
		else occupancy = MISSING_DATA;
		if(n_flow > 0) flow = Math.round(t_flow / n_flow);
		else flow = MISSING_DATA;
		if(n_speed > 0) speed = Math.round(t_speed / n_speed);
		else speed = MISSING_DATA;
		notifyStatus();
	}
}
