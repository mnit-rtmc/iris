/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraAction;
import us.mn.state.dot.tms.CameraActionHelper;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.PlanPhase;

/**
 * Job to update camera actions
 *
 * @author Douglas Lau
 */
public class CameraActionJob extends Job {

	/** Create a new camera action job */
	public CameraActionJob() {
		super(0);
	}

	/** Perform all camera actions */
	@Override
	public void perform() {
		Iterator<CameraAction> it = CameraActionHelper.iterator();
		while (it.hasNext()) {
			CameraAction ca = it.next();
			ActionPlan ap = ca.getActionPlan();
			if (ap.getActive())
				performCameraAction(ca, ap.getPhase());
		}
	}

	/** Perform a camera action */
	private void performCameraAction(CameraAction ca, PlanPhase phase) {
		CameraPreset cp = ca.getPreset();
		if (ca.getPhase() == phase) {
			Camera camera = cp.getCamera();
			camera.setRecallPreset(cp.getPresetNum());
			// FIXME: save a snapshot after a few seconds
		}
	}
}
