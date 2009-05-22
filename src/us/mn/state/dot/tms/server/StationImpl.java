/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.io.PrintWriter;
import us.mn.state.dot.tms.Constants;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Station;

/**
 * A station is a group of related detectors.
 *
 * @author Douglas Lau
 */
public class StationImpl implements Station {

	/** Speed ranks for extending rolling sample averaging */
	static public final int[] SPEED_RANK = { 25, 20, 15, 0 };

	/** Number of samples to average for each speed rank */
	static public final int[] SAMPLES = { 4, 6, 8, 10 };

	/** Calculate the average from a total and sample count */
	static protected float calculateAverage(float total, int count) {
		if(count > 0)
			return total / count;
		else
			return Constants.MISSING_DATA;
	}

	/** Get the number of rolling samples for the given speed */
	static protected int speed_samples(float s) {
		for(int r = 0; r < SPEED_RANK.length; r++) {
			if(s > SPEED_RANK[r])
				return SAMPLES[r];
		}
		return SAMPLES[SAMPLES.length - 1];
	}

	/** Calculate the rolling average speed */
	static protected float calculateRollingSpeed(float[] speeds) {
		float total = 0;
		int count = 0;
		int samples = speed_samples(DEFAULT_SPEED_LIMIT);
		for(int i = 0; i < samples; i++) {
			float s = speeds[i];
			if(s > 0) {
				total += s;
				count += 1;
				samples = Math.max(samples, speed_samples(s));
			}
		}
		return calculateAverage(total, count);
	}

	/** Staiton name */
	protected final String name;

	/** Get the station name */
	public String getName() {
		return name;
	}

	/** Get the station index */
	public String getIndex() {
		if(name.startsWith("S"))
			return name.substring(1);
		else
			return name;
	}

	/** Roadway node */
	protected final R_NodeImpl r_node;

	/** Get the roadway node */
	public R_Node getR_Node() {
		return r_node;
	}

        /** Create a new station */
	public StationImpl(String station_id, R_NodeImpl n) {
		name = station_id;
		r_node = n;
		for(int i = 0; i < avg_speed.length; i++)
			avg_speed[i] = Constants.MISSING_DATA;
		for(int i = 0; i < low_speed.length; i++)
			low_speed[i] = Constants.MISSING_DATA;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Destroy a station */
	public void destroy() {
		// Nothing to do
	}

	/** Get a string representation of the station */
	public String toString() {
		return name;
	}

	/** Does this node have the specified detector? */
	public boolean hasDetector(DetectorImpl det) {
		return r_node.hasDetector(det);
	}

	/** Get the station label */
	public String getLabel() {
		DetectorImpl[] dets = r_node.getDetectors();
		if(dets.length > 0)
			return DetectorHelper.getStationLabel(dets[0]);
		else
			return "UNASSIGNED";
	}

	/** Is this station active? */
	public boolean getActive() {
		return r_node.getDetectors().length > 0;
	}

	/** Current average station volume */
	protected float volume = Constants.MISSING_DATA;

	/** Current average station occupancy */
	protected float occupancy = Constants.MISSING_DATA;

	/** Current average station flow */
	protected int flow = Constants.MISSING_DATA;

	/** Get the average station flow */
	public int getFlow() {
		return flow;
	}

	/** Current average station speed */
	protected int speed = Constants.MISSING_DATA;

	/** Get the average station speed */
	public int getSpeed() {
		return speed;
	}

	/** Average station speed for previous ten samples */
	protected float[] avg_speed = new float[SAMPLES[SAMPLES.length - 1]];

	/** Update average station speed with a new sample */
	protected void updateAvgSpeed(float s) {
		System.arraycopy(avg_speed, 0, avg_speed, 1,
			avg_speed.length - 1);
		avg_speed[0] = Math.min(s, r_node.getSpeedLimit());
	}

	/** Get the average speed for travel time calculation */
	public float getTravelSpeed(boolean low) {
		if(low)
			return calculateRollingSpeed(low_speed);
		else
			return calculateRollingSpeed(avg_speed);
	}

	/** Low station speed for previous ten samples */
	protected float[] low_speed = new float[SAMPLES[SAMPLES.length - 1]];

	/** Update low station speed with a new sample */
	protected void updateLowSpeed(float s) {
		System.arraycopy(low_speed, 0, low_speed, 1,
			low_speed.length - 1);
		low_speed[0] = Math.min(s, r_node.getSpeedLimit());
	}

	/** Calculate the current station data */
	public void calculateData() {
		float low = Constants.MISSING_DATA;
		float t_volume = 0;
		int n_volume = 0;
		float t_occ = 0;
		int n_occ = 0;
		float t_flow = 0;
		int n_flow = 0;
		float t_speed = 0;
		int n_speed = 0;
		for(DetectorImpl det: r_node.getDetectors()) {
			if(!det.isStationOrCD())
				continue;
			float f = det.getVolume();
			if(f != Constants.MISSING_DATA) {
				t_volume += f;
				n_volume++;
			}
			f = det.getOccupancy();
			if(f != Constants.MISSING_DATA) {
				t_occ += f;
				n_occ++;
			}
			f = det.getFlow();
			if(f != Constants.MISSING_DATA) {
				t_flow += f;
				n_flow++;
			}
			f = det.getSpeed();
			if(f > 0) {
				t_speed += f;
				n_speed++;
				if(low == Constants.MISSING_DATA)
					low = f;
				else
					low = Math.min(f, low);
			}
		}
		volume = calculateAverage(t_volume, n_volume);
		occupancy = calculateAverage(t_occ, n_occ);
		flow = (int)calculateAverage(t_flow, n_flow);
		speed = (int)calculateAverage(t_speed, n_speed);
		updateAvgSpeed(speed);
		updateLowSpeed(low);
	}

	/** Print the current sample as an XML element */
	public void printSampleXmlElement(PrintWriter out) {
		if(!getActive())
			return;
		int f = getFlow();
		int s = getSpeed();
		out.print("\t<sample sensor='" + name);
		if(f > Constants.MISSING_DATA)
			out.print("' flow='" + f);
		if(s > 0)
			out.print("' speed='" + s);
		out.println("'/>");
	}

	/** Print the current sample as an XML element */
	public void printStationXmlElement(PrintWriter out) {
		if(!getActive())
			return;
		String n = getIndex();
		if(n.length() < 1)
			return;
		if(volume == Constants.MISSING_DATA) {
			out.println("\t<station id='" + n +
				"' status='fail'/>");
		} else {
			out.println("\t<station id='" + n + "' status='ok'>");
			out.println("\t\t<volume>" + volume + "</volume>");
			out.println("\t\t<occupancy>" + occupancy +
				"</occupancy>");
			out.println("\t\t<flow>" + flow + "</flow>");
			out.println("\t\t<speed>" + speed + "</speed>");
			out.println("\t</station>");
		}
	}
}
