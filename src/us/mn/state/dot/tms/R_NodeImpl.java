/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import us.mn.state.dot.vault.FieldMap;

/**
 * R_NodeImpl is an implementation of the R_Node interface. Each
 * object of this class represents one node on the roadway network.
 *
 * @author Douglas Lau
 */
public class R_NodeImpl extends TMSObjectImpl implements R_Node, Storable {

	/** ObjectVault table name */
	static public final String tableName = "r_node";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Maximum number of lanes allowed */
	static protected final int LANES_MAX = 8;

	/** Minimum lane shift allowed */
	static protected final int SHIFT_MIN = 0;

	/** Maximum lane shift allowed */
	static protected final int SHIFT_MAX = 12;

	/** Default speed limit */
	static protected final int DEFAULT_SPEED_LIMIT = 55;

	/** Minimum freeway speed limit */
	static protected final int MINIMUM_SPEED_LIMIT = 45;

	/** Maximum freeway speed limit */
	static protected final int MAXIMUM_SPEED_LIMIT = 75;

	/** Table mapping for r_node_detector relation */
	static public TableMapping mapping;

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		// FIXME: implement this for SONAR
		return null;
	}

	/** Node location */
	protected String geo_loc;

	/** Set the controller location */
	public synchronized void setGeoLoc(String l) throws TMSException {
		if(l == geo_loc)
			return;
		store.update(this, "geo_loc", l);
		geo_loc = l;
	}

	/** Get the location */
	public String getGeoLoc() {
		return geo_loc;
	}

	/** Lookup the geo location */
	public GeoLocImpl lookupGeoLoc() {
		return lookupGeoLoc(geo_loc);
	}

	/** Check if the location is valid */
	public boolean hasLocation() {
		return !GeoLocHelper.isNull(lookupGeoLoc());
	}

	/** Calculate the distance to another roadway node (in meters) */
	public Double metersTo(R_NodeImpl other) {
		return metersTo(other.lookupGeoLoc());
	}

	/** Calculate the distance to another location (in meters) */
	public Double metersTo(GeoLoc loc) {
		return GeoLocHelper.metersTo(lookupGeoLoc(), loc);
	}

	/** Node type */
	protected int node_type;

	/** Get the node type */
	public int getNodeType() {
		return node_type;
	}

	/** Set the node type */
	public synchronized void setNodeType(int t) throws TMSException {
		if(t == node_type)
			return;
		if(t < 0 || t >= TYPES.length)
			throw new ChangeVetoException("Bad node type: " + t);
		store.update(this, "node_type", t);
		node_type = t;
	}

	/** Check if the r_node is a station */
	public boolean isStation() {
		return node_type == TYPE_STATION;
	}

	/** Check if the r_node is an entrance */
	public boolean isEntrance() {
		return node_type == TYPE_ENTRANCE;
	}

	/** Check if the r_node is an exit */
	public boolean isExit() {
		return node_type == TYPE_EXIT;
	}

	/** Check if the r_node is an access node */
	public boolean isAccess() {
		return node_type == TYPE_ACCESS;
	}

	/** Test if this r_node type can be linked in a corridor */
	protected boolean isCorridorType() {
		switch(node_type) {
			case TYPE_STATION:
			case TYPE_ENTRANCE:
			case TYPE_EXIT:
			case TYPE_INTERSECTION:
				return true;
			default:
				return false;
		}
	}

	/** Pickable flag */
	protected boolean pickable;

	/** Is this node pickable? */
	public boolean isPickable() {
		return pickable;
	}

	/** Set the pickable flag */
	public synchronized void setPickable(boolean p) throws TMSException {
		if(p == pickable)
			return;
		store.update(this, "pickable", p);
		pickable = p;
	}

	/** Transition type */
	protected int transition;

	/** Get the transition type */
	public int getTransition() {
		return transition;
	}

	/** Set the transition type */
	public synchronized void setTransition(int t) throws TMSException {
		if(t == transition)
			return;
		if(t < 0 || t >= TRANSITIONS.length)
			throw new ChangeVetoException("Bad transition: " + t);
		store.update(this, "transition", t);
		transition = t;
	}

	/** Check if this r_node is an exit to a common section */
	protected boolean isCommonExit() {
		return isExit() && (transition == TRANSITION_COMMON);
	}

	/** Check if this r_node links to a CD road */
	public boolean isCD() {
		return transition == TRANSITION_CD;
	}

	/** Check if this r_node has a link to the downstream r_node */
	protected boolean hasDownstreamLink() {
		return isCorridorType() && !isCommonExit();
	}

	/** Check if this r_node should impose a "turn" penalty */
	public boolean hasTurnPenalty() {
		return (transition == TRANSITION_LOOP) ||
			(transition == TRANSITION_LEG) ||
			(transition == TRANSITION_HOV) ||
			(transition == TRANSITION_FLYOVER);
	}

	/** Number of lanes */
	protected int lanes;

	/** Get the number of lanes */
	public int getLanes() {
		return lanes;
	}

	/** Set the number of lanes */
	public synchronized void setLanes(int l) throws TMSException {
		if(l == lanes)
			return;
		if(l < 0 || l > LANES_MAX)
			throw new ChangeVetoException("Bad lanes: " + l);
		store.update(this, "lanes", l);
		lanes = l;
	}

	/** Attach side value */
	protected boolean attach_side;

	/** Get the attach side (true = left, false = right) */
	public boolean getAttachSide() {
		return attach_side;
	}

	/** Set the attach side */
	public synchronized void setAttachSide(boolean s) throws TMSException {
		if(s == attach_side)
			return;
		store.update(this, "attach_side", s);
		attach_side = s;
	}

	/** Lane shift from corridor reference to attach side */
	protected int shift;

	/** Get the lane shift */
	public int getShift() {
		return shift;
	}

	/** Set the lane shift */
	public synchronized void setShift(int s) throws TMSException {
		if(s == shift)
			return;
		if(s < SHIFT_MIN || s > SHIFT_MAX)
			throw new ChangeVetoException("Bad shift: " + s);
		store.update(this, "shift", s);
		shift = s;
	}

	/** Staiton ID */
	protected String station_id = "";

	/** Get the staiton ID */
	static protected String getStationID(String sid) {
		if(sid == null || sid.length() < 1)
			return "";
		else
			return sid;
	}

	/** Get the station ID */
	public String getStationID() {
		return getStationID(station_id);
	}

	/** Create a remove station object */
	protected StationImpl createStation(String s) throws RemoteException {
		if(s != null)
			return new StationImpl(s, this);
		else
			return null;
	}

	/** Put the station ID into the station list */
	protected void _setStationID(String s) throws TMSException,
		RemoteException
	{
		if(statMap.getElement(s) != null)
			throw new ChangeVetoException("Duplicate ID: " + s);
		StationImpl station = createStation(s);
		store.update(this, "station_id", s);
		try {
			if(station != null)
				statMap.add(s, station);
			if(station_id.length() > 0)
				statMap.remove(station_id);
		}
		finally {
			station_id = s;
		}
	}

	/** Set the station ID */
	public synchronized void setStationID(String s) throws TMSException,
		RemoteException
	{
		s = getStationID(s);
		if(s.equals(station_id))
			return;
		validateText(s);
		synchronized(statMap) {
			_setStationID(s);
		}
	}

	/** Get the associated station */
	public Station getStation() {
		synchronized(statMap) {
			String sid = getStationID();
			if(sid.length() > 0)
				return statMap.getElement(sid);
		}
		return null;
	}

	/** Speed limit */
	protected int speed_limit = DEFAULT_SPEED_LIMIT;

	/** Get the speed limit */
	public int getSpeedLimit() {
		return speed_limit;
	}

	/** Set the speed limit */
	public synchronized void setSpeedLimit(int l) throws TMSException {
		if(l == speed_limit)
			return;
		if(l < MINIMUM_SPEED_LIMIT || l > MAXIMUM_SPEED_LIMIT)
			throw new ChangeVetoException("Bad speed limit: " + l);
		store.update(this, "speed_limit", l);
		speed_limit = l;
	}

	/** Administrator notes */
	protected String notes;

	/** Get the administrator notes */
	public String getNotes() {
		return notes;
	}

	/** Set the administrator notes */
	public synchronized void setNotes(String n) throws TMSException {
		if(n.equals(notes))
			return;
		store.update(this, "notes", n);
		notes = n;
	}

	/** Test if a detector is valid for the node type */
	protected boolean validateDetector(DetectorImpl det) {
		switch(node_type) {
			case TYPE_STATION:
				return det.isMainline();
			case TYPE_ENTRANCE:
				return det.isOnRamp();
			case TYPE_EXIT:
				return det.isOffRamp();
		}
		return false;
	}

	/** Get all detectors with a matching location */
	public Detector[] getMatchingDetectors() {
		List<DetectorImpl> dets = detList.getFiltered(lookupGeoLoc());
		Iterator<DetectorImpl> it = dets.iterator();
		while(it.hasNext()) {
			DetectorImpl det = it.next();
			if(!validateDetector(det))
				it.remove();
		}
		return makeDetectorArray(dets);
	}

	/** Node detectors */
	protected transient DetectorImpl[] detectors = new DetectorImpl[0];

	/** Set the array of detectors */
	protected synchronized void _setDetectors(DetectorImpl[] dets)
		throws TMSException
	{
		Arrays.sort(dets);
		if(Arrays.equals(dets, detectors))
			return;
		mapping.update("r_node", this, dets);
		detectors = dets;
		notifyUpdate();
	}

	/** Set the array of detectors */
	public void setDetectors(Detector[] dets) throws TMSException,
		RemoteException
	{
		// Need to look up DetectorImpl objects, since RMI refs
		// cannot be cast to Impls.
		DetectorImpl[] n_dets = new DetectorImpl[dets.length];
		for(int i = 0; i < n_dets.length; i++) {
			n_dets[i] = (DetectorImpl)detList.getElement(
				dets[i].getIndex());
		}
		_setDetectors(n_dets);
	}

	/** Get an array of all node detectors */
	public Detector[] getDetectors() {
		return makeDetectorArray(detectors);
	}

	/** Get the detector array */
	public DetectorImpl[] getDetectorArray() {
		return detectors;
	}

	/** Get the detector set for the r_node */
	public DetectorSet getDetectorSet() {
		DetectorImpl[] dets = detectors;	// Avoid race
		DetectorSet set = new DetectorSet();
		for(DetectorImpl d: dets) {
			if(!d.isAbandoned())
				set.addDetector(d);
		}
		return set;
	}

	/** Does this node have the specified detector? */
	public boolean hasDetector(DetectorImpl det) {
		DetectorImpl[] dets = detectors;	// Avoid race
		for(DetectorImpl d: dets) {
			if(d == det)
				return true;
		}
		return false;
	}

	/** Create a new r_node */
	public R_NodeImpl() throws RemoteException {
		geo_loc = null;
	}

	/** Create an r_node from an ObjectVault field map */
	protected R_NodeImpl(FieldMap fields) throws RemoteException {
		// hmmmm
	}

	/** Initialize transient fields */
	public void initTransients() throws TMSException,
		RemoteException
	{
		// NOTE: must be called after detList is populated
		LinkedList<DetectorImpl> dets = new LinkedList<DetectorImpl>();
		Set s = mapping.lookup("r_node", this);
		Iterator it = s.iterator();
		while(it.hasNext()) {
			Integer det_no = (Integer)it.next();
			dets.add((DetectorImpl)detList.getElement(det_no));
		}
		detectors = makeDetectorImplArray(dets);
		String sid = getStationID();
		if(sid.length() > 0)
			statMap.add(sid, new StationImpl(sid, this));
	}

	/** Is this object deletable? */
	public boolean isDeletable() throws TMSException {
		// Cannot delete node with a Station
		if(getStationID().length() > 0)
			return false;
		return super.isDeletable();
	}

	/** Get the true UTM Northing (without offset) */
	protected Integer getTrueNorthing() {
		return GeoLocHelper.getTrueNorthing(lookupGeoLoc());
	}

	/** Get the true UTM Easting (without offset) */
	protected Integer getTrueEasting() {
		return GeoLocHelper.getTrueEasting(lookupGeoLoc());
	}

	/** Test if an other r_node is a matching entrance */
	protected boolean isMatchingEntrance(R_NodeImpl other) {
		return other.isEntrance() &&
			GeoLocHelper.rampMatches(lookupGeoLoc(),
			other.lookupGeoLoc());
	}

	/** Test if an other r_node is a matching access */
	protected boolean isMatchingAccess(R_NodeImpl other) {
		return other.isAccess() &&
			GeoLocHelper.accessMatches(lookupGeoLoc(),
			other.lookupGeoLoc());
	}

	/** Test if an other r_node links with this (exit) r_node */
	public boolean isExitLink(R_NodeImpl other) {
		return isMatchingEntrance(other) || isMatchingAccess(other);
	}

	/** Test if an other r_node links with this (access) r_node */
	public boolean isAccessLink(R_NodeImpl other) {
		return other.isEntrance() &&
			GeoLocHelper.accessMatches(lookupGeoLoc(),
			other.lookupGeoLoc());
	}

	/** Downstream roadway nodes */
	protected transient final List<R_NodeImpl> downstream =
		new LinkedList<R_NodeImpl>();

	/** Clear the downstream roadway nodes */
	public void clearDownstream() {
		downstream.clear();
	}

	/** Add a downstream roadway node */
	public void addDownstream(R_NodeImpl d) {
		downstream.add(d);
	}

	/** Get a list of the downstream nodes */
	public List<R_NodeImpl> getDownstream() {
		return downstream;
	}

	/** Get the linked corridor for an entrance or exit */
	public Corridor getLinkedCorridor() {
		return nodeMap.getCorridor(GeoLocHelper.getLinkedCorridor(
			lookupGeoLoc()));
	}

	/** Print the r_node as an XML element */
	public void printXml(PrintWriter out) {
		out.print("\t<r_node ");
		out.print("id='N" + getOID() + "' ");
		out.print("n_type='" + TYPES[node_type] + "' ");
		if(pickable)
			out.print("pickable='t' ");
		String sid = getStationID();
		if(sid.length() > 0)
			out.print("station_id='" + sid + "' ");
		GeoLoc loc = lookupGeoLoc();
		if(loc != null) {
			Road x = loc.getCrossStreet();
			if(x != null) {
				String xs = replaceEntities(x.getName());
				out.print("label='" + xs + "' ");
			}
			out.print("easting='" + getTrueEasting() + "' ");
			out.print("northing='" + getTrueNorthing() + "' ");
		}
		int l = getLanes();
		if(l != 0)
			out.print("lanes='" + l + "' ");
		boolean side = getAttachSide();
		if(side)
			out.print("attach_side='left' ");
		int s = getShift();
		if(s != 0)
			out.print("shift='" + s + "' ");
		int slim = getSpeedLimit();
		if(slim != DEFAULT_SPEED_LIMIT)
			out.print("s_limit='" + slim + "' ");
		DetectorImpl[] dets = detectors;
		if(dets.length > 0) {
			out.print("dets='");
			StringBuilder b = new StringBuilder();
			for(DetectorImpl det: dets)
				b.append("D" + det.getIndex() + " ");
			out.print(b.toString().trim() + "' ");
		}
		if(downstream.size() > 0) {
			out.print("downstream='");
			StringBuilder b = new StringBuilder();
			for(R_NodeImpl d: downstream)
				b.append("N" + d.getOID() + " ");
			out.print(b.toString().trim() + "' ");
		}
		out.println("/>");
	}

	/** Make a sorted array of detectors */
	static protected Detector[] makeDetectorArray(DetectorImpl[] dets) {
		Detector[] result = new Detector[dets.length];
		for(int i = 0; i < result.length; i++)
			result[i] = dets[i];
		Arrays.sort(result);
		return result;
	}

	/** Make a sorted array of detectors */
	static protected Detector[] makeDetectorArray(List<DetectorImpl> dets) {
		Detector[] result = new Detector[dets.size()];
		for(int i = 0; i < result.length; i++)
			result[i] = dets.get(i);
		Arrays.sort(result);
		return result;
	}

	/** Make a sorted array of detectors */
	static protected DetectorImpl[] makeDetectorImplArray(
		List<DetectorImpl> dets)
	{
		DetectorImpl[] result = new DetectorImpl[dets.size()];
		for(int i = 0; i < result.length; i++)
			result[i] = dets.get(i);
		Arrays.sort(result);
		return result;
	}
}
