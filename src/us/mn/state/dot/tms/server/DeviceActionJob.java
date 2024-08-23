/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.DeviceActionHelper;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.LaneMarkingHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;

/**
 * Job to perform device actions.
 *
 * @author Douglas Lau
 */
public class DeviceActionJob extends Job {

	/** Logger for debugging */
	private final DebugLog logger;

	/** Mapping of DMS to action tag messages */
	private final HashMap<DMSImpl, ActionTagMsg> dms_actions =
		new HashMap<DMSImpl, ActionTagMsg>();

	/** Mapping of ramp meters to operating states */
	private final HashMap<RampMeterImpl, Boolean> meters =
		new HashMap<RampMeterImpl, Boolean>();

	/** Create a new device action job */
	public DeviceActionJob(DebugLog dl) {
		super(0);
		logger = dl;
	}

	/** Log a DMS schedule message */
	private void logSched(DMS dms, String msg) {
		if (logger.isOpen())
			logger.log(dms.getName() + ": " + msg);
	}

	/** Perform device actions */
	@Override
	public void perform() {
		Iterator<DeviceAction> it = DeviceActionHelper.iterator();
		while (it.hasNext()) {
			DeviceAction da = it.next();
			ActionPlan ap = da.getActionPlan();
			if (ap.getActive()) {
				boolean deploy =
					(ap.getPhase() == da.getPhase());
				if (deploy)
					performDmsAction(da);
				performCameraAction(da);
				performLaneMarkingAction(da, deploy);
				performRampMeterAction(da, deploy);
			}
		}
		updateDmsMessages();
		updateRampMeterStates();
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
		if (logger.isOpen())
			logSched(dms, "checking " + da);
		if (shouldReplace(da, dms)) {
			ActionTagMsg amsg = new ActionTagMsg(da, dms,
				dms.getGeoLoc(), logger);
			if (DMSHelper.isRasterizable(dms, amsg.getMulti()))
				dms_actions.put(dms, amsg);
		} else if (logger.isOpen())
			logSched(dms, "dropping " + da);
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
					logSched(dms, "scheduling " + amsg);
				dmsi.setActionMsg(amsg);
			}
		}
	}

	/** Perform an action for cameras */
	private void performCameraAction(DeviceAction da) {
		// FIXME: only perform this action when phase is first changed
		Iterator<Camera> it = CameraHelper.iterator();
		while (it.hasNext()) {
			Camera c = it.next();
			if (c instanceof CameraImpl)
				performCameraAction(da, (CameraImpl) c);
		}
	}

	/** Perform a camera action */
	private void performCameraAction(DeviceAction da, CameraImpl cam) {
		Hashtags tags = new Hashtags(cam.getNotes());
		if (tags.contains(da.getHashtag())) {
			ActionTagMsg amsg = new ActionTagMsg(da, cam,
				cam.getGeoLoc(), logger);
			if (amsg.isPassing()) {
				// FIXME: recall preset / save a snapshot
				//        after a moment
				// cam.setRecallPreset(...);
			}
		}
	}

	/** Perform an action for lane markings */
	private void performLaneMarkingAction(DeviceAction da, boolean deploy) {
		Iterator<LaneMarking> it = LaneMarkingHelper.iterator();
		while (it.hasNext()) {
			LaneMarking lm = it.next();
			if (lm instanceof LaneMarkingImpl) {
				performLaneMarkingAction(da, deploy,
					(LaneMarkingImpl) lm);
			}
		}
	}

	/** Perform a lane marking action */
	private void performLaneMarkingAction(DeviceAction da, boolean deploy,
		LaneMarkingImpl lm)
	{
		Hashtags tags = new Hashtags(lm.getNotes());
		if (tags.contains(da.getHashtag())) {
			ActionTagMsg amsg = new ActionTagMsg(da, lm,
				lm.getGeoLoc(), logger);
			if (amsg.isPassing())
				lm.setDeployed(deploy);
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
				rm.getGeoLoc(), logger);
			boolean operate = amsg.isPassing() && deploy;
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
