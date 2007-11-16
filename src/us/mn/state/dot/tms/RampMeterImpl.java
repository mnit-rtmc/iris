/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
			meterList.update(id);
		}
		super.notifyStatus();
	}

	/** Create a new ramp meter */
	public RampMeterImpl(String id) throws ChangeVetoException,
		RemoteException
	{
		super(id);
		detector = null;
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
	protected DetectorImpl detector;

	/** Set the green count detector */
	public void setDetector(int i) throws TMSException {
		DetectorImpl det = null;
		if(i > 0) {
			det = (DetectorImpl)detList.getElement(i);
			if(det == null)
				throw new ChangeVetoException("Not found");
			if(det.getLaneType() != Detector.GREEN)
				throw new ChangeVetoException("Bad detector");
		}
		int index = setDetector(det);
		if(index > 0)
			detList.update(index);
		if(i > 0)
			detList.update(i);
	}

	/** Set the green count detector. This is the low-level version which
	    does not acquire any locks. */
	protected synchronized int setDetector(DetectorImpl det)
		throws TMSException
	{
		if(det == detector)
			return 0;
		try { vault.update(this, "detector", det, getUserName()); }
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		int index = 0;
		if(detector != null)
			index = detector.getIndex();
		detector = det;
		return index;
	}

	/** Get the green count detector */
	public Detector getDetector() {
		return detector;
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
		try {
			vault.update(this, "controlMode", new Integer(m),
				getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
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
		try {
			vault.update(this, "singleRelease", new Boolean(s),
				getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
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
		if(p.equals(plans))
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
		DetectorImpl det = detector;
		if(det != null) {
			if(singleRelease) {
				if((g % 2) != 0)
					g++;
				g /= 2;
			}
			if(g == 0 && isMetering())
				return;
			det.storeData30Second(stamp, g, MISSING_DATA);
		}
	}

	/** Update the 5-minute green count */
	public void updateGreenCount5(Calendar stamp, int g)
		throws IOException
	{
		DetectorImpl det = detector;
		if(det != null) {
			if(singleRelease) {
				if((g % 2) != 0)
					g++;
				g /= 2;
			}
			det.storeData5Minute(stamp, g, MISSING_DATA);
		}
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
		int green = policy.getValue(SystemPolicy.METER_GREEN_TIME);
		int yellow = policy.getValue(SystemPolicy.METER_YELLOW_TIME);
		int min_red = policy.getValue(SystemPolicy.METER_MIN_RED_TIME);
		int red_time = Math.round(cycle * 10) - (green + yellow);
		return Math.max(red_time, min_red);
	}

	/** Calculate the release rate from a given red time */
	public int calculateReleaseRate(int red_time) {
		int green = policy.getValue(SystemPolicy.METER_GREEN_TIME);
		int yellow = policy.getValue(SystemPolicy.METER_YELLOW_TIME);
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
		try {
			vault.update(this, "storage", new Integer(s),
				getUserName());
		} catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
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
		try {
			vault.update(this, "maxWait", new Integer(w),
				getUserName());
		} catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
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
		try { vault.update(this, "camera", c, getUserName()); }
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		camera = c;
	}

	/** Get verification camera */
	public TrafficDevice getCamera() {
		return camera;
	}

	/** Get the segment list for this meter */
	public SegmentListImpl getSegmentList() {
		RoadwayImpl freeway = (RoadwayImpl)location.getFreeway();
		short freeDir = location.getFreeDir();
		if(freeway == null)
			return null;
		else
			return (SegmentListImpl)freeway.getSegmentList(freeDir);
	}

	/** Get the detectors in the associated meterable segment */
	public Detector[] getSegmentDetectors() {
		SegmentListImpl sList = getSegmentList();
		if(sList == null)
			return new Detector[0];
		RoadwayImpl cross = (RoadwayImpl)location.getCrossStreet();
		short crossDir = location.getCrossDir();
		MeterableImpl meterable = sList.findMeterable(cross, crossDir);
		if(meterable != null)
			return meterable.getDetectors();
		else
			return new Detector[0];
	}

	/** Get the ID of the corridor containing the ramp meter */
	public String getCorridorID() {
		return location.getCorridorID();
	}

	/** Print a single detector as an XML element */
	public void printXmlElement(PrintWriter out) {
		out.print("<meter id='" + getId() + "' ");
		out.print("label='" + getLabel() + "' ");
		out.print("storage='" + getStorage() + "' ");
		int w = getMaxWait();
		if(w != DEFAULT_MAX_WAIT)
			out.print("max_wait='" + w + "' ");
		DetectorImpl green = (DetectorImpl)getDetector();
		if(green != null)
			out.print("green='D" + green.getIndex() + "' ");
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
		StringBuffer passage = new StringBuffer();
		StringBuffer merge = new StringBuffer();
		StringBuffer queue = new StringBuffer();
		StringBuffer bypass = new StringBuffer();
		Detector[] dets = getSegmentDetectors();
		for(int i = 0; i < dets.length; i++) {
			DetectorImpl det = (DetectorImpl)dets[i];
			String index = " D" + det.getIndex();
			short lane = det.getLaneType();
			if(lane == Detector.PASSAGE)
				passage.append(index);
			if(lane == Detector.MERGE)
				merge.append(index);
			if(lane == Detector.QUEUE)
				queue.append(index);
			if(lane == Detector.BYPASS)
				bypass.append(index);
		}
		printAttribute("passage", passage, out);
		printAttribute("merge", merge, out);
		printAttribute("queue", queue, out);
		printAttribute("bypass", bypass, out);
	}

	/** Print all the meter detectors for a specified lane type */
	protected void printAttribute(String attr, StringBuffer value,
		PrintWriter out)
	{
		if(value.length() > 0) {
			out.print(attr + "='");
			out.print(value.toString().trim());
			out.print("' ");
		}
	}
}
