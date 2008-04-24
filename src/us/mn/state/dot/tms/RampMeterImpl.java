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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.rmi.RemoteException;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.vault.FieldMap;
import us.mn.state.dot.vault.ObjectVaultException;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.MeterPoller;

/**
 * Each ramp meter is an object of this class.
 *
 * @author Douglas Lau
 */
public class RampMeterImpl extends TrafficDeviceImpl
	implements RampMeter, Constants, Storable
{
	/** ObjectVault table name */
	static public final String tableName = "ramp_meter";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Meter debug log */
	static protected final DebugLog METER_LOG = new DebugLog("meter");

	/** Date instance for compute current minute */
	static protected final Date DATE = new Date();

	/** Calendar instance for computing current minute */
	static protected final Calendar STAMP = Calendar.getInstance();

	/** Get the current interval of the day */
	static protected int currentInterval() {
		synchronized(STAMP) {
			DATE.setTime(System.currentTimeMillis());
			STAMP.setTime(DATE);
			return STAMP.get(Calendar.HOUR_OF_DAY) * 120 +
				STAMP.get(Calendar.MINUTE) * 2 +
				STAMP.get(Calendar.SECOND) / 30;
		}
	}

	/** Notify all observers for an update */
	public void notifyUpdate() {
		super.notifyUpdate();
		meterList.update(id);
	}

	/** Status code from last notification */
	protected transient int status_code;

	/** Notify all observers for a status change */
	public void notifyStatus() {
		int s = getStatusCode();
		if(s != status_code) {
			status_code = s;
			final String meter_id = id;
			WORKER.addJob(new Job() {
				public void perform() {
					// NOTE: this grabs the RampMeterList
					// lock, so must be done on the WORKER
					// thread to avoid deadlocks
					meterList.update(meter_id);
				}
			});
		}
		super.notifyStatus();
	}

	/** Create a new ramp meter */
	public RampMeterImpl(String id) throws ChangeVetoException,
		RemoteException
	{
		super(id);
		controlMode = MODE_UNAVAILABLE;
		singleRelease = false;
		plans = new MeterPlanImpl[0];
		policePanelFlash = false;
		metering = false;
		shouldMeter = false;
		minimum = MAX_RELEASE_RATE;
		demand = MIN_RELEASE_RATE;
		releaseRate = MAX_RELEASE_RATE;
		lock = null;
		deviceList.add(id, this);
	}

	/** Create a ramp meter from an ObjectVault field map */
	protected RampMeterImpl(FieldMap fields) throws RemoteException {
		super(fields);
		storage = fields.getInt("storage");
		maxWait = fields.getInt("maxWait");
		policePanelFlash = false;
		metering = false;
		shouldMeter = false;
		minimum = MAX_RELEASE_RATE;
		demand = MIN_RELEASE_RATE;
		releaseRate = MAX_RELEASE_RATE;
		lock = null;
	}

	/** Initialize the transient state */
	public void initTransients() throws ObjectVaultException,
		TMSException, RemoteException
	{
		super.initTransients();
		LinkedList p = new LinkedList();
		Set s = plan_mapping.lookup("traffic_device", this);
		Iterator it = s.iterator();
		while(it.hasNext())
			p.add(vault.load(it.next()));
		plans = (MeterPlanImpl [])p.toArray(new MeterPlanImpl[0]);
		Arrays.sort(plans);
	}

	/** Set the controller for this device */
	public void setController(ControllerImpl c) throws TMSException {
		super.setController(c);
		if(c == null) {
			deviceList.add(id, this);
			availableMeters.add(id, this);
		} else {
			deviceList.remove(id);
			availableMeters.remove(id);
		}
	}

	/** Get the meter number on the controller */
	public int getMeterNumber() {
		ControllerImpl c = controller;	// Avoid races
		if(c != null)
			return c.getMeterNumber(this);
		else
			return 0;
	}

	/** Get the meter poller */
	protected MeterPoller getMeterPoller() {
		if(isActive()) {
			MessagePoller p = getPoller();
			if(p instanceof MeterPoller)
				return (MeterPoller)p;
		}
		return null;
	}

	/** Check if the meter has been failed beyond threshold time */
	public boolean isFailedBeyondThreshold() {
		ControllerImpl c = controller;	// Avoid races
		if(c instanceof Controller170Impl)
			return ((Controller170Impl)c).isFailedBeyondThreshold();
		else
			return false;
	}

	/** Green count detector */
	protected transient DetectorImpl green_det;

	/** Lookup the green count detector */
	protected void lookupGreenDetector() {
		DetectorImpl[] g = getDetectorSet().getDetectorSet(
			Detector.GREEN).toArray();
		if(g.length > 0)
			green_det = g[0];
		else
			green_det = null;
	}

	/** Ramp meter control mode (MODE_STANDBY, MODE_CENTRAL, etc.) */
	protected int controlMode;

	/** Set the ramp meter control mode */
	public synchronized void setControlMode(int m) throws TMSException {
		if(m == controlMode)
			return;
		if(m < 0 || m >= MODE.length)
			throw new ChangeVetoException("Invalid mode");
		if((m != MODE_UNAVAILABLE) && !isActive())
			throw new ChangeVetoException("Meter not active");
		store.update(this, "controlMode", m);
		controlMode = m;
	}

	/** Get the ramp meter control mode */
	public int getControlMode() {
		return controlMode;
	}

	/** Test if the ramp meter control mode is unavailable */
	public boolean isModeUnavailable() {
		return getControlMode() == MODE_UNAVAILABLE || !isActive();
	}

	/** Test if the ramp meter status is unavailable */
	public boolean isUnavailable() {
		return isModeUnavailable() || isPolicePanelFlash();
	}

	/** Single release flag (false indicates dual/alternate release) */
	protected boolean singleRelease;

	/** Set single release (true) or dual/alternate release (false) */
	public synchronized void setSingleRelease(boolean s)
		throws TMSException
	{
		if(s == singleRelease)
			return;
		store.update(this, "singleRelease", s);
		singleRelease = s;
	}

	/** Is this a single or dual/alternate release ramp? */
	public boolean isSingleRelease() {
		return singleRelease;
	}

	/** Array of timing plans for this meter */
	protected transient MeterPlanImpl[] plans;

	/** Add a simple timing plan to the ramp meter */
	public void addSimpleTimingPlan(int period) throws TMSException,
		RemoteException
	{
		addTimingPlan(new SimplePlanImpl(period));
	}

	/** Add a stratified timing plan to the ramp meter */
	public void addStratifiedTimingPlan(int period) throws TMSException,
		RemoteException
	{
		RoadwayImpl freeway = (RoadwayImpl)location.getFreeway();
		short freeDir = location.getFreeDir();
		if(freeway != null) {
			MeterPlanImpl plan = meterList.findStratifiedPlan(
				freeway, freeDir, period);
			if(plan == null)
				plan = new StratifiedPlanImpl(period);
			addTimingPlan(plan);
		}
	}

	/** Add a timing plan to the ramp meter */
	protected synchronized void addTimingPlan(MeterPlanImpl plan)
		throws TMSException
	{
		MeterPlanImpl[] p = new MeterPlanImpl[plans.length + 1];
		for(int i = 0; i < plans.length; i++) {
			p[i] = plans[i];
			if(p[i].equals(plan))
				return;
		}
		p[plans.length] = plan;
		try {
			vault.save(plan, getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		setTimingPlans(p);
	}

	/** Remove a timing plan from the ramp meter */
	public synchronized void removeTimingPlan(MeterPlan plan)
		throws TMSException
	{
		MeterPlanImpl old_plan = null;
		MeterPlanImpl[] p = new MeterPlanImpl[plans.length - 1];
		for(int i = 0, j = 0; i < plans.length; i++) {
			if(plans[i].equals(plan))
				old_plan = plans[i];
			else {
				p[j] = plans[i];
				j++;
			}
		}
		if(old_plan == null)
			throw new ChangeVetoException("Plan not found");
		boolean lastReference = old_plan.isDeletable();
		setTimingPlans(p);
		if(lastReference)
			old_plan.notifyDelete();
	}

	/** Ensure stratified plans are for the right corridor */
	public void checkStratifiedPlans() throws TMSException {
		RoadwayImpl freeway = (RoadwayImpl)location.getFreeway();
		short freeDir = location.getFreeDir();
		StratifiedPlanImpl am_plan = null;
		StratifiedPlanImpl pm_plan = null;
		if(freeway != null) {
			am_plan = meterList.findStratifiedPlan(freeway,
				freeDir, TimingPlan.AM);
			pm_plan = meterList.findStratifiedPlan(freeway,
				freeDir, TimingPlan.PM);
		}
		removeInvalidStratifiedPlans(am_plan, pm_plan);
	}

	/** Remove all stratified timing plans from the ramp meter */
	protected synchronized void removeInvalidStratifiedPlans(
		StratifiedPlanImpl am_plan, StratifiedPlanImpl pm_plan)
		throws TMSException
	{
		MeterPlanImpl[] p = plans;
		for(int i = 0; i < p.length; i++) {
			MeterPlanImpl plan = p[i];
			if(plan instanceof StratifiedPlanImpl) {
				if((plan != am_plan) && (plan != pm_plan))
					removeTimingPlan(plan);
			}
		}
	}

	/** Set all current timing plans which affect this meter */
	protected void setTimingPlans(MeterPlanImpl[] p) throws TMSException {
		Arrays.sort(p);
		if(Arrays.equals(p, plans))
			return;
		plan_mapping.update("traffic_device", this, p);
		plans = p;
	}

	/** Get an array of all timing plans which affect this meter */
	public MeterPlan[] getTimingPlans() {
		MeterPlanImpl[] plans = this.plans;	// Avoid race
		MeterPlan[] p = new MeterPlan[plans.length];
		for(int i = 0; i < plans.length; i++)
			p[i] = plans[i];
		return p;
	}

	/** Find a stratified timing plan in the given time period */
	public StratifiedPlanImpl findStratifiedPlan(int period) {
		MeterPlanImpl[] plans = this.plans;	// Avoid race
		for(int i = 0; i < plans.length; i++) {
			if(plans[i] instanceof StratifiedPlanImpl) {
				StratifiedPlanImpl plan =
					(StratifiedPlanImpl)plans[i];
				if(plan.checkPeriod(period))
					return plan;
			}
		}
		return null;
	}

	/** Get the target rate for the specified minute */
	public int getTarget(int minute) {
		MeterPlanImpl[] plans = this.plans;	// Avoid race
		int target = RampMeter.MAX_RELEASE_RATE;
		for(int i = 0; i < plans.length; i++) {
			if(plans[i] instanceof SimplePlanImpl) {
				SimplePlanImpl plan = (SimplePlanImpl)plans[i];
				if(minute >= plan.getStartTime() &&
					minute <= plan.getStopTime())
				{
					target = Math.min(target,
						plan.getTarget(this));
				}
			}
		}
		return target;
	}

	/** Police panel flash flag */
	protected transient boolean policePanelFlash;

	/** Set the police panel flash flag */
	public void setPolicePanelFlash( boolean p ) {
		policePanelFlash = p;
	}

	/** Is the ramp meter in police panel flash? */
	public boolean isPolicePanelFlash() {
		return policePanelFlash;
	}

	/** Metering on/off flag */
	protected transient boolean metering;

	/** Metering command on/off flag */
	protected transient boolean shouldMeter;

	/** Set the metering status read from the controller */
	public void setMetering(boolean m, boolean central) {
		if(isUnavailable() || !central)
			shouldMeter = false;
		metering = m;
		if(metering != shouldMeter) {
			MeterPoller mp = getMeterPoller();
			if(mp != null) {
				if(shouldMeter)
					mp.startMetering(this);
				else
					mp.stopMetering(this);
			}
		}
	}

	/** Is the ramp meter currently metering? */
	public boolean isMetering() {
		return metering;
	}

	/** Start metering this ramp meter */
	public void startMetering() throws TMSException {
		if(isModeUnavailable())
			throw new ChangeVetoException("Unavailable: " + id);
		if(isPolicePanelFlash())
			throw new ChangeVetoException("Police panel: " + id);
		shouldMeter = true;
		MeterPoller mp = getMeterPoller();
		if(mp != null)
			mp.startMetering(this);
	}

	/** Stop metering this ramp meter */
	public void stopMetering() {
		shouldMeter = false;
		MeterPoller mp = getMeterPoller();
		if(mp != null)
			mp.stopMetering(this);
	}

	/** Minimum release rate (vehicles per hour) */
	protected transient int minimum;

	/** Set the minimum release rate (vehicles per hour) */
	public void setMinimum(int m) {
		if(m < MIN_RELEASE_RATE)
			m = MIN_RELEASE_RATE;
		if(m > MAX_RELEASE_RATE)
			m = MAX_RELEASE_RATE;
		minimum = m;
		setReleaseRate(releaseRate);
	}

	/** Get the minimum release rate (vehicles per hour). */
	public int getMinimum() {
		return minimum;
	}

	/** Current estimated ramp demand (vehicles per hour) */
	protected transient int demand;

	/** Set the ramp meter demand (vehicles per hour) */
	protected void setDemand(int d) {
		if(d < MIN_RELEASE_RATE)
			d = MIN_RELEASE_RATE;
		if(d > MAX_RELEASE_RATE)
			d = MAX_RELEASE_RATE;
		demand = d;
	}

	/** Get the current estimated ramp demand (vehicles per hour). */
	public int getDemand() {
		return demand;
	}

	/** Update the 30-second green count */
	public void updateGreenCount(Calendar stamp, int g) throws IOException {
		DetectorImpl det = green_det;
		if(det != null) {
			if(singleRelease) {
				if((g % 2) != 0)
					g++;
				g /= 2;
			}
			if(g == 0 && isMetering())
				return;
			det.storeData30Second(stamp, g, MISSING_DATA);
		} else
			METER_LOG.log("No green det for " + id);
	}

	/** Update the 5-minute green count */
	public void updateGreenCount5(Calendar stamp, int g)
		throws IOException
	{
		DetectorImpl det = green_det;
		if(det != null) {
			if(singleRelease) {
				if((g % 2) != 0)
					g++;
				g /= 2;
			}
			det.storeData5Minute(stamp, g, MISSING_DATA);
		} else
			METER_LOG.log("No green det for " + id);
	}

	/** Determine whether a queue exists at the ramp meter */
	public boolean queueExists() {
		if(metering) {
			MeterPlanImpl[] plans = this.plans;	// Avoid race
			for(int i = 0; i < plans.length; i++) {
				if(plans[i].checkQueue(this))
					return true;
			}
		}
		return false;
	}

	/** Current ramp meter release rate (vehicles per hour) */
	protected transient int releaseRate;

	/** Set the ramp meter release rate (vehicles per hour) */
	public void setReleaseRate(int r) {
		if(r < minimum)
			r = minimum;
		if(r > MAX_RELEASE_RATE)
			r = MAX_RELEASE_RATE;
		releaseRate = r;
	}

	/** Get the current ramp meter release rate (vehciels per hour) */
	public int getReleaseRate() {
		return releaseRate;
	}

	/** Grow the length of the queue by decreasing the demand */
	public void growQueue() {
		setDemand(Math.round(getDemand() * 0.9f));
		validateTimingPlans(currentInterval());
	}

	/** Shrink the length of the queue by increasing the demand */
	public void shrinkQueue() {
		setDemand(Math.round(getDemand() * 1.1f));
		validateTimingPlans(currentInterval());
	}

	/** Compute the ramp demand (and minimum release rate) */
	public void computeDemand(int interval) {
		MeterPlanImpl[] plans = this.plans;	// Avoid race
		int min = MIN_RELEASE_RATE;
		int dem = MIN_RELEASE_RATE;
		for(int i = 0; i < plans.length; i++) {
			dem = Math.max(dem, plans[i].computeDemand(this,
				interval));
			min = Math.max(min, plans[i].getMinimum(this));
		}
		if(isFailed())
			min = Math.max(min, getTarget(interval / 2));
		setMinimum(min);
		if(!isLocked())
			setDemand(dem);
	}

	/** Compute the current release rate */
	protected int computeReleaseRate(int interval) {
		if(isLocked())
			return getDemand();
		MeterPlanImpl[] plans = this.plans;	// Avoid race
		int release = MAX_RELEASE_RATE;
		for(int i = 0; i < plans.length; i++) {
			release = Math.min(release,
				plans[i].validate(this, interval));
		}
		return release;
	}

	/** Validate all timing plans for this meter */
	public void validateTimingPlans(int interval) {
		setReleaseRate(computeReleaseRate(interval));
		updateReleaseRate();
		notifyStatus();
	}

	/** Update the ramp meter release rate */
	public void updateReleaseRate() {
		if(isUnavailable() || !isMetering())
			return;
		MeterPoller mp = getMeterPoller();
		if(mp != null)
			mp.sendReleaseRate(this, releaseRate);
	}

	/** metering rate lock, if one is set.*/
	protected transient RampMeterLock lock;
	
	/** Is the metering rate locked? */
	public boolean isLocked() {
		return lock != null;
	}

	/** Lock or unlock the metering rate */
	public void setLocked(boolean l, String reason) {
		if(l == isLocked())
			return;
		if(l)
			lock = new RampMeterLock(getUserName(), reason); 
		else
			lock = null;
		setDemand(getReleaseRate());
		notifyStatus();
	}

	/** Get the name of the user who set the lock */
	public RampMeterLock getLock() {
		return lock;
	}

	/** Calculate the red time from the given release rate */
	public int calculateRedTime(int release_rate) {
		float cycle = SECONDS_PER_HOUR / (float)release_rate;
		if(singleRelease)
			cycle /= 2;
		int green = getPolicyValue(SystemPolicy.METER_GREEN_TIME);
		int yellow = getPolicyValue(SystemPolicy.METER_YELLOW_TIME);
		int min_red = getPolicyValue(SystemPolicy.METER_MIN_RED_TIME);
		int red_time = Math.round(cycle * 10) - (green + yellow);
		return Math.max(red_time, min_red);
	}

	/** Calculate the release rate from a given red time */
	public int calculateReleaseRate(int red_time) {
		int green = getPolicyValue(SystemPolicy.METER_GREEN_TIME);
		int yellow = getPolicyValue(SystemPolicy.METER_YELLOW_TIME);
		float cycle = (red_time + yellow + green) /
			10.0f;
		if(singleRelease)
			cycle *= 2;
		return Math.round(SECONDS_PER_HOUR / cycle);
	}

	/** Get the current status code */
	public int getStatusCode() {
		if(!isActive())
			return STATUS_INACTIVE;
		if(isFailed())
			return STATUS_FAILED;
		if(isUnavailable())
			return STATUS_UNAVAILABLE;
		if(isMetering())
			return getMeteringStatus();
		if(isLocked())
			return STATUS_LOCKED_OFF;
		else
			return STATUS_AVAILABLE;
	}

	/** Get the current status code if metering */
	protected int getMeteringStatus() {
		if(isLocked())
			return STATUS_LOCKED_ON;
		int s = STATUS_METERING;
		MeterPlanImpl[] plans = this.plans;	// Avoid race
		for(int i = 0; i < plans.length; i++) {
			if(plans[i].checkQueueBackup(this))
				return STATUS_QUEUE_BACKUP;
			if(plans[i].checkCongested(this))
				return STATUS_CONGESTED;
			if(plans[i].checkWarning(this))
				return STATUS_WARNING;
			if(plans[i].checkQueue(this))
				s = STATUS_QUEUE;
		}
		return s;
	}

	/** Queue storage length (in feet) */
	protected int storage = 1;

	/** Set the queue storage length (in feet) */
	public synchronized void setStorage(int s) throws TMSException {
		if(s == storage)
			return;
		if(s < 1)
			throw new ChangeVetoException("Storage must be > 0");
		store.update(this, "storage", s);
		storage = s;
	}

	/** Get the queue storage length (in feet) */
	public int getStorage() {
		return storage;
	}

	/** Maximum allowed meter wait time (in seconds) */
	protected int maxWait = DEFAULT_MAX_WAIT;

	/** Set the maximum allowed meter wait time (in seconds) */
	public synchronized void setMaxWait(int w) throws TMSException {
		if(w == maxWait)
			return;
		if(w < 1)
			throw new ChangeVetoException("Wait must be > 0");
		store.update(this, "maxWait", w);
		maxWait = w;
	}

	/** Get the maximum allowed meter wait time (in seconds) */
	public int getMaxWait() {
		return maxWait;
	}

	/** Camera from which this can be seen */
	protected CameraImpl camera;

	/** Set the verification camera */
	public void setCamera(String id) throws TMSException {
		setCamera((CameraImpl)cameraList.getElement(id));
	}

	/** Set the verification camera */
	protected synchronized void setCamera(CameraImpl c)
		throws TMSException
	{
		if(c == camera)
			return;
		// FIXME: use toString() instead of getOID()
		if(c == null)
			store.update(this, "camera", "0");
		else
			store.update(this, "camera", c.getOID());
		camera = c;
	}

	/** Get verification camera */
	public TrafficDevice getCamera() {
		return camera;
	}

	/** Get the detector set associated with the ramp meter */
	public DetectorSet getDetectorSet() {
		final DetectorSet ds = new DetectorSet();
		Corridor.NodeFinder finder = new Corridor.NodeFinder() {
			public boolean check(R_NodeImpl n) {
				if(n.getNodeType() != R_Node.TYPE_ENTRANCE)
					return false;
				LocationImpl l = (LocationImpl)n.getLocation();
				if(l.matchesRoot(location))
					ds.addDetectors(n.getDetectorSet());
				return false;
			}
		};
		Corridor corridor = getCorridor();
		if(corridor != null) {
			corridor.findNode(finder);
			String cd = corridor.getLinkedCDRoad();
			if(cd != null) {
				Corridor cd_road = nodeMap.getCorridor(cd);
				if(cd_road != null)
					cd_road.findNode(finder);
			}
		}
		return ds;
	}

	/** Get the detectors associated with the ramp meter */
	public Detector[] getDetectors() {
		DetectorImpl[] dets = getDetectorSet().toArray();
		Detector[] ds = new Detector[dets.length];
		for(int i = 0; i < dets.length; i++)
			ds[i] = dets[i];
		return ds;
	}

	/** Get the ID of the corridor containing the ramp meter */
	public String getCorridorID() {
		return location.getCorridorID();
	}

	/** Get the corridor containing the ramp meter */
	public Corridor getCorridor() {
		return nodeMap.getCorridor(location.getCorridor());
	}

	/** Print a single detector as an XML element */
	public void printXmlElement(PrintWriter out) {
		lookupGreenDetector();
		out.print("<meter id='" + getId() + "' ");
		out.print("label='" + getLabel() + "' ");
		out.print("storage='" + getStorage() + "' ");
		int w = getMaxWait();
		if(w != DEFAULT_MAX_WAIT)
			out.print("max_wait='" + w + "' ");
		printMeterDetectors(out);
		out.println("/>");
	}

	/** Get the label of a ramp meter */
	protected String getLabel() {
		StringBuffer b = new StringBuffer();
		b.append(DIRECTION[location.getCrossDir()]);
		b.append(' ');
		RoadwayImpl x = (RoadwayImpl)location.getCrossStreet();
		if(x != null)
			b.append(x.getName());
		return replaceEntities(b.toString().trim());
	}

	/** Print the detectors associated with a ramp meter */
	protected void printMeterDetectors(PrintWriter out) {
		DetectorSet ds = getDetectorSet();
		printAttribute(out, "green",
			ds.getDetectorSet(Detector.GREEN));
		printAttribute(out, "passage",
			ds.getDetectorSet(Detector.PASSAGE));
		printAttribute(out, "merge",
			ds.getDetectorSet(Detector.MERGE));
		printAttribute(out, "queue",
			ds.getDetectorSet(Detector.QUEUE));
		printAttribute(out, "bypass",
			ds.getDetectorSet(Detector.BYPASS));
	}

	/** Print a meter detector set attribute */
	protected void printAttribute(PrintWriter out, String attr,
		DetectorSet ds)
	{
		if(ds.size() > 0) {
			StringBuilder b = new StringBuilder();
			for(DetectorImpl det: ds.toArray()) {
				b.append(" D");
				b.append(det.getIndex());
			}
			out.print(attr + "='");
			out.print(b.toString().trim());
			out.print("' ");
		}
	}
}
