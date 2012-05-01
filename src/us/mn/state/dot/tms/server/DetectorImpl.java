/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Interval;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import static us.mn.state.dot.tms.server.Constants.FEET_PER_MILE;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.event.DetFailEvent;

/**
 * Detector for traffic data sampling
 *
 * @author Douglas Lau
 */
public class DetectorImpl extends DeviceImpl implements Detector {

	/** Default average detector field length */
	static private final float DEFAULT_FIELD_LENGTH = 22.0f;

	/** Valid density threshold for speed calculation */
	static private final float DENSITY_THRESHOLD = 1.2f;

	/** Maximum "realistic" volume for a 30-second sample */
	static private final int MAX_VOLUME = 37;

	/** Maximum occupancy value (100%) */
	static private final int MAX_OCCUPANCY = 100;

	/** Maximum number of scans in 30 seconds */
	static private final int MAX_S30 = 1800;

	/** Maximum number of scans in 300 seconds */
	static private final int MAX_S300 = 18000;

	/** Detector debug log */
	static protected final IDebugLog DET_LOG = new IDebugLog("detector");

	/** Sample period for detectors (seconds) */
	static protected final int SAMPLE_PERIOD_SEC = 30;

	/** Sample period (Milliseconds) */
	static protected final int SAMPLE_PERIOD_MS = SAMPLE_PERIOD_SEC * 1000;

	/** Create a cache for periodic volume data */
	static protected PeriodicSampleCache createVolumeCache(String n) {
		return new PeriodicSampleCache.EightBit(
			new SampleArchiveFactoryImpl(n, ".v30"),
			SAMPLE_PERIOD_SEC);
	}

	/** Create a cache for periodic scan data */
	static protected PeriodicSampleCache createScanCache(String n) {
		return new PeriodicSampleCache.SixteenBit(
			new SampleArchiveFactoryImpl(n, ".c30"),
			SAMPLE_PERIOD_SEC);
	}

	/** Create a cache for periodic speed data */
	static protected PeriodicSampleCache createSpeedCache(String n) {
		return new PeriodicSampleCache.EightBit(
			new SampleArchiveFactoryImpl(n, ".s30"),
			SAMPLE_PERIOD_SEC);
	}

	/** Create a vehicle event log */
	static protected VehicleEventLog createVehicleEventLog(String n) {
		return new VehicleEventLog(new SampleArchiveFactoryImpl(n,
			".vlog"));
	}

	/** Load all the detectors */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading detectors...");
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
		// Transients need to be initialized after all detectors are
		// loaded (for resolving fake detectors).
		namespace.findObject(SONAR_TYPE, new Checker<DetectorImpl>() {
			public boolean check(DetectorImpl d) {
				d.initTransients();
				return false;
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		if(controller != null)
			map.put("controller", controller.getName());
		map.put("pin", pin);
		if(r_node != null)
			map.put("r_node", r_node.getName());
		map.put("lane_type", (short)lane_type.ordinal());
		map.put("lane_number", lane_number);
		map.put("abandoned", abandoned);
		map.put("force_fail", force_fail);
		map.put("field_length", field_length);
		map.put("fake", fake);
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

	/** Create a new detector */
	public DetectorImpl(String n) throws TMSException, SonarException {
		super(n);
		vol_cache = createVolumeCache(n);
		scn_cache = createScanCache(n);
		spd_cache = createSpeedCache(n);
		v_log = createVehicleEventLog(n);
		initTransients();
	}

	/** Create a detector */
	protected DetectorImpl(String n, ControllerImpl c, int p, R_NodeImpl r,
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
		vol_cache = createVolumeCache(n);
		scn_cache = createScanCache(n);
		spd_cache = createSpeedCache(n);
		v_log = createVehicleEventLog(n);
	}

	/** Create a detector */
	protected DetectorImpl(Namespace ns, String n, String c, int p,
		String r, short lt, short ln, boolean a, boolean ff, float fl,
		String f, String nt)
	{
		this(n,(ControllerImpl)ns.lookupObject(Controller.SONAR_TYPE,c),
			p, (R_NodeImpl)ns.lookupObject(R_Node.SONAR_TYPE, r),
			lt, ln, a, ff, fl, f, nt);
	}

	/** Initialize the transient state */
	public void initTransients() {
		super.initTransients();
		if(r_node != null)
			r_node.addDetector(this);
		try {
			fake_det = createFakeDetector(fake);
		}
		catch(ChangeVetoException e) {
			DET_LOG.log("Invalid FAKE Detector: " + name +
				" (" + fake + ")");
			fake = null;
		}
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		super.doDestroy();
		if(r_node != null)
			r_node.removeDetector(this);
	}

	/** R_Node (roadway network node) */
	protected R_NodeImpl r_node;

	/** Set the r_node (roadway network node) */
	public void setR_Node(R_Node n) {
		r_node = (R_NodeImpl)n;
	}

	/** Set the r_node (roadway network node) */
	public void doSetR_Node(R_Node n) throws TMSException {
		if(n == r_node)
			return;
		store.update(this, "r_node", n);
		if(r_node != null)
			r_node.removeDetector(this);
		if(n instanceof R_NodeImpl)
			((R_NodeImpl)n).addDetector(this);
		setR_Node(n);
	}

	/** Get the r_node (roadway network node) */
	public R_Node getR_Node() {
		return r_node;
	}

	/** Lookup the geo location */
	public GeoLoc lookupGeoLoc() {
		R_Node n = r_node;
		if(n != null)
			return n.getGeoLoc();
		return null;
	}

	/** Lane type */
	protected LaneType lane_type = LaneType.NONE;

	/** Set the lane type */
	public void setLaneType(short t) {
		lane_type = LaneType.fromOrdinal(t);
	}

	/** Set the lane type */
	public void doSetLaneType(short t) throws TMSException {
		LaneType lt = LaneType.fromOrdinal(t);
		if(lt == lane_type)
			return;
		store.update(this, "lane_type", t);
		setLaneType(t);
	}

	/** Get the lane type */
	public short getLaneType() {
		return (short)lane_type.ordinal();
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
		if(io instanceof DetectorImpl) {
			DetectorImpl d = (DetectorImpl)io;
			GeoLoc loc = lookupGeoLoc();
			GeoLoc oloc = d.lookupGeoLoc();
			if(loc != null && oloc != null) {
				return GeoLocHelper.matches(loc, oloc) &&
					lane_number == d.lane_number &&
					!d.isVelocity() && d.isMainline();
			}
		}
		return false;
	}

	/** Lane number */
	protected short lane_number;

	/** Set the lane number */
	public void setLaneNumber(short l) {
		lane_number = l;
	}

	/** Set the lane number */
	public void doSetLaneNumber(short l) throws TMSException {
		if(l == lane_number)
			return;
		store.update(this, "lane_number", l);
		setLaneNumber(l);
	}

	/** Get the lane number */
	public short getLaneNumber() {
		return lane_number;
	}

	/** Abandoned status flag */
	protected boolean abandoned;

	/** Set the abandoned status */
	public void setAbandoned(boolean a) {
		abandoned = a;
	}

	/** Set the abandoned status */
	public void doSetAbandoned(boolean a) throws TMSException {
		if(a == abandoned)
			return;
		store.update(this, "abandoned", a);
		setAbandoned(a);
	}

	/** Get the abandoned status */
	public boolean getAbandoned() {
		return abandoned;
	}

	/** Force Fail status flag */
	protected boolean force_fail;

	/** Set the Force Fail status */
	public void setForceFail(boolean f) {
		force_fail = f;
	}

	/** Set the Force Fail status */
	public void doSetForceFail(boolean f) throws TMSException {
		if(f == force_fail)
			return;
		store.update(this, "force_fail", f);
		setForceFail(f);
	}

	/** Get the Force Fail status */
	public boolean getForceFail() {
		return force_fail;
	}

	/** Check if the detector is currently 'failed' */
	public boolean isFailed() {
		return force_fail || last_volume == MISSING_DATA;
	}

	/** Check if the detector is currently sampling data */
	public boolean isSampling() {
		return isActive() && !force_fail;
	}

	/** Average detector field length */
	protected float field_length = DEFAULT_FIELD_LENGTH;

	/** Set the average field length */
	public void setFieldLength(float f) {
		field_length = f;
	}

	/** Set the average field length */
	public void doSetFieldLength(float f) throws TMSException {
		if(f == field_length)
			return;
		store.update(this, "field_length", f);
		setFieldLength(f);
	}

	/** Get the average field length */
	public float getFieldLength() {
		return field_length;
	}

	/** Fake detector expression */
	protected String fake = null;

	/** Fake detector to use if detector is failed */
	protected transient FakeDetector fake_det;

	/** Create a fake detector object */
	static protected FakeDetector createFakeDetector(String f)
		throws ChangeVetoException
	{
		try {
			if(f != null)
				return new FakeDetector(f, namespace);
			else
				return null;
		}
		catch(NumberFormatException e) {
			throw new ChangeVetoException(
				"Invalid detector number");
		}
		catch(IndexOutOfBoundsException e) {
			throw new ChangeVetoException(
				"Bad detector #:" + e.getMessage());
		}
	}
	
	/** Set the fake expression */
	public void setFake(String f) {
		fake = f;
	}

	/** Set the fake expression */
	public void doSetFake(String f) throws TMSException {
		FakeDetector fd = createFakeDetector(f);
		if(fd != null) {
			// Normalize the fake detector string
			f = fd.toString();
			if(f.equals("")) {
				fd = null;
				f = null;
			}
		}
		if(f == fake || (f != null && f.equals(fake)))
			return;
		store.update(this, "fake", f);
		fake_det = fd;
		setFake(f);
	}

	/** Get the fake expression */
	public String getFake() {
		return fake;
	}

	/** Calculate the fake data if necessary */
	public void calculateFakeData() {
		FakeDetector f = fake_det;
		if(f != null)
			f.calculate();
	}

	/** Request a device operation */
	public void setDeviceRequest(int r) {
		// no detector device requests
	}

	/** Accumulator for number of samples locked on (scans) */
	protected transient int locked_on = 0;

	/** Accumulator for number of samples with no hits (volume) */
	protected transient int no_hits = 0;

	/** Volume from the last 30-second sample period */
	protected transient int last_volume = MISSING_DATA;

	/** Scans from the last 30-second sample period */
	protected transient int last_scans = MISSING_DATA;

	/** Speed from the last 30-second sample period */
	protected transient int last_speed = MISSING_DATA;

	/** Get the current volume */
	public int getVolume() {
		if(isSampling())
			return last_volume;
		else
			return MISSING_DATA;
	}

	/** Get the current occupancy */
	public float getOccupancy() {
		if(isSampling() && last_scans != MISSING_DATA)
			return MAX_OCCUPANCY * (float)last_scans / MAX_S30;
		else
			return MISSING_DATA;
	}

	/** Get the current flow rate (vehicles per hour) */
	public float getFlow() {
		int flow = getFlowRaw();
		if(flow != MISSING_DATA)
			return flow;
		else
			return getFlowFake();
	}

	/** Get the current raw flow rate (vehicles per hour) */
	protected int getFlowRaw() {
		int volume = getVolume();
		if(volume >= 0)
			return volume * Interval.HOUR / SAMPLE_PERIOD_SEC;
		else
			return MISSING_DATA;
	}

	/** Get the fake flow rate (vehicles per hour) */
	protected float getFlowFake() {
		FakeDetector f = fake_det;
		if(f != null)
			return f.getFlow();
		else
			return MISSING_DATA;
	}

	/** Get the current density (vehicles per mile) */
	public float getDensity() {
		float density = getDensityFromFlowSpeed();
		if(density != MISSING_DATA)
			return density;
		else
			return getDensityFromOccupancy();
	}

	/** Get the density from flow and speed (vehicles per mile) */
	protected float getDensityFromFlowSpeed() {
		float speed = getSpeedRaw();
		if(speed > 0) {
			int flow = getFlowRaw();
			if(flow > MISSING_DATA)
				return flow / speed;
		}
		return MISSING_DATA;
	}

	/** Get the density from occupancy (vehicles per mile) */
	protected float getDensityFromOccupancy() {
		float occ = getOccupancy();
		if(occ == MISSING_DATA || field_length <= 0)
			return MISSING_DATA;
		return occ * FEET_PER_MILE / field_length / MAX_OCCUPANCY;
	}

	/** Get the current speed (miles per hour) */
	public float getSpeed() {
		float speed = getSpeedRaw();
		if(speed > 0)
			return speed;
		speed = getSpeedEstimate();
		if(speed > 0)
			return speed;
		else
			return getSpeedFake();
	}

	/** Get the current raw speed (miles per hour) */
	protected float getSpeedRaw() {
		if(isSampling())
			return last_speed;
		else
			return MISSING_DATA;
	}

	/** Get speed estimate based on flow / density */
	protected float getSpeedEstimate() {
		int flow = getFlowRaw();
		if(flow <= 0)
			return MISSING_DATA;
		float density = getDensityFromOccupancy();
		if(density <= DENSITY_THRESHOLD)
			return MISSING_DATA;
		return flow / density;
	}

	/** Get fake speed (miles per hour) */
	protected float getSpeedFake() {
		FakeDetector f = fake_det;
		if(f != null)
			return f.getSpeed();
		else
			return MISSING_DATA;
	}

	/** Handle a detector malfunction */
	protected void malfunction(EventType event_type) {
		if(force_fail)
			return;
		if(isDetectorAutoFailEnabled())
			doForceFail();
		DetFailEvent ev = new DetFailEvent(event_type, getName());
		try {
			ev.doStore();
		}
		catch(TMSException e) {
			e.printStackTrace();
		}
	}

	/** Is detector auto-fail enabled? */
	protected boolean isDetectorAutoFailEnabled() {
		return SystemAttrEnum.DETECTOR_AUTO_FAIL_ENABLE.getBoolean();
	}

	/** Force fail the detector */
	protected void doForceFail() {
		try {
			doSetForceFail(true);
			notifyAttribute("forceFail");
		}
		catch(TMSException e) {
			e.printStackTrace();
		}
	}

	/** Get the volume "no hit" threshold */
	private Interval getNoHitThreshold() {
		if(isRamp()) {
			GeoLoc loc = lookupGeoLoc();
			if(loc != null && isReversibleLocationHack(loc))
				return Interval.hour(72);
		}
		return lane_type.no_hit_threshold;
	}

	/** Reversible lane name */
	static protected final String REV = "I-394 Rev";

	/** Check if a location is on a reversible road */
	protected boolean isReversibleLocationHack(GeoLoc loc) {
		// FIXME: this is a Mn/DOT-specific hack
		Road roadway = loc.getRoadway();
		if(roadway != null && REV.equals(roadway.getName()))
			return true;
		Road cross = loc.getCrossStreet();
		if(cross != null && REV.equals(cross.getName()))
			return true;
		return false;
	}

	/** Get the scan "locked on" threshold */
	private Interval getLockedOnThreshold() {
		return lane_type.lock_on_threshold;
	}

	/** Test the detector volume data with error detecting algorithms */
	protected void testVolume(int volume) {
		if(volume > MAX_VOLUME)
			malfunction(EventType.DET_CHATTER);
		if(volume == 0) {
			no_hits++;
			int secs = no_hits * SAMPLE_PERIOD_SEC;
			if(secs > getNoHitThreshold().seconds())
				malfunction(EventType.DET_NO_HITS);
		} else
			no_hits = 0;
	}

	/** Test the detector scan data with error detecting algorithms */
	protected void testScans(int scans) {
		if(scans >= MAX_S30) {
			locked_on++;
			int secs = locked_on * SAMPLE_PERIOD_SEC;
			if(secs > getLockedOnThreshold().seconds())
				malfunction(EventType.DET_LOCKED_ON);
		} else
			locked_on = 0;
	}

	/** Test the detector data with error detecting algorithms */
	protected void testData(int volume, int scans) {
		if(lane_type != LaneType.GREEN) {
			testVolume(volume);
			testScans(scans);
		}
	}

	/** Store 30-second data for this detector.
	 * @param stamp Time stamp after end of sample period.
	 * @param volume Vehicle count.
	 * @param oscans Occupancy scans (ranging from 0 to max_scans)
	 * @param max_scans Maximum occupancy scans */
	public void storeData30Second(long stamp, int volume, int oscans,
		int max_scans)
	{
		int scans = scansToS30(oscans, max_scans);
		testData(volume, scans);
		vol_cache.addSample(new PeriodicSample(stamp,
			SAMPLE_PERIOD_SEC, volume));
		scn_cache.addSample(new PeriodicSample(stamp,
			SAMPLE_PERIOD_SEC, scans));
		last_volume = volume;
		last_scans = scans;
		last_speed = MISSING_DATA;
	}

	/** Convert protocol-specific scans to .s30 (0-1800) scans */
	static private int scansToS30(int scans, int max_scans) {
		assert(max_scans > 0);
		if(scans >= 0)
			return Math.round((float)scans / max_scans * MAX_S30);
		else
			return MISSING_DATA;
	}

	/** Periodic volume sample cache */
	protected transient final PeriodicSampleCache vol_cache;

	/** Periodic scan sample cache */
	protected transient final PeriodicSampleCache scn_cache;

	/** Periodic speed sample cache */
	protected transient final PeriodicSampleCache spd_cache;

	/** Store 30-second speed sample for this detector */
	public void storeSpeed30Second(long stamp, int speed) {
		spd_cache.addSample(new PeriodicSample(stamp,
			SAMPLE_PERIOD_SEC, speed));
		last_speed = speed;
	}

	/** Store 5-minute data for this detector.
	 * @param stamp Time stamp after end of sample period.
	 * @param volume Vehicle count.
	 * @param oscans Occupancy scans (ranging from 0 to max_scans)
	 * @param max_scans Maximum occupancy scans. */
	public void storeData5Minute(long stamp, int volume, int oscans,
		int max_scans)
	{
		int scans = scansToS300(oscans, max_scans);
		vol_cache.addSample(new PeriodicSample(stamp,
			SAMPLE_PERIOD_SEC * 10, volume));
		scn_cache.addSample(new PeriodicSample(stamp,
			SAMPLE_PERIOD_SEC * 10, scans));
	}

	/** Convert protocol-specific scans to .s300 (0-18000) scans */
	static private int scansToS300(int scans, int max_scans) {
		assert(max_scans > 0);
		if(scans >= 0)
			return Math.round((float)scans / max_scans * MAX_S300);
		else
			return MISSING_DATA;
	}

	/** Flush buffered data to disk */
	public void flush() {
		try {
			vol_cache.flush();
			scn_cache.flush();
			spd_cache.flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/** Vehicle event log */
	protected transient final VehicleEventLog v_log;

	/** Count of vehicles in current sampling period */
	protected transient int ev_vehicles = 0;

	/** Total vehicle duration (milliseconds) in current sampling period */
	protected transient int ev_duration = 0;

	/** Count of sampled speed events in current sampling period */
	protected transient int ev_n_speed = 0;

	/** Sum of all vehicle speeds (mph) in current sampling period */
	protected transient int ev_speed = 0;

	/** Log a vehicle detection event.
	 * @param stamp Timestamp of detection event.
	 * @param duration Event duration in milliseconds.
	 * @param headway Headway since last event in milliseconds.
	 * @param speed Speed in miles per hour. */
	public void logVehicle(final Calendar stamp, final int duration,
		final int headway, final int speed)
	{
		ev_vehicles++;
		ev_duration += duration;
		if(speed > 0) {
			ev_n_speed++;
			ev_speed += speed;
		}
		MainServer.FLUSH.addJob(new Job() {
			public void perform() throws IOException {
				v_log.logVehicle(stamp, duration, headway,
					speed);
			}
		});
	}

	/** Log a gap in vehicle events.
	 */
	public void logGap() {
		MainServer.FLUSH.addJob(new Job() {
			public void perform() throws IOException {
				v_log.logGap();
			}
		});
	}

	/** Bin 30-second sample data */
	public void binEventSamples() {
		last_volume = ev_vehicles;
		last_scans = scansToS30(ev_duration, SAMPLE_PERIOD_MS);
		last_speed = calculate_speed();
		ev_vehicles = 0;
		ev_duration = 0;
		ev_n_speed = 0;
		ev_speed = 0;
	}

	/** Calculate the average vehicle speed */
	protected int calculate_speed() {
		if(ev_n_speed > 0 && ev_speed > 0)
			return ev_speed / ev_n_speed;
		else
			return MISSING_DATA;
	}

	/** Print a single detector as an XML element */
	public void printXmlElement(PrintWriter out) {
		LaneType lt = lane_type;
		short lane = getLaneNumber();
		float field = getFieldLength();
		String l = DetectorHelper.getLabel(this);
		out.print("<detector");
		out.print(XmlWriter.createAttribute("name", name));
		if(!l.equals("FUTURE"))
			out.print(XmlWriter.createAttribute("label", l));
		if(abandoned)
			out.print(XmlWriter.createAttribute("abandoned", "t"));
		if(lt != LaneType.NONE && lt != LaneType.MAINLINE) {
			out.print(XmlWriter.createAttribute("category",
				lt.suffix));
		}
		if(lane > 0)
			out.print(XmlWriter.createAttribute("lane", lane));
		if(field != DEFAULT_FIELD_LENGTH)
			out.print(XmlWriter.createAttribute("field", field));
		Controller c = getController();
		if(c != null) 
			out.print(XmlWriter.createAttribute("controller", c.getName()));
		out.println("/>");
	}

	/** Print the current sample as an XML element */
	public void printSampleXml(PrintWriter out) {
		if(abandoned || !isSampling())
			return;
		int flow = getFlowRaw();
		int speed = Math.round(getSpeed());
		float occ = getOccupancy();
		out.print("\t<sample");
		out.print(XmlWriter.createAttribute("sensor", name));
		if(flow != MISSING_DATA)
			out.print(XmlWriter.createAttribute("flow", flow));
		if(isMainline() && speed > 0)
			out.print(XmlWriter.createAttribute("speed", speed));
		if(occ >= 0) {
			out.print(XmlWriter.createAttribute("occ",
				formatFloat(occ, 2)));
		}
		out.println("/>");
	}
}
