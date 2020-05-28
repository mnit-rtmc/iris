/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2020  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.LocModifier;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;

/**
 * GeoLoc contains attributes necessary to describe a map location.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 * @author Michael Janson
 * @author John L. Stanley
 */
public class GeoLocImpl extends BaseObjectImpl implements GeoLoc {

	/** Maximum distance to snap */
	static private final Distance MAX_DIST = new Distance(1, MILES);

	/** Create a spherical mercator position */
	static private SphericalMercatorPosition getPosition(double latitude,
		double longitude)
	{
		Position pos = new Position(latitude, longitude);
		return SphericalMercatorPosition.convert(pos);
	}

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
		store.query("SELECT name, notify_tag, roadway, road_dir, " +
			"cross_street, cross_dir, cross_mod, landmark, lat, " +
			"lon FROM iris." + SONAR_TYPE  + ";",
			new ResultFactory()
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
		map.put("notify_tag", notify_tag);
		map.put("roadway", roadway);
		map.put("road_dir", road_dir);
		map.put("cross_street", cross_street);
		map.put("cross_dir", cross_dir);
		map.put("cross_mod", cross_mod);
		map.put("landmark", landmark);
		map.put("lat", lat);
		map.put("lon", lon);
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
		// This constructor should only be called by sonar,
		// when a client creates an r_node.
		this(n, R_Node.SONAR_TYPE);
	}

	/** Create a new geo location */
	public GeoLocImpl(String n, String nt) {
		super(n);
		notify_tag = nt;
	}

	/** Create a new geo location */
	private GeoLocImpl(ResultSet row) throws SQLException {
		this(row.getString(1),          // name
		     row.getString(2),          // notify_tag
		     row.getString(3),          // roadway
		     row.getShort(4),           // road_dir
		     row.getString(5),          // cross_street
		     row.getShort(6),           // cross_dir
		     row.getShort(7),           // cross_mod
		     row.getString(8),          // landmark
		     (Double) row.getObject(9), // lat
		     (Double) row.getObject(10) // lon
		);
	}

	/** Create a new geo location */
	private GeoLocImpl(String n, String nt, Road r, short rd, Road x,
		short xd, short xm, String lm, Double lt, Double ln)
	{
		this(n, nt);
		roadway = r;
		road_dir = rd;
		cross_street = x;
		cross_dir = xd;
		cross_mod = xm;
		landmark = lm;
		lat = lt;
		lon = ln;
	}

	/** Create a new geo location */
	private GeoLocImpl(String n, String nt, String r, short rd, String x,
		short xd, short xm, String lm, Double lt, Double ln)
	{
		this(n, nt, lookupRoad(r), rd, lookupRoad(x), xd, xm, lm,
		     lt, ln);
	}

	/** Tag for pg_notify trigger on update */
	private String notify_tag;

	/** Roadway road */
	private Road roadway;

	/** Set the roadway road */
	@Override
	public void setRoadway(Road r) {
		GateArmSystem.checkDisable(this, "set roadway");
		roadway = r;
	}

	/** Set the roadway road */
	public void doSetRoadway(Road r) throws TMSException {
		if (r != roadway) {
			store.update(this, "roadway", r);
			setRoadway(r);
		}
	}

	/** Set the roadway and notify clients of the change */
	private void setRoadwayNotify(Road r) throws TMSException {
		doSetRoadway(r);
		notifyAttribute("roadway");
	}

	/** Get the roadway location */
	@Override
	public Road getRoadway() {
		return roadway;
	}

	/** Roadway direction */
	private short road_dir;

	/** Set the roadway direction */
	@Override
	public void setRoadDir(short d) {
		GateArmSystem.checkDisable(this, "set road_dir");
		road_dir = d;
	}

	/** Set the roadway direction */
	public void doSetRoadDir(short d) throws TMSException {
		if (d != road_dir) {
			checkDir(d);
			store.update(this, "road_dir", d);
			setRoadDir(d);
		}
	}

	/** Set the roadway direction and notify clients of the change */
	private void setRoadDirNotify(short d) throws TMSException {
		doSetRoadDir(d);
		notifyAttribute("roadDir");
	}

	/** Get the roadway direction */
	@Override
	public short getRoadDir() {
		return road_dir;
	}

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
		}
	}

	/** Set the cross-street and notify clients of the change */
	private void setCrossStreetNotify(Road x) throws TMSException {
		doSetCrossStreet(x);
		notifyAttribute("crossStreet");
	}

	/** Get the cross-street road */
	@Override
	public Road getCrossStreet() {
		return cross_street;
	}

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
		}
	}

	/** Set the cross street direction and notify clients of the change */
	private void setCrossDirNotify(short d) throws TMSException {
		doSetCrossDir(d);
		notifyAttribute("crossDir");
	}

	/** Get the cross street direction */
	@Override
	public short getCrossDir() {
		return cross_dir;
	}

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
		}
	}

	/** Set the cross-street modifier and notify clients of the change */
	private void setCrossModNotify(short m) throws TMSException {
		doSetCrossMod(m);
		notifyAttribute("crossMod");
	}

	/** Get the cross street modifier */
	@Override
	public short getCrossMod() {
		return cross_mod;
	}

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
		}
	}

	/** Set the landmark and notify clients of the change */
	private void setLandmarkNotify(String lm) throws TMSException {
		doSetLandmark(lm);
		notifyAttribute("landmark");
	}

	/** Get the landmark */
	@Override
	public String getLandmark() {
		return landmark;
	}

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
		}
	}

	/** Set the latitude and notify clients */
	public void setLatNotify(Double lt) throws TMSException {
		doSetLat(lt);
		notifyAttribute("lat");
	}

	/** Get the latitude */
	@Override
	public Double getLat() {
		return lat;
	}

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
		}
	}

	/** Set the longitude and notify clients */
	public void setLonNotify(Double lon) throws TMSException {
		doSetLon(lon);
		notifyAttribute("lon");
	}

	/** Get the longitude */
	@Override
	public Double getLon() {
		return lon;
	}

	/** Calculate nearest roadway, direction, cross-street,
	 * and landmark for current lat/lon location. */
	public void doCalculateGIS() throws TMSException {
		GeoLoc loc = findNearest();
		if (null == loc)
			throw new TMSException("Failed to snap location");
		setRoadwayNotify(loc.getRoadway());
		setRoadDirNotify(loc.getRoadDir());
		setCrossStreetNotify(loc.getCrossStreet());
		setCrossDirNotify(loc.getCrossDir());
		setCrossModNotify(loc.getCrossMod());
		setLandmarkNotify(loc.getLandmark());
	}

	/** Find the nearest geo location to current lat/lon. */
	private GeoLoc findNearest() {
		SphericalMercatorPosition smp = getPosition(lat, lon);
		GeoLoc loc = corridors.snapGeoLoc(smp, LaneType.MAINLINE,
			MAX_DIST, Direction.UNKNOWN);
		return (loc != null) ? findNearest(loc) : null;
	}

	/** Find the nearest geo location to the given location. */
	private GeoLoc findNearest(GeoLoc loc) {
		String cid = GeoLocHelper.getCorridorName(loc);
		Corridor cb = corridors.getCorridor(cid);
		if (cb != null) {
			R_Node rn = cb.findNearest(loc);
			return (rn != null) ? rn.getGeoLoc() : null;
		} else
			return null;
	}
}
