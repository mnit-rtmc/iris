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
import us.mn.state.dot.vault.ObjectVaultException;

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

	/** Default speed limit */
	static public final int DEFAULT_SPEED_LIMIT = 55;

	/** Minimum freeway speed limit */
	static public final int MINIMUM_SPEED_LIMIT = 45;

	/** Maximum freeway speed limit */
	static public final int MAXIMUM_SPEED_LIMIT = 75;

	/** Number of samples to average when speed is calm */
	static public final int CALM_SAMPLES = 4;

	/** Number of samples to average when speed is chaotic */
	static public final int CHAOTIC_SAMPLES = 10;

	/** Threshold for chaotic speed changes */
	static public final int CHAOTIC_SPEED = 15;

	/** Calculate the rolling average speed */
	static protected float calculateRollingSpeed(float[] speed) {
		float total = 0;
		float count = 0;
		int samples = CALM_SAMPLES;
		for(int i = 0; i < samples; i++) {
			float s = speed[i];
			if(s != MISSING_DATA) {
				total += s;
				count += 1;
				if(s < CHAOTIC_SPEED)
					samples = speed.length;
			}
		}
		if(count > 0)
			return total / count;
		else
			return MISSING_DATA;
	}

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
		if(index != null) {
			try { statList.add(index, this); }
			catch(TMSException e) {
				e.printStackTrace();
			}
		}
		for(int i = 0; i < avg_speed.length; i++)
			avg_speed[i] = MISSING_DATA;
		for(int i = 0; i < low_speed.length; i++)
			low_speed[i] = MISSING_DATA;
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
		synchronized(statList) {
			if(i != null) statList.add(i, this);
			try { vault.update(this, "index", i, getUserName()); }
			catch(ObjectVaultException e) {
				throw new TMSException(e);
			}
			if(index != null) statList.remove(index);
			index = i;
		}
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
		try {
			vault.update(this, "speed_limit", new Integer(l),
				getUserName());
		} catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
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

	/** Average station speed for previous ten samples */
	protected transient float[] avg_speed = new float[CHAOTIC_SAMPLES];

	/** Update average station speed with a new sample */
	protected void updateAvgSpeed(float s) {
		System.arraycopy(avg_speed, 0, avg_speed, 1,
			avg_speed.length - 1);
		avg_speed[0] = Math.min(s, speed_limit);
	}

	/** Get the average speed for travel time calculation */
	public float getTravelSpeed() {
		return calculateRollingSpeed(avg_speed);
	}

	/** Low station speed for previous ten samples */
	protected transient float[] low_speed = new float[CHAOTIC_SAMPLES];

	/** Update low station speed with a new sample */
	protected void updateLowSpeed(float s) {
		System.arraycopy(low_speed, 0, low_speed, 1,
			low_speed.length - 1);
		low_speed[0] = Math.min(s, speed_limit);
	}

	/** Get the low speed for travel time calculation */
	public float getLowTravelSpeed() {
		return calculateRollingSpeed(low_speed);
	}

	/** Calculate the current station data */
	public void calculateData() {
		float low = MISSING_DATA;
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
				if(low == MISSING_DATA) low = f;
				else low = Math.min(f, low);
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
		updateAvgSpeed(speed);
		updateLowSpeed(low);
		notifyStatus();
	}

	/** Calculate the travel time for one link */
	protected float linkTravelTime(float start, float end, float speed) {
		return (end - start) / speed;
	}

	/** Calculate the travel time from this station to the next */
	public float calculateTravelTime(float start, StationSegmentImpl next,
		float end_mile, boolean testing)
	{
		float hours = 0;
		Float mile1 = mile;
		Float mile2 = next.mile;
		if(mile1 == null || mile2 == null)
			return MISSING_DATA;
		float m1 = mile1.floatValue();
		float m2 = mile2.floatValue();
		float md = (m2 - m1) / 3.0f;
		float p1 = getTravelSpeed();
		float p2 = next.getTravelSpeed();
		if(start < m1)
			start = m1;
		if(start > end_mile - 1) {
			p1 = getLowTravelSpeed();
			p2 = next.getLowTravelSpeed();
		}
		if(p1 <= 0 || p2 <= 0)
			return MISSING_DATA;
		if(start < m1 + md) {
			hours += linkTravelTime(start, m1 + md, p1);
			start = m1 + md;
		}
		if(start > end_mile - 1) {
			p1 = getLowTravelSpeed();
			p2 = next.getLowTravelSpeed();
		}
		if(p1 <= 0 || p2 <= 0)
			return MISSING_DATA;
		if(start < m1 + 2 * md) {
			hours += linkTravelTime(start, m1 + 2 * md,
				(p1 + p2) / 2.0f);
			start = m1 + 2 * md;
		}
		if(start > end_mile - 1) {
			p1 = getLowTravelSpeed();
			p2 = next.getLowTravelSpeed();
		}
		if(p1 <= 0 || p2 <= 0)
			return MISSING_DATA;
		if(start < m2) {
			hours += linkTravelTime(start, m2, p2);
		}
		if(testing) {
			System.err.println("TRAVEL TIME from: " + getIndex() +
				", speed: " + p1 + ", to: " + next.getIndex() +
				", speed: " + p2 + ", minutes: " +
				(hours * 60));
		}
		return hours;
	}

	/** Print the segment as an XML element */
	public float printXml(PrintWriter out, float last_mile) {
		RoadwayImpl xs = (RoadwayImpl)location.getCrossStreet();
		if(xs == null)
			return last_mile;
		DetectorImpl[] dets = detectors;
		if(dets.length == 0)
			return last_mile;
		Integer idx = getIndex();
		if(idx == null) {
			System.err.print("No station index: " + xs.getName());
			System.err.println(", det: " + dets[0].getIndex());
			return last_mile;
		}
		out.print("\t<station ");
		out.print("index='S" + idx + "' ");
		out.print("label='" + xs.getName() + "' ");
		Float m = getMile();
		if(m != null && (m.floatValue() > last_mile)) {
			out.print("mile='" + m + "' ");
			last_mile = m.floatValue();
		}
		int s = getShift();
		if(s != 0) {
			out.print("shift='" + s + "' ");
		}
		int s_limit = getSpeedLimit();
		if(s_limit != DEFAULT_SPEED_LIMIT) {
			out.print("s_limit='" + s_limit + "' ");
		}
		Location loc = this.getLocation();
		try{
			if(loc != null){
				int n, e;
				n = loc.getNorthing();
				e = loc.getEasting();
				out.print("northing='" + n + "' ");
				out.print("easting='" + e + "' ");
			}
		}catch(RemoteException re){
			//ignore, we just won't print the loc info
		}
		out.print("dets='");
		for(int d = 0; d < dets.length; d++) {
			if(d > 0) out.print(" ");
			out.print("D" + dets[d].getIndex());
		}
		out.println("' />");
		return last_mile;
	}

	/** Print the current sample as an XML element */
	public void printSampleXmlElement(PrintWriter out) {
		if(!isActive()) return;
		Integer i = getIndex();
		if(i == null) return;
		int f = getFlow();
		int s = getSpeed();
		out.print("\t<sample sensor='S" + i);
		if(f > MISSING_DATA) out.print("' flow='" + f);
		if(s > MISSING_DATA) out.print("' speed='" + s);
		out.println("'/>");
	}

	/** Print the current sample as an XML element */
	public void printStationXmlElement(PrintWriter out) {
		if(!isActive()) return;
		if(volume == MISSING_DATA) {
			out.println("\t<station id='" + index +
				"' status='fail'/>");
		} else {
			out.println("\t<station id='" + index +
				"' status='ok'>");
			out.println("\t\t<volume>" + volume + "</volume>");
			out.println("\t\t<occupancy>" + occupancy +
				"</occupancy>");
			out.println("\t\t<flow>" + flow + "</flow>");
			out.println("\t\t<speed>" + speed + "</speed>");
			out.println("\t</station>");
		}
	}
}
