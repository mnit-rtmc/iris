/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tdxml.SensorSample;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;

/**
 * A segment is the shape of a roadway segment on a map.
 *
 * @author Douglas Lau
 */
public class Segment implements MapObject {

	/** Identity transform */
	static protected final AffineTransform IDENTITY_TRANSFORM =
		new AffineTransform();

	/** R_Node for segment */
	protected final R_Node r_node;

	/** Get the station ID */
	public String getStationID() {
		if(r_node != null)
			return r_node.getStationID();
		else
			return "";
	}

	/** List of map geo locations */
	protected final List<MapGeoLoc> locs = new LinkedList<MapGeoLoc>();

	/** Mapping of sensor ID to lane number */
	protected final HashMap<String, Integer> lane_sensors =
		new HashMap<String, Integer>();

	/** Mapping of sensor ID to sample data */
	protected final HashMap<String, SensorSample> samples =
		new HashMap<String, SensorSample>();

	/** Shape to render */
	protected Shape shape = null;

	/** Get the shape to draw this object */
	public Shape getShape() {
		return shape;
	}

	/** Create a new segment */
	public Segment() {
		this(null);
	}

	/** Create a new segment */
	public Segment(R_Node n) {
		r_node = n;
		if(r_node != null) {
			DetectorHelper.find(new Checker<Detector>() {
				public boolean check(Detector d) {
					if(d.getR_Node() == r_node) {
						String id = "D" + d.getName();
						int n = d.getLaneNumber();
						lane_sensors.put(id, n);
					}
					return false;
				}
			});
		}
	}

	/** Add a point to the segment */
	public void addNode(MapGeoLoc loc) {
		locs.add(loc);
	}

	/** Create the shape to draw this object */
	public void createShape() {
		boolean first = true;
		Point2D.Float p = new Point2D.Float();
		Path2D.Float path = new Path2D.Float(Path2D.WIND_NON_ZERO);
		ListIterator<MapGeoLoc> li = locs.listIterator();
		while(li.hasNext()) {
			MapGeoLoc loc = li.next();
			if(loc.setPoint(p, 300)) {
				if(first) {
					path.moveTo(p.getX(), p.getY());
					first = false;
				} else
					path.lineTo(p.getX(), p.getY());
			}
		}
		while(li.hasPrevious()) {
			MapGeoLoc loc = li.previous();
			if(loc.setPoint(p, 75))
				path.lineTo(p.getX(), p.getY());
		}
		if(!locs.isEmpty())
			path.closePath();
		shape = path;
	}

	/** Get the coordinate transform */
	public AffineTransform getTransform() {
		return IDENTITY_TRANSFORM;
	}

	/** Get the inverse coordinate transform */
	public AffineTransform getInverseTransform() {
		return IDENTITY_TRANSFORM;
	}

	/** Update one sample */
	public void updateSample(SensorSample s) {
		if(lane_sensors.containsKey(s.id)) {
			synchronized(samples) {
				samples.put(s.id, s);
			}
		}
	}

	/** Clear the samples */
	public void clearSamples() {
		synchronized(samples) {
			samples.clear();
		}
	}

	/** Get the flow for the given lane */
	public Integer getFlow(Integer lane) {
		int total = 0;
		int count = 0;
		synchronized(samples) {
			for(String sid: samples.keySet()) {
				SensorSample s = samples.get(sid);
				if(s != null) {
					Integer f = s.getFlow();
					if(f != null && (lane == null ||
					   lane == lane_sensors.get(sid)))
					{
						total += f;
						count++;
					}
				}
			}
		}
		if(count > 0)
			return total / count;
		else
			return null;
	}

	/** Get the speed for the given lane */
	public Integer getSpeed(Integer lane) {
		int total = 0;
		int count = 0;
		synchronized(samples) {
			for(String sid: samples.keySet()) {
				SensorSample s = samples.get(sid);
				if(s != null) {
					Integer spd = s.getSpeed();
					if(spd != null && (lane == null ||
					   lane == lane_sensors.get(sid)))
					{
						total += spd;
						count++;
					}
				}
			}
		}
		if(count > 0)
			return total / count;
		else
			return null;
	}

	/** Get the density for the given lane */
	public Integer getDensity(Integer lane) {
		int total = 0;
		int count = 0;
		synchronized(samples) {
			for(String sid: samples.keySet()) {
				SensorSample s = samples.get(sid);
				if(s != null) {
					Integer d = s.getDensity();
					if(d != null && (lane == null ||
					   lane == lane_sensors.get(sid)))
					{
						total += d;
						count++;
					}
				}
			}
		}
		if(count > 0)
			return total / count;
		else
			return null;
	}
}
