/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2026  Minnesota Department of Transportation
 * Copyright (C) 2011  Berkeley Transportation Systems Inc.
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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.CommConfig;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.LaneCode;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.VehLengthClass;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.HOUR;
import static us.mn.state.dot.tms.units.Interval.Units.MILLISECONDS;
import static us.mn.state.dot.tms.units.Interval.Units.SECONDS;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.FEET;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;
import us.mn.state.dot.tms.server.event.DetAutoFailEvent;

/**
 * Detector for traffic data sampling
 *
 * @author Douglas Lau
 */
public class DetectorImpl extends DeviceImpl implements Detector,VehicleSampler{

	/** Reversible lane name */
	static private final String REV = "I-394 Rev";

	/** Is detector auto-fail enabled? */
	static private boolean isDetectorAutoFailEnabled() {
		return SystemAttrEnum.DETECTOR_AUTO_FAIL_ENABLE.getBoolean();
	}

	/** Get the occupancy spike duration threshold */
	static private int getOccSpikeSecs() {
		return SystemAttrEnum.DETECTOR_OCC_SPIKE_SECS.getInt();
	}

	/** Default average detector field length (feet) */
	static private final float DEFAULT_FIELD_FT = 22.0f;

	/** Valid density threshold for speed calculation */
	static private final float DENSITY_THRESHOLD = 1.2f;

	/** Auto fail counter */
	static private class AutoFailCounter {

		/** Seconds required with state to trigger failure */
		private final int trigger_threshold_sec;

		/** Seconds required without state to clear failure */
		private final int clear_threshold_sec;

		/** Failure checking state */
		private boolean failed;

		/** Number of seconds in current state */
		private int state_sec;

		/** Failure triggered state */
		private boolean triggered;

		/** Number of seconds since last logging of state */
		private int logging_sec;

		/** Create a new auto fail counter */
		private AutoFailCounter(Interval t, Interval c) {
			trigger_threshold_sec = t.round(SECONDS);
			clear_threshold_sec = c.round(SECONDS);
			failed = false;
			state_sec = 0;
			triggered = false;
			logging_sec = 0;
		}
		/** Create a new auto fail counter */
		private AutoFailCounter() {
			this(new Interval(0), new Interval(0));
		}
		/** Update the fail/trigger states.
		 * @param s Number of seconds since last update.
		 * @param st Fail state. */
		private void updateState(int s, boolean st) {
			if (st != failed) {
				state_sec = 0;
				failed = st;
			}
			state_sec += s;
			triggered = isEnabled() && checkTriggered();
		}
		/** Check if the fail counter is enabled */
		private boolean isEnabled() {
			return trigger_threshold_sec > 0;
		}
		/** Check the triggered state */
		private boolean checkTriggered() {
			return (failed)
			      ? checkTriggeredNow()
			      : checkTriggerHang();
		}
		/** Check if the fail status is currently triggered */
		private boolean checkTriggeredNow() {
			return triggered || state_sec > trigger_threshold_sec;
		}
		/** Check if the fail status is hung in triggered state */
		private boolean checkTriggerHang() {
			return triggered && state_sec < clear_threshold_sec;
		}
		/** Check if a trigger event should be logged */
		private boolean checkLogging(int s) {
			if (triggered) {
				boolean first = (logging_sec == 0);
				logging_sec += s;
				boolean log = first || checkLoggingThreshold();
				if (log)
					logging_sec = s;
				return log;
			} else {
				logging_sec = 0;
				return false;
			}
		}
		/** Check if the logging threshold time has elapsed */
		private boolean checkLoggingThreshold() {
			return logging_sec >= LOG_THRESHOLD.round(SECONDS);
		}
	}

	/** Logging threshold */
	static private final Interval LOG_THRESHOLD =
		new Interval(1, Interval.Units.HOURS);

	/** Vehicle count "chatter" threshold */
	static private final Interval CHATTER_THRESHOLD =
		new Interval(30, SECONDS);

	/** Clear threshold */
	static private final Interval CLEAR_THRESHOLD =
		new Interval(24, Interval.Units.HOURS);

	/** Fast clear threshold */
	static private final Interval FAST_CLEAR_THRESHOLD =
		new Interval(30, SECONDS);

	/** Occupancy spike clear threshold */
	static private final Interval OCC_SPIKE_CLEAR_THRESHOLD =
		new Interval(1, Interval.Units.HOURS);

	/** Maximum "realistic" vehicle count for a 30-second period */
	static private final int MAX_VEH_COUNT_30 = 37;

	/** Maximum occupancy value (100%) */
	static private final int MAX_OCCUPANCY = 100;

	/** Scan time (ms) */
	static private final float SCAN_MS = 1000f / 60f;

	/** Change in occupancy to indicate a spike */
	static private final int OCC_SPIKE = OccupancySample.MAX / 4;

	/** Binning period for detectors (seconds) */
	static private final int BIN_PERIOD_SEC = 30;

	/** Binning period for detectors (ms) */
	static public final int BIN_PERIOD_MS =
		(int) new Interval(BIN_PERIOD_SEC).ms();

	/** Calculate the end time of previous period (ms) */
	static public long calculateEndTime(int per_ms) {
		long stamp = TimeSteward.currentTimeMillis();
		long p = per_ms;
		return stamp / p * p;
	}

	/** Load all the detectors */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, controller, pin, r_node, lane_code, "+
			"lane_number, abandoned, force_fail, auto_fail, " +
			"field_length, fake, notes FROM iris." + SONAR_TYPE +
			";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DetectorImpl(row));
			}
		});
		initAllTransients();
	}

	/** Initialize transients for all detectors.  This needs to happen after
	 * all detectors are loaded (for resolving fake detectors). */
	static private void initAllTransients() {
		Iterator<Detector> it = DetectorHelper.iterator();
		while (it.hasNext()) {
			Detector d = it.next();
			if (d instanceof DetectorImpl)
				((DetectorImpl) d).initTransients();
		}
	}

	/** Update auto_fail for all detectors. */
	static public void updateAutoFailAll() {
		Iterator<Detector> it = DetectorHelper.iterator();
		while (it.hasNext()) {
			Detector d = it.next();
			if (d instanceof DetectorImpl)
				((DetectorImpl) d).updateAutoFail();
		}
	}

	/** Create a fake detector object */
	static private FakeDetector createFakeDetector(String f)
		throws ChangeVetoException
	{
		try {
			return (f != null) ? new FakeDetector(f) : null;
		}
		catch (NumberFormatException e) {
			throw new ChangeVetoException(
				"Invalid detector number");
		}
		catch (IndexOutOfBoundsException e) {
			throw new ChangeVetoException(
				"Bad detector #:" + e.getMessage());
		}
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		if (controller != null)
			map.put("controller", controller.getName());
		map.put("pin", pin);
		if (r_node != null)
			map.put("r_node", r_node.getName());
		map.put("lane_code", lane_code);
		map.put("lane_number", lane_number);
		map.put("abandoned", abandoned);
		map.put("force_fail", force_fail);
		map.put("auto_fail", auto_fail);
		map.put("field_length", field_length);
		map.put("fake", fake);
		map.put("notes", notes);
		return map;
	}

	/** Create a new detector */
	public DetectorImpl(String n) throws TMSException, SonarException {
		super(n);
		veh_cache = new PeriodicSampleCache(
			PeriodicSampleType.VEH_COUNT);
		scn_cache = new PeriodicSampleCache(PeriodicSampleType.SCAN);
		spd_cache = new PeriodicSampleCache(PeriodicSampleType.SPEED);
		mc_count_cache = new PeriodicSampleCache(
			PeriodicSampleType.MOTORCYCLE);
		s_count_cache = new PeriodicSampleCache(
			PeriodicSampleType.SHORT);
		m_count_cache = new PeriodicSampleCache(
			PeriodicSampleType.MEDIUM);
		l_count_cache = new PeriodicSampleCache(
			PeriodicSampleType.LONG);
		v_log = new VehicleEventLog(n);
		initTransients();
	}

	/** Create a detector */
	private DetectorImpl(ResultSet row) throws SQLException, TMSException {
		this(row.getString(1),  // name
		     row.getString(2),  // controller
		     row.getInt(3),     // pin
		     row.getString(4),  // r_node
		     row.getString(5),  // lane_code
		     row.getShort(6),   // lane_number
		     row.getBoolean(7), // abandoned
		     row.getBoolean(8), // force_fail
		     row.getBoolean(9), // auto_fail
		     row.getFloat(10),  // field_length
		     row.getString(11), // fake
		     row.getString(12)  // notes
		);
	}

	/** Create a detector */
	private DetectorImpl(String n, String c, int p, String r, String lc,
		short ln, boolean a, boolean ff, boolean af, float fl, String f,
		String nt)
	{
		this(n, lookupController(c), p, lookupR_Node(r), lc, ln, a, ff,
		     af, fl, f, nt);
	}

	/** Create a detector */
	private DetectorImpl(String n, ControllerImpl c, int p, R_NodeImpl r,
		String lc, short ln, boolean a, boolean ff, boolean af,
		float fl, String f, String nt)
	{
		super(n, c, p, nt);
		r_node = r;
		lane_code = lc;
		lane_number = ln;
		abandoned = a;
		force_fail = ff;
		auto_fail = af;
		field_length = fl;
		fake = f;
		veh_cache = new PeriodicSampleCache(
			PeriodicSampleType.VEH_COUNT);
		scn_cache = new PeriodicSampleCache(PeriodicSampleType.SCAN);
		spd_cache = new PeriodicSampleCache(PeriodicSampleType.SPEED);
		mc_count_cache = new PeriodicSampleCache(
			PeriodicSampleType.MOTORCYCLE);
		s_count_cache = new PeriodicSampleCache(
			PeriodicSampleType.SHORT);
		m_count_cache = new PeriodicSampleCache(
			PeriodicSampleType.MEDIUM);
		l_count_cache = new PeriodicSampleCache(
			PeriodicSampleType.LONG);
		v_log = new VehicleEventLog(n);
	}

	/** Initialize the transient state */
	@Override
	public void initTransients() {
		super.initTransients();
		resetAutoFailCounters();
		if (r_node != null)
			r_node.addDetector(this);
		try {
			fake_det = createFakeDetector(fake);
		}
		catch (ChangeVetoException e) {
			logError("Invalid FAKE Detector (" + fake + ")");
			fake = null;
		}
	}

	/** Auto fail counter for no hits (flow) */
	private transient AutoFailCounter no_hits = new AutoFailCounter();

	/** Auto fail counter for chattering */
	private transient AutoFailCounter chatter = new AutoFailCounter();

	/** Auto fail counter for locked on (scans) */
	private transient AutoFailCounter locked_on = new AutoFailCounter();

	/** Auto fail counter for no change (scans) */
	private transient AutoFailCounter no_change = new AutoFailCounter();

	/** Auto fail counter for occupancy spikes (scans) */
	private transient AutoFailCounter occ_spike = new AutoFailCounter();

	/** Reset auto fail counters */
	private void resetAutoFailCounters() {
		no_hits = new AutoFailCounter(getNoHitThreshold(),
			FAST_CLEAR_THRESHOLD);
		chatter = new AutoFailCounter(getChatterThreshold(),
			CLEAR_THRESHOLD);
		locked_on = new AutoFailCounter(getLockedOnThreshold(),
			CLEAR_THRESHOLD);
		no_change = new AutoFailCounter(getNoChangeThreshold(),
			FAST_CLEAR_THRESHOLD);
		occ_spike = new AutoFailCounter(getOccSpikeTriggerThreshold(),
			OCC_SPIKE_CLEAR_THRESHOLD);
		updateAutoFail();
	}

	/** Get the vehicle count "no hit" threshold */
	private Interval getNoHitThreshold() {
		if (isRamp()) {
			GeoLoc loc = lookupGeoLoc();
			if (loc != null && isReversibleLocationHack(loc))
				return new Interval(72, Interval.Units.HOURS);
		}
		return LaneCode.fromCode(lane_code).no_hit_threshold;
	}

	/** Get the vehicle count "chatter" threshold */
	private Interval getChatterThreshold() {
		return CHATTER_THRESHOLD;
	}

	/** Get the scan "locked on" threshold */
	private Interval getLockedOnThreshold() {
		return LaneCode.fromCode(lane_code).getLockedOnThreshold();
	}

	/** Get the scan "no change" threshold */
	private Interval getNoChangeThreshold() {
		return LaneCode.fromCode(lane_code).getNoChangeThreshold();
	}

	/** Get the scan "occ spike" trigger threshold */
	private Interval getOccSpikeTriggerThreshold() {
		return LaneCode.fromCode(lane_code).getOccSpikeThreshold();
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		if (r_node != null)
			r_node.removeDetector(this);
	}

	/** R_Node (roadway network node) */
	private R_NodeImpl r_node;

	/** Set the r_node (roadway network node) */
	@Override
	public void setR_Node(R_Node n) {
		R_NodeImpl orn = r_node;
		if (orn != null)
			orn.removeDetector(this);
		if (n instanceof R_NodeImpl) {
			R_NodeImpl rn = (R_NodeImpl) n;
			rn.addDetector(this);
			r_node = rn;
		} else
			r_node = null;
	}

	/** Set the r_node (roadway network node) */
	public void doSetR_Node(R_Node n) throws TMSException {
		if (n != r_node) {
			store.update(this, "r_node", n);
			setR_Node(n);
		}
	}

	/** Get the r_node (roadway network node) */
	@Override
	public R_Node getR_Node() {
		return r_node;
	}

	/** Lookup the geo location */
	public GeoLoc lookupGeoLoc() {
		R_Node rn = r_node;
		return (rn != null) ? rn.getGeoLoc() : null;
	}

	/** Lane code */
	private String lane_code = LaneCode.MAINLINE.lcode;

	/** Set the lane code */
	@Override
	public void setLaneCode(String lc) {
		lane_code = lc;
		resetAutoFailCounters();
	}

	/** Set the lane code */
	public void doSetLaneCode(String lc) throws TMSException {
		if (!objectEquals(lc, lane_code)) {
			store.update(this, "lane_code", lc);
			setLaneCode(lc);
		}
	}

	/** Get the lane code */
	@Override
	public String getLaneCode() {
		return lane_code;
	}

	/** Is this a mailline detector? (auxiliary, cd, etc.) */
	public boolean isMainline() {
		return LaneCode.fromCode(lane_code).isMainline();
	}

	/** Is this a station detector? (mainline, non-HOV) */
	public boolean isStation() {
		return LaneCode.fromCode(lane_code).isStation();
	}

	/** Is this a station or CD detector? */
	public boolean isStationOrCD() {
		return LaneCode.fromCode(lane_code).isStationOrCD();
	}

	/** Is this a ramp detector? (merge, queue, exit, bypass) */
	public boolean isRamp() {
		return LaneCode.fromCode(lane_code).isRamp();
	}

	/** Is this a velocity detector? */
	public boolean isVelocity() {
		return LaneCode.fromCode(lane_code).isVelocity();
	}

	/** Test if the given detector is a speed pair with this detector */
	public boolean isSpeedPair(ControllerIO io) {
		if (io instanceof DetectorImpl) {
			DetectorImpl d = (DetectorImpl) io;
			GeoLoc loc = lookupGeoLoc();
			GeoLoc oloc = d.lookupGeoLoc();
			if (loc != null && oloc != null) {
				return GeoLocHelper.matches(loc, oloc) &&
				       lane_number == d.lane_number &&
				       !d.isVelocity() && d.isMainline();
			}
		}
		return false;
	}

	/** Lane number */
	private short lane_number;

	/** Set the lane number */
	@Override
	public void setLaneNumber(short l) {
		lane_number = l;
	}

	/** Set the lane number */
	public void doSetLaneNumber(short l) throws TMSException {
		if (l != lane_number) {
			store.update(this, "lane_number", l);
			setLaneNumber(l);
		}
	}

	/** Get the lane number */
	@Override
	public short getLaneNumber() {
		return lane_number;
	}

	/** Abandoned status flag */
	private boolean abandoned;

	/** Set the abandoned status */
	@Override
	public void setAbandoned(boolean a) {
		abandoned = a;
	}

	/** Set the abandoned status */
	public void doSetAbandoned(boolean a) throws TMSException {
		if (a != abandoned) {
			store.update(this, "abandoned", a);
			setAbandoned(a);
		}
		resetAutoFailCounters();
	}

	/** Get the abandoned status */
	@Override
	public boolean getAbandoned() {
		return abandoned;
	}

	/** Force Fail status flag */
	private boolean force_fail;

	/** Set the Force Fail status */
	@Override
	public void setForceFail(boolean f) {
		force_fail = f;
	}

	/** Set the Force Fail status */
	public void doSetForceFail(boolean f) throws TMSException {
		if (f != force_fail) {
			store.update(this, "force_fail", f);
			setForceFail(f);
			if (!f)
				resetAutoFailCounters();
		}
	}

	/** Get the Force Fail status */
	@Override
	public boolean getForceFail() {
		return force_fail;
	}

	/** Auto Fail status flag */
	private boolean auto_fail;

	/** Is auto-fail enabled for this detector? */
	private boolean isAutoFailEnabled() {
		return isDetectorAutoFailEnabled() && !getAbandoned();
	}

	/** Update the auto fail status */
	private void updateAutoFail() {
		boolean af = no_hits.triggered
		          || chatter.triggered
		          || locked_on.triggered
		          || no_change.triggered
		          || occ_spike.triggered;
		setAutoFailNotify(af && isAutoFailEnabled());
	}

	/** Set the Auto Fail status */
	private void setAutoFailNotify(boolean f) {
		if (f != auto_fail) {
			try {
				store.update(this, "auto_fail", f);
				auto_fail = f;
				notifyAttribute("autoFail");
			}
			catch (TMSException e) {
				e.printStackTrace();
			}
		}
	}

	/** Get the Auto Fail status */
	@Override
	public boolean getAutoFail() {
		return auto_fail;
	}

	/** Check if the detector is currently offline / 'failed' */
	@Override
	public boolean isOffline() {
		return isFailed(false);
	}

	/** Check if the detector is currently offline / 'failed' */
	private boolean isFailed(boolean ignore_auto_fail) {
		return force_fail ||
		       (auto_fail && !ignore_auto_fail) ||
		       (super.isOffline() && !isPollinator());
	}

	/** Check if the comm link is handled by pollinator */
	private boolean isPollinator() {
		ControllerImpl c = controller;
		CommLink cl = (c != null) ? c.getCommLink() : null;
		CommConfig cc = (cl != null) ? cl.getCommConfig() : null;
		return cc != null && cc.getPollinator();
	}

	/** Get the active status */
	@Override
	public boolean isActive() {
		return LaneCode.fromCode(lane_code) == LaneCode.GREEN ||
			super.isActive();
	}

	/** Check if the detector is currently sampling data */
	public boolean isSampling() {
		return isSampling(false);
	}

	/** Check if the detector is currently sampling data */
	private boolean isSampling(boolean ignore_auto_fail) {
		return (LaneCode.fromCode(lane_code) == LaneCode.GREEN) ||
		       (isActive() && !isFailed(ignore_auto_fail));
	}

	/** Average detector field length (feet) */
	private float field_length = DEFAULT_FIELD_FT;

	/** Set the average field length (feet) */
	@Override
	public void setFieldLength(float f) {
		field_length = f;
	}

	/** Set the average field length (feet) */
	public void doSetFieldLength(float f) throws TMSException {
		if (f != field_length) {
			store.update(this, "field_length", f);
			setFieldLength(f);
		}
	}

	/** Get the average field length (feet) */
	@Override
	public float getFieldLength() {
		return field_length;
	}

	/** Fake detector expression */
	private String fake = null;

	/** Fake detector to use if detector is failed */
	private transient FakeDetector fake_det;

	/** Set the fake expression */
	@Override
	public void setFake(String f) {
		fake = f;
	}

	/** Set the fake expression */
	public void doSetFake(String f) throws TMSException {
		FakeDetector fd = createFakeDetector(f);
		if (fd != null) {
			// Normalize the fake detector string
			f = fd.toString();
			if (f.equals("")) {
				fd = null;
				f = null;
			}
		}
		if (!objectEquals(f, fake)) {
			store.update(this, "fake", f);
			fake_det = fd;
			setFake(f);
		}
	}

	/** Get the fake expression */
	@Override
	public String getFake() {
		return fake;
	}

	/** Periodic vehicle count cache */
	private transient final PeriodicSampleCache veh_cache;

	/** Periodic scan cache */
	private transient final PeriodicSampleCache scn_cache;

	/** Periodic speed cache */
	private transient final PeriodicSampleCache spd_cache;

	/** Periodic MOTORCYCLE class count cache */
	private transient final PeriodicSampleCache mc_count_cache;

	/** Periodic SHORT class count cache */
	private transient final PeriodicSampleCache s_count_cache;

	/** Periodic MEDIUM class count cache */
	private transient final PeriodicSampleCache m_count_cache;

	/** Periodic LONG class count cache */
	private transient final PeriodicSampleCache l_count_cache;

	/** Vehicle event log */
	private transient final VehicleEventLog v_log;

	/** Event Logging flag */
	private transient boolean is_logging_events = true;

	/** Occupancy value from previous 30-second period */
	private transient int prev_value = MISSING_DATA;

	/** Period to hold occ spike failure */
	private transient int spike_hold_sec = 0;

	/** Get vehicle count */
	@Override
	public int getVehCount(long stamp, int per_ms) {
		return getVehCount(stamp, per_ms, false);
	}

	/** Get vehicle count */
	private int getVehCount(long stamp, int per_ms,
		boolean ignore_auto_fail)
	{
		return isSampling(ignore_auto_fail)
		      ? veh_cache.getValue(stamp - per_ms, stamp)
		      : MISSING_DATA;
	}

	/** Get the occupancy for most recent period */
	public float getOccupancy(int per_ms) {
		return getOccupancy(per_ms, false);
	}

	/** Get the occupancy for an interval */
	protected float getOccupancy(long stamp, int per_ms) {
		return getOccupancy(stamp, per_ms, false);
	}

	/** Get the occupancy for most recent period */
	public float getOccupancy(int per_ms, boolean ignore_auto_fail) {
		long stamp = calculateEndTime(per_ms);
		return getOccupancy(stamp, per_ms, ignore_auto_fail);
	}

	/** Get the occupancy for an interval */
	private float getOccupancy(long stamp, int per_ms,
		boolean ignore_auto_fail)
	{
		int scn = isSampling(ignore_auto_fail)
		       ? scn_cache.getValue(stamp - per_ms, stamp)
		       : MISSING_DATA;
		return (scn >= 0)
		      ? MAX_OCCUPANCY * scn * SCAN_MS / per_ms
		      : MISSING_DATA;
	}

	/** Get a flow rate (vehicles per hour) */
	@Override
	public int getFlow(long stamp, int per_ms) {
		int flow = getFlowRaw(stamp, per_ms);
		return (flow >= 0) ? flow : getFlowFake(stamp, per_ms);
	}

	/** Get a raw (non-faked) flow rate (vehicles per hour) */
	protected int getFlowRaw(long stamp, int per_ms) {
		return getFlowRaw(stamp, per_ms, false);
	}

	/** Get a raw (non-faked) flow rate (vehicles per hour) */
	private int getFlowRaw(long stamp, int per_ms,
		boolean ignore_auto_fail)
	{
		int v = getVehCount(stamp, per_ms, ignore_auto_fail);
		float ph = new Interval(per_ms, MILLISECONDS).per(HOUR);
		return (v >= 0) ? Math.round(v * ph) : MISSING_DATA;
	}

	/** Get a fake flow rate (vehicles per hour) */
	private int getFlowFake(long stamp, int per_ms) {
		FakeDetector f = fake_det;
		return (f != null) ? f.getFlow(stamp, per_ms) : MISSING_DATA;
	}

	/** Get the density (vehicles per mile) */
	@Override
	public float getDensity(long stamp, int per_ms) {
		return getDensity(stamp, per_ms, false);
	}

	/** Get the density (vehicles per mile) */
	public float getDensity(long stamp, int per_ms,
		boolean ignore_auto_fail)
	{
		float k = getDensityRaw(stamp, per_ms, ignore_auto_fail);
		return (k >= 0) ? k : getDensityFake(stamp, per_ms);
	}

	/** Get the current raw (non-faked) density (vehicles per mile) */
	protected float getDensityRaw(long stamp, int per_ms,
		boolean ignore_auto_fail)
	{
		float k = getDensityFromFlowSpeed(stamp, per_ms, ignore_auto_fail);
		return (k >= 0)
		      ? k
		      : getDensityFromOccupancy(stamp, per_ms, ignore_auto_fail);
	}

	/** Get the density from flow and speed (vehicles per mile) */
	private float getDensityFromFlowSpeed(long stamp, int per_ms,
		boolean ignore_auto_fail)
	{
		float speed = getSpeedRaw(stamp, per_ms, ignore_auto_fail);
		if (speed > 0) {
			int flow = getFlowRaw(stamp, per_ms, ignore_auto_fail);
			if (flow > MISSING_DATA)
				return flow / speed;
		}
		return MISSING_DATA;
	}

	/** Get the density from occupancy (vehicles per mile) */
	private float getDensityFromOccupancy(long stamp, int per_ms,
		boolean ignore_auto_fail)
	{
		float occ = getOccupancy(stamp, per_ms, ignore_auto_fail);
		if (occ >= 0 && field_length > 0) {
			Distance fl = new Distance(field_length, FEET);
			return occ / (fl.asFloat(MILES) * MAX_OCCUPANCY);
		} else
			return MISSING_DATA;
	}

	/** Get fake density (vehicles per mile) */
	private float getDensityFake(long stamp, int per_ms) {
		FakeDetector f = fake_det;
		return (f != null) ? f.getDensity(stamp, per_ms) : MISSING_DATA;
	}

	/** Get recorded speed (miles per hour) */
	@Override
	public float getSpeed(long stamp, int per_ms) {
		return getSpeed(stamp, per_ms, false);
	}

	/** Get recorded speed (miles per hour) */
	public float getSpeed(long stamp, int per_ms, boolean ignore_auto_fail) {
		float speed = getSpeedRaw(stamp, per_ms, ignore_auto_fail);
		if (speed > 0)
			return speed;
		speed = getSpeedEstimate(stamp, per_ms, ignore_auto_fail);
		if (speed > 0)
			return speed;
		else
			return getSpeedFake(stamp, per_ms);
	}

	/** Get the raw (non-faked) speed (MPH) */
	protected float getSpeedRaw(long stamp, int per_ms) {
		return getSpeedRaw(stamp, per_ms, false);
	}

	/** Get the raw (non-faked) speed (MPH) */
	private float getSpeedRaw(long stamp, int per_ms,
		boolean ignore_auto_fail)
	{
		return isSampling(ignore_auto_fail)
		      ? spd_cache.getValue(stamp - per_ms, stamp)
		      : MISSING_DATA;
	}

	/** Get speed estimate based on flow / density */
	private float getSpeedEstimate(long stamp, int per_ms,
		boolean ignore_auto_fail)
	{
		int flow = getFlowRaw(stamp, per_ms, ignore_auto_fail);
		if (flow <= 0)
			return MISSING_DATA;
		float density = getDensityFromOccupancy(stamp, per_ms,
			ignore_auto_fail);
		if (density <= DENSITY_THRESHOLD)
			return MISSING_DATA;
		return flow / density;
	}

	/** Get fake speed (miles per hour) */
	private float getSpeedFake(long stamp, int per_ms) {
		FakeDetector f = fake_det;
		return (f != null) ? f.getSpeed(stamp, per_ms) : MISSING_DATA;
	}

	/** Log a detector event */
	private void logEvent(EventType event_type) {
		logEvent(new DetAutoFailEvent(event_type, getName()));
	}

	/** Store vehicle count for one binning interval.
	 * @param v PeriodicSample containing vehicle count data.
	 * @param vc Vehicle class. */
	public void storeVehCount(PeriodicSample v, VehLengthClass vc) {
		if (vc == null)
			storeVehCount(v, false);
		else if (v != null) {
			switch (vc) {
			case MOTORCYCLE:
				mc_count_cache.add(v, name);
				break;
			case SHORT:
				s_count_cache.add(v, name);
				break;
			case MEDIUM:
				m_count_cache.add(v, name);
				break;
			case LONG:
				l_count_cache.add(v, name);
				break;
			}
		}
	}

	/** Store vehicle count for one binning interval.
	 * @param v PeriodicSample containing vehicle count data. */
	public void storeVehCount(PeriodicSample v, boolean logging) {
		is_logging_events = logging;
		if (v != null) {
			if (LaneCode.fromCode(lane_code) != LaneCode.GREEN &&
			    v.per_sec == BIN_PERIOD_SEC)
				testVehCount(v);
			veh_cache.add(v, name);
		}
	}

	/** Test vehicle count with error detecting algorithms */
	private void testVehCount(PeriodicSample vs) {
		chatter.updateState(vs.per_sec, vs.value > MAX_VEH_COUNT_30);
		if (chatter.checkLogging(vs.per_sec))
			logEvent(EventType.DET_CHATTER);
		no_hits.updateState(vs.per_sec, vs.value == 0);
		if (no_hits.checkLogging(vs.per_sec))
			logEvent(EventType.DET_NO_HITS);
		updateAutoFail();
	}

	/** Check if a location is on a reversible road */
	private boolean isReversibleLocationHack(GeoLoc loc) {
		// FIXME: this is a MnDOT-specific hack
		Road roadway = loc.getRoadway();
		if (roadway != null && REV.equals(roadway.getName()))
			return true;
		Road cross = loc.getCrossStreet();
		if (cross != null && REV.equals(cross.getName()))
			return true;
		return false;
	}

	/** Store occupancy for one binning interval.
	 * @param occ Occupancy data. */
	public void storeOccupancy(OccupancySample occ, boolean logging) {
		is_logging_events = logging;
		if (occ != null) {
			int n_scans = occ.as60HzScans();
			if (occ.per_sec == BIN_PERIOD_SEC) {
				testScans(occ);
				prev_value = occ.value;
			}
			scn_cache.add(new PeriodicSample(occ.stamp, occ.per_sec,
				n_scans), name);
		} else
			prev_value = MISSING_DATA;
	}

	/** Test binned occupancy with error detecting algorithms */
	private void testScans(OccupancySample occ) {
		boolean lock = occ.value >= OccupancySample.MAX;
		locked_on.updateState(occ.per_sec, lock);
		if (locked_on.checkLogging(occ.per_sec))
			logEvent(EventType.DET_LOCKED_ON);
		boolean v = (occ.value > 0) && (occ.value == prev_value);
		no_change.updateState(occ.per_sec, v);
		if (no_change.checkLogging(occ.per_sec))
			logEvent(EventType.DET_NO_CHANGE);
		if (occ.value >= 0 && prev_value >= 0) {
			int spk = Math.abs(occ.value - prev_value) / OCC_SPIKE;
			spike_hold_sec += occ.per_sec * spk;
		}
		int threshold = getOccSpikeSecs();
		boolean sf = (threshold > 0) && (spike_hold_sec > threshold);
		occ_spike.updateState(occ.per_sec, sf);
		if (occ_spike.checkLogging(occ.per_sec))
			logEvent(EventType.DET_OCC_SPIKE);
		spike_hold_sec = Math.max(0, spike_hold_sec - occ.per_sec);
		updateAutoFail();
	}

	/** Store speed for one binning interval.
	 * @param speed PeriodicSample containing speed data. */
	public void storeSpeed(PeriodicSample speed, boolean logging) {
		is_logging_events = logging;
		if (speed != null)
			spd_cache.add(speed, name);
	}

	/** Flush buffered data to disk */
	public void flush(PeriodicSampleWriter writer) {
		// Only flush periodic binned data if not logging events
		if (!is_logging_events) {
			writer.flush(veh_cache, name);
			writer.flush(scn_cache, name);
			writer.flush(spd_cache, name);
			writer.flush(mc_count_cache, name);
			writer.flush(s_count_cache, name);
			writer.flush(m_count_cache, name);
			writer.flush(l_count_cache, name);
		}
	}

	/** Purge all binned data before a given stamp. */
	public void purge(long before) {
		veh_cache.purge(before);
		scn_cache.purge(before);
		spd_cache.purge(before);
		mc_count_cache.purge(before);
		s_count_cache.purge(before);
		m_count_cache.purge(before);
		l_count_cache.purge(before);
	}

	/** Log a vehicle detection event.
	 * @param duration Event duration in milliseconds.
	 * @param headway Headway since last event in milliseconds.
	 * @param stamp Timestamp of detection event.
	 * @param speed Speed in miles per hour.
	 * @param length Length in feet. */
	public void logVehicle(int duration, int headway, long stamp,
		int speed, int length)
	{
		v_log.logVehicle(duration, headway, stamp, speed, length);
	}

	/** Log a gap in vehicle events */
	public void logGap(long stamp) {
		v_log.logGap(stamp);
	}

	/** Bin event data to the specified period */
	public void binEventData(int per_sec, boolean success) {
		long stamp = calculateEndTime(per_sec * 1000);
		if (success) {
			storeVehCount(v_log.getVehCountSam(stamp, per_sec),
				true);
			storeOccupancy(v_log.getOccupancySam(stamp, per_sec),
				true);
			storeSpeed(v_log.getSpeedSam(stamp, per_sec), true);
		}
		v_log.clear_bin(stamp);
	}

	/** Write a single detector as an XML element */
	public void writeXmlElement(Writer w) throws IOException {
		LaneCode lc = LaneCode.fromCode(lane_code);
		short lane = getLaneNumber();
		float field = getFieldLength();
		String l = DetectorHelper.getLabel(this);
		w.write("<detector");
		w.write(createAttribute("name", name));
		if (!l.equals("FUTURE"))
			w.write(createAttribute("label", l));
		if (abandoned)
			w.write(createAttribute("abandoned", "t"));
		if (LaneCode.MAINLINE != lc)
			w.write(createAttribute("category", lc));
		if (lane > 0)
			w.write(createAttribute("lane", lane));
		if (field != DEFAULT_FIELD_FT)
			w.write(createAttribute("field", field));
		Controller c = getController();
		if (c != null)
			w.write(createAttribute("controller", c.getName()));
		w.write("/>\n");
	}

	/** Print binned data as an XML element */
	public void writeSampleXml(Writer w, long stamp, int per_ms)
		throws IOException
	{
		if (abandoned || !isSampling())
			return;
		int flow = getFlowRaw(stamp, per_ms);
		int speed = Math.round(getSpeed(stamp, per_ms));
		float occ = getOccupancy(stamp, per_ms);
		w.write("\t<sample");
		w.write(createAttribute("sensor", name));
		if (flow != MISSING_DATA)
			w.write(createAttribute("flow", flow));
		if (isMainline() && speed > 0)
			w.write(createAttribute("speed", speed));
		if (occ >= 0)
			w.write(createAttribute("occ", formatFloat(occ, 2)));
		w.write("/>\n");
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		// no detector device requests
	}
}
