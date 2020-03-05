/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2018  Minnesota Department of Transportation
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
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.StationHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
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

	/** Get density "alpha" coefficient */
	static private float defaultAlpha() {
		return SystemAttrEnum.TOLL_DENSITY_ALPHA.getFloat();
	}

	/** Get density "beta" coefficient (exponent) */
	static private float defaultBeta() {
		return SystemAttrEnum.TOLL_DENSITY_BETA.getFloat();
	}

	/** Get minimum tolling price */
	static public float min_price() {
		return SystemAttrEnum.TOLL_MIN_PRICE.getFloat();
	}

	/** Get default maximum tolling price (dollars) */
	static public float max_price() {
		return SystemAttrEnum.TOLL_MAX_PRICE.getFloat();
	}

	/** Round price to nearest $0.25 */
	static private float nearestQuarter(double price) {
		int quarters = (int) Math.round(price * 4);
		return quarters / 4.0f;
	}

	/** Load all the toll zones */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, TollZoneImpl.class);
		store.query("SELECT name, start_id, end_id, tollway, alpha, " +
			"beta, max_price FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new TollZoneImpl(row));
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
		map.put("tollway", tollway);
		map.put("alpha", alpha);
		map.put("beta", beta);
		map.put("max_price", max_price);
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

	/** Create a toll zone */
	private TollZoneImpl(ResultSet row) throws SQLException {
		this(row.getString(1),          // name
		     row.getString(2),          // start_id
		     row.getString(3),          // end_id
		     row.getString(4),          // tollway
		     (Float) row.getObject(5),  // alpha
		     (Float) row.getObject(6),  // beta
		     (Float) row.getObject(7)   // max_price
		);
	}

	/** Create a toll zone */
	private TollZoneImpl(String n, String sid, String eid, String tw,
		Float a, Float b, Float mp)
	{
		this(n);
		start_id = sid;
		end_id = eid;
		tollway = tw;
		alpha = a;
		beta = b;
		max_price = mp;
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
		if (!objectEquals(sid, start_id)) {
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
		if (!objectEquals(eid, end_id)) {
			store.update(this, "end_id", eid);
			setEndID(eid);
		}
	}

	/** Get the ending station ID */
	@Override
	public String getEndID() {
		return end_id;
	}

	/** Tollway ID */
	private String tollway;

	/** Set the tollway ID */
	@Override
	public void setTollway(String tw) {
		tollway = tw;
	}

	/** Set the tollway ID */
	public void doSetTollway(String tw) throws TMSException {
		if (!objectEquals(tw, tollway)) {
			store.update(this, "tollway", tw);
			setTollway(tw);
		}
	}

	/** Get the tollway ID */
	@Override
	public String getTollway() {
		return tollway;
	}

	/** Density alpha coefficient */
	private Float alpha;

	/** Set the density alpha coefficient */
	@Override
	public void setAlpha(Float a) {
		alpha = a;
	}

	/** Set the density alpha coefficient */
	public void doSetAlpha(Float a) throws TMSException {
		if (!objectEquals(a, alpha)) {
			store.update(this, "alpha", a);
			setAlpha(a);
		}
	}

	/** Get the density alpha coefficient */
	@Override
	public Float getAlpha() {
		return alpha;
	}

	/** Get density alpha coefficient (or default) */
	private float getAlphaOrDefault() {
		Float a = getAlpha();
		return (a != null) ? a : defaultAlpha();
	}

	/** Density beta coefficient */
	private Float beta;

	/** Set the density beta coefficient */
	@Override
	public void setBeta(Float b) {
		beta = b;
	}

	/** Set the density beta coefficient */
	public void doSetBeta(Float b) throws TMSException {
		if (!objectEquals(b, beta)) {
			store.update(this, "beta", b);
			setBeta(b);
		}
	}

	/** Get the density beta coefficient */
	@Override
	public Float getBeta() {
		return beta;
	}

	/** Get density beta coefficient (or default) */
	private float getBetaOrDefault() {
		Float b = getBeta();
		return (b != null) ? b : defaultBeta();
	}

	/** Max price (dollars) */
	private Float max_price;

	/** Set the max price (dollars) */
	@Override
	public void setMaxPrice(Float p) {
		max_price = p;
	}

	/** Set the max price (dollars) */
	public void doSetMaxPrice(Float p) throws TMSException {
		if (!objectEquals(p, max_price)) {
			store.update(this, "max_price", p);
			setMaxPrice(p);
		}
	}

	/** Get the max price (dollars) */
	@Override
	public Float getMaxPrice() {
		return max_price;
	}

	/** Density history for one detector */
	static private class DensityHist {

		/** Density history for 6 minutes */
		private final BoundedSampleHistory hist =
			new BoundedSampleHistory(MAX_STEPS);

		/** Current density */
		private Double density;

		/** Update the density history.
		 * @param np New period.
		 * @param k Current density. */
		void updateDensity(boolean np, double k) {
			hist.push((k >= 0) ? k : null);
			if (np)
				density = hist.average();
		}
	}

	/** Mapping of density history for all vehicle samplers */
	private transient final HashMap<VehicleSampler, DensityHist> k_hist =
		new HashMap<VehicleSampler, DensityHist>();

	/** Lookup all HOT detectors in a route.
	 * @param r The route.
	 * @return Set of all HOT detectors in the route. */
	private SamplerSet lookupDetectors(Route r) {
		return (r != null)
		      ? r.getSamplerSet(LaneType.HOT)
		      : new SamplerSet();
	}

	/** Build the route for the whole toll zone */
	private Route buildRoute() {
		GeoLoc o = StationHelper.lookupGeoLoc(start_id);
		if (o != null)
			return buildRoute(o);
		else {
			if (isLogging())
				log("Invalid zone start: " + start_id);
			return null;
		}
	}

	/** Build the route from an origin.
	 * @param o Origin geo location.
	 * @return Route from origin to end of zone, or null */
	private Route buildRoute(GeoLoc o) {
		GeoLoc d = StationHelper.lookupGeoLoc(end_id);
		if (d != null)
			return buildRoute(o, d);
		else {
			if (isLogging())
				log("Invalid zone end: " + end_id);
			return null;
		}
	}

	/** Build a route from an origin to a destination.
	 * @param o Origin geo location.
	 * @param d Destination geo location.
	 * @return Route from origin to destination, or null */
	private Route buildRoute(GeoLoc o, GeoLoc d) {
		long st = TimeSteward.currentTimeMillis();
		RouteFinder rf = new RouteFinder(BaseObjectImpl.corridors);
		Route r = rf.findRoute(o, d);
		if (isLogging()) {
			long e = TimeSteward.currentTimeMillis() - st;
			log("ROUTE TO " + end_id + strNot(r) + "FOUND: " + e);
		}
		return r;
	}

	/** Get a debugging "NOT" string */
	static private String strNot(Route r) {
		return (r != null) ? " " : " NOT ";
	}

	/** Update density.
	 * @param np New pricing period (if true). */
	public synchronized void updateDensity(boolean np) {
		updateDensityHistory();
		for (Map.Entry<VehicleSampler,DensityHist> e:k_hist.entrySet()){
			double k = e.getKey().getDensity();
			e.getValue().updateDensity(np, k);
		}
	}

	/** Update density history for all detectors in the toll zone */
	private void updateDensityHistory() {
		SamplerSet ss = lookupDetectors(buildRoute());
		if (isLogging())
			log("all detectors: " + ss);
		removeHistoryMappings(ss);
		addHistoryMappings(ss);
	}

	/** Remove mappings from k_hist if not in sampler set */
	private void removeHistoryMappings(SamplerSet ss) {
		Iterator<VehicleSampler> it = k_hist.keySet().iterator();
		while (it.hasNext()) {
			if (!ss.contains(it.next()))
				it.remove();
		}
	}

	/** Add mappings from sampler set if they don't exist */
	private void addHistoryMappings(SamplerSet ss) {
		for (VehicleSampler vs: ss.getAll()) {
			if (!k_hist.containsKey(vs))
				k_hist.put(vs, new DensityHist());
		}
	}

	/** Find the max density sampler.
	 * @param lbl Sign label for logging.
	 * @param o Origin (location of DMS).
	 * @return VehicleSampler with maximum density. */
	public VehicleSampler findMaxDensity(String lbl, GeoLoc o) {
		SamplerSet ss = lookupDetectors(buildRoute(o));
		if (isLogging())
			log(lbl + " use detectors: " + ss);
		VehicleSampler sampler = findMaxDensity(ss);
		if (isLogging())
			log(lbl + " max density @ " + sampler);
		return sampler;
	}

	/** Find the max density sampler within a sampler set */
	private synchronized VehicleSampler findMaxDensity(SamplerSet ss) {
		Double k_max = null;
		VehicleSampler sampler = null;
		for (Map.Entry<VehicleSampler, DensityHist> e:
		     k_hist.entrySet())
		{
			VehicleSampler vs = e.getKey();
			if (ss.contains(vs)) {
				Double k = e.getValue().density;
				if (k_max == null || (k != null && k > k_max)) {
					k_max = k;
					sampler = vs;
				}
			}
		}
		return sampler;
	}

	/** Get the density for one vehicle sampler */
	private synchronized Double getDensity(VehicleSampler vs) {
		DensityHist hist = k_hist.get(vs);
		return (hist != null) ? hist.density : null;
	}

	/** Get the current toll zone price.
	 * @param sampler Vehicle sampler with max density.
	 * @param lbl Sign label for logging.
	 * @param o Origin (location of DMS).
	 * @return Price (dollars). */
	public Float getPrice(VehicleSampler sampler, String lbl, GeoLoc o) {
		Double k_hot = getDensity(sampler);
		Float price = (k_hot != null) ? calculatePricing(k_hot) : null;
		if (isLogging())
			log(lbl + " k_hot: " + k_hot + ", price: $" + price);
		return price;
	}

	/** Calculate the toll pricing.
	 * @param k_hot Maximum density in toll zone.
	 * @return Price (dollars), rounded to nearest $0.25. */
	private float calculatePricing(double k_hot) {
		float a = getAlphaOrDefault();
		float b = getBetaOrDefault();
		double price = a * Math.pow(k_hot, b);
		return Math.max(nearestQuarter(price), min_price());
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
