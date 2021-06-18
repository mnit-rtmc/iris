/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2021  Minnesota Department of Transportation
 * Copyright (C) 2015  Iteris Inc.
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

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.R_NodeTransition;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.SystemAttrEnum; 
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.geo.Position;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;
import us.mn.state.dot.tms.units.Distance;

/**
 * R_NodeImpl is an implementation of the R_Node interface.  Each
 * object of this class represents one node on the roadway network.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class R_NodeImpl extends BaseObjectImpl implements R_Node {

	/** Check for a valid station ID */
	static private boolean isStationIdValid(String s) {
		if (s.startsWith("ST"))
			return parseId(s.substring(2)) > 0;
		else if (s.startsWith("S"))
			return parseId(s.substring(1)) > 0;
		else
			return false;
	}

	/** Parse a station ID number */
	static private int parseId(String v) {
		try {
			return Integer.parseInt(v);
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}

	/** Get the minimum roadway speed limit */
	static public int getMinSpeedLimit() {
		return SystemAttrEnum.SPEED_LIMIT_MIN_MPH.getInt();
	}

	/** Get the default roadway speed limit */
	static public int getDefaultSpeedLimit() {
		return SystemAttrEnum.SPEED_LIMIT_DEFAULT_MPH.getInt();
	}

	/** Get the maximum roadway speed limit */
	static public int getMaxSpeedLimit() {
		return SystemAttrEnum.SPEED_LIMIT_MAX_MPH.getInt();
	}

	/** Load all the r_nodes */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, R_NodeImpl.class);
		store.query("SELECT name, geo_loc, node_type, pickable, " +
			"above, transition, lanes, attach_side, shift, " +
			"active, station_id, speed_limit, notes FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new R_NodeImpl(
					row.getString(1),   // name
					row.getString(2),   // geo_loc
					row.getInt(3),      // node_type
					row.getBoolean(4),  // pickable
					row.getBoolean(5),  // above
					row.getInt(6),      // transition
					row.getInt(7),      // lanes
					row.getBoolean(8),  // attach_side
					row.getInt(9),      // shift
					row.getBoolean(10), // active
					row.getString(11),  // station_id
					row.getInt(12),     // speed_limit
					row.getString(13)   // notes
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("node_type", node_type.ordinal());
		map.put("pickable", pickable);
		map.put("above", above);
		map.put("transition", transition.ordinal());
		map.put("lanes", lanes);
		map.put("attach_side", attach_side);
		map.put("shift", shift);
		map.put("active", active);
		map.put("station_id", station_id);
		map.put("speed_limit", speed_limit);
		map.put("notes", notes);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new r_node */
	public R_NodeImpl(String n) {
		super(n);
		notes = "";
		active = true;
	}

	/** Create an r_node */
	private R_NodeImpl(String n, GeoLocImpl loc, int typ, boolean p,
		boolean a, int trn, int l, boolean as, int s, boolean act,
		String st, int sl, String nt)
	{
		super(n);
		geo_loc = loc;
		node_type = R_NodeType.fromOrdinal(typ);
		pickable = p;
		above = a;
		transition = R_NodeTransition.fromOrdinal(trn);
		lanes = l;
		attach_side = as;
		shift = s;
		active = act;
		station_id = st;
		speed_limit = sl;
		notes = nt;
		initTransients();
	}

	/** Create an r_node */
	private R_NodeImpl(String n, String loc, int typ, boolean p, boolean a,
		int trn, int l, boolean as, int s, boolean act, String st,
		int sl, String nt)
	{
		this(n, lookupGeoLoc(loc), typ, p, a, trn, l, as, s, act, st,
		     sl, nt);
	}

	/** Initialize transient fields */
	@Override
	public void initTransients() {
		station = createStation(station_id);
		if (station != null) {
			try {
				namespace.addObject(station);
			}
			catch (NamespaceError e) {
				e.printStackTrace();
			}
		}
		fork = null;
	}

	/** Destroy an r_node */
	@Override
	public void doDestroy() throws TMSException {
		updateStation(station, null);
		super.doDestroy();
	}

	/** Node location */
	private GeoLocImpl geo_loc;

	/** Set the location.  This is needed for creating a new phantom r_node
	 * with SONAR.  It is an error to call this method this after the
	 * r_node has been created. */
	@Override
	public void setGeoLoc(GeoLoc loc) {
		assert geo_loc == null;
		geo_loc = (GeoLocImpl) loc;
	}

	/** Get the location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Node type */
	private R_NodeType node_type = R_NodeType.STATION;

	/** Get the node type */
	@Override
	public int getNodeType() {
		return node_type.ordinal();
	}

	/** Set the node type */
	@Override
	public void setNodeType(int t) {
		node_type = R_NodeType.fromOrdinal(t);
	}

	/** Set the node type */
	public void doSetNodeType(int t) throws TMSException {
		R_NodeType nt = R_NodeType.fromOrdinal(t);
		if (nt == null)
			throw new ChangeVetoException("Bad node type: " + t);
		if (nt == node_type)
			return;
		store.update(this, "node_type", t);
		setNodeType(t);
	}

	/** Check if the r_node is an exit */
	public boolean isExit() {
		return node_type == R_NodeType.EXIT;
	}

	/** Pickable flag */
	private boolean pickable;

	/** Set the pickable flag */
	@Override
	public void setPickable(boolean p) {
		pickable = p;
	}

	/** Set the pickable flag */
	public void doSetPickable(boolean p) throws TMSException {
		if (p != pickable) {
			store.update(this, "pickable", p);
			setPickable(p);
		}
	}

	/** Is this node pickable? */
	@Override
	public boolean getPickable() {
		return pickable;
	}

	/** Above flag */
	private boolean above;

	/** Set the above flag */
	@Override
	public void setAbove(boolean a) {
		above = a;
	}

	/** Set the above flag */
	public void doSetAbove(boolean a) throws TMSException {
		if (a != above) {
			store.update(this, "above", a);
			setAbove(a);
		}
	}

	/** Is this node above? */
	@Override
	public boolean getAbove() {
		return above;
	}

	/** Transition type */
	private R_NodeTransition transition = R_NodeTransition.NONE;

	/** Set the transition type */
	@Override
	public void setTransition(int t) {
		transition = R_NodeTransition.fromOrdinal(t);
	}

	/** Set the transition type */
	public void doSetTransition(int t) throws TMSException {
		R_NodeTransition trn = R_NodeTransition.fromOrdinal(t);
		if (trn == null)
			throw new ChangeVetoException("Bad transition: " + t);
		if (trn == transition)
			return;
		store.update(this, "transition", t);
		setTransition(t);
	}

	/** Get the transition type */
	@Override
	public int getTransition() {
		return transition.ordinal();
	}

	/** Check if this r_node is an exit to a common section */
	public boolean isCommonExit() {
		return isExit() && (transition == R_NodeTransition.COMMON);
	}

	/** Check if this r_node should impose a "turn" penalty */
	public boolean hasTurnPenalty() {
		switch (transition) {
		case LOOP:
		case LEG:
		case HOV:
		case FLYOVER:
			return true;
		default:
			return false;
		}
	}

	/** Number of lanes */
	private int lanes;

	/** Set the number of lanes */
	@Override
	public void setLanes(int l) {
		lanes = l;
	}

	/** Set the number of lanes */
	public void doSetLanes(int l) throws TMSException {
		if (l == lanes)
			return;
		if (l < 0 || l > MAX_LANES)
			throw new ChangeVetoException("Bad lanes: " + l);
		store.update(this, "lanes", l);
		setLanes(l);
	}

	/** Get the number of lanes */
	@Override
	public int getLanes() {
		return lanes;
	}

	/** Attach side value */
	private boolean attach_side;

	/** Set the attach side */
	@Override
	public void setAttachSide(boolean s) {
		attach_side = s;
	}

	/** Set the attach side */
	public void doSetAttachSide(boolean s) throws TMSException {
		if (s != attach_side) {
			store.update(this, "attach_side", s);
			setAttachSide(s);
		}
	}

	/** Get the attach side (true = left, false = right) */
	@Override
	public boolean getAttachSide() {
		return attach_side;
	}

	/** Lane shift from corridor reference to attach side */
	private int shift = MIN_SHIFT;

	/** Set the lane shift */
	@Override
	public void setShift(int s) {
		shift = s;
	}

	/** Set the lane shift */
	public void doSetShift(int s) throws TMSException {
		if (s == shift)
			return;
		if (s < MIN_SHIFT || s > MAX_SHIFT)
			throw new ChangeVetoException("Bad shift: " + s);
		store.update(this, "shift", s);
		setShift(s);
	}

	/** Get the lane shift */
	@Override
	public int getShift() {
		return shift;
	}

	/** Active state */
	private boolean active;

	/** Set the active state */
	@Override
	public void setActive(boolean a) {
		active = a;
	}

	/** Set the active state */
	public void doSetActive(boolean a) throws TMSException {
		if (a != active) {
			store.update(this, "active", a);
			setActive(a);
		}
	}

	/** Get the active state */
	@Override
	public boolean getActive() {
		return active;
	}

	/** Staiton ID */
	private String station_id;

	/** Station object */
	private StationImpl station;

	/** Set the station ID */
	@Override
	public void setStationID(String s) {
		station_id = s;
	}

	/** Set the station ID */
	public void doSetStationID(String s) throws TMSException {
		if (objectEquals(s, station_id))
			return;
		if (s != null && !isStationIdValid(s))
			throw new ChangeVetoException("Format: S1, S100, ...");
		store.update(this, "station_id", s);
		StationImpl stat = createStation(s);
		updateStation(station, stat);
		setStationID(s);
	}

	/** Get the station ID */
	@Override
	public String getStationID() {
		return station_id;
	}

	/** Create a station */
	private StationImpl createStation(String sid) {
		return (sid != null) ? new StationImpl(sid, this) : null;
	}

	/** Update the station */
	private void updateStation(StationImpl os, StationImpl s) {
		if (s != null)
			MainServer.server.addObject(s);
		if (os != null)
			os.notifyRemove();
		station = s;
	}

	/** Get the station */
	public StationImpl getStation() {
		return station;
	}

	/** Speed limit */
	private int speed_limit = getDefaultSpeedLimit();

	/** Set the speed limit */
	@Override
	public void setSpeedLimit(int l) {
		speed_limit = l;
	}

	/** Set the speed limit */
	public void doSetSpeedLimit(int l) throws TMSException {
		if (l == speed_limit)
			return;
		if (l < getMinSpeedLimit()) {
			throw new ChangeVetoException("Specified speed " +
				"limit (" + l + ") is less than minimum");
		} else if (l > getMaxSpeedLimit()) {
			throw new ChangeVetoException("Specified speed " +
				"limit (" + l + ") is greater than maximum");
		}
		store.update(this, "speed_limit", l);
		setSpeedLimit(l);
	}

	/** Get the speed limit, which is bounded by a minimum and maximum */
	@Override
	public int getSpeedLimit() {
		return boundedValue(getMinSpeedLimit(), speed_limit, 
			getMaxSpeedLimit());
	}

	/** Return an integer bounded by a minimum and maximum value */
	private int boundedValue(int min, int x, int max) {
		if (x < min)
			return min;
		else if (x > max)
			return max;
		else
			return x;
	}

	/** Administrator notes */
	private String notes;

	/** Set the administrator notes */
	@Override
	public void setNotes(String n) {
		notes = n;
	}

	/** Set the administrator notes */
	public void doSetNotes(String n) throws TMSException {
		if (!n.equals(notes)) {
			store.update(this, "notes", n);
			setNotes(n);
		}
	}

	/** Get the administrator notes */
	@Override
	public String getNotes() {
		return notes;
	}

	/** Node detectors (ordered by lane number) */
	private transient DetectorSet detectors = new DetectorSet();

	/** Add a detector to the r_node */
	public void addDetector(DetectorImpl det) {
		synchronized (detectors) {
			detectors.addDetector(det);
		}
	}

	/** Remove a detector from the r_node */
	public void removeDetector(DetectorImpl det) {
		synchronized (detectors) {
			detectors.removeDetector(det);
		}
	}

	/** Get an array of all node detectors */
	public DetectorImpl[] getDetectors() {
		synchronized (detectors) {
			return detectors.toArray();
		}
	}

	/** Get the sampler set */
	public SamplerSet getSamplerSet() {
		synchronized (detectors) {
			return detectors.getSamplerSet();
		}
	}

	/** Fork node to branch to other corridor */
	private transient R_NodeImpl fork;

	/** Update exit fork (branch to other corridor) */
	public void updateFork() {
		fork = isExit() ? findFork() : null;
	}

	/** Find fork node (branch to other corridor) */
	private R_NodeImpl findFork() {
		// FIXME: use CorridorBase.findFork
		R_NodeImpl nearest = null;
		Distance d = new Distance(0);
		Iterator<R_Node> it = R_NodeHelper.iterator();
		while (it.hasNext()) {
			R_Node n = it.next();
			if (isExitLink(n)) {
				Distance m = Corridor.nodeDistance(this, n);
				if ((m != null) &&
				    ((null == nearest) || m.m() < d.m()))
				{
					nearest = (R_NodeImpl) n;
					d = m;
				}
			}
		}
		return nearest;
	}

	/** Test if an exit node links with a matching entrance node.
	 * @param n Node to check.
	 * @return true If nodes should link. */
	private boolean isExitLink(R_Node n) {
		return (n instanceof R_NodeImpl)
		    && R_NodeHelper.isEntrance(n)
		    && GeoLocHelper.rampMatches(geo_loc, n.getGeoLoc());
	}

	/** Get the fork node */
	public R_NodeImpl getFork() {
		return fork;
	}

	/** Write the r_node as an XML element */
	public void writeXml(Writer w, Map<String, RampMeterImpl> m_nodes)
		throws IOException
	{
		w.write("  <r_node");
		w.write(createAttribute("name", name));
		if (node_type != R_NodeType.STATION)
			w.write(" n_type='" + node_type.description + "'");
		if (pickable)
			w.write(" pickable='t'");
		if (above)
			w.write(" above='t'");
		if (transition != R_NodeTransition.NONE)
			w.write(" transition='" + transition.description+"'");
		String sid = station_id;
		if (sid != null)
			w.write(createAttribute("station_id", sid));
		GeoLoc loc = geo_loc;
		if (loc != null) {
			String lbl = GeoLocHelper.getCrossLandmark(loc);
			if (lbl != null && lbl.length() > 0)
				w.write(createAttribute("label", lbl));
			Position pos = GeoLocHelper.getWgs84Position(loc);
			if (pos != null) {
				w.write(createAttribute("lon",
					formatDouble(pos.getLongitude())));
				w.write(createAttribute("lat",
					formatDouble(pos.getLatitude())));
			}
		}
		int l = getLanes();
		if (l != 0)
			w.write(" lanes='" + l + "'");
		if (getAttachSide())
			w.write(" attach_side='left'");
		int s = getShift();
		if (s != 0)
			w.write(" shift='" + s + "'");
		if (!getActive())
			w.write(" active='f'");
		int slim = getSpeedLimit();
		if (slim != getDefaultSpeedLimit())
			w.write(" s_limit='" + slim + "'");
		R_NodeImpl f = getFork();
		if (f != null)
			w.write(" forks='" + f.getName() + "'");
		DetectorImpl[] dets = getDetectors();
		if (dets.length > 0 || m_nodes.containsKey(name)) {
			w.write(">\n");
			for (DetectorImpl det: dets) {
				w.write("    ");
				det.writeXmlElement(w);
			}
			if (m_nodes.containsKey(name)) {
				RampMeterImpl meter = m_nodes.get(name);
				w.write("    ");
				meter.writeXml(w);
			}
			w.write("  </r_node>\n");
		} else
			w.write("/>\n");
	}
}
