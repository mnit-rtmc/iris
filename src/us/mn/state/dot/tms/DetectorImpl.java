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
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.log.DetectorMalfunctionEvent;
import us.mn.state.dot.tms.log.Log;
import us.mn.state.dot.vault.FieldMap;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * Detector for traffic data sampling
 *
 * @author Douglas Lau
 */
public class DetectorImpl extends DeviceImpl implements Detector, Constants,
	Comparable<DetectorImpl>, Storable
{
	/** ObjectVault table name */
	static public final String tableName = "detector";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Detector debug log */
	static protected final DebugLog DET_LOG = new DebugLog("detector");

	/** Create a new detector */
	public DetectorImpl( int i ) throws RemoteException {
		super();
		index = i;
		fieldLength = DEFAULT_FIELD_LENGTH;
		fake = "";
		locked_on = 0;
		no_hits = 0;
		last_volume = MISSING_DATA;
		last_scans = MISSING_DATA;
		last_speed = MISSING_DATA;
		data_cache = new DataCache(getKey());
		fake_det = null;
	}

	/** Create a detector from an ObjectVault field map */
	protected DetectorImpl( FieldMap fields ) throws RemoteException {
		super(fields);
		index = fields.getInt( "index" );
		locked_on = 0;
		no_hits = 0;
		last_volume = MISSING_DATA;
		last_scans = MISSING_DATA;
		last_speed = MISSING_DATA;
		data_cache = new DataCache(getKey());
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		// FIXME: implement this for SONAR
		return null;
	}

	/** Initialize the transient state */
	public void initTransients() throws ObjectVaultException,
		TMSException, RemoteException
	{
		super.initTransients();
		try {
			fake_det = createFakeDetector(fake);
		}
		catch(ChangeVetoException e) {
			DET_LOG.log("Invalid FAKE Detector: " + index +
				" (" + fake + ")");
			fake = "";
			fake_det = null;
		}
	}

	/** Get a padded string version of the detector index */
	String getId() {
		StringBuffer buf = new StringBuffer().append( index );
		while(buf.length() < 4)
			buf.insert(0, ' ');
		return buf.toString();
	}

	/** Get the primary key name */
	public String getKeyName() {
		return "index";
	}

	/** Get the detector key */
	public String getKey() {
		return Integer.toString(index);
	}

	/** Get a String representation of the detector */
	public String toString() {
		StringBuffer buffer = new StringBuffer().append( index );
		while(buffer.length() < 4)
			buffer.insert(0, ' ');
		buffer.append("  ").append(getLabel(false));
		return buffer.toString();
	}

	/** Compare for sorting by lane number */
	public int compareTo(DetectorImpl other) {
		int l = laneNumber - other.laneNumber;
		if(l == 0)
			return index - other.index;
		else
			return l;
	}

	/** Get a String representation of the detector */
	public String getLabel() throws RemoteException {
		return toString();
	}

	/** Get the detector label */
	public String getLabel(boolean statName) {
		RoadImpl freeway = location.lookupFreeway();
		RoadImpl cross = location.lookupCrossStreet();
		if(freeway == null || cross == null) {
			if(isActive())
				return "INVALID";
			else
				return "FUTURE";
		}
		short freeDir = location.getFreeDir();
		short crossDir = location.getCrossDir();
		short crossMod = location.getCrossMod();
		StringBuffer buffer = new StringBuffer();
		buffer.append(freeway.getAbbrev());
		buffer.append("/");
		if(crossDir > 0)
			buffer.append(DIRECTION[crossDir]);
		buffer.append(MOD_SHORT[crossMod]);
		buffer.append(cross.getAbbrev());
		buffer.append(DIR_FREEWAY[freeDir]);
		if( !statName ) {
			if(laneType == REVERSIBLE) {
				if(freeDir == Road.EAST_WEST) {
					if(laneNumber == 1)
						buffer.append('S');
					if(laneNumber == 2)
						buffer.append('N');
				}
			}
			else if(isMainline()) {
				if(laneNumber > 0)
					buffer.append(laneNumber);
				buffer.append(LANE_SUFFIX[laneType]);
			}
			else {
				buffer.append(LANE_SUFFIX[laneType]);
				if(laneNumber > 0)
					buffer.append(laneNumber);
			}
			if(abandoned)
				buffer.append("-ABND");
		}
		return buffer.toString();
	}

	/** Is the detector available for a controller? */
	public boolean isAvailable() {
		if(abandoned)
			return false;
		if(laneType == NONE)
			return false;
		return super.isAvailable();
	}

	/** Set the controller for this device */
	public void setController(Controller c) throws TMSException {
		super.setController(c);
		try {
			detList.update(index);
		}
		catch(IndexOutOfBoundsException e) {
			// During startup, detList is still empty
		}
	}

	/** Is the detector free to be linked to a station? */
	public boolean isFreeMainline() {
		if(abandoned || isActive())
			return false;
		if(!isMainline())
			return false;
		if(getStation() != null)
			return false;
		return true;
	}

	/** Detector index */
	protected final int index;

	/** Get the detector index */
	public int getIndex() { return index; }

	/** Get the station which contains this detector */
	public Station getStation() {
		return statMap.getStation(this);
	}

	/** Lane type */
	protected short laneType;

	/** Set the lane type */
	public void setLaneType(short t) throws TMSException {
		if(t == laneType)
			return;
		if(t < 0 || t > LANE_TYPE.length)
			throw new ChangeVetoException("Invalid lane type");
		if(!isMainlineType(t) && getStation() != null)
			throw new ChangeVetoException("Station link exists");
		_setLaneType(t);
	}

	/** Set the lane type */
	protected synchronized void _setLaneType(short t) throws TMSException {
		if(t == laneType)
			return;
		store.update(this, "laneType", t);
		laneType = t;
	}

	/** Get the lane type */
	public short getLaneType() { return laneType; }

	/** Is this a mailline detector? (auxiliary, cd, etc.) */
	public boolean isMainline() {
		return isMainlineType(laneType);
	}

	/** Is this a station detector? (mainline, non-HOV) */
	public boolean isStation() {
		return laneType == MAINLINE;
	}

	/** Is the given lane type a mainline? (auxiliary, cd, etc.) */
	static public boolean isMainlineType(int t) {
		return t == MAINLINE || t == AUXILIARY || t == CD_LANE ||
			t == REVERSIBLE || t == VELOCITY ||
			t == HOV || t == HOT;
	}

	/** Is this a CD lane detector? */
	public boolean isCD() {
		return laneType == CD_LANE;
	}

	/** Is this a station or CD detector? */
	public boolean isStationOrCD() {
		return isStation() || isCD();
	}

	/** Is this a ramp detector? (merge, queue, exit, bypass) */
	public boolean isRamp() {
		return isRampType(laneType);
	}

	/** Is the given lane type a ramp? (merge, queuee, exit, bypass) */
	static public boolean isRampType(int t) {
		return t == MERGE || t == QUEUE || t == EXIT ||
			t == BYPASS || t == PASSAGE || t == OMNIBUS;
	}

	/** Is this an onramp detector? */
	public boolean isOnRamp() {
		return isOnRampType(laneType);
	}

	/** Is the given lane type an on-ramp? (merge, queue, bypass ) */
	static public boolean isOnRampType(int t) {
		return t == MERGE || t == QUEUE || t == BYPASS ||
			t == PASSAGE || t == OMNIBUS || t == GREEN;
	}

	/** Is this an offRamp detector? */
	public boolean isOffRamp() {
		return laneType == EXIT;
	}

	/** Is this a velocity detector? */
	public boolean isVelocity() {
		return laneType == VELOCITY;
	}

	/** Test if the given detector is a speed pair with this detector */
	public boolean isSpeedPair(Object o) {
		if(o instanceof DetectorImpl) {
			DetectorImpl d = (DetectorImpl)o;
			return location.matches(d.location) &&
				laneNumber == d.laneNumber &&
				!d.isVelocity() && d.isMainline();
		}
		return false;
	}

	/** Lane number */
	protected short laneNumber;

	/** Set the lane number */
	public synchronized void setLaneNumber(short l) throws TMSException {
		if(l == laneNumber)
			return;
		store.update(this, "laneNumber", l);
		laneNumber = l;
	}

	/** Get the lane number */
	public short getLaneNumber() { return laneNumber; }

	/** Abandoned status flag */
	protected boolean abandoned;

	/** Set the abandoned status */
	public synchronized void setAbandoned(boolean a) throws TMSException {
		if(a == abandoned)
			return;
		store.update(this, "abandoned", a);
		abandoned = a;
	}

	/** Get the abandoned status */
	public boolean isAbandoned() { return abandoned; }

	/** Force Fail status flag */
	protected boolean forceFail;

	/** Set the Force Fail status */
	public synchronized void setForceFail(boolean f) throws TMSException {
		if(f == forceFail)
			return;
		store.update(this, "forceFail", f);
		forceFail = f;
	}

	/** Get the Force Fail status */
	public boolean getForceFail() { return forceFail; }

	/** Check if the detector is currently 'failed' */
	public boolean isFailed() {
		return forceFail || last_volume == MISSING_DATA;
	}

	/** Check if the detector is currently sampling data */
	public boolean isSampling() {
		return isActive() && !forceFail;
	}

	/** Average detector field length */
	protected float fieldLength;

	/** Set the average field length */
	public synchronized void setFieldLength(float field)
		throws TMSException
	{
		if(field == fieldLength)
			return;
		store.update(this, "fieldLength", field);
		fieldLength = field;
	}

	/** Get the average field length */
	public float getFieldLength() { return fieldLength; }

	/** Fake detector expression */
	protected String fake;

	/** Fake detector to use if detector is failed */
	protected transient FakeDetector fake_det;

	/** Create a fake detector object */
	static protected FakeDetector createFakeDetector(String f)
		throws ChangeVetoException
	{
		if(f.equals(""))
			return null;
		else {
			try {
				return new FakeDetector(f);
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
	}

	/** Set the fake detector */
	public synchronized void setFakeDetector(String f) throws TMSException,
		RemoteException
	{
		if(f.equals(fake))
			return;
		FakeDetector fd = createFakeDetector(f);
		// Normalize the fake detector string
		if(fd != null)
			f = fd.toString();
		store.update(this, "fake", f);
		fake = f;
		fake_det = fd;
	}

	/** Get the fake detector */
	public String getFakeDetector() {
		return fake;
	}

	/** Calculate the fake flow rate if necessary */
	public void calculateFakeFlow() {
		FakeDetector f = fake_det;
		if(f == null)
			return;
		f.calculateFlow();
	}

	/** Accumulator for number of samples locked on (scans) */
	protected transient int locked_on;

	/** Accumulator for number of samples with no hits (volume) */
	protected transient int no_hits;

	/** Volume from the last 30-second sample period */
	protected transient int last_volume;

	/** Scans from the last 30-second sample period */
	protected transient int last_scans;

	/** Speed from the last 30-second sample period */
	protected transient int last_speed;

	/** Get the current volume */
	public float getVolume() {
		if(isSampling())
			return last_volume;
		else
			return MISSING_DATA;
	}

	/** Get the current occupancy */
	public float getOccupancy() {
		if(isSampling() && last_scans != MISSING_DATA)
			return MAX_OCCUPANCY * (float)last_scans / MAX_SCANS;
		else
			return MISSING_DATA;
	}

	/** Get the current raw flost rate (vehicles per hour) */
	protected float getRawFlow() {
		if(isSampling() && last_volume != MISSING_DATA)
			return last_volume * SAMPLES_PER_HOUR;
		else
			return MISSING_DATA;
	}

	/** Get the current flow rate (vehicles per hour) */
	public float getFlow() {
		if(isSampling() && last_volume != MISSING_DATA)
			return last_volume * SAMPLES_PER_HOUR;
		else {
			FakeDetector f = fake_det;
			if(f == null)
				return MISSING_DATA;
			else
				return f.getFlow();
		}
	}

	/** Get the current density (vehicles per mile) */
	public float getDensity() {
		int speed = last_speed;
		if(speed > 0) {
			float flow = getRawFlow();
			if(flow > MISSING_DATA)
				return flow / speed;
			else
				return MISSING_DATA;
		}
		float occ = getOccupancy();
		if(occ == MISSING_DATA || fieldLength <= 0)
			return MISSING_DATA;
		return occ * FEET_PER_MILE / fieldLength / MAX_OCCUPANCY;
	}

	/** Get the current speed (miles per hour) */
	public float getSpeed() {
		if(isSampling() && last_speed != MISSING_DATA)
			return last_speed;
		if(last_scans == MISSING_DATA)
			return MISSING_DATA;
		float flow = getRawFlow();
		if(flow <= 0)
			return MISSING_DATA;
		float density = getDensity();
		if(density <= DENSITY_THRESHOLD)
			return MISSING_DATA;
		return flow / density;
	}

	/** Force fail detector and log the cause */
	protected void malfunction(String description) {
		if(forceFail)
			return;
		try {
			store.update(this, "forceFail", true);
			forceFail = true;
		}
		catch(TMSException e) {
			e.printStackTrace();
			return;
		}
		DetectorMalfunctionEvent ev = new DetectorMalfunctionEvent(
			Log.IRIS, description, Log.DETECTOR, getId(),
			Calendar.getInstance());
		try{ eventLog.add(ev); }
		catch(TMSException e) { e.printStackTrace(); }
	}

	/** Reversible lane name */
	static protected final String REV = "I-394 HOV";

	/** Get the volume "no hit" threshold */
	protected int getNoHitThreshold() {
		if(isRamp()) {
			RoadImpl freeway = location.lookupFreeway();
			if(freeway != null && REV.equals(freeway.getName()))
				return SAMPLE_3_DAYS;
			RoadImpl cross = location.lookupCrossStreet();
			if(cross != null && REV.equals(cross.getName()))
				return SAMPLE_3_DAYS;
		}
		return SAMPLE_THRESHOLD[laneType];
	}

	/** Get the scan "locked on" threshold */
	protected int getLockedOnThreshold() {
		if(isMainlineType(laneType))
			return SAMPLE_3_MINUTES;
		if(laneType == QUEUE)
			return SAMPLE_30_MINUTES;
		return SAMPLE_20_MINUTES;
	}

	/** Test the detector volume data with error detecting algorithms */
	protected void testVolume(int volume) {
		if(volume > MAX_VOLUME)
			malfunction(DetectorMalfunctionEvent.CHATTER);
		if(volume == 0) {
			no_hits++;
			if(no_hits > getNoHitThreshold())
				malfunction(DetectorMalfunctionEvent.NO_HITS);
		} else
			no_hits = 0;
	}

	/** Test the detector scan data with error detecting algorithms */
	protected void testScans(int scans) {
		if(scans >= MAX_SCANS) {
			locked_on++;
			if(locked_on > getLockedOnThreshold())
				malfunction(DetectorMalfunctionEvent.LOCKED_ON);
		} else
			locked_on = 0;
	}

	/** Test the detector data with error detecting algorithms */
	protected void testData(int volume, int scans) {
		if(laneType == GREEN)
			return;
		testVolume(volume);
		testScans(scans);
	}

	/** Data cache */
	protected transient DataCache data_cache;

	/** Store 30-second data for this detector */
	public void storeData30Second(Calendar stamp, int volume, int scans) {
		testData(volume, scans);
		try { data_cache.write(stamp, volume, scans); }
		catch(IndexOutOfBoundsException e) {
			DET_LOG.log("CACHE OVERFLOW for detector " + index);
		}
		last_volume = volume;
		last_scans = scans;
		last_speed = MISSING_DATA;
	}

	/** Store 30-second speed for this detector */
	public void storeSpeed30Second(Calendar stamp, int speed) {
		try { data_cache.writeSpeed(stamp, speed); }
		catch(IndexOutOfBoundsException e) {
			DET_LOG.log("CACHE OVERFLOW for detector " + index);
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
		try { data_cache.flush(stamp); }
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
		final String det_id = Integer.toString(index);
		final String line = formatEvent(stamp, duration, headway,
			speed);
		TMSImpl.FLUSH.addJob(new Job() {
			public void perform() throws IOException {
				EventLogger.print(stamp, det_id, line);
			}
		});
	}

	/** Print a single detector as an XML element */
	public void printXmlElement(PrintWriter out) {
		short cat = getLaneType();
		short lane = getLaneNumber();
		float field = getFieldLength();
		String l = replaceEntities(getLabel(false));
		out.print("<detector index='D" + index + "' ");
		if(!l.equals("FUTURE"))
			out.print("label='" + l + "' ");
		if(cat > MAINLINE)
			out.print("category='" + LANE_SUFFIX[cat] + "' ");
		if(lane > 0)
			out.print("lane='" + lane + "' ");
		if(field != DEFAULT_FIELD_LENGTH)
			out.print("field='" + field + "' ");
		out.println("/>");
	}

	/** Print the current sample as an XML element */
	public void printSampleXmlElement(PrintWriter out) {
		if(abandoned || !isSampling())
			return;
		int flow = Math.round(getRawFlow());
		int speed = Math.round(getSpeed());
		out.print("\t<sample sensor='D" + index);
		if(flow != MISSING_DATA)
			out.print("' flow='" + flow);
		if(isMainline() && speed > 0)
			out.print("' speed='" + speed);
		out.println("'/>");
	}
}
