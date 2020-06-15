/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2020  Minnesota Department of Transportation
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.LaneType;
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

	/** Is detector occupancy spike failure enabled? */
	static private boolean isOccSpikeEnabled() {
		return SystemAttrEnum.DETECTOR_OCC_SPIKE_ENABLE.getBoolean();
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

	/** Scan "no change" threshold */
	static private final Interval NO_CHANGE_THRESHOLD =
		new Interval(24, Interval.Units.HOURS);

	/** Scan "occ spike" threshold */
	static private final Interval OCC_SPIKE_THRESHOLD =
		new Interval(30, SECONDS);

	/** Threshold for occ spike timer to trigger auto fail */
	static private final int OCC_SPIKE_TIMER_THRESHOLD = 60;

	/** Clear threshold */
	static private final Interval CLEAR_THRESHOLD =
		new Interval(24, Interval.Units.HOURS);

	/** Fast clear threshold */
	static private final Interval FAST_CLEAR_THRESHOLD =
		new Interval(30, SECONDS);

	/** Maximum "realistic" vehicle count for a 30-second sample */
	static private final int MAX_VEH_COUNT_30 = 37;

	/** Maximum occupancy value (100%) */
	static private final int MAX_OCCUPANCY = 100;

	/** Maximum number of scans in 30 seconds */
	static private final int MAX_C30 = 1800;

	/** Change in occupancy to indicate a spike */
	static private final int OCC_SPIKE = OccupancySample.MAX / 4;

	/** Sample period for detectors (seconds) */
	static private final int SAMPLE_PERIOD_SEC = 30;

	/** Sample period for detectors (ms) */
	static public final long SAMPLE_PERIOD_MS = new Interval(
		SAMPLE_PERIOD_SEC).ms();

	/** Calculate the end time of previous period */
	static public long calculateEndTime() {
		long stamp = TimeSteward.currentTimeMillis();
		return stamp / SAMPLE_PERIOD_MS * SAMPLE_PERIOD_MS;
	}

	/** Load all the detectors */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, DetectorImpl.class);
		store.query("SELECT name, controller, pin, r_node, lane_type, "+
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
		map.put("lane_type", (short) lane_type.ordinal());
		map.put("lane_number", lane_number);
		map.put("abandoned", abandoned);
		map.put("force_fail", force_fail);
		map.put("auto_fail", auto_fail);
		map.put("field_length", field_length);
		map.put("fake", fake);
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

	/** Create a new detector */
	public DetectorImpl(String n) throws TMSException, SonarException {
		super(n);
		veh_cache = new PeriodicSampleCache(PeriodicSampleType.VEH_COUNT);
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
		     row.getShort(5),   // lane_type
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
	private DetectorImpl(String n, String c, int p, String r, short lt,
		short ln, boolean a, boolean ff, boolean af, float fl, String f,
		String nt)
	{
		this(n, lookupController(c), p, lookupR_Node(r), lt, ln, a, ff,
		     af, fl, f, nt);
	}

	/** Create a detector */
	private DetectorImpl(String n, ControllerImpl c, int p, R_NodeImpl r,
		short lt, short ln, boolean a, boolean ff, boolean af, float fl,
		String f, String nt)
	{
		super(n, c, p, nt);
		r_node = r;
		lane_type = LaneType.fromOrdinal(lt);
		lane_number = ln;
		abandoned = a;
		force_fail = ff;
		auto_fail = af;
		field_length = fl;
		fake = f;
		veh_cache = new PeriodicSampleCache(PeriodicSampleType.VEH_COUNT);
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
		occ_spike = new AutoFailCounter(getOccSpikeThreshold(),
			CLEAR_THRESHOLD);
		updateAutoFail();
	}

	/** Get the vehicle count "no hit" threshold */
	private Interval getNoHitThreshold() {
		if (isRamp()) {
			GeoLoc loc = lookupGeoLoc();
			if (loc != null && isReversibleLocationHack(loc))
				return new Interval(72, Interval.Units.HOURS);
		}
		return lane_type.no_hit_threshold;
	}

	/** Get the vehicle count "chatter" threshold */
	private Interval getChatterThreshold() {
		return CHATTER_THRESHOLD;
	}

	/** Get the scan "locked on" threshold */
	private Interval getLockedOnThreshold() {
		return lane_type.getLockedOnThreshold();
	}

	/** Get the scan "no change" threshold */
	private Interval getNoChangeThreshold() {
		return NO_CHANGE_THRESHOLD;
	}

	/** Get the scan "occ spike" threshold */
	private Interval getOccSpikeThreshold() {
		return OCC_SPIKE_THRESHOLD;
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

	/** Lane type */
	private LaneType lane_type = LaneType.NONE;

	/** Set the lane type */
	@Override
	public void setLaneType(short t) {
		lane_type = LaneType.fromOrdinal(t);
		resetAutoFailCounters();
	}

	/** Set the lane type */
	public void doSetLaneType(short t) throws TMSException {
		LaneType lt = LaneType.fromOrdinal(t);
		if (lt != lane_type) {
			store.update(this, "lane_type", t);
			setLaneType(t);
		}
	}

	/** Get the lane type */
	@Override
	public short getLaneType() {
		return (short) lane_type.ordinal();
	}

	/** Is this a mailline detector? (auxiliary, cd, etc.) */
	public boolean isMainline() {
		return lane_type.isMainline();
	}

	/** Is this a station detector? (mainline, non-HOV) */
	public boolean isStation() {
		return lane_type.isStation();
	}

	/** Is this a station or CD detector? */
	public boolean isStationOrCD() {
		return lane_type.isStationOrCD();
	}

	/** Is this a ramp detector? (merge, queue, exit, bypass) */
	public boolean isRamp() {
		return lane_type.isRamp();
	}

	/** Is this a velocity detector? */
	public boolean isVelocity() {
		return lane_type.isVelocity();
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
		          || (occ_spike.triggered && isOccSpikeEnabled());
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

	/** Check if the detector is currently 'failed' */
	@Override
	public boolean isFailed() {
		return force_fail || auto_fail || super.isFailed();
	}

	/** Get the active status */
	@Override
	public boolean isActive() {
		return lane_type == LaneType.GREEN || super.isActive();
	}

	/** Check if the detector is currently sampling data */
	public boolean isSampling() {
		return (lane_type == LaneType.GREEN) ||
		       (isActive() && !isFailed());
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

	/** Periodic vehicle count sample cache */
	private transient final PeriodicSampleCache veh_cache;

	/** Periodic scan sample cache */
	private transient final PeriodicSampleCache scn_cache;

	/** Periodic speed sample cache */
	private transient final PeriodicSampleCache spd_cache;

	/** Periodic MOTORCYCLE class count sample cache */
	private transient final PeriodicSampleCache mc_count_cache;

	/** Periodic SHORT class count sample cache */
	private transient final PeriodicSampleCache s_count_cache;

	/** Periodic MEDIUM class count sample cache */
	private transient final PeriodicSampleCache m_count_cache;

	/** Periodic LONG class count sample cache */
	private transient final PeriodicSampleCache l_count_cache;

	/** Vehicle event log */
	private transient final VehicleEventLog v_log;

	/** Occupancy value from previous 30-second sample period */
	private transient int prev_value = MISSING_DATA;

	/** Period to hold occ spike failure */
	private transient int spike_hold_sec = 0;

	/** Get the current vehicle count */
	@Override
	public int getVehCount(long start, long end) {
		return isSampling()
		      ? veh_cache.getValue(start, end)
		      : MISSING_DATA;
	}

	/** Get the current occupancy */
	public float getOccupancy() {
		long end = calculateEndTime();
		long start = end - SAMPLE_PERIOD_MS;
		return getOccupancy(start, end);
	}

	/** Get the current occupancy */
	private float getOccupancy(long start, long end) {
		int scn = isSampling()
		       ? scn_cache.getValue(start, end)
		       : MISSING_DATA;
		return (scn != MISSING_DATA)
		      ? MAX_OCCUPANCY * (float) scn / MAX_C30
		      : MISSING_DATA;
	}

	/** Get a flow rate (vehicles per hour) */
	@Override
	public int getFlow(long start, long end) {
		int flow = getFlowRaw(start, end);
		return (flow >= 0) ? flow : getFlowFake(start, end);
	}

	/** Get a raw (non-faked) flow rate (vehicles per hour) */
	protected int getFlowRaw(long start, long end) {
		int v = getVehCount(start, end);
		float ph = new Interval(end - start, MILLISECONDS).per(HOUR);
		return (v >= 0) ? Math.round(v * ph) : MISSING_DATA;
	}

	/** Get the current raw (non-faked) flow rate (vehicles per hour) */
	private int getFlowRaw() {
		long end = calculateEndTime();
		long start = end - SAMPLE_PERIOD_MS;
		return getFlowRaw(start, end);
	}

	/** Get a fake flow rate (vehicles per hour) */
	private int getFlowFake(long start, long end) {
		FakeDetector f = fake_det;
		return (f != null) ? f.getFlow(start, end) : MISSING_DATA;
	}

	/** Get the current density (vehicles per mile) */
	@Override
	public float getDensity() {
		float k = getDensityRaw();
		return (k >= 0) ? k : getDensityFake();
	}

	/** Get the current raw (non-faked) density (vehicles per mile) */
	protected float getDensityRaw() {
		float k = getDensityFromFlowSpeed();
		return (k >= 0) ? k : getDensityFromOccupancy();
	}

	/** Get the density from flow and speed (vehicles per mile) */
	private float getDensityFromFlowSpeed() {
		float speed = getSpeedRaw();
		if (speed > 0) {
			int flow = getFlowRaw();
			if (flow > MISSING_DATA)
				return flow / speed;
		}
		return MISSING_DATA;
	}

	/** Get the density from occupancy (vehicles per mile) */
	private float getDensityFromOccupancy() {
		float occ = getOccupancy();
		if (occ >= 0 && field_length > 0) {
			Distance fl = new Distance(field_length, FEET);
			return occ / (fl.asFloat(MILES) * MAX_OCCUPANCY);
		} else
			return MISSING_DATA;
	}

	/** Get fake density (vehicles per mile) */
	private float getDensityFake() {
		FakeDetector f = fake_det;
		return (f != null) ? f.getDensity() : MISSING_DATA;
	}

	/** Get the current speed (miles per hour) */
	@Override
	public float getSpeed() {
		float speed = getSpeedRaw();
		if (speed > 0)
			return speed;
		speed = getSpeedEstimate();
		if (speed > 0)
			return speed;
		else
			return getSpeedFake();
	}

	/** Get the current raw (non-faked) speed (miles per hour) */
	protected float getSpeedRaw() {
		long end = calculateEndTime();
		long start = end - SAMPLE_PERIOD_MS;
		return getSpeedRaw(start, end);
	}

	/** Get the raw (non-faked) speed (MPH) */
	private float getSpeedRaw(long start, long end) {
		return isSampling()
		      ? spd_cache.getValue(start, end)
		      : MISSING_DATA;
	}

	/** Get speed estimate based on flow / density */
	private float getSpeedEstimate() {
		int flow = getFlowRaw();
		if (flow <= 0)
			return MISSING_DATA;
		float density = getDensityFromOccupancy();
		if (density <= DENSITY_THRESHOLD)
			return MISSING_DATA;
		return flow / density;
	}

	/** Get fake speed (miles per hour) */
	private float getSpeedFake() {
		FakeDetector f = fake_det;
		if (f != null)
			return f.getSpeed();
		else
			return MISSING_DATA;
	}

	/** Log a detector event */
	private void logEvent(EventType event_type) {
		logEvent(new DetAutoFailEvent(event_type, getName()));
	}

	/** Store one vehicle count sample for this detector.
	 * @param v PeriodicSample containing vehicle count data.
	 * @param vc Vehicle class. */
	public void storeVehCount(PeriodicSample v, VehLengthClass vc) {
		if (vc == null)
			storeVehCount(v);
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

	/** Store one vehicle count sample for this detector.
	 * @param v PeriodicSample containing vehicle count data. */
	public void storeVehCount(PeriodicSample v) {
		if (v != null) {
			if (lane_type != LaneType.GREEN &&
			    v.period == SAMPLE_PERIOD_SEC)
				testVehCount(v);
			veh_cache.add(v, name);
		}
	}

	/** Test a vehicle count sample with error detecting algorithms */
	private void testVehCount(PeriodicSample vs) {
		chatter.updateState(vs.period, vs.value > MAX_VEH_COUNT_30);
		if (chatter.checkLogging(vs.period))
			logEvent(EventType.DET_CHATTER);
		no_hits.updateState(vs.period, vs.value == 0);
		if (no_hits.checkLogging(vs.period))
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

	/** Store one occupancy sample for this detector.
	 * @param occ Occupancy sample data. */
	public void storeOccupancy(OccupancySample occ) {
		if (occ != null) {
			int n_scans = occ.as60HzScans();
			if (occ.period == SAMPLE_PERIOD_SEC) {
				testScans(occ);
				prev_value = occ.value;
			}
			scn_cache.add(new PeriodicSample(occ.stamp, occ.period,
				n_scans), name);
		} else
			prev_value = MISSING_DATA;
	}

	/** Test an occupancy sample with error detecting algorithms */
	private void testScans(OccupancySample occ) {
		boolean lock = occ.value >= OccupancySample.MAX;
		// Locked-on counter should be cleared only with good
		// non-zero samples.  This helps when the duration of
		// occupancy spikes is shorter than the threshold time
		// and interspersed with zeroes.
		boolean hold = locked_on.failed && (occ.value == 0);
		locked_on.updateState(occ.period, lock || hold);
		if (locked_on.checkLogging(occ.period))
			logEvent(EventType.DET_LOCKED_ON);
		boolean v = (occ.value > 0) && (occ.value == prev_value);
		no_change.updateState(occ.period, v);
		if (no_change.checkLogging(occ.period))
			logEvent(EventType.DET_NO_CHANGE);
		if (occ.value >= 0 && prev_value >= 0) {
			int spk = Math.abs(occ.value - prev_value) / OCC_SPIKE;
			spike_hold_sec += occ.period * spk;
		}
		boolean sf = spike_hold_sec > OCC_SPIKE_TIMER_THRESHOLD;
		occ_spike.updateState(occ.period, sf);
		if (occ_spike.checkLogging(occ.period))
			logEvent(EventType.DET_OCC_SPIKE);
		spike_hold_sec = Math.max(0, spike_hold_sec - occ.period);
		updateAutoFail();
	}

	/** Store one speed sample for this detector.
	 * @param speed PeriodicSample containing speed data. */
	public void storeSpeed(PeriodicSample speed) {
		if (speed != null)
			spd_cache.add(speed, name);
	}

	/** Flush buffered data to disk */
	public void flush(PeriodicSampleWriter writer) {
		writer.flush(veh_cache, name);
		writer.flush(scn_cache, name);
		writer.flush(spd_cache, name);
		writer.flush(mc_count_cache, name);
		writer.flush(s_count_cache, name);
		writer.flush(m_count_cache, name);
		writer.flush(l_count_cache, name);
	}

	/** Purge all samples before a given stamp. */
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
	 * @param stamp Timestamp of detection event.
	 * @param duration Event duration in milliseconds.
	 * @param headway Headway since last event in milliseconds.
	 * @param speed Speed in miles per hour. */
	public void logVehicle(Calendar stamp, int duration, int headway,
		int speed)
	{
		v_log.logVehicle(stamp, duration, headway, speed);
	}

	/** Log a gap in vehicle events */
	public void logGap() {
		v_log.logGap();
	}

	/** Bin sample data to the specified period */
	public void binEventSamples(int p) {
		long end = calculateEndTime();
		storeVehCount(v_log.getVehCount(end, p));
		storeOccupancy(v_log.getOccupancy(end, p));
		storeSpeed(v_log.getSpeed(end, p));
		v_log.binEventSamples();
	}

	/** Write a single detector as an XML element */
	public void writeXmlElement(Writer w) throws IOException {
		LaneType lt = lane_type;
		short lane = getLaneNumber();
		float field = getFieldLength();
		String l = DetectorHelper.getLabel(this);
		w.write("<detector");
		w.write(createAttribute("name", name));
		if (!l.equals("FUTURE"))
			w.write(createAttribute("label", l));
		if (abandoned)
			w.write(createAttribute("abandoned", "t"));
		if (lt != LaneType.NONE && lt != LaneType.MAINLINE)
			w.write(createAttribute("category", lt.suffix));
		if (lane > 0)
			w.write(createAttribute("lane", lane));
		if (field != DEFAULT_FIELD_FT)
			w.write(createAttribute("field", field));
		Controller c = getController();
		if (c != null)
			w.write(createAttribute("controller", c.getName()));
		w.write("/>\n");
	}

	/** Print the current sample as an XML element */
	public void writeSampleXml(Writer w) throws IOException {
		if (abandoned || !isSampling())
			return;
		int flow = getFlowRaw();
		int speed = Math.round(getSpeed());
		float occ = getOccupancy();
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
