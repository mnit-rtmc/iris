/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.MeterLock;
import us.mn.state.dot.tms.MeterQueueState;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.RampMeterType;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.TimingTable;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.MINUTES;
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

	/** Comm loss threshold */
	static public final Interval COMM_LOSS_THRESHOLD =
		new Interval(3, MINUTES);

	/** Occupancy to determine merge backup */
	static private final int MERGE_BACKUP_OCC = 30;

	/** Default maximum wait time (in seconds) */
	static public final int DEFAULT_MAX_WAIT = 240;

	/** Get the current AM/PM period */
	static private int currentPeriod() {
		return TimeSteward.getCalendarInstance().get(Calendar.AM_PM);
	}

	/** Load all the ramp meters */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"meter_type, storage, max_wait, algorithm, am_target, "+
			"pm_target, beacon, preset, lock, status FROM iris." +
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
					row.getString(14), // lock
					row.getString(15)  // status
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
		map.put("lock", lock);
		map.put("status", status);
		return map;
	}

	/** Create a new ramp meter with a string name */
	public RampMeterImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name, SONAR_TYPE);
		g.notifyCreate();
		geo_loc = g;
		lock = null;
		status = null;
	}

	/** Create a ramp meter */
	private RampMeterImpl(String n, String loc, String c, int p,
		String nt, int t, int s, int w, int alg, int at, int pt,
		String b, String cp, String lk, String st)
	{
		super(n, lookupController(c), p, nt);
		geo_loc = lookupGeoLoc(loc);
		meter_type = RampMeterType.fromOrdinal(t);
		storage = s;
		max_wait = w;
		setPreset(lookupPreset(cp));
		algorithm = alg;
		am_target = at;
		pm_target = pt;
		beacon = lookupBeacon(b);
		lock = lk;
		status = st;
		initTransients();
	}

	/** Initialize the transient state */
	@Override
	public void initTransients() {
		super.initTransients();
		lookupEntranceNode();
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

	/** Meter lock (JSON) */
	private String lock = null;

	/** Set the lock as JSON */
	@Override
	public void setLock(String lk) {
		lock = lk;
	}

	/** Set the lock as JSON */
	public void doSetLock(String lk) throws TMSException {
		if (!objectEquals(lk, lock)) {
			MeterLock ml = new MeterLock(lk);
			if (lk != null)
				checkLock(ml);
			setLockChecked(lk);
		}
	}

	/** Check a lock */
	private void checkLock(MeterLock ml) throws TMSException {
		if (ml.optReason() == null)
			throw new ChangeVetoException("No reason!");
		if (!getProcUser().equals(ml.optUser()))
			throw new ChangeVetoException("Bad user!");
		String exp = ml.optExpires();
		if (exp != null && TimeSteward.parse8601(exp) == null)
			throw new ChangeVetoException("Bad expiration!");
		if (exp != null && ml.optRate() == null)
			throw new ChangeVetoException("Bad rate!");
	}

	/** Set the lock as JSON */
	private void setLockChecked(String lk) throws TMSException {
		store.update(this, "lock", lk);
		lock = lk;
		logEvent(new MeterLockEvent(name, lk));
		updateStyles();
		// send the new rate immediately
		Integer rt = getLockRate();
		if (rt != null)
			sendReleaseRate(validateRate(rt));
	}

	/** Check if lock has expired */
	public void checkLockExpired() {
		MeterLock ml = new MeterLock(lock);
		String exp = ml.optExpires();
		if (exp != null) {
			Long e = TimeSteward.parse8601(exp);
			if (e != null && e < TimeSteward.currentTimeMillis()) {
				try {
					setLockChecked(null);
					notifyAttribute("lock");
				}
				catch (TMSException ex) {
					logError("checkLockExpired: " +
						ex.getMessage());
				}
			}
		}
	}

	/** Get the lock as JSON */
	@Override
	public String getLock() {
		return lock;
	}

	/** Is the metering rate locked? */
	public boolean isLocked() {
		return lock != null;
	}

	/** Get the lock metering rate */
	private Integer getLockRate() {
		MeterLock ml = new MeterLock(lock);
		return ml.optRate();
	}

	/** Current (JSON) meter status */
	private String status;

	/** Set the current meter status as JSON */
	private void setStatusNotify(String st) {
		if (!objectEquals(st, status)) {
			try {
				store.update(this, "status", st);
				status = st;
				notifyAttribute("status");
				updateStyles();
			}
			catch (TMSException e) {
				logError("status: " + e.getMessage());
			}
		}
	}

	/** Set a status value and notify clients of the change */
	private void setStatusNotify(String key, Object value) {
		String st = RampMeterHelper.putJson(status, key, value);
		setStatusNotify(st);
	}

	/** Set a fault value and notify clients of the change */
	private void setFaultNotify(Object value) {
		setStatusNotify(RampMeter.FAULT, value);
	}

	/** Update fault with checks */
	private void updateFault(String flt) {
		String f = RampMeterHelper.optFault(this);
		// Don't overwrite POLICE_PANEL / MANUAL_MODE
		if (!objectEquals(f, RampMeter.FAULT_POLICE_PANEL) &&
		    !objectEquals(f, RampMeter.FAULT_MANUAL_MODE))
			setFaultNotify(flt);
	}

	/** Get the current status as JSON */
	@Override
	public String getStatus() {
		return status;
	}

	/** Set the status of the police panel switch */
	public void setPolicePanel(boolean p) {
		String f = RampMeterHelper.optFault(this);
		if (p)
			setFaultNotify(RampMeter.FAULT_POLICE_PANEL);
		else if (RampMeter.FAULT_POLICE_PANEL.equals(f))
			setFaultNotify(null);
	}

	/** Set the status of manual metering */
	public void setManualMode(boolean m) {
		String f = RampMeterHelper.optFault(this);
		if (m)
			setFaultNotify(RampMeter.FAULT_MANUAL_MODE);
		else if (RampMeter.FAULT_MANUAL_MODE.equals(f))
			setFaultNotify(null);
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
			return createKAdaptiveState();
		default:
			return null;
		}
	}

	/** Create K-Adaptive algorithm state */
	private MeterAlgorithmState createKAdaptiveState() {
		MeterAlgorithmState as = null;
		if (ent_node != null) {
			as = KAdaptiveAlgorithm.createState(this);
			if (null == as)
				updateFault(RampMeter.FAULT_MISSING_STATE);
		}
		return as;
	}

	/** Validate the metering algorithm */
	public void validateAlgorithm() {
		MeterAlgorithmState s = alg_state;
		if (s != null)
			s.validate(this);
		else
			logError("validateAlgorithm: No state");
	}

	/** Meter queue state */
	private MeterQueueState queue = MeterQueueState.UNKNOWN;

	/** Set the queue state */
	private void setQueueNotify(MeterQueueState q) {
		if (q != queue) {
			queue = q;
			setStatusNotify(RampMeter.QUEUE, q.description);
		}
	}

	/** Update the ramp meter queue state */
	public void updateQueueState() {
		setQueueNotify(queueState(alg_state));
	}

	/** Determine the queue state */
	private MeterQueueState queueState(MeterAlgorithmState as) {
		if (as != null && !isOffline()) {
			if (isMetering())
				return as.getQueueState(this);
			else
				return MeterQueueState.EMPTY;
		} else
			return MeterQueueState.UNKNOWN;
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
		Integer rt = isLocked() ? getLockRate() : ratePlanned;
		sendReleaseRate(validateRate(rt));
		setRatePlanned(null);
	}

	/** Validate a release rate to send to the meter */
	private Integer validateRate(Integer r) {
		return (r != null && isCommOk())
		      ? RampMeterHelper.filterRate(Math.max(r, getMinimum()))
		      : null;
	}

	/** Check if communication to the meter is OK */
	private boolean isCommOk() {
		return getFailMillis() < COMM_LOSS_THRESHOLD.ms();
	}

	/** Get current minimum release rate (vehicles per hour) */
	private int getMinimum() {
		return isOffline()
		     ? RampMeterHelper.getMaxRelease()
		     : RampMeterHelper.getMinRelease();
	}

	/** Send a new release rate to the meter */
	private void sendReleaseRate(Integer r) {
		if (!objectEquals(r, getStatusRate())) {
			MeterPoller mp = getMeterPoller();
			if (mp != null)
				mp.sendReleaseRate(this, r);
		}
	}

	/** Set the release rate (and notify clients) */
	public void setRateNotify(Integer r) {
		setStatusNotify(RampMeter.RATE, r);
		updateBeacon();
	}

	/** Get the release rate (vehciels per hour) */
	private Integer getStatusRate() {
		return RampMeterHelper.optRate(this);
	}

	/** Is the ramp meter currently metering? */
	public boolean isMetering() {
		return getStatusRate() != null;
	}

	/** Test if a meter has faults */
	@Override
	protected boolean hasFaults() {
		return RampMeterHelper.hasFault(this);
	}

	/** Test if meter is available */
	@Override
	protected boolean isAvailable() {
		return super.isAvailable() && !isMetering();
	}

	/** Test if meter has a full queue */
	private boolean isQueueFull() {
		return isOnline() && queue == MeterQueueState.FULL;
	}

	/** Test if meter has a queue */
	private boolean queueExists() {
		return isOnline() && queue == MeterQueueState.EXISTS;
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

	/** Meter entrance node */
	private transient R_NodeImpl ent_node = null;

	/** Get the meter entrance r_node */
	public R_NodeImpl getEntranceNode() {
		return ent_node;
	}

	/** Lookup the entrance R_Node */
	public void lookupEntranceNode() {
		final R_NodeImpl n = findEntranceNode();
		if (n != null)
			updateFault(null);
		else
			updateFault(RampMeter.FAULT_NO_ENTRANCE_NODE);
		ent_node = n;
	}

	/** Find the entrance R_Node */
	private R_NodeImpl findEntranceNode() {
		Corridor corridor = getCorridor();
		if (corridor != null) {
			R_NodeImpl n = corridor.findActiveNode(finder);
			if (n != null)
				return n;
			Iterator<String> it = corridor.getLinkedCDRoads();
			while (it.hasNext()) {
				String cd = it.next();
				Corridor cd_road = corridors.getCorridor(cd);
				if (cd_road != null) {
					n = cd_road.findActiveNode(finder);
					if (n != null)
						return n;
				}
			}
		}
		return null;
	}

	/** Get a sampler set for a lane code */
	public SamplerSet getSamplerSet(LaneCode lc) {
		R_NodeImpl n = ent_node;
		return (n != null)
		      ? n.getSamplerSet().filter(lc)
		      : new SamplerSet();
	}

	/** Node finder for meter entrance node */
	private final Corridor.NodeFinder finder = new Corridor.NodeFinder() {
		public boolean check(float m, R_NodeImpl n) {
			return checkNode(n);
		}
	};

	/** Check if a node is an entrance for the meter */
	private boolean checkNode(R_NodeImpl n) {
		if (n.getNodeType() != R_NodeType.ENTRANCE.ordinal())
			return false;
		if (!matchesCross(n.getGeoLoc()))
			return false;
		SamplerSet greens = n.getSamplerSet().filter(LaneCode.GREEN);
		return greens.size() == 1;
	}

	/** Test if a location cross street and direction matches the meter.
	 * Ignore roadway/direction, since it might be on CD road. */
	private boolean matchesCross(GeoLoc loc) {
		Road x = geo_loc.getCrossStreet();
		return (x != null) &&
		       (x == loc.getCrossStreet()) &&
		       (geo_loc.getCrossDir() == loc.getCrossDir()) &&
		       (geo_loc.getCrossMod() == loc.getCrossMod());
	}

	/** Check if traffic is backed up over merge detector */
	private boolean isMergeBackedUp() {
		int per_ms = DetectorImpl.BIN_PERIOD_MS;
		long stamp = DetectorImpl.calculateEndTime(per_ms);
		SamplerSet merge_set = getSamplerSet(LaneCode.MERGE);
		float occ = merge_set.getMaxOccupancy(stamp, per_ms);
		return merge_set.isPerfect() && occ >= MERGE_BACKUP_OCC;
	}

	/** Get the green count detector */
	public DetectorImpl getGreenDet() {
		SamplerSet greens = getSamplerSet(LaneCode.GREEN);
		if (1 == greens.size()) {
			VehicleSampler vs = greens.getAll().get(0);
			if (vs instanceof DetectorImpl)
				return (DetectorImpl) vs;
		}
		logError("No green detector");
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
}
