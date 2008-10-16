/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.event.DetFailEvent;
import us.mn.state.dot.tms.event.EventType;

/**
 * Detector for traffic data sampling
 *
 * @author Douglas Lau
 */
public class DetectorImpl extends Device2Impl implements Detector,
	Comparable<DetectorImpl>
{
	/** Detector debug log */
	static protected final DebugLog DET_LOG = new DebugLog("detector");

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
				namespace.add(new DetectorImpl(namespace,
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
		data_cache = new DataCache(name);
		if(r_node != null)
			r_node.addDetector(this);
		try {
			if(fake != null)
				fake_det = createFakeDetector(fake);
		}
		catch(ChangeVetoException e) {
			DET_LOG.log("Invalid FAKE Detector: " + name +
				" (" + fake + ")");
			fake = null;
		}
		fake_det = null;
	}

	/** Compare to another detector */
	public int compareTo(DetectorImpl other) {
		return DetectorHelper.compare(this, other);
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
		if(n != null)
			store.update(this, "r_node", n.getName());
		else
			store.update(this, "r_node", null);
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

	/** Notify SONAR clients of changes to "force_fail" attribute */
	public void notifyForceFail() {
		if(MainServer.server != null) {
			String[] ff = new String[] {
				String.valueOf(force_fail)
			};
			MainServer.server.setAttribute(this, "force_fail", ff);
		}
	}

	/** Check if the detector is currently 'failed' */
	public boolean isFailed() {
		return force_fail || last_volume == Constants.MISSING_DATA;
	}

	/** Check if the detector is currently sampling data */
	public boolean isSampling() {
		return isActive() && !force_fail;
	}

	/** Average detector field length */
	protected float field_length = Constants.DEFAULT_FIELD_LENGTH;

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
			return new FakeDetector(f, namespace);
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
		FakeDetector fd = null;
		if(f != null) {
			fd = createFakeDetector(f);
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

	/** Accumulator for number of samples locked on (scans) */
	protected transient int locked_on = 0;

	/** Accumulator for number of samples with no hits (volume) */
	protected transient int no_hits = 0;

	/** Volume from the last 30-second sample period */
	protected transient int last_volume = Constants.MISSING_DATA;

	/** Scans from the last 30-second sample period */
	protected transient int last_scans = Constants.MISSING_DATA;

	/** Speed from the last 30-second sample period */
	protected transient int last_speed = Constants.MISSING_DATA;

	/** Get the current volume */
	public float getVolume() {
		if(isSampling())
			return last_volume;
		else
			return Constants.MISSING_DATA;
	}

	/** Get the current occupancy */
	public float getOccupancy() {
		if(isSampling() && last_scans != Constants.MISSING_DATA)
			return Constants.MAX_OCCUPANCY *
				(float)last_scans / Constants.MAX_SCANS;
		else
			return Constants.MISSING_DATA;
	}

	/** Get the current flow rate (vehicles per hour) */
	public float getFlow() {
		float flow = getFlowRaw();
		if(flow != Constants.MISSING_DATA)
			return flow;
		else
			return getFlowFake();
	}

	/** Get the current raw flow rate (vehicles per hour) */
	protected float getFlowRaw() {
		float volume = getVolume();
		if(volume >= 0)
			return volume * Constants.SAMPLES_PER_HOUR;
		else
			return Constants.MISSING_DATA;
	}

	/** Get the fake flow rate (vehicles per hour) */
	protected float getFlowFake() {
		FakeDetector f = fake_det;
		if(f != null)
			return f.getFlow();
		else
			return Constants.MISSING_DATA;
	}

	/** Get the current density (vehicles per mile) */
	public float getDensity() {
		float density = getDensityFromFlowSpeed();
		if(density != Constants.MISSING_DATA)
			return density;
		else
			return getDensityFromOccupancy();
	}

	/** Get the density from flow and speed (vehicles per mile) */
	protected float getDensityFromFlowSpeed() {
		float speed = getSpeedRaw();
		if(speed > 0) {
			float flow = getFlowRaw();
			if(flow > Constants.MISSING_DATA)
				return flow / speed;
		}
		return Constants.MISSING_DATA;
	}

	/** Get the density from occupancy (vehicles per mile) */
	protected float getDensityFromOccupancy() {
		float occ = getOccupancy();
		if(occ == Constants.MISSING_DATA || field_length <= 0)
			return Constants.MISSING_DATA;
		return occ * Constants.FEET_PER_MILE / field_length /
			Constants.MAX_OCCUPANCY;
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
			return Constants.MISSING_DATA;
	}

	/** Get speed estimate based on flow / density */
	protected float getSpeedEstimate() {
		float flow = getFlowRaw();
		if(flow <= 0)
			return Constants.MISSING_DATA;
		float density = getDensityFromOccupancy();
		if(density <= Constants.DENSITY_THRESHOLD)
			return Constants.MISSING_DATA;
		return flow / density;
	}

	/** Get fake speed (miles per hour) */
	protected float getSpeedFake() {
		FakeDetector f = fake_det;
		if(f != null)
			return f.getSpeed();
		else
			return Constants.MISSING_DATA;
	}

	/** Force fail detector and log the cause */
	protected void malfunction(EventType event_type) {
		if(force_fail)
			return;
		try {
			doSetForceFail(true);
			notifyForceFail();
		}
		catch(TMSException e) {
			e.printStackTrace();
			return;
		}
		DetFailEvent ev = new DetFailEvent(event_type, getName());
		try {
			ev.doStore();
		}
		catch(TMSException e) {
			e.printStackTrace();
		}
	}

	/** Get the volume "no hit" threshold (seconds) */
	protected int getNoHitThreshold() {
		if(isRamp()) {
			GeoLoc loc = lookupGeoLoc();
			if(loc != null && isReversibleLocationHack(loc))
				return 72 * Interval.HOUR;
		}
		return lane_type.no_hit_threshold;
	}

	/** Reversible lane name */
	static protected final String REV = "I-394 Rev";

	/** Check if a location is on a reversible road */
	protected boolean isReversibleLocationHack(GeoLoc loc) {
		// FIXME: this is a Mn/DOT-specific hack
		Road freeway = loc.getFreeway();
		if(freeway != null && REV.equals(freeway.getName()))
			return true;
		Road cross = loc.getCrossStreet();
		if(cross != null && REV.equals(cross.getName()))
			return true;
		return false;
	}

	/** Get the scan "locked on" threshold (seconds) */
	protected int getLockedOnThreshold() {
		return lane_type.lock_on_threshold;
	}

	/** Test the detector volume data with error detecting algorithms */
	protected void testVolume(int volume) {
		if(volume > Constants.MAX_VOLUME)
			malfunction(EventType.DET_CHATTER);
		if(volume == 0) {
			no_hits++;
			int secs = no_hits * Constants.SECONDS_PER_SAMPLE;
			if(secs > getNoHitThreshold())
				malfunction(EventType.DET_NO_HITS);
		} else
			no_hits = 0;
	}

	/** Test the detector scan data with error detecting algorithms */
	protected void testScans(int scans) {
		if(scans >= Constants.MAX_SCANS) {
			locked_on++;
			int secs = locked_on * Constants.SECONDS_PER_SAMPLE;
			if(secs > getLockedOnThreshold())
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

	/** Data cache */
	protected transient DataCache data_cache;

	/** Store 30-second data for this detector */
	public void storeData30Second(Calendar stamp, int volume, int scans) {
		testData(volume, scans);
		try {
			data_cache.write(stamp, volume, scans);
		}
		catch(IndexOutOfBoundsException e) {
			DET_LOG.log("CACHE OVERFLOW for detector " + name);
		}
		last_volume = volume;
		last_scans = scans;
		last_speed = Constants.MISSING_DATA;
	}

	/** Store 30-second speed for this detector */
	public void storeSpeed30Second(Calendar stamp, int speed) {
		try {
			data_cache.writeSpeed(stamp, speed);
		}
		catch(IndexOutOfBoundsException e) {
			DET_LOG.log("CACHE OVERFLOW for detector " + name);
		}
		last_speed = speed;
	}

	/** Store 5-minute data for this detector */
	public void storeData5Minute(Calendar stamp, int volume, int scans)
		throws IOException
	{
		data_cache.merge(stamp, volume, scans);
	}

	/** Flush buffered data from before the given time stamp to disk */
	public void flush(Calendar stamp) {
		try {
			data_cache.flush(stamp);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/** Maximum logged headway is 90 seconds */
	static protected final int MAX_HEADWAY = 90 * 1000;

	static protected final DateFormat F_STAMP =
		new SimpleDateFormat("HH:mm:ss");

	protected transient Calendar p_stamp;

	/** Format a vehicle detection event */
	protected String formatEvent(Calendar stamp, int duration, int headway,
		int speed)
	{
		if(stamp == null) {
			p_stamp = null;
			return "*\n";
		}
		boolean log_stamp = false;
		StringBuffer b = new StringBuffer();
		if(duration > 0)
			b.append(duration);
		else
			b.append('?');
		b.append(',');
		if(headway > 0 && headway <= MAX_HEADWAY)
			b.append(headway);
		else {
			b.append('?');
			log_stamp = true;
		}
		if(p_stamp == null || (stamp.get(Calendar.HOUR) !=
			p_stamp.get(Calendar.HOUR)))
		{
			log_stamp = true;
		}
		b.append(',');
		p_stamp = stamp;
		if(log_stamp) {
			if(headway > 0)
				b.append(F_STAMP.format(stamp.getTime()));
			else
				p_stamp = null;
		}
		b.append(',');
		if(speed > 0)
			b.append(speed);
		while(b.charAt(b.length() - 1) == ',')
			b.setLength(b.length() - 1);
		b.append('\n');
		return b.toString();
	}

	/** Log a vehicle detection event */
	public void logEvent(final Calendar stamp, int duration, int headway,
		int speed)
	{
		final String line = formatEvent(stamp, duration, headway,
			speed);
		TMSImpl.FLUSH.addJob(new Job() {
			public void perform() throws IOException {
				EventLogger.print(stamp, name, line);
			}
		});
	}

	/** Print a single detector as an XML element */
	public void printXmlElement(PrintWriter out) {
		LaneType lt = lane_type;
		short lane = getLaneNumber();
		float field = getFieldLength();
		String l = XmlWriter.replaceEntities(
			DetectorHelper.getLabel(this));
		// NOTE: the 'D' is needed for XML validity
		out.print("<detector index='D" + name + "' ");
		if(!l.equals("FUTURE"))
			out.print("label='" + l + "' ");
		if(lt != LaneType.NONE && lt != LaneType.MAINLINE)
			out.print("category='" + lt.suffix + "' ");
		if(lane > 0)
			out.print("lane='" + lane + "' ");
		if(field != Constants.DEFAULT_FIELD_LENGTH)
			out.print("field='" + field + "' ");
		out.println("/>");
	}

	/** Print the current sample as an XML element */
	public void printSampleXmlElement(PrintWriter out) {
		if(abandoned || !isSampling())
			return;
		int flow = Math.round(getFlowRaw());
		int speed = Math.round(getSpeed());
		// NOTE: the 'D' is needed for XML validity
		out.print("\t<sample sensor='D" + name);
		if(flow != Constants.MISSING_DATA)
			out.print("' flow='" + flow);
		if(isMainline() && speed > 0)
			out.print("' speed='" + speed);
		out.println("'/>");
	}
}
