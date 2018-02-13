/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ParkingArea;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.MINUTES;

/**
 * A parking area can report the number of available stalls.
 *
 * @author Douglas Lau
 */
public class ParkingAreaImpl extends BaseObjectImpl implements ParkingArea {

	/** Sample period (30 seconds) */
	static private final Interval SAMPLE_PERIOD = new Interval(30);

	/** History period (30 minutes) */
	static private final Interval HIST_PERIOD = new Interval(30, MINUTES);

	/** Time steps needed for history */
	static private final int HIST_STEPS =
		(int) SAMPLE_PERIOD.per(HIST_PERIOD);

	/** Format available parking spaces */
	static private String formatAvailable(int a, Integer low, Boolean op) {
		return (op != null && op) ? formatAvailable(a, low) : "CLOSED";
	}

	/** Format available parking spaces */
	static private String formatAvailable(int a, Integer low) {
		return (null == low || a > low) ? Integer.toString(a) : "LOW";
	}

	/** Calculate available space trend.
	 * @param a Current available spaces.
	 * @param p Previous available spaces.
	 * @param i Previous interval number.
	 * @param cap Total capacity. */
	static private String calculateTrend(int a, int p, int i, int cap) {
		assert cap > 0;
		float d = (a - p) / cap;
		float r = d * (i + 1) / HIST_STEPS;
		if (r >= 0.045f)
			return "CLEARING";
		else if (r <= -0.045f)
			return "FILLING";
		else
			return "STEADY";
	}

	/** Determine whether parking data should be trusted */
	static private boolean shouldTrust(int t, int cap) {
		// At least 75% of spaces must be reporting
		int min_trust = cap - (cap / 4);
		return (t >= min_trust) && (t <= cap);
	}

	/** Load all the parking areas */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, ParkingAreaImpl.class);
		store.query("SELECT name, geo_loc, preset_1, preset_2, " +
			"preset_3, site_id, time_stamp_static, " +
			"relevant_highway, reference_post, exit_id, " +
			"facility_name, street_adr, city, state, zip, " +
			"time_zone, ownership, capacity, low_threshold, " +
			"amenities, time_stamp, reported_available, " +
			"true_available, trend, open, trust_data, " +
			"last_verification_check, verification_check_amplitude"+
			" FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new ParkingAreaImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("preset_1", preset_1);
		map.put("preset_2", preset_2);
		map.put("preset_3", preset_3);
		map.put("site_id", site_id);
		map.put("time_stamp_static", asTimestamp(time_stamp_static));
		map.put("relevant_highway", relevant_highway);
		map.put("reference_post", reference_post);
		map.put("exit_id", exit_id);
		map.put("facility_name", facility_name);
		map.put("street_adr", street_adr);
		map.put("city", city);
		map.put("state", state);
		map.put("zip", zip);
		map.put("time_zone", time_zone);
		map.put("ownership", ownership);
		map.put("capacity", capacity);
		map.put("low_threshold", low_threshold);
		map.put("amenities", amenities);
		map.put("time_stamp", asTimestamp(time_stamp));
		map.put("reported_available", reported_available);
		map.put("true_available", true_available);
		map.put("trend", trend);
		map.put("open", open);
		map.put("trust_data", trust_data);
		map.put("last_verification_check", asTimestamp(
			last_verification_check));
		map.put("verification_check_amplitude",
			verification_check_amplitude);
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

	/** Create a new parking area */
	public ParkingAreaImpl(String n) throws SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		g.notifyCreate();
		geo_loc = g;
	}

	/** Create a parking area */
	private ParkingAreaImpl(ResultSet row) throws SQLException {
		this(row.getString(1),	        // name
		     row.getString(2),          // geo_loc
		     row.getString(3),          // preset_1
		     row.getString(4),          // preset_2
		     row.getString(5),          // preset_3
		     row.getString(6),          // site_id
		     row.getTimestamp(7),       // time_stamp_static
		     row.getString(8),          // relevant_highway
		     row.getString(9),          // reference_post
		     row.getString(10),         // exit_id
		     row.getString(11),         // facility_name
		     row.getString(12),         // street_adr
		     row.getString(13),         // city
		     row.getString(14),         // state
		     row.getString(15),         // zip
		     row.getString(16),         // time_zone
		     row.getString(17),         // ownership
		     (Integer) row.getObject(18),// capacity
		     (Integer) row.getObject(19),// low_threshold
		     row.getString(20),         // amenities
		     row.getTimestamp(21),      // time_stamp
		     row.getString(22),         // reported_available
		     (Integer) row.getObject(23),// true_available
		     row.getString(24),         // trend
		     (Boolean) row.getObject(25),// open
		     (Boolean) row.getObject(26),// trust_data
		     row.getTimestamp(27),      // last_verification_check
		     (Integer) row.getObject(28)// verification_check_amplitude
		);
	}

	/** Create a parking area */
	private ParkingAreaImpl(String n, String loc, String p1, String p2,
		String p3, String sid, Date tss, String rh, String rp,
		String xid, String fn, String adr, String c, String st,
		String z, String tz, String ow, Integer cap, Integer lt,
		String a, Date ts, String ra, Integer ta, String t, Boolean op,
		Boolean td, Date lvc, Integer vca)
	{
		super(n);
		geo_loc = lookupGeoLoc(loc);
		setPreset1(lookupPreset(p1));
		setPreset2(lookupPreset(p2));
		setPreset3(lookupPreset(p3));
		site_id = sid;
		time_stamp_static = stampMillis(tss);
		relevant_highway = rh;
		reference_post = rp;
		exit_id = xid;
		facility_name = fn;
		street_adr = adr;
		city = c;
		state = st;
		zip = z;
		time_zone = tz;
		ownership = ow;
		capacity = cap;
		low_threshold = lt;
		amenities = a;
		time_stamp = stampMillis(ts);
		reported_available = ra;
		true_available = ta;
		trend = t;
		open = op;
		trust_data = td;
		last_verification_check = stampMillis(lvc);
		verification_check_amplitude = vca;
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		geo_loc.notifyRemove();
	}

	/** Location */
	private GeoLocImpl geo_loc;

	/** Get the location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Camera preset 1 */
	private CameraPresetImpl preset_1;

	/** Set the verification camera preset 1 */
	@Override
	public void setPreset1(CameraPreset cp) {
		CameraPresetImpl op = preset_1;
		preset_1 = (cp instanceof CameraPresetImpl)
		         ? (CameraPresetImpl) cp
		         : null;
		assignPreset(op, preset_1);
	}

	/** Set the verification camera preset 1 */
	public void doSetPreset1(CameraPreset cp) throws TMSException {
		if (cp != preset_1) {
			store.update(this, "preset_1", cp);
			setPreset1(cp);
		}
	}

	/** Get verification camera preset 1 */
	@Override
	public CameraPreset getPreset1() {
		return preset_1;
	}

	/** Camera preset 2 */
	private CameraPresetImpl preset_2;

	/** Set the verification camera preset 2 */
	@Override
	public void setPreset2(CameraPreset cp) {
		CameraPresetImpl op = preset_2;
		preset_2 = (cp instanceof CameraPresetImpl)
		         ? (CameraPresetImpl) cp
		         : null;
		assignPreset(op, preset_2);
	}

	/** Set the verification camera preset 2 */
	public void doSetPreset2(CameraPreset cp) throws TMSException {
		if (cp != preset_2) {
			store.update(this, "preset_2", cp);
			setPreset2(cp);
		}
	}

	/** Get verification camera preset 2 */
	@Override
	public CameraPreset getPreset2() {
		return preset_2;
	}

	/** Camera preset 3 */
	private CameraPresetImpl preset_3;

	/** Set the verification camera preset 3 */
	@Override
	public void setPreset3(CameraPreset cp) {
		CameraPresetImpl op = preset_3;
		preset_3 = (cp instanceof CameraPresetImpl)
		         ? (CameraPresetImpl) cp
		         : null;
		assignPreset(op, preset_3);
	}

	/** Set the verification camera preset 3 */
	public void doSetPreset3(CameraPreset cp) throws TMSException {
		if (cp != preset_3) {
			store.update(this, "preset_3", cp);
			setPreset3(cp);
		}
	}

	/** Get verification camera preset 3 */
	@Override
	public CameraPreset getPreset3() {
		return preset_3;
	}

	/** Site ID */
	private String site_id;

	/** Set the site ID */
	@Override
	public void setSiteId(String sid) {
		site_id = sid;
	}

	/** Set the site ID */
	public void doSetSiteId(String sid) throws TMSException {
		if (!objectEquals(sid, site_id)) {
			store.update(this, "site_id", sid);
			updateTimeStampStatic();
			setSiteId(sid);
		}
	}

	/** Get the site ID */
	@Override
	public String getSiteId() {
		return site_id;
	}

	/** Time stamp for static updates */
	private Long time_stamp_static;

	/** Update the static time stamp */
	private void updateTimeStampStatic() throws TMSException {
		long st = TimeSteward.currentTimeMillis();
		store.update(this, "time_stamp_static", asTimestamp(st));
		time_stamp_static = st;
	}

	/** Relevant highway */
	private String relevant_highway;

	/** Set the relevant highway */
	@Override
	public void setRelevantHighway(String h) {
		relevant_highway = h;
	}

	/** Set the relevant highway */
	public void doSetRelevantHighway(String h) throws TMSException {
		if (!objectEquals(h, relevant_highway)) {
			store.update(this, "relevant_highway", h);
			updateTimeStampStatic();
			setRelevantHighway(h);
		}
	}

	/** Get the relevant highway */
	@Override
	public String getRelevantHighway() {
		return relevant_highway;
	}

	/** Reference post */
	private String reference_post;

	/** Set the reference post */
	@Override
	public void setReferencePost(String p) {
		reference_post = p;
	}

	/** Set the reference post */
	public void doSetReferencePost(String p) throws TMSException {
		if (!objectEquals(p, reference_post)) {
			store.update(this, "reference_post", p);
			updateTimeStampStatic();
			setReferencePost(p);
		}
	}

	/** Get the reference post */
	@Override
	public String getReferencePost() {
		return reference_post;
	}

	/** Exit ID */
	private String exit_id;

	/** Set the exit ID */
	@Override
	public void setExitId(String x) {
		exit_id = x;
	}

	/** Set the exit ID */
	public void doSetExitId(String x) throws TMSException {
		if (!objectEquals(x, exit_id)) {
			store.update(this, "exit_id", x);
			updateTimeStampStatic();
			setExitId(x);
		}
	}

	/** Get the exit ID */
	@Override
	public String getExitId() {
		return exit_id;
	}

	/** Name of facility */
	private String facility_name;

	/** Set the facility name */
	@Override
	public void setFacilityName(String n) {
		facility_name = n;
	}

	/** Set the facility name */
	public void doSetFacilityName(String n) throws TMSException {
		if (!objectEquals(n, facility_name)) {
			store.update(this, "facility_name", n);
			updateTimeStampStatic();
			setFacilityName(n);
		}
	}

	/** Get the facility name */
	@Override
	public String getFacilityName() {
		return facility_name;
	}

	/** Street address */
	private String street_adr;

	/** Set the street address */
	@Override
	public void setStreetAdr(String a) {
		street_adr = a;
	}

	/** Set the street address */
	public void doSetStreetAdr(String a) throws TMSException {
		if (!objectEquals(a, street_adr)) {
			store.update(this, "street_adr", a);
			updateTimeStampStatic();
			setStreetAdr(a);
		}
	}

	/** Get the street address */
	@Override
	public String getStreetAdr() {
		return street_adr;
	}

	/** City */
	private String city;

	/** Set the city */
	@Override
	public void setCity(String c) {
		city = c;
	}

	/** Set the city */
	public void doSetCity(String c) throws TMSException {
		if (!objectEquals(c, city)) {
			store.update(this, "city", c);
			updateTimeStampStatic();
			setCity(c);
		}
	}

	/** Get the city */
	@Override
	public String getCity() {
		return city;
	}

	/** State (2-char code) */
	private String state;

	/** Set the state */
	@Override
	public void setState(String s) {
		state = s;
	}

	/** Set the state */
	public void doSetState(String s) throws TMSException {
		if (!objectEquals(s, state)) {
			store.update(this, "state", s);
			updateTimeStampStatic();
			setState(s);
		}
	}

	/** Get the state */
	@Override
	public String getState() {
		return state;
	}

	/** Zip code */
	private String zip;

	/** Set the zip code */
	@Override
	public void setZip(String z) {
		zip = z;
	}

	/** Set the zip code */
	public void doSetZip(String z) throws TMSException {
		if (!objectEquals(z, zip)) {
			store.update(this, "zip", z);
			updateTimeStampStatic();
			setZip(z);
		}
	}

	/** Get the zip code */
	@Override
	public String getZip() {
		return zip;
	}

	/** Time zone */
	private String time_zone;

	/** Set the time zone */
	@Override
	public void setTimeZone(String tz) {
		time_zone = tz;
	}

	/** Set the time zone */
	public void doSetTimeZone(String tz) throws TMSException {
		if (!objectEquals(tz, time_zone)) {
			store.update(this, "time_zone", tz);
			updateTimeStampStatic();
			setTimeZone(tz);
		}
	}

	/** Get the time zone */
	@Override
	public String getTimeZone() {
		return time_zone;
	}

	/** Ownership (PU / PR) */
	private String ownership;

	/** Set the ownership */
	@Override
	public void setOwnership(String o) {
		ownership = o;
	}

	/** Set the ownership */
	public void doSetOwnership(String o) throws TMSException {
		if (!objectEquals(o, ownership)) {
			store.update(this, "ownership", o);
			updateTimeStampStatic();
			setOwnership(o);
		}
	}

	/** Get the ownership */
	@Override
	public String getOwnership() {
		return ownership;
	}

	/** Capacity (number of parking spaces) */
	private Integer capacity;

	/** Set the capacity */
	@Override
	public void setCapacity(Integer c) {
		capacity = c;
	}

	/** Set the capacity */
	public void doSetCapacity(Integer c) throws TMSException {
		if (!objectEquals(c, capacity)) {
			store.update(this, "capacity", c);
			updateTimeStampStatic();
			setCapacity(c);
		}
	}

	/** Get the capacity */
	@Override
	public Integer getCapacity() {
		return capacity;
	}

	/** Low threshold (number of parking spaces) */
	private Integer low_threshold;

	/** Set the low threshold */
	@Override
	public void setLowThreshold(Integer t) {
		low_threshold = t;
	}

	/** Set the low threshold */
	public void doSetLowThreshold(Integer t) throws TMSException {
		if (!objectEquals(t, low_threshold)) {
			store.update(this, "low_threshold", t);
			updateTimeStampStatic();
			setLowThreshold(t);
		}
	}

	/** Get the low threshold */
	@Override
	public Integer getLowThreshold() {
		return low_threshold;
	}

	/** Amenities (vending machine, etc.) */
	private String amenities;

	/** Set the amenities */
	@Override
	public void setAmenities(String a) {
		amenities = a;
	}

	/** Set the amenities */
	public void doSetAmenities(String a) throws TMSException {
		if (!objectEquals(a, amenities)) {
			store.update(this, "amenities", a);
			updateTimeStampStatic();
			setAmenities(a);
		}
	}

	/** Get the amenities */
	@Override
	public String getAmenities() {
		return amenities;
	}

	/** Time stamp of last report */
	private Long time_stamp;

	/** Update the time stamp */
	private void updateTimeStamp() throws TMSException {
		long st = TimeSteward.currentTimeMillis();
		store.update(this, "time_stamp", asTimestamp(st));
		time_stamp = st;
	}

	/** Update the available parking spaces */
	public void updateAvailable() throws TMSException {
		Corridor c = corridors.getCorridor(geo_loc);
		if (c != null)
			updateAvailable(c);
		else {
			setReportedAvailableNotify(null);
			setTrueAvailableNotify(null);
			setTrendNotify(null);
			setTrustDataNotify(null);
		}
	}

	/** Update the available parking spaces */
	private void updateAvailable(Corridor c) throws TMSException {
		int t = 0;
		int a = 0;
		Iterator<R_NodeImpl> it = c.iterator();
		while (it.hasNext()) {
			R_NodeImpl n = it.next();
			Boolean p = n.getParkingAvailable();
			if (p != null) {
				t++;
				if (p)
					a++;
			}
		}
		updateAvailable(a, t);
	}

	/** Update the available parking spaces */
	private void updateAvailable(int a, int t) throws TMSException {
		String ra = formatAvailable(a, low_threshold, open);
		setReportedAvailableNotify(ra);
		setTrueAvailableNotify(a);
		setTrendNotify(calculateTrend(a));
		Integer cap = capacity;
		setTrustDataNotify((cap != null) && shouldTrust(t, cap));
	}

	/** Reported available parking spaces (or LOW) */
	private String reported_available;

	/** Set the reported available parking spaces */
	private void setReportedAvailableNotify(String a) throws TMSException {
		if (!objectEquals(a, reported_available)) {
			store.update(this, "reported_available", a);
			updateTimeStamp();
			reported_available = a;
			notifyAttribute("reportedAvailable");
		}
	}

	/** Get the reported available parking spaces */
	@Override
	public String getReportedAvailable() {
		return reported_available;
	}

	/** Available space history for 30 minutes */
	private transient final BoundedSampleHistory hist =
		new BoundedSampleHistory(HIST_STEPS);

	/** Calculate available space trend */
	private String calculateTrend(int a) {
		int i = getOldestSample();
		if (i > 0) {
			Double p = hist.get(i);
			Integer cap = capacity;
			if (p != null && cap != null && cap > 0) {
				double pr = p;
				return calculateTrend(a, (int) pr, i, cap);
			}
		}
		return null;
	}

	/** Get the oldest sample number in history */
	private int getOldestSample() {
		for (int i = HIST_STEPS - 1; i > 0; i--) {
			Double a = hist.get(i);
			if (a != null)
				return i;
		}
		return 0;
	}

	/** Calculated number of available parking spaces */
	private Integer true_available;

	/** Set the number of available parking spaces */
	private void setTrueAvailableNotify(Integer a) throws TMSException {
		hist.push((a != null) ? (double) a : null);
		if (!objectEquals(a, true_available)) {
			store.update(this, "true_available", a);
			updateTimeStamp();
			true_available = a;
			notifyAttribute("trueAvailable");
		}
	}

	/** Get the true available parking spaces */
	@Override
	public Integer getTrueAvailable() {
		return true_available;
	}

	/** Trend description */
	private String trend;

	/** Set the trend */
	private void setTrendNotify(String t) throws TMSException {
		if (!objectEquals(t, trend)) {
			store.update(this, "trend", t);
			updateTimeStamp();
			trend = t;
			notifyAttribute("trend");
		}
	}

	/** Get the trend (CLEARING, STEADY, FILLING) */
	@Override
	public String getTrend() {
		return trend;
	}

	/** Flag for open facility */
	private Boolean open;

	/** Set the open status */
	@Override
	public void setOpen(Boolean o) {
		open = o;
	}

	/** Set the open status */
	public void doSetOpen(Boolean o) throws TMSException {
		if (!objectEquals(o, open)) {
			store.update(this, "open", o);
			updateTimeStamp();
			setOpen(o);
		}
	}

	/** Get the open status */
	@Override
	public Boolean getOpen() {
		return open;
	}

	/** Flag for trustworthy data */
	private Boolean trust_data;

	/** Set the trustworthy data flag */
	private void setTrustDataNotify(Boolean t) throws TMSException {
		if (!objectEquals(t, trust_data)) {
			store.update(this, "trust_data", t);
			updateTimeStamp();
			trust_data = t;
			notifyAttribute("trustData");
		}
	}

	/** Get the trust data value */
	@Override
	public Boolean getTrustData() {
		return trust_data;
	}

	/** Time stamp of last manual verification */
	private Long last_verification_check;

	/** Last verification check adjustment */
	private Integer verification_check_amplitude;

	/** Set the verified available parking spaces */
	@Override
	public void setVerifiedAvailable(int a) {
		// FIXME
	}
}
