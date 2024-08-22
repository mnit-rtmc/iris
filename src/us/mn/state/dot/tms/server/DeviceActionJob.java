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
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.DeviceActionHelper;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.LaneMarkingHelper;

/**
 * Job to perform device actions.
 *
 * @author Douglas Lau
 */
public class DeviceActionJob extends Job {

	/** Logger for debugging */
	private final DebugLog logger;

	/** Mapping of DMS to device actions */
	private final HashMap<DMSImpl, DeviceActionMsg> dms_actions =
		new HashMap<DMSImpl, DeviceActionMsg>();

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
				performLaneMarkingAction(da, deploy);
			}
		}
		updateDmsMessages();
	}

	/** Perform a DMS action */
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
			DeviceActionMsg amsg = new DeviceActionMsg(da, dms,
				dms.getGeoLoc(), logger);
			if (amsg.isRasterizable())
				dms_actions.put(dms, amsg);
		} else if (logger.isOpen())
			logSched(dms, "dropping " + da);
	}

	/** Check if an action should replace the current DMS action */
	private boolean shouldReplace(DeviceAction da, DMSImpl dms) {
		DeviceActionMsg amsg = dms_actions.get(dms);
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
				DeviceActionMsg amsg = dms_actions.get(dmsi);
				if (logger.isOpen())
					logSched(dms, "scheduling " + amsg);
				dmsi.setActionMsg(amsg);
			}
		}
	}

	/** Perform a lane marking action */
	private void performLaneMarkingAction(DeviceAction da, boolean deploy) {
		String ht = da.getHashtag();
		Iterator<LaneMarking> it = LaneMarkingHelper.iterator();
		while (it.hasNext()) {
			LaneMarking lm = it.next();
			if (lm instanceof LaneMarkingImpl) {
				LaneMarkingImpl lmi = (LaneMarkingImpl) lm;
				Hashtags tags = new Hashtags(lmi.getNotes());
				if (tags.contains(ht)) {
					DeviceActionMsg amsg =
						new DeviceActionMsg(
							da,
							lmi,
							lmi.getGeoLoc(),
							logger
						);
					if (amsg.isPassing())
						lmi.setDeployed(deploy);
				}
			}
		}
	}
}
