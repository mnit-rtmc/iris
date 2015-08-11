/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.StationHelper;
import us.mn.state.dot.tms.TollZone;
import us.mn.state.dot.tms.TMSException;

/**
 * A toll zone is a roadway segment which is tolled by usage.
 *
 * @author Douglas Lau
 */
public class TollZoneImpl extends BaseObjectImpl implements TollZone {

	/** Toll zone debug log */
	static private final DebugLog TOLL_LOG = new DebugLog("toll");

	/** Maximum number of time steps needed for sample history */
	static private final int MAX_STEPS = 12;

	/** Magic constant to convert density to price dollars */
	static private final double ALPHA = 0.05932;

	/** Magic exponent to convert density to price dollars */
	static private final double BETA = 1.156;

	/** Load all the toll zones */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, TollZoneImpl.class);
		store.query("SELECT name, start_id, end_id " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new TollZoneImpl(
					row.getString(1),	// name
					row.getString(2),	// start_id
					row.getString(3)	// end_id
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("start_id", start_id);
		map.put("end_id", end_id);
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

	/** Create a new toll zone */
	public TollZoneImpl(String n) {
		super(n);
	}

	/** Create a new toll zone */
	protected TollZoneImpl(String n, String sid, String eid) {
		this(n);
		start_id = sid;
		end_id = eid;
	}

	/** Starting station ID */
	private String start_id;

	/** Set the starting station ID */
	@Override
	public void setStartID(String sid) {
		start_id = sid;
	}

	/** Set the starting station ID */
	public void doSetStartID(String sid) throws TMSException {
		if (!stringEquals(sid, start_id)) {
			store.update(this, "start_id", sid);
			setStartID(sid);
		}
	}

	/** Get the starting station ID */
	@Override
	public String getStartID() {
		return start_id;
	}

	/** Ending station ID */
	private String end_id;

	/** Set the ending station ID */
	@Override
	public void setEndID(String eid) {
		end_id = eid;
	}

	/** Set the ending station ID */
	public void doSetEndID(String eid) throws TMSException {
		if (!stringEquals(eid, end_id)) {
			store.update(this, "end_id", eid);
			setEndID(eid);
		}
	}

	/** Get the ending station ID */
	@Override
	public String getEndID() {
		return end_id;
	}

	/** Density history (vehicles / mile) */
	private transient final BoundedSampleHistory k_hist =
		new BoundedSampleHistory(MAX_STEPS);

	/** Update density */
	public void updateDensity() {
		k_hist.push(calculateMaxDensity());
	}

	/** Calculate the current maximum zone density */
	private Double calculateMaxDensity() {
		GeoLoc o = StationHelper.lookupGeoLoc(start_id);
		GeoLoc d = StationHelper.lookupGeoLoc(end_id);
		if (o == null) {
			if (isLogging())
				log("Invalid zone start: " + start_id);
			return null;
		} else if (d == null) {
			if (isLogging())
				log("Invalid zone end: " + end_id);
			return null;
		} else
			return calculateMaxDensity(buildRoute(o, d));
	}

	/** Calculate the current maximum zone density */
	private Double calculateMaxDensity(Route r) {
		if (r != null) {
			DetectorSet ds = r.getDetectorSet(LaneType.HOT);
			Double max_k = ds.getMaxDensity();
			if (isLogging())
				log("Det: " + ds + ", k: " + max_k);
			return max_k;
		} else {
			if (isLogging()) {
				log("No route from " + start_id + " to " +
				    end_id);
			}
			return null;
		}
	}

	/** Build a route from an origin to a destination */
	private Route buildRoute(GeoLoc o, GeoLoc d) {
		RouteBuilder builder = new RouteBuilder(TOLL_LOG, name,
			BaseObjectImpl.corridors);
		return builder.findBestRoute(o, d);
	}

	/** Calculate the pricing */
	public void calculatePricing() {
		Double k_hot = k_hist.average();
		if (k_hot != null) {
			/* This was arrived at by using a least squares fit */
			double price = ALPHA * Math.pow(k_hot, BETA);
			int quarters = (int)Math.round(price * 4);
			float rprice = quarters / 4.0f;
			if (isLogging()) {
				log("k_hot: " + k_hot + ", price: " + price +
				    ", $:" + rprice);
			}
		}
	}

	/** Check if we're logging */
	private boolean isLogging() {
		return TOLL_LOG.isOpen();
	}

	/** Log a toll zone message */
	private void log(String m) {
		TOLL_LOG.log(name + ": " + m);
	}
}
