/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.DeviceActionHelper;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentHelper;
import us.mn.state.dot.tms.PlanPhase;

/**
 * Job to perform plan phase change actions.
 *
 * @author Douglas Lau
 */
public class PlanPhaseChangeJob extends Job {

	/** Logger for debugging */
	private final DebugLog logger;

	/** Action plan */
	private final ActionPlanImpl plan;

	/** Plan phase */
	private final PlanPhase phase;

	/** Create a new plan phase change job */
	public PlanPhaseChangeJob(ActionPlanImpl ap, PlanPhase p) {
		super(0);
		logger = DeviceActionJob.PLAN_LOG;
		plan = ap;
		phase = p;
	}

	/** Log a message */
	private void logMsg(Device dev, String msg) {
		if (logger.isOpen())
			logger.log(dev.getName() + ": " + msg);
	}

	/** Perform plan phase change action */
	@Override
	public void perform() {
		Iterator<DeviceAction> it = DeviceActionHelper.iterator(plan);
		while (it.hasNext()) {
			DeviceAction da = it.next();
			if (da.getPhase() == phase)
				performCameraAction(da);
		}
	}

	/** Perform an action for cameras */
	private void performCameraAction(DeviceAction da) {
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
			logMsg(cam, "action " + da.getName());
			TagProcessor tag = new TagProcessor(da, cam,
				cam.getGeoLoc());
			PlannedAction pa = tag.process();
			if (pa.condition) {
				int preset_num = da.getMsgPriority();
				if (preset_num >= 1 && preset_num <= 12) {
					if (!isIncidentCamera(cam))
						cam.setRecallPreset(preset_num);
				} else if (preset_num == 15) {
					cam.setDeviceReq(DeviceRequest.
						CAMERA_WIPER_ONESHOT);
				} else {
					// FIXME: save snapshot?
				}
			}
		}
	}

	/** Check if a camera is associated with an incident */
	private boolean isIncidentCamera(CameraImpl cam) {
		Iterator<Incident> it = IncidentHelper.iterator();
		while (it.hasNext()) {
			Incident i = it.next();
			if (cam == i.getCamera() && !i.getCleared())
				return true;
		}
		return false;
	}
}
