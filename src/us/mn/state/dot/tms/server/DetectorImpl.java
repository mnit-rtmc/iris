/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
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
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.FEET;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;
import us.mn.state.dot.tms.server.event.DetFailEvent;

/**
 * Detector for traffic data sampling
 *
 * @author Douglas Lau
 */
public class DetectorImpl extends DeviceImpl implements Detector,VehicleSampler{

	/** Default average detector field length (feet) */
	static private final float DEFAULT_FIELD_FT = 22.0f;

	/** Valid density threshold for speed calculation */
	static private final float DENSITY_THRESHOLD = 1.2f;

	/** Maximum "realistic" volume for a 30-second sample */
	static private final int MAX_VOLUME = 37;

	/** Maximum occupancy value (100%) */
	static private final int MAX_OCCUPANCY = 100;

	/** Maximum number of scans in 30 seconds */
	static private final int MAX_C30 = 1800;

	/** Sample period for detectors (seconds) */
	static public final int SAMPLE_PERIOD_SEC = 30;

	/** Time interval for sample period */
	static private final Interval SAMPLE_INTERVAL = new Interval(
		SAMPLE_PERIOD_SEC);

	/** Load all the detectors */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, DetectorImpl.class);
		store.query("SELECT name, controller, pin, r_node, lane_type, "+
			"lane_number, abandoned, force_fail, field_length, " +
			"fake, notes FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DetectorImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// controller
					row.getInt(3),		// pin
					row.getString(4),	// r_node
					row.getShort(5),	// lane_type
					row.getShort(6),	// lane_number
					row.getBoolean(7),	// abandoned
					row.getBoolean(8),	// force_fail
					row.getFloat(9),	// field_length
					row.getString(10),	// fake
					row.getString(11)	// notes
				));
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
		vol_cache = new PeriodicSampleCache(PeriodicSampleType.VOLUME);
		scn_cache = new PeriodicSampleCache(PeriodicSampleType.SCAN);
		spd_cache = new PeriodicSampleCache(PeriodicSampleType.SPEED);
		vol_mc_cache = new PeriodicSampleCache(
			PeriodicSampleType.MOTORCYCLE);
		vol_s_cache = new PeriodicSampleCache(
			PeriodicSampleType.SHORT);
		vol_m_cache = new PeriodicSampleCache(
			PeriodicSampleType.MEDIUM);
		vol_l_cache = new PeriodicSampleCache(
			PeriodicSampleType.LONG);
		v_log = new VehicleEventLog(n);
		initTransients();
	}

	/** Create a detector */
	private DetectorImpl(String n, ControllerImpl c, int p, R_NodeImpl r,
		short lt, short ln, boolean a, boolean ff, float fl, String f,
		String nt)
	{
		super(n, c, p, nt);
		r_node = r;
		lane_type = LaneType.fromOrdinal(lt);
		lane_number = ln;
		abandoned = a;
		force_fail = ff;
		field_length = fl;
		fake = f;
		vol_cache = new PeriodicSampleCache(PeriodicSampleType.VOLUME);
		scn_cache = new PeriodicSampleCache(PeriodicSampleType.SCAN);
		spd_cache = new PeriodicSampleCache(PeriodicSampleType.SPEED);
		vol_mc_cache = new PeriodicSampleCache(
			PeriodicSampleType.MOTORCYCLE);
		vol_s_cache = new PeriodicSampleCache(
			PeriodicSampleType.SHORT);
		vol_m_cache = new PeriodicSampleCache(
			PeriodicSampleType.MEDIUM);
		vol_l_cache = new PeriodicSampleCache(
			PeriodicSampleType.LONG);
		v_log = new VehicleEventLog(n);
	}

	/** Create a detector */
	private DetectorImpl(Namespace ns, String n, String c, int p,
		String r, short lt, short ln, boolean a, boolean ff, float fl,
		String f, String nt)
	{
		this(n,(ControllerImpl)ns.lookupObject(Controller.SONAR_TYPE,c),
		     p, (R_NodeImpl) ns.lookupObject(R_Node.SONAR_TYPE, r),
		     lt, ln, a, ff, fl, f, nt);
	}

	/** Initialize the transient state */
	@Override
	public void initTransients() {
		super.initTransients();
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

	/** Is this an onramp detector? */
	public boolean isOnRamp() {
		return lane_type.isOnRamp();
	}

	/** Is this an offRamp detector? */
	public boolean isOffRamp() {
		return lane_type.isOffRamp();
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

	/** Check if the detector is currently 'failed' */
	@Override
	public boolean isFailed() {
		return force_fail || super.isFailed();
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
		if (!stringEquals(f, fake)) {
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

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		// no detector device requests
	}

	/** Perform a periodic poll */
	@Override
	public void periodicPoll() {
		// no periodic polling
	}

	/** Accumulator for number of seconds with no hits (volume) */
	private transient int no_hits = 0;

	/** Accumulator for number of seconds locked on (scans) */
	private transient int locked_on = 0;

	/** Periodic volume sample cache */
	private transient final PeriodicSampleCache vol_cache;

	/** Periodic scan sample cache */
	private transient final PeriodicSampleCache scn_cache;

	/** Periodic speed sample cache */
	private transient final PeriodicSampleCache spd_cache;

	/** Periodic volume sample cache (MOTORCYCLE class) */
	private transient final PeriodicSampleCache vol_mc_cache;

	/** Periodic volume sample cache (SHORT class) */
	private transient final PeriodicSampleCache vol_s_cache;

	/** Periodic volume sample cache (MEDIUM class) */
	private transient final PeriodicSampleCache vol_m_cache;

	/** Periodic volume sample cache (LONG class) */
	private transient final PeriodicSampleCache vol_l_cache;

	/** Volume from the last 30-second sample period.  FIXME: use
	 * vol_cache to get "last_volume" value. */
	private transient int last_volume = MISSING_DATA;

	/** Scans from the last 30-second sample period.  FIXME: use
	 * scn_cache to get "last_scans" value. */
	private transient int last_scans = MISSING_DATA;

	/** Speed from the last 30-second sample period.  FIXME: use
	 * spd_cache to get "last_speed" value. */
	private transient int last_speed = MISSING_DATA;

	/** Get the current volume */
	public int getVolume() {
		return isSampling() ? last_volume : MISSING_DATA;
	}

	/** Get the most recent sample count */
	@Override
	public int getCount() {
		return getVolume();
	}

	/** Get the current occupancy */
	public float getOccupancy() {
		if (isSampling() && last_scans != MISSING_DATA)
			return MAX_OCCUPANCY * (float) last_scans / MAX_C30;
		else
			return MISSING_DATA;
	}

	/** Get the current flow rate (vehicles per hour) */
	@Override
	public int getFlow() {
		int flow = getFlowRaw();
		return (flow >= 0) ? flow : getFlowFake();
	}

	/** Get the current raw (non-faked) flow rate (vehicles per hour) */
	protected int getFlowRaw() {
		int volume = getVolume();
		if (volume >= 0) {
			return Math.round(volume *
				SAMPLE_INTERVAL.per(Interval.HOUR));
		} else
			return MISSING_DATA;
	}

	/** Get the fake flow rate (vehicles per hour) */
	private int getFlowFake() {
		FakeDetector f = fake_det;
		return (f != null) ? f.getFlow() : MISSING_DATA;
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
		return isSampling() ? last_speed : MISSING_DATA;
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

	/** Handle a detector malfunction */
	private void malfunction(EventType event_type) {
		if (force_fail)
			return;
		if (isDetectorAutoFailEnabled())
			doForceFail();
		logEvent(new DetFailEvent(event_type, getName()));
	}

	/** Is detector auto-fail enabled? */
	private boolean isDetectorAutoFailEnabled() {
		return SystemAttrEnum.DETECTOR_AUTO_FAIL_ENABLE.getBoolean();
	}

	/** Force fail the detector */
	private void doForceFail() {
		try {
			doSetForceFail(true);
			notifyAttribute("forceFail");
		}
		catch (TMSException e) {
			e.printStackTrace();
		}
	}

	/** Store one volume sample for this detector.
	 * @param vol PeriodicSample containing volume data.
	 * @param vc Vehicle class. */
	public void storeVolume(PeriodicSample vol, VehLengthClass vc) {
		if (vc == null)
			storeVolume(vol);
		else {
			switch (vc) {
			case MOTORCYCLE:
				vol_mc_cache.add(vol);
				break;
			case SHORT:
				vol_s_cache.add(vol);
				break;
			case MEDIUM:
				vol_m_cache.add(vol);
				break;
			case LONG:
				vol_l_cache.add(vol);
				break;
			}
		}
	}

	/** Store one volume sample for this detector.
	 * @param vol PeriodicSample containing volume data. */
	public void storeVolume(PeriodicSample vol) {
		if (lane_type != LaneType.GREEN &&
		    vol.period == SAMPLE_PERIOD_SEC)
			testVolume(vol);
		vol_cache.add(vol);
		if (vol.period == SAMPLE_PERIOD_SEC) {
			last_volume = vol.value;
			/* FIXME: this shouldn't be needed */
			last_speed = MISSING_DATA;
		}
	}

	/** Test a volume sample with error detecting algorithms */
	private void testVolume(PeriodicSample vs) {
		if (vs.value > MAX_VOLUME)
			malfunction(EventType.DET_CHATTER);
		if (vs.value == 0) {
			no_hits += vs.period;
			if (no_hits > getNoHitThreshold().seconds())
				malfunction(EventType.DET_NO_HITS);
		} else
			no_hits = 0;
	}

	/** Get the volume "no hit" threshold */
	private Interval getNoHitThreshold() {
		if (isRamp()) {
			GeoLoc loc = lookupGeoLoc();
			if (loc != null && isReversibleLocationHack(loc))
				return new Interval(72, Interval.Units.HOURS);
		}
		return lane_type.no_hit_threshold;
	}

	/** Reversible lane name */
	static private final String REV = "I-394 Rev";

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
		int n_scans = occ.as60HzScans();
		if (occ.period == SAMPLE_PERIOD_SEC) {
			testScans(occ);
			last_scans = n_scans;
		}
		scn_cache.add(new PeriodicSample(occ.stamp,occ.period,n_scans));
	}

	/** Test an occupancy sample with error detecting algorithms */
	private void testScans(OccupancySample occ) {
		if (occ.value >= OccupancySample.MAX) {
			locked_on += occ.period;
			if (locked_on > getLockedOnThreshold().seconds())
				malfunction(EventType.DET_LOCKED_ON);
		} else
			locked_on = 0;
	}

	/** Get the scan "locked on" threshold */
	private Interval getLockedOnThreshold() {
		return lane_type.lock_on_threshold;
	}

	/** Store one speed sample for this detector.
	 * @param speed PeriodicSample containing speed data. */
	public void storeSpeed(PeriodicSample speed) {
		spd_cache.add(speed);
		if (speed.period == SAMPLE_PERIOD_SEC)
			last_speed = speed.value;
	}

	/** Flush buffered data to disk */
	public void flush(PeriodicSampleWriter writer) throws IOException {
		writer.flush(vol_cache, name);
		writer.flush(scn_cache, name);
		writer.flush(spd_cache, name);
		writer.flush(vol_mc_cache, name);
		writer.flush(vol_s_cache, name);
		writer.flush(vol_m_cache, name);
		writer.flush(vol_l_cache, name);
	}

	/** Purge all samples before a given stamp. */
	public void purge(long before) {
		vol_cache.purge(before);
		scn_cache.purge(before);
		spd_cache.purge(before);
		vol_mc_cache.purge(before);
		vol_s_cache.purge(before);
		vol_m_cache.purge(before);
		vol_l_cache.purge(before);
	}

	/** Vehicle event log */
	private transient final VehicleEventLog v_log;

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

	/** Log a gap in vehicle events.
	 */
	public void logGap() {
		v_log.logGap();
	}

	/** Bin 30-second sample data */
	public void binEventSamples() {
		last_volume = v_log.getVehicleCount();
		last_scans = v_log.getOccupancy().as60HzScans();
		last_speed = v_log.getSpeed();
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
}
