/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.BeaconHelper;
import us.mn.state.dot.tms.BeaconState;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.DeviceActionHelper;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;

/**
 * Job to perform device actions.
 *
 * @author Douglas Lau
 */
public class DeviceActionJob extends Job {

	/** Plan debug log */
	static final DebugLog PLAN_LOG = new DebugLog("plan");

	/** Single action plan to process (null for all) */
	private final ActionPlanImpl plan;

	/** Logger for debugging */
	private final DebugLog logger;

	/** Mapping of DMS to action tag messages */
	private final HashMap<DMSImpl, ActionTagMsg> dms_actions =
		new HashMap<DMSImpl, ActionTagMsg>();

	/** Mapping of ramp meters to operating states */
	private final HashMap<RampMeterImpl, Boolean> meters =
		new HashMap<RampMeterImpl, Boolean>();

	/** Create a new device action job */
	public DeviceActionJob(ActionPlanImpl ap) {
		super(0);
		logger = PLAN_LOG;
		plan = ap;
	}

	/** Create a new device action job */
	public DeviceActionJob() {
		this(null);
	}

	/** Log a device plan message */
	private void logMsg(Device dev, String msg) {
		logger.log(dev.getName() + ": " + msg);
	}

	/** Perform device actions */
	@Override
	public void perform() {
		Iterator<DeviceAction> it = DeviceActionHelper.iterator();
		while (it.hasNext()) {
			DeviceAction da = it.next();
			ActionPlan ap = da.getActionPlan();
			if (ap.getActive() && (plan == null || plan == ap))
				processAction(ap, da);
		}
		updateDmsMessages();
		updateRampMeterStates();
	}

	/** Process one device action */
	private void processAction(ActionPlan ap, DeviceAction da) {
		boolean deploy = (ap.getPhase() == da.getPhase());
		if (deploy)
			performDmsAction(da);
		performBeaconAction(da, deploy);
		performRampMeterAction(da, deploy);
	}

	/** Perform an action for DMS */
	private void performDmsAction(DeviceAction da) {
		String ht = da.getHashtag();
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS d = it.next();
			if (d instanceof DMSImpl) {
				DMSImpl dms = (DMSImpl) d;
				Hashtags tags = new Hashtags(dms.getNotes());
				if (tags.contains(ht))
					checkAction(da, dms);
			}
		}
	}

	/** Check an action for one DMS */
	private void checkAction(DeviceAction da, DMSImpl dms) {
		if (shouldReplace(da, dms)) {
			if (logger.isOpen())
				logMsg(dms, "checking " + da);
			ActionTagMsg amsg = new ActionTagMsg(da, dms,
				dms.getGeoLoc());
			if (DMSHelper.isRasterizable(dms, amsg.getMulti()))
				dms_actions.put(dms, amsg);
		} else if (logger.isOpen())
			logMsg(dms, "dropping " + da);
	}

	/** Check if an action should replace the current DMS action */
	private boolean shouldReplace(DeviceAction da, DMSImpl dms) {
		ActionTagMsg amsg = dms_actions.get(dms);
		DeviceAction o = (amsg != null) ? amsg.action : null;
		return (null == o) || da.getMsgPriority() >= o.getMsgPriority();
	}

	/** Update the DMS messages */
	private void updateDmsMessages() {
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS dms = it.next();
			if (dms instanceof DMSImpl) {
				DMSImpl dmsi = (DMSImpl) dms;
				ActionTagMsg amsg = dms_actions.get(dmsi);
				if (logger.isOpen())
					logMsg(dms, "scheduling " + amsg);
				dmsi.setActionMsg(amsg);
			}
		}
	}

	/** Perform an action for beacons */
	private void performBeaconAction(DeviceAction da, boolean deploy) {
		Iterator<Beacon> it = BeaconHelper.iterator();
		while (it.hasNext()) {
			Beacon b = it.next();
			if (b instanceof BeaconImpl)
				performBeaconAction(da, deploy, (BeaconImpl) b);
		}
	}

	/** Perform a beacon action */
	private void performBeaconAction(DeviceAction da, boolean deploy,
		BeaconImpl b)
	{
		Hashtags tags = new Hashtags(b.getNotes());
		if (tags.contains(da.getHashtag())) {
			ActionTagMsg amsg = new ActionTagMsg(da, b,
				b.getGeoLoc());
			BeaconState bs = (amsg.isCondition() && deploy)
				? BeaconState.FLASHING_REQ
				: BeaconState.DARK_REQ;
			b.setState(bs.ordinal());
		}
	}

	/** Perform an action for ramp meters */
	private void performRampMeterAction(DeviceAction da, boolean deploy) {
		Iterator<RampMeter> it = RampMeterHelper.iterator();
		while (it.hasNext()) {
			RampMeter rm = it.next();
			if (rm instanceof RampMeterImpl) {
				performRampMeterAction(da, deploy,
					(RampMeterImpl) rm);
			}
		}
	}

	/** Perform a ramp meter action */
	private void performRampMeterAction(DeviceAction da, boolean deploy,
		RampMeterImpl rm)
	{
		Hashtags tags = new Hashtags(rm.getNotes());
		if (tags.contains(da.getHashtag())) {
			ActionTagMsg amsg = new ActionTagMsg(da, rm,
				rm.getGeoLoc());
			boolean operate = amsg.isCondition() && deploy;
			if (meters.containsKey(rm))
				operate |= meters.get(rm);
			meters.put(rm, operate);
		}
	}

	/** Update the ramp meter states */
	private void updateRampMeterStates() {
		for (Map.Entry<RampMeterImpl, Boolean> e: meters.entrySet())
			e.getKey().setOperating(e.getValue());
	}
}
