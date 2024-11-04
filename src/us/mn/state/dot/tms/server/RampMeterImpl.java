/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2024  Minnesota Department of Transportation
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.BeaconState;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.LaneCode;
import us.mn.state.dot.tms.MeterAlgorithm;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterLock;
import us.mn.state.dot.tms.RampMeterQueue;
import us.mn.state.dot.tms.RampMeterType;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.TimingTable;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.geo.Position;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.MeterPoller;
import us.mn.state.dot.tms.server.event.MeterLockEvent;

/**
 * A ramp meter is a traffic signal which meters the flow of traffic on a
 * freeway entrance ramp.
 *
 * @author Douglas Lau
 */
public class RampMeterImpl extends DeviceImpl implements RampMeter {

	/** Occupancy to determine merge backup */
	static private final int MERGE_BACKUP_OCC = 30;

	/** Default maximum wait time (in seconds) */
	static public final int DEFAULT_MAX_WAIT = 240;

	/** Filter a releae rate for valid range */
	static public int filterRate(int r) {
		r = Math.max(r, getMinRelease());
		return Math.min(r, getMaxRelease());
	}

	/** Get the absolute minimum release rate */
	static public int getMinRelease() {
		return SystemAttributeHelper.getMeterMinRelease();
	}

	/** Get the absolute maximum release rate */
	static public int getMaxRelease() {
		return SystemAttributeHelper.getMeterMaxRelease();
	}

	/** Get the current AM/PM period */
	static private int currentPeriod() {
		return TimeSteward.getCalendarInstance().get(Calendar.AM_PM);
	}

	/** Load all the ramp meters */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"meter_type, storage, max_wait, algorithm, am_target, "+
			"pm_target, beacon, preset, m_lock FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new RampMeterImpl(
					row.getString(1),  // name
					row.getString(2),  // geo_loc
					row.getString(3),  // controller
					row.getInt(4),     // pin
					row.getString(5),  // notes
					row.getInt(6),     // meter_type
					row.getInt(7),     // storage
					row.getInt(8),     // max_wait
					row.getInt(9),     // algorithm
					row.getInt(10),    // am_target
					row.getInt(11),    // pm_target
					row.getString(12), // beacon
					row.getString(13), // preset
					row.getInt(14)     // m_lock
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
		map.put("meter_type", meter_type.ordinal());
		map.put("storage", storage);
		map.put("max_wait", max_wait);
		map.put("algorithm", algorithm);
		map.put("am_target", am_target);
		map.put("pm_target", pm_target);
		map.put("beacon", beacon);
		map.put("preset", preset);
		if (m_lock != null)
			map.put("m_lock", m_lock.ordinal());
		return map;
	}

	/** Create a new ramp meter with a string name */
	public RampMeterImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name, SONAR_TYPE);
		g.notifyCreate();
		geo_loc = g;
	}

	/** Create a ramp meter */
	private RampMeterImpl(String n, GeoLocImpl loc, ControllerImpl c,
		int p, String nt, int t, int st, int w, int alg, int at, int pt,
		Beacon b, CameraPreset cp, Integer lk)
	{
		super(n, c, p, nt);
		geo_loc = loc;
		meter_type = RampMeterType.fromOrdinal(t);
		storage = st;
		max_wait = w;
		setPreset(cp);
		algorithm = alg;
		am_target = at;
		pm_target = pt;
		beacon = b;
		m_lock = RampMeterLock.fromOrdinal(lk);
		rate = null;
		initTransients();
	}

	/** Create a ramp meter */
	private RampMeterImpl(String n, String loc, String c, int p,
		String nt, int t, int st, int w, int alg, int at, int pt,
		String b, String cp, Integer lk)
	{
		this(n, lookupGeoLoc(loc), lookupController(c), p, nt, t, st, w,
		     alg, at, pt, lookupBeacon(b), lookupPreset(cp), lk);
	}

	/** Initialize the transient state */
	@Override
	public void initTransients() {
		super.initTransients();
		lookupDetectors();
		updateStyles();
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		setPreset(null);
		geo_loc.notifyRemove();
	}

	/** Device location */
	private final GeoLocImpl geo_loc;

	/** Get the device location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Ramp meter type */
	private RampMeterType meter_type = RampMeterType.DUAL_ALTERNATE;

	/** Set ramp meter type */
	@Override
	public void setMeterType(int t) {
		meter_type = RampMeterType.fromOrdinal(t);
	}

	/** Set the ramp meter type */
	public void doSetMeterType(int t) throws TMSException {
		if (t == meter_type.ordinal())
			return;
		store.update(this, "meter_type", t);
		setMeterType(t);
	}

	/** Get the ramp meter type */
	@Override
	public int getMeterType() {
		return meter_type.ordinal();
	}

	/** Get the number of lanes on the ramp */
	public int getLaneCount() {
		return meter_type.lanes;
	}

	/** Queue storage length (in feet) */
	private int storage = 1;

	/** Set the queue storage length (in feet) */
	@Override
	public void setStorage(int s) {
		storage = s;
	}

	/** Set the queue storage length (in feet) */
	public void doSetStorage(int s) throws TMSException {
		if (s == storage)
			return;
		if (s < 1)
			throw new ChangeVetoException("Storage must be > 0");
		store.update(this, "storage", s);
		setStorage(s);
	}

	/** Get the queue storage length (in feet) */
	@Override
	public int getStorage() {
		return storage;
	}

	/** Maximum allowed meter wait time (in seconds) */
	private int max_wait = DEFAULT_MAX_WAIT;

	/** Set the maximum allowed meter wait time (in seconds) */
	@Override
	public void setMaxWait(int w) {
		max_wait = w;
	}

	/** Set the maximum allowed meter wait time (in seconds) */
	public void doSetMaxWait(int w) throws TMSException {
		if (w == max_wait)
			return;
		if (w < 1)
			throw new ChangeVetoException("Wait must be > 0");
		store.update(this, "max_wait", w);
		setMaxWait(w);
	}

	/** Get the maximum allowed meter wait time (in seconds) */
	@Override
	public int getMaxWait() {
		return max_wait;
	}

	/** Ordinal of meter algorithm enumeration */
	private int algorithm;

	/** Set the metering algorithm */
	@Override
	public void setAlgorithm(int a) {
		algorithm = a;
	}

	/** Set the metering algorithm */
	public void doSetAlgorithm(int a) throws TMSException {
		int alg = MeterAlgorithm.fromOrdinal(a).ordinal();
		if (alg == algorithm)
			return;
		store.update(this, "algorithm", alg);
		setAlgorithm(alg);
		setOperating(false);
	}

	/** Get the metering algorithm */
	@Override
	public int getAlgorithm() {
		return algorithm;
	}

	/** AM target rate */
	private int am_target;

	/** Set the AM target rate */
	@Override
	public void setAmTarget(int t) {
		am_target = t;
	}

	/** Set the AM target rate */
	public void doSetAmTarget(int t) throws TMSException {
		if (t == am_target)
			return;
		store.update(this, "am_target", t);
		setAmTarget(t);
	}

	/** Get the AM target rate */
	@Override
	public int getAmTarget() {
		return am_target;
	}

	/** PM target rate */
	private int pm_target;

	/** Set the PM target rate */
	@Override
	public void setPmTarget(int t) {
		pm_target = t;
	}

	/** Set the PM target rate */
	public void doSetPmTarget(int t) throws TMSException {
		if (t == pm_target)
			return;
		store.update(this, "pm_target", t);
		setPmTarget(t);
	}

	/** Get the PM target rate */
	@Override
	public int getPmTarget() {
		return pm_target;
	}

	/** Get the target rate for current period */
	public int getTarget() {
		if (currentPeriod() == Calendar.AM)
			return getAmTarget();
		else
			return getPmTarget();
	}

	/** Get the stop minute for current period */
	public int getStopMin() {
		int period = currentPeriod();
		Hashtags tags = new Hashtags(getNotes());
		TimingTable table = new TimingTable(tags);
		for (int e = 0;; e++) {
			int min = table.lookupStop(e);
			if (min <= 0)
				break;
			if (TimeActionHelper.getPeriod(min) == period)
				return min;
		}
		return TimeActionHelper.NOON;
	}

	/** Advance warning beacon */
	private Beacon beacon;

	/** Set advance warning beacon */
	@Override
	public void setBeacon(Beacon b) {
		beacon = b;
	}

	/** Set advance warning beacon */
	public void doSetBeacon(Beacon b) throws TMSException {
		if (b != beacon) {
			store.update(this, "beacon", b);
			setBeacon(b);
		}
	}

	/** Get advance warning beacon */
	@Override
	public Beacon getBeacon() {
		return beacon;
	}

	/** Update advance warning beacon */
	private void updateBeacon() {
		Beacon b = beacon;
		if (b != null) {
			boolean f = (isOnline() && isMetering())
			          || isMergeBackedUp();
			BeaconState bs = (f)
				? BeaconState.FLASHING_REQ
				: BeaconState.DARK_REQ;
			b.setState(bs.ordinal());
		}
	}

	/** Camera preset from which this can be seen */
	private CameraPreset preset;

	/** Set the verification camera preset */
	@Override
	public void setPreset(CameraPreset cp) {
		final CameraPreset ocp = preset;
		if (cp instanceof CameraPresetImpl) {
			CameraPresetImpl cpi = (CameraPresetImpl)cp;
			cpi.setAssignedNotify(true);
		}
		preset = cp;
		if (ocp instanceof CameraPresetImpl) {
			CameraPresetImpl ocpi = (CameraPresetImpl)ocp;
			ocpi.setAssignedNotify(false);
		}
	}

	/** Set the verification camera preset */
	public void doSetPreset(CameraPreset cp) throws TMSException {
		if (cp == preset)
			return;
		store.update(this, "preset", cp);
		setPreset(cp);
	}

	/** Get verification camera preset */
	@Override
	public CameraPreset getPreset() {
		return preset;
	}

	/** Meter lock code */
	private RampMeterLock m_lock = null;

	/** Set the meter lock code */
	@Override
	public void setMLock(Integer lk) {
		m_lock = RampMeterLock.fromOrdinal(lk);
	}

	/** Set the meter lock (update) */
	private void setMLock(Integer lk, String u) throws TMSException {
		if (RampMeterLock.fromOrdinal(lk) != m_lock) {
			store.update(this, "m_lock", lk);
			setMLock(lk);
			updateStyles();
			logEvent(new MeterLockEvent(name, lk, u));
		}
	}

	/** Set the meter lock (notify clients) */
	private void setMLockNotify(RampMeterLock ml) {
		try {
			Integer lk = (ml != null) ? ml.ordinal() : null;
			setMLock(lk, null);
			notifyAttribute("mLock");
		}
		catch (TMSException e) {
			e.printStackTrace();
		}
	}

	/** Set the meter lock code */
	public void doSetMLock(Integer lk) throws TMSException {
		RampMeterLock ml = RampMeterLock.fromOrdinal(lk);
		if (ml != null && ml.controller_lock)
			throw new ChangeVetoException("Invalid lock value");
		setMLock(lk, getProcUser());
	}

	/** Get the ramp meter lock code */
	@Override
	public Integer getMLock() {
		return (m_lock != null) ? m_lock.ordinal() : null;
	}

	/** Is the metering rate locked? */
	public boolean isLocked() {
		return m_lock != null;
	}

	/** Set the status of the police panel switch */
	public void setPolicePanel(boolean p) {
		if (p) {
			if (m_lock == null)
				setMLockNotify(RampMeterLock.POLICE_PANEL);
		} else {
			if (m_lock == RampMeterLock.POLICE_PANEL)
				setMLockNotify(null);
		}
	}

	/** Set the status of manual metering */
	public void setManual(boolean m) {
		if (m) {
			if (m_lock == null)
				setMLockNotify(RampMeterLock.MANUAL);
		} else {
			if (m_lock == RampMeterLock.MANUAL)
				setMLockNotify(null);
		}
	}

	/** Get the meter poller */
	private MeterPoller getMeterPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof MeterPoller) ? (MeterPoller) dp : null;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		MeterPoller mp = getMeterPoller();
		if (mp != null)
			mp.sendRequest(this, dr);
	}

	/** Metering algorithm state */
	private transient MeterAlgorithmState alg_state;

	/** Set the algorithm operating state */
	public void setOperating(boolean o) {
		if (o) {
			if (alg_state == null)
				alg_state = createState();
		} else {
			alg_state = null;
			setRatePlanned(null);
		}
	}

	/** Get the algorithm operating state */
	public boolean isOperating() {
		return alg_state != null;
	}

	/** Create the meter algorithm state */
	private MeterAlgorithmState createState() {
		switch (MeterAlgorithm.fromOrdinal(algorithm)) {
		case SIMPLE:
			return new SimpleAlgorithm();
		case K_ADAPTIVE:
			return KAdaptiveAlgorithm.meterState(this);
		default:
			return null;
		}
	}

	/** Validate the metering algorithm */
	public void validateAlgorithm() {
		MeterAlgorithmState s = alg_state;
		if (s != null)
			s.validate(this);
		else
			logError("validateAlgorithm: No state");
	}

	/** Ramp meter queue status */
	private RampMeterQueue queue = RampMeterQueue.UNKNOWN;

	/** Set the queue status */
	private void setQueueNotify(RampMeterQueue q) {
		if (q != queue) {
			queue = q;
			notifyAttribute("queue");
			updateStyles();
		}
	}

	/** Get the queue status */
	@Override
	public int getQueue() {
		return queue.ordinal();
	}

	/** Update the ramp meter queue status */
	public void updateQueueState() {
		setQueueNotify(queueStatus(alg_state));
	}

	/** Determine the queue status */
	private RampMeterQueue queueStatus(MeterAlgorithmState as) {
		if (as != null && !isFailed()) {
			if (isMetering())
				return as.getQueueState(this);
			else
				return RampMeterQueue.EMPTY;
		} else
			return RampMeterQueue.UNKNOWN;
	}

	/** Planned next release rate */
	private transient Integer ratePlanned = null;

	/** Set the planned next release rate */
	public void setRatePlanned(Integer r) {
		Integer rp = ratePlanned;
		if (r != null && rp != null)
			ratePlanned = Math.min(rp, r);
		else
			ratePlanned = r;
	}

	/** Update the planned rate */
	public void updateRatePlanned() {
		if (!isLocked())
			setRateNext(ratePlanned);
		setRatePlanned(null);
	}

	/** Set the release rate (vehicles per hour) */
	@Override
	public void setRateNext(Integer r) {
		sendReleaseRate(validateRate(r));
	}

	/** Validate a release rate to send to the meter */
	private Integer validateRate(Integer r) {
		return (r != null && isCommOk())
		      ? filterRate(Math.max(r, getMinimum()))
		      : null;
	}

	/** Check if communication to the meter is OK */
	private boolean isCommOk() {
		return getFailMillis() < MeterPoller.COMM_FAIL_THRESHOLD_MS;
	}

	/** Get current minimum release rate (vehicles per hour) */
	private int getMinimum() {
		return (isFailed()) ? getMaxRelease() : getMinRelease();
	}

	/** Send a new release rate to the meter */
	private void sendReleaseRate(Integer r) {
		if (rateChanged(r)) {
			MeterPoller mp = getMeterPoller();
			if (mp != null)
				mp.sendReleaseRate(this, r);
		}
	}

	/** Release rate (vehicles per hour) */
	private transient Integer rate = null;

	/** Set the release rate (and notify clients) */
	public void setRateNotify(Integer r) {
		if (rateChanged(r)) {
			rate = r;
			notifyAttribute("rate");
			updateStyles();
		}
		updateBeacon();
	}

	/** Test if the release rate has changed */
	private boolean rateChanged(Integer r) {
		return !objectEquals(r, rate);
	}

	/** Get the release rate (vehciels per hour) */
	public Integer getRate() {
		return rate;
	}

	/** Is the ramp meter currently metering? */
	public boolean isMetering() {
		return rate != null;
	}

	/** Test if a meter needs maintenance */
	@Override
	protected boolean needsMaintenance() {
		if (super.needsMaintenance())
			return true;
		RampMeterLock lk = m_lock;
		return lk == RampMeterLock.POLICE_PANEL ||
		       lk == RampMeterLock.MAINTENANCE;
	}

	/** Test if meter is available */
	@Override
	protected boolean isAvailable() {
		return super.isAvailable() && !isMetering();
	}

	/** Test if meter has a full queue */
	private boolean isQueueFull() {
		return isOnline() && queue == RampMeterQueue.FULL;
	}

	/** Test if meter has a queue */
	private boolean queueExists() {
		return isOnline() && queue == RampMeterQueue.EXISTS;
	}

	/** Calculate the item styles */
	@Override
	protected long calculateStyles() {
		long s = super.calculateStyles();
		if (isQueueFull())
			s |= ItemStyle.QUEUE_FULL.bit();
		if (queueExists())
			s |= ItemStyle.QUEUE_EXISTS.bit();
		if (isOnline() && isMetering())
			s |= ItemStyle.METERING.bit();
		if (isLocked())
			s |= ItemStyle.LOCKED.bit();
		return s;
	}

	/** Get the detector set associated with the ramp meter */
	private SamplerSet getSamplerSet(SamplerSet.Filter f) {
		DetFinder finder = new DetFinder(f);
		Corridor corridor = getCorridor();
		if (corridor != null) {
			corridor.findActiveNode(finder);
			Iterator<String> it = corridor.getLinkedCDRoads();
			while (it.hasNext()) {
				String cd = it.next();
				Corridor cd_road = corridors.getCorridor(cd);
				if (cd_road != null)
					cd_road.findActiveNode(finder);
			}
		} else
			logError("getSamplerSet: no corridor");
		return new SamplerSet(finder.samplers);
	}

	/** Get the set of non-abandoned detectors */
	public SamplerSet getSamplerSet() {
		return getSamplerSet(new SamplerSet.Filter() {
			public boolean check(VehicleSampler vs) {
				if (vs instanceof DetectorImpl) {
					DetectorImpl d = (DetectorImpl) vs;
					return !d.getAbandoned();
				} else
					return false;
			}
		});
	}

	/** Detector finder */
	private class DetFinder implements Corridor.NodeFinder {
		private final ArrayList<VehicleSampler> samplers =
			new ArrayList<VehicleSampler>();
		private final SamplerSet.Filter filter;
		private DetFinder(SamplerSet.Filter f) {
			filter = f;
		}
		public boolean check(float m, R_NodeImpl n) {
			if (n.getNodeType() == R_NodeType.ENTRANCE.ordinal()) {
				GeoLoc l = n.getGeoLoc();
				if (GeoLocHelper.matchesRoot(l, geo_loc)) {
					SamplerSet ds = n.getSamplerSet();
					samplers.addAll(ds.filter(filter)
					                  .getAll());
				}
			}
			return false;
		}
	}

	/** Merge detector */
	private transient SamplerSet merge_set = new SamplerSet();

	/** Check if traffic is backed up over merge detector */
	private boolean isMergeBackedUp() {
		int per_ms = DetectorImpl.BIN_PERIOD_MS;
		long stamp = DetectorImpl.calculateEndTime(per_ms);
		float occ = merge_set.getMaxOccupancy(stamp, per_ms);
		return merge_set.isPerfect() && occ >= MERGE_BACKUP_OCC;
	}

	/** Green count detector */
	private transient DetectorImpl green_det = null;

	/** Get the green count detector */
	public DetectorImpl getGreenDet() {
		return green_det;
	}

	/** Lookup the merge and green count detectors from R_Nodes */
	public void lookupDetectors() {
		SamplerSet ss = getSamplerSet();
		merge_set = ss.filter(LaneCode.MERGE);
		green_det = lookupGreen(ss);
	}

	/** Lookup a single green detector in a sampler set */
	private DetectorImpl lookupGreen(SamplerSet ss) {
		SamplerSet greens = ss.filter(LaneCode.GREEN);
		if (1 == greens.size()) {
			VehicleSampler vs = greens.getAll().get(0);
			if (vs instanceof DetectorImpl)
				return (DetectorImpl) vs;
		}
		logError("lookupGreen: wrong size " + greens.size());
		return null;
	}

	/** Get the corridor containing the ramp meter */
	public Corridor getCorridor() {
		return corridors.getCorridor(geo_loc);
	}

	/** Write meter as an XML element */
	public void writeXml(Writer w) throws IOException {
		w.write("<meter");
		w.write(createAttribute("name", getName()));
		Position pos = GeoLocHelper.getWgs84Position(geo_loc);
		if (pos != null) {
			w.write(createAttribute("lon",
				formatDouble(pos.getLongitude())));
			w.write(createAttribute("lat",
				formatDouble(pos.getLatitude())));
		}
		w.write(" storage='" + getStorage() + "'");
		int mw = getMaxWait();
		if (mw != DEFAULT_MAX_WAIT)
			w.write(" max_wait='" + mw + "'");
		w.write("/>\n");
	}

	/** Get the r_node associated with the ramp meter */
	public R_NodeImpl getR_Node() {
		DetectorImpl det = green_det;
		if (det != null) {
			R_Node n = det.getR_Node();
			if (n instanceof R_NodeImpl)
				return (R_NodeImpl) n;
		}
		logError("getR_Node: No green det");
		return null;
	}

	/** Get the entrance r_node on same corridor as ramp meter */
	public R_NodeImpl getEntranceNode() {
		R_NodeImpl n = getR_Node();
		return (n != null) ? findEntrance(n) : null;
	}

	/** Find entrance r_node on same corridor as ramp meter */
	private R_NodeImpl findEntrance(R_NodeImpl n) {
		GeoLoc loc = n.getGeoLoc();
		if (isSameCorridor(loc))
			return n;
		if (isDeviceLogging())
			logError("findEntrance: corridor mismatch " + n);
		Corridor c = corridors.getCorridor(loc);
		if (c != null)
			return c.findActiveNode(new EntranceFinder());
		else
			return null;
	}

	/** Check if a locations is on the same corridor as the ramp meter */
	private boolean isSameCorridor(GeoLoc loc) {
		return getCorridor() == corridors.getCorridor(loc);
	}

	/** Entrance finder */
	private class EntranceFinder implements Corridor.NodeFinder {
		public boolean check(float m, R_NodeImpl n) {
			R_NodeImpl f = n.getFork();
			return (f != null) && isSameCorridor(f.getGeoLoc());
		}
	}
}
