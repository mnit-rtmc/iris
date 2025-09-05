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

import java.util.Iterator;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.BeaconHelper;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.DeviceActionHelper;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;

/**
 * Job to perform device actions.
 *
 * All device actions are normally performed at regular 30-second intervals.
 * When an action plan changes to a new phase, the planned actions are
 * checked again for changes.
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
		if (plan == null)
			clearActions();
		processActions();
		chooseActions();
	}

	/** Clear all previous planned actions */
	private void clearActions() {
		Iterator<Beacon> bit = BeaconHelper.iterator();
		while (bit.hasNext())
			clearActions(bit.next());
		Iterator<Camera> cit = CameraHelper.iterator();
		while (cit.hasNext())
			clearActions(cit.next());
		Iterator<DMS> dit = DMSHelper.iterator();
		while (dit.hasNext())
			clearActions(dit.next());
		Iterator<GateArm> git = GateArmHelper.iterator();
		while (git.hasNext())
			clearActions(git.next());
		Iterator<RampMeter> mit = RampMeterHelper.iterator();
		while (mit.hasNext())
			clearActions(mit.next());
	}

	/** Clear previous planned actions for one device */
	private void clearActions(Device d) {
		if (d instanceof DeviceImpl) {
			DeviceImpl dev = (DeviceImpl) d;
			dev.clearPlannedActions();
		}
	}

	/** Process all device actions */
	private void processActions() {
		Iterator<DeviceAction> it = DeviceActionHelper.iterator();
		while (it.hasNext()) {
			DeviceAction da = it.next();
			ActionPlan ap = da.getActionPlan();
			if (ap.getActive() && (plan == null || plan == ap)) {
				processActionBeacon(da);
				processActionCamera(da);
				processActionDms(da);
				processActionGateArm(da);
				processActionMeter(da);
			}
		}
	}

	/** Process an action for beacons */
	private void processActionBeacon(DeviceAction da) {
		String ht = da.getHashtag();
		Iterator<Beacon> it = BeaconHelper.iterator();
		while (it.hasNext()) {
			Beacon b = it.next();
			Hashtags tags = new Hashtags(b.getNotes());
			if (tags.contains(ht))
				checkAction(da, b, b.getGeoLoc());
		}
	}

	/** Process an action for cameras */
	private void processActionCamera(DeviceAction da) {
		String ht = da.getHashtag();
		Iterator<Camera> it = CameraHelper.iterator();
		while (it.hasNext()) {
			Camera c = it.next();
			Hashtags tags = new Hashtags(c.getNotes());
			if (tags.contains(ht))
				checkAction(da, c, c.getGeoLoc());
		}
	}

	/** Process an action for DMS */
	private void processActionDms(DeviceAction da) {
		String ht = da.getHashtag();
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS d = it.next();
			Hashtags tags = new Hashtags(d.getNotes());
			if (tags.contains(ht))
				checkAction(da, d, d.getGeoLoc());
		}
	}

	/** Process an action for gate arms */
	private void processActionGateArm(DeviceAction da) {
		String ht = da.getHashtag();
		Iterator<GateArm> it = GateArmHelper.iterator();
		while (it.hasNext()) {
			GateArm g = it.next();
			Hashtags tags = new Hashtags(g.getNotes());
			if (tags.contains(ht))
				checkAction(da, g, g.getGeoLoc());
		}
	}

	/** Process an action for ramp meters */
	private void processActionMeter(DeviceAction da) {
		String ht = da.getHashtag();
		Iterator<RampMeter> it = RampMeterHelper.iterator();
		while (it.hasNext()) {
			RampMeter rm = it.next();
			Hashtags tags = new Hashtags(rm.getNotes());
			if (tags.contains(ht))
				checkAction(da, rm, rm.getGeoLoc());
		}
	}

	/** Check an action for one device */
	private void checkAction(DeviceAction da, Device d, GeoLoc loc) {
		if (d instanceof DeviceImpl) {
			DeviceImpl dev = (DeviceImpl) d;
			if (logger.isOpen())
				logMsg(dev, "checking " + da);
			TagProcessor tag = new TagProcessor(da, dev, loc);
			PlannedAction pa = tag.process();
			dev.addPlannedAction(pa);
		}
	}

	/** Choose the planned actions for all devices */
	private void chooseActions() {
		Iterator<Beacon> bit = BeaconHelper.iterator();
		while (bit.hasNext())
			choosePlannedAction(bit.next());
		Iterator<Camera> cit = CameraHelper.iterator();
		while (cit.hasNext())
			choosePlannedAction(cit.next());
		Iterator<DMS> dit = DMSHelper.iterator();
		while (dit.hasNext())
			choosePlannedAction(dit.next());
		Iterator<GateArm> git = GateArmHelper.iterator();
		while (git.hasNext())
			choosePlannedAction(git.next());
		Iterator<RampMeter> mit = RampMeterHelper.iterator();
		while (mit.hasNext())
			choosePlannedAction(mit.next());
	}

	/** Choose planned action for one device */
	private void choosePlannedAction(Device d) {
		if (d instanceof DeviceImpl) {
			DeviceImpl dev = (DeviceImpl) d;
			PlannedAction pa = dev.choosePlannedAction();
			if (logger.isOpen())
				logMsg(dev, "chose " + pa);
		}
	}
}
