/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2016  Minnesota Department of Transportation
 * Copyright (C) 2014       AHMCT, University of California
 * Copyright (C) 2016-2017  SRF Consulting Group
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.LocModifier;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.RoadClass;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.TransGeoLoc;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;

/**
 * GeoLoc contains attributes necessary to describe a map location.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 * @author Michael Janson
 * @author John L. Stanley
 */
public class GeoLocImpl extends BaseObjectImpl implements GeoLoc {

	/** Check a direction for validity */
	static private void checkDir(short d) throws TMSException {
		if (!Direction.isValid(d))
			throw new ChangeVetoException("Invalid direction");
	}

	/** Check a modifier for validity */
	static private void checkMod(short m) throws TMSException {
		if (!LocModifier.isValid(m))
			throw new ChangeVetoException("Invalid modifier");
	}

	/** Check a latitude for validity */
	static private void checkLat(Double lt) throws TMSException {
		if (lt != null && (lt < -85 || lt > 85))
			throw new ChangeVetoException("Invalid latitude");
	}

	/** Check a longitude for validity */
	static private void checkLon(Double ln) throws TMSException {
		if (ln != null && (ln < -180 || ln > 180))
			throw new ChangeVetoException("Invalid longitude");
	}

	/** Load all the geo locations */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, GeoLocImpl.class);
		store.query("SELECT name, roadway, road_dir, cross_street, " +
			"cross_dir, cross_mod, lat, lon, landmark " +
			"FROM iris." + SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GeoLocImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("roadway", roadway);
		map.put("road_dir", road_dir);
		map.put("cross_street", cross_street);
		map.put("cross_dir", cross_dir);
		map.put("cross_mod", cross_mod);
		map.put("lat", lat);
		map.put("lon", lon);
		map.put("landmark", landmark);
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

	/** Create a new geo location */
	public GeoLocImpl(String n) {
		super(n);
	}

	/** Create a new geo location */
	private GeoLocImpl(ResultSet row) throws SQLException {
		this(row.getString(1),		// name
		     row.getString(2),		// roadway
		     row.getShort(3),		// road_dir
		     row.getString(4),		// cross_street
		     row.getShort(5),		// cross_dir
		     row.getShort(6),		// cross_mod
		     (Double) row.getObject(7), // lat
		     (Double) row.getObject(8), // lon
		     row.getString(9)		// landmark
		);
	}

	/** Create a new geo location */
	private GeoLocImpl(String n, Road r, short rd, Road x, short xd,
		short xm, Double lt, Double ln, String lm)
	{
		this(n);
		roadway = r;
		road_dir = rd;
		cross_street = x;
		cross_dir = xd;
		cross_mod = xm;
		lat = lt;
		lon = ln;
		landmark = lm;
	}

	/** Create a new geo location */
	private GeoLocImpl(String n, String r, short rd, String x, short xd,
		short xm, Double lt, Double ln, String lm)
	{
		this(n, lookupRoad(r), rd, lookupRoad(x), xd, xm, lt, ln, lm);
	}

	//-----------------------------------------

	/** Given a device name, lookup the associated GeoLocImpl (if there is one)
	 * 
	 * @param devname - Name of device
	 * @return Returns the associated GeoLocImpl or null.
	 */
	public static GeoLocImpl lookupGeoLocImplForDevice(String devname) {
		GeoLoc proxy = GeoLocHelper.lookup(devname);
		if ((proxy != null) && (proxy instanceof GeoLocImpl))
			return (GeoLocImpl)proxy;
		return null;
	}
	
	/** Given a device, lookup the associated GeoLocImpl (if there is one)
	 * 
	 * @param dev - Device reference
	 * @return Returns the associated GeoLocImpl or null.
	 */
	public static GeoLocImpl lookupGeoLocImplForDevice(Device dev) {
		return lookupGeoLocImplForDevice(dev.getName());
	}

	//------------------------------
	
	/** Roadway road */
	private Road roadway;

	/** Set the roadway road */
	@Override
	public void setRoadway(Road r) {
		GateArmSystem.checkDisable(this, "roadway");
		roadway = r;
	}

	/** Set the roadway road */
	public void doSetRoadway(Road r) throws TMSException {
		if (r != roadway) {
			store.update(this, "roadway", r);
			setRoadway(r);
			notifyAttribute("roadway");
		}
	}

	/** Get the roadway location */
	@Override
	public Road getRoadway() {
		return roadway;
	}

	//------------------------------
	
	/** Roadway direction */
	private short road_dir;

	/** Set the roadway direction */
	@Override
	public void setRoadDir(short d) {
		GateArmSystem.checkDisable(this, "road_dir");
		road_dir = d;
	}

	/** Set the roadway direction */
	public void doSetRoadDir(short d) throws TMSException {
		if (d != road_dir) {
			checkDir(d);
			store.update(this, "road_dir", d);
			setRoadDir(d);
			notifyAttribute("roadDir");
		}
	}

	/** Get the roadway direction */
	@Override
	public short getRoadDir() {
		return road_dir;
	}

	//------------------------------
	
	/** Nearest cross-street */
	private Road cross_street;

	/** Set the cross-street road */
	@Override
	public void setCrossStreet(Road x) {
		cross_street = x;
	}

	/** Set the cross-street road */
	public void doSetCrossStreet(Road x) throws TMSException {
		if (x != cross_street) {
			store.update(this, "cross_street", x);
			setCrossStreet(x);
			notifyAttribute("crossStreet");
		}
	}

	/** Get the cross-street road */
	@Override
	public Road getCrossStreet() {
		return cross_street;
	}

	//------------------------------
	
	/** Cross street direction */
	private short cross_dir;

	/** Set the cross street direction */
	@Override
	public void setCrossDir(short d) {
		cross_dir = d;
	}

	/** Set the cross street direction */
	public void doSetCrossDir(short d) throws TMSException {
		if (d != cross_dir) {
			checkDir(d);
			store.update(this, "cross_dir", d);
			setCrossDir(d);
			notifyAttribute("crossDir");
		}
	}

	/** Get the cross street direction */
	@Override
	public short getCrossDir() {
		return cross_dir;
	}

	//------------------------------
	
	/** Cross street modifier */
	private short cross_mod;

	/** Set the cross street modifier */
	@Override
	public void setCrossMod(short m) {
		cross_mod = m;
	}

	/** Set the cross street modifier */
	public void doSetCrossMod(short m) throws TMSException {
		if (m != cross_mod) {
			checkMod(m);
			store.update(this, "cross_mod", m);
			setCrossMod(m);
			notifyAttribute("crossMod");
		}
	}

	/** Get the cross street modifier */
	@Override
	public short getCrossMod() {
		return cross_mod;
	}

	//------------------------------
	
	/** Latitude */
	private Double lat;

	/** Set the latitude */
	@Override
	public void setLat(Double lt) {
		lat = lt;
	}

	/** Set the latitude */
	public void doSetLat(Double lt) throws TMSException {
		if (lt != lat) {
			checkLat(lt);
			store.update(this, "lat", lt);
			setLat(lt);
			notifyAttribute("lat");
		}
	}

	/** Get the latitude */
	@Override
	public Double getLat() {
		return lat;
	}

	//------------------------------
	
	/** Longitude */
	private Double lon;

	/** Set the longitude */
	@Override
	public void setLon(Double ln) {
		lon = ln;
	}

	/** Set the longitude */
	public void doSetLon(Double ln) throws TMSException {
		if (ln != lon) {
			checkLon(ln);
			store.update(this, "lon", ln);
			setLon(ln);
			notifyAttribute("lon");
		}
	}

	/** Get the longitude */
	@Override
	public Double getLon() {
		return lon;
	}

	//------------------------------
	
	/** Landmark */
	private String landmark;

	/** Set the landmark */
	@Override
	public void setLandmark(String lm) {
		landmark = lm;
	}

	/** Set the landmark */
	public void doSetLandmark(String lm) throws TMSException {
		if (!objectEquals(lm, landmark)) {
			store.update(this, "landmark", lm);
			setLandmark(lm);
			notifyAttribute("landmark");
		}
	}

	/** Get the landmark */
	@Override
	public String getLandmark() {
		return landmark;
	}
	
	//-----------------------------------------------------------------------
	// Following code uses lat/long to calculate GIS roadway/milepost/etc. info
	// (Sections of this code were borrowed from R_NodeManager.java)
	//-----------------------------------------------------------------------
	
	/** Get the corridor containing the ramp meter */
	private Corridor getCorridor(GeoLoc geo_loc) {
		String cid = GeoLocHelper.getCorridorName(geo_loc);
		return corridors.getCorridor(cid);
	}

	/** Create a GeoLoc snapped to nearest corridor */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private GeoLoc createGeoLoc(SphericalMercatorPosition smp)
	{
		final int roadclass_CD = RoadClass.CD_ROAD.ordinal();
		GeoLoc loc = null;
		double distance = Double.POSITIVE_INFINITY;
		for (CorridorBase c: corridors.corridors.values()) {
			if (c.getRoadway().getRClass() == roadclass_CD)
				continue;
			TransGeoLoc l = createGeoLoc(c, smp);
			if ((l != null) && (l.getDistance() < distance)) {
				loc = l;
				distance = l.getDistance();
			}
		}
		return loc;
	}

	/** Create the nearest GeoLoc for the given corridor.
	 * @param c Corridor to search.
	 * @param smp Selected point (spherical mercator position).
	 * @return ClientGeoLoc snapped to corridor, or null if not found. */
	private TransGeoLoc createGeoLoc(CorridorBase<R_Node> c, SphericalMercatorPosition smp)
	{
		R_Node n0 = null;
		R_Node n1 = null;
		R_Node n_prev = null;
		double n_meters = Double.POSITIVE_INFINITY;
		for (R_Node n: c) {
			if (R_NodeHelper.isContinuityBreak(n)) {
				n_prev = null;
				continue;
			}
			if (n_prev != null) {
				double m = calcDistance(n_prev, n, smp);
				if (m < n_meters) {
					n0 = n_prev;
					n1 = n;
					n_meters = m;
				}
			}
			n_prev = n;
		}
		if (n0 != null)
			return createGeoLoc(n0, n1, smp, n_meters);
		else
			return null;
	}

	/** Calculate the distance from a point to the given line segment.
	 * @param n0 First r_node
	 * @param n1 Second (adjacent) r_node.
	 * @param smp Selected point (spherical mercator position).
	 * @return Distance (spherical mercator "meters") from segment to
	 *         selected point. */
	private double calcDistance(R_Node n0, R_Node n1,
		SphericalMercatorPosition smp)
	{
		GeoLoc l0 = n0.getGeoLoc();
		GeoLoc l1 = n1.getGeoLoc();
		return GeoLocHelper.segmentDistance(l0, l1, smp);
	}

	/** Create a GeoLoc projected onto the line between two nodes.
	 * @param n0 First node.
	 * @param n1 Second (adjacent) node.
	 * @param smp Selected point (spherical mercator position).
	 * @param d Distance (meters).
	 * @return ClientGeoLoc snapped to corridor, or null if not found. */
	private TransGeoLoc createGeoLoc(R_Node n0, R_Node n1,
		SphericalMercatorPosition smp, double dist)
	{
		GeoLoc l0 = n0.getGeoLoc();
		GeoLoc l1 = n1.getGeoLoc();
		GeoLoc pos = GeoLocHelper.snapSegment(l0, l1, smp);
		if (pos != null)
			return new TransGeoLoc(l0.getRoadway(), l0.getRoadDir(),
			                       pos.getLat().floatValue(),
			                       pos.getLon().floatValue(), dist);
		else
			return null;
	}
	
	/** Create a spherical mercator position */
	static private SphericalMercatorPosition getPosition(double latitude, double longitude) {
		Position pos = new Position(latitude, longitude);
		return SphericalMercatorPosition.convert(pos);
	}

	/** Calculate nearest roadway, direction, cross-street,
	 *  and landmark for current lat/lon location. */
	public void doCalculateGIS() {
		// Assign device to nearest roadway and direction
		// Populate milepost and cross street if available on nearest r-node
		SphericalMercatorPosition smp = getPosition(lat, lon);
		GeoLoc loc = createGeoLoc(smp);
		Corridor cb = getCorridor(loc);
		R_Node rn = cb.findNearest(loc);
		GeoLoc rn_geo_loc = rn.getGeoLoc();
		// Use one try/catch frame on the assumption that whatever
		// effects one of them will probably effect all of them...
		try {
			doSetRoadway(loc.getRoadway());
			doSetRoadDir(loc.getRoadDir());
			doSetCrossStreet(rn_geo_loc.getCrossStreet());
			doSetLandmark(rn_geo_loc.getLandmark());
		} catch (TMSException e) {
			e.printStackTrace();
		}
	}
}
