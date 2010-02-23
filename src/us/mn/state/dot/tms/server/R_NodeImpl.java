/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeTransition;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.TMSException;

/**
 * R_NodeImpl is an implementation of the R_Node interface. Each
 * object of this class represents one node on the roadway network.
 *
 * @author Douglas Lau
 */
public class R_NodeImpl extends BaseObjectImpl implements R_Node {

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

	/** Load all the r_nodes */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading r_nodes...");
		namespace.registerType(SONAR_TYPE, R_NodeImpl.class);
		store.query("SELECT name, geo_loc, node_type, pickable, " +
			"transition, lanes, attach_side, shift, station_id, " +
			"speed_limit, notes FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new R_NodeImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// geo_loc
					row.getInt(3),		// node_type
					row.getBoolean(4),	// pickable
					row.getInt(5),		// transition
					row.getInt(6),		// lanes
					row.getBoolean(7),	// attach_side
					row.getInt(8),		// shift
					row.getString(9),	// station_id
					row.getInt(10),		// speed_limit
					row.getString(11)	// notes
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("node_type", node_type.ordinal());
		map.put("pickable", pickable);
		map.put("transition", transition.ordinal());
		map.put("lanes", lanes);
		map.put("attach_side", attach_side);
		map.put("shift", shift);
		map.put("station_id", station_id);
		map.put("speed_limit", speed_limit);
		map.put("notes", notes);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new r_node */
	public R_NodeImpl(String n) {
		super(n);
		notes = "";
	}

	/** Create an r_node */
	protected R_NodeImpl(String n, GeoLocImpl loc, int typ, boolean p,
		int trn, int l, boolean as, int s, String st, int sl, String nt)
	{
		super(n);
		geo_loc = loc;
		node_type = R_NodeType.fromOrdinal(typ);
		pickable = p;
		transition = R_NodeTransition.fromOrdinal(trn);
		lanes = l;
		attach_side = as;
		shift = s;
		station_id = st;
		speed_limit = sl;
		notes = nt;
		initTransients();
	}

	/** Create an r_node */
	protected R_NodeImpl(Namespace ns, String n, String loc, int typ,
		boolean p, int trn, int l, boolean as, int s, String st, int sl,
		String nt)
	{
		this(n, (GeoLocImpl)ns.lookupObject(GeoLoc.SONAR_TYPE, loc),
			typ, p, trn, l, as, s, st, sl, nt);
	}

	/** Initialize transient fields */
	public void initTransients() {
		station = createStation(station_id);
		if(station != null) {
			try {
				namespace.addObject(station);
			}
			catch(NamespaceError e) {
				e.printStackTrace();
			}
		}
	}

	/** Node location */
	protected GeoLocImpl geo_loc;

	/** Set the location.  This is needed for creating a new phantom r_node
	 * with SONAR.  It is an error to call this method this after the
	 * r_node has been created. */
	public void setGeoLoc(GeoLoc loc) {
		assert geo_loc == null;
		geo_loc = (GeoLocImpl)loc;
	}

	/** Get the location */
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Check if the location is valid */
	public boolean hasLocation() {
		return !GeoLocHelper.isNull(geo_loc);
	}

	/** Node type */
	protected R_NodeType node_type = R_NodeType.STATION;

	/** Get the node type */
	public int getNodeType() {
		return node_type.ordinal();
	}

	/** Set the node type */
	public void setNodeType(int t) {
		node_type = R_NodeType.fromOrdinal(t);
	}

	/** Set the node type */
	public void doSetNodeType(int t) throws TMSException {
		R_NodeType nt = R_NodeType.fromOrdinal(t);
		if(nt == null)
			throw new ChangeVetoException("Bad node type: " + t);
		if(nt == node_type)
			return;
		store.update(this, "node_type", t);
		setNodeType(t);
	}

	/** Check if the r_node is a station */
	public boolean isStation() {
		return node_type == R_NodeType.STATION;
	}

	/** Check if the r_node is an entrance */
	public boolean isEntrance() {
		return node_type == R_NodeType.ENTRANCE;
	}

	/** Check if the r_node is an exit */
	public boolean isExit() {
		return node_type == R_NodeType.EXIT;
	}

	/** Check if the r_node is an access node */
	public boolean isAccess() {
		return node_type == R_NodeType.ACCESS;
	}

	/** Test if this r_node type can be linked in a corridor */
	protected boolean isCorridorType() {
		switch(node_type) {
			case STATION:
			case ENTRANCE:
			case EXIT:
			case INTERSECTION:
				return true;
			default:
				return false;
		}
	}

	/** Pickable flag */
	protected boolean pickable;

	/** Set the pickable flag */
	public void setPickable(boolean p) {
		pickable = p;
	}

	/** Set the pickable flag */
	public void doSetPickable(boolean p) throws TMSException {
		if(p == pickable)
			return;
		store.update(this, "pickable", p);
		setPickable(p);
	}

	/** Is this node pickable? */
	public boolean getPickable() {
		return pickable;
	}

	/** Transition type */
	protected R_NodeTransition transition = R_NodeTransition.NONE;

	/** Set the transition type */
	public void setTransition(int t) {
		transition = R_NodeTransition.fromOrdinal(t);
	}

	/** Set the transition type */
	public void doSetTransition(int t) throws TMSException {
		R_NodeTransition trn = R_NodeTransition.fromOrdinal(t);
		if(trn == null)
			throw new ChangeVetoException("Bad transition: " + t);
		if(trn == transition)
			return;
		store.update(this, "transition", t);
		setTransition(t);
	}

	/** Get the transition type */
	public int getTransition() {
		return transition.ordinal();
	}

	/** Check if this r_node is an exit to a common section */
	protected boolean isCommonExit() {
		return isExit() && (transition == R_NodeTransition.COMMON);
	}

	/** Check if this r_node has a link to the downstream r_node */
	protected boolean hasDownstreamLink() {
		return isCorridorType() && !isCommonExit();
	}

	/** Check if this r_node should impose a "turn" penalty */
	public boolean hasTurnPenalty() {
		return (transition == R_NodeTransition.LOOP) ||
			(transition == R_NodeTransition.LEG) ||
			(transition == R_NodeTransition.HOV) ||
			(transition == R_NodeTransition.FLYOVER);
	}

	/** Number of lanes */
	protected int lanes;

	/** Set the number of lanes */
	public void setLanes(int l) {
		lanes = l;
	}

	/** Set the number of lanes */
	public void doSetLanes(int l) throws TMSException {
		if(l == lanes)
			return;
		if(l < 0 || l > LANES_MAX)
			throw new ChangeVetoException("Bad lanes: " + l);
		store.update(this, "lanes", l);
		setLanes(l);
	}

	/** Get the number of lanes */
	public int getLanes() {
		return lanes;
	}

	/** Attach side value */
	protected boolean attach_side;

	/** Set the attach side */
	public void setAttachSide(boolean s) {
		attach_side = s;
	}

	/** Set the attach side */
	public void doSetAttachSide(boolean s) throws TMSException {
		if(s == attach_side)
			return;
		store.update(this, "attach_side", s);
		setAttachSide(s);
	}

	/** Get the attach side (true = left, false = right) */
	public boolean getAttachSide() {
		return attach_side;
	}

	/** Lane shift from corridor reference to attach side */
	protected int shift;

	/** Set the lane shift */
	public void setShift(int s) {
		shift = s;
	}

	/** Set the lane shift */
	public void doSetShift(int s) throws TMSException {
		if(s == shift)
			return;
		if(s < SHIFT_MIN || s > SHIFT_MAX)
			throw new ChangeVetoException("Bad shift: " + s);
		store.update(this, "shift", s);
		setShift(s);
	}

	/** Get the lane shift */
	public int getShift() {
		return shift;
	}

	/** Staiton ID */
	protected String station_id;

	/** Station object */
	protected StationImpl station;

	/** Set the station ID */
	public void setStationID(String s) {
		station_id = s;
	}

	/** Set the station ID */
	public void doSetStationID(String s) throws TMSException {
		if(s == station_id || (s != null && s.equals(station_id)))
			return;
		store.update(this, "station_id", s);
		StationImpl stat = createStation(s);
		updateStation(station, stat);
		setStationID(s);
	}

	/** Get the station ID */
	public String getStationID() {
		return station_id;
	}

	/** Create a station */
	protected StationImpl createStation(String sid) {
		if(sid != null)
			return new StationImpl(sid, this);
		else
			return null;
	}

	/** Update the station */
	protected void updateStation(StationImpl os, StationImpl s) {
		if(s != null)
			MainServer.server.addObject(s);
		if(os != null)
			MainServer.server.removeObject(os);
		station = s;
	}

	/** Get the station */
	public StationImpl getStation() {
		return station;
	}

	/** Speed limit */
	protected int speed_limit = DEFAULT_SPEED_LIMIT;

	/** Set the speed limit */
	public void setSpeedLimit(int l) {
		speed_limit = l;
	}

	/** Set the speed limit */
	public void doSetSpeedLimit(int l) throws TMSException {
		if(l == speed_limit)
			return;
		if(l < MINIMUM_SPEED_LIMIT || l > MAXIMUM_SPEED_LIMIT)
			throw new ChangeVetoException("Bad speed limit: " + l);
		store.update(this, "speed_limit", l);
		setSpeedLimit(l);
	}

	/** Get the speed limit */
	public int getSpeedLimit() {
		return speed_limit;
	}

	/** Administrator notes */
	protected String notes;

	/** Set the administrator notes */
	public void setNotes(String n) {
		notes = n;
	}

	/** Set the administrator notes */
	public void doSetNotes(String n) throws TMSException {
		if(n.equals(notes))
			return;
		store.update(this, "notes", n);
		setNotes(n);
	}

	/** Get the administrator notes */
	public String getNotes() {
		return notes;
	}

	/** Node detectors */
	protected transient DetectorSet detectors = new DetectorSet();

	/** Add a detector to the r_node */
	public void addDetector(DetectorImpl det) {
		detectors.addDetector(det);
	}

	/** Remove a detector from the r_node */
	public void removeDetector(DetectorImpl det) {
		detectors.removeDetector(det);
	}

	/** Get an array of all node detectors */
	public DetectorImpl[] getDetectors() {
		return detectors.toArray();
	}

	/** Get the (active) detector set for the r_node */
	public DetectorSet getDetectorSet() {
		DetectorSet set = new DetectorSet();
		for(DetectorImpl d: detectors.toArray()) {
			if(!d.getAbandoned())
				set.addDetector(d);
		}
		return set;
	}

	/** Does this node have the specified detector? */
	public boolean hasDetector(DetectorImpl det) {
		for(DetectorImpl d: detectors.toArray()) {
			if(d == det)
				return true;
		}
		return false;
	}

	/** Get the true UTM Northing (without offset) */
	protected Integer getTrueNorthing() {
		return GeoLocHelper.getTrueNorthing(geo_loc);
	}

	/** Get the true UTM Easting (without offset) */
	protected Integer getTrueEasting() {
		return GeoLocHelper.getTrueEasting(geo_loc);
	}

	/** Test if an other r_node is a matching entrance */
	protected boolean isMatchingEntrance(R_NodeImpl other) {
		return other.isEntrance() &&
			GeoLocHelper.rampMatches(geo_loc, other.getGeoLoc());
	}

	/** Test if an other r_node is a matching access */
	protected boolean isMatchingAccess(R_NodeImpl other) {
		return other.isAccess() &&
			GeoLocHelper.accessMatches(geo_loc, other.getGeoLoc());
	}

	/** Test if an other r_node links with this (exit) r_node */
	public boolean isExitLink(R_NodeImpl other) {
		return isMatchingEntrance(other) || isMatchingAccess(other);
	}

	/** Test if an other r_node links with this (access) r_node */
	public boolean isAccessLink(R_NodeImpl other) {
		return other.isEntrance() &&
			GeoLocHelper.accessMatches(geo_loc, other.getGeoLoc());
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
		if(corridors != null) {
			String c = GeoLocHelper.getLinkedCorridor(geo_loc);
			return corridors.getCorridor(c);
		} else
			return null;
	}

	/** Print the r_node as an XML element */
	public void printXml(PrintWriter out) {
		out.print("\t<r_node ");
		out.print("id='N" + getName() + "' ");
		out.print("n_type='" + node_type.description + "' ");
		if(pickable)
			out.print("pickable='t' ");
		out.print("transition='" + transition.description + "' ");
		String sid = station_id;
		if(sid != null)
			out.print("station_id='" + sid + "' ");
		GeoLoc loc = geo_loc;
		if(loc != null) {
			Road x = loc.getCrossStreet();
			if(x != null) {
				String xs = XmlWriter.validateElementValue(
					x.getName());
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
		DetectorImpl[] dets = detectors.toArray();
		if(dets.length > 0) {
			out.print("dets='");
			StringBuilder b = new StringBuilder();
			for(DetectorImpl det: dets)
				b.append("D" + det.getName() + " ");
			out.print(b.toString().trim() + "' ");
		}
		if(downstream.size() > 0) {
			out.print("downstream='");
			StringBuilder b = new StringBuilder();
			for(R_NodeImpl d: downstream)
				b.append("N" + d.getName() + " ");
			out.print(b.toString().trim() + "' ");
		}
		out.println("/>");
	}
}
