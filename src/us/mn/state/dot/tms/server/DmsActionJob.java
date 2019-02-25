/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DmsActionHelper;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.SignGroup;

/**
 * Job to perform DMS actions.
 *
 * @author Douglas Lau
 */
public class DmsActionJob extends Job {

	/** Logger for debugging */
	private final DebugLog logger;

	/** Mapping of DMS actions */
	private final HashMap<DMSImpl, DmsActionMsg> dms_actions =
		new HashMap<DMSImpl, DmsActionMsg>();

	/** Create a new DMS action job */
	public DmsActionJob(DebugLog dl) {
		super(0);
		logger = dl;
	}

	/** Log a DMS schedule message */
	private void logSched(DMS dms, String msg) {
		if (logger.isOpen())
			logger.log(dms.getName() + ": " + msg);
	}

	/** Perform DMS actions */
	@Override
	public void perform() {
		Iterator<DmsAction> it = DmsActionHelper.iterator();
		while (it.hasNext()) {
			DmsAction da = it.next();
			ActionPlan ap = da.getActionPlan();
			if (ap.getActive()) {
				if (ap.getPhase() == da.getPhase())
					performDmsAction(da);
			}
		}
		updateDmsMessages();
	}

	/** Perform a DMS action */
	private void performDmsAction(DmsAction da) {
		SignGroup sg = da.getSignGroup();
		Iterator<DmsSignGroup> it = DmsSignGroupHelper.iterator();
		while (it.hasNext()) {
			DmsSignGroup dsg = it.next();
			if (dsg.getSignGroup() == sg) {
				DMS dms = dsg.getDms();
				if (dms instanceof DMSImpl)
					checkAction(da, (DMSImpl) dms);
			}
		}
	}

	/** Check an action for one DMS */
	private void checkAction(DmsAction da, DMSImpl dms) {
		if (logger.isOpen())
			logSched(dms, "checking " + da);
		if (shouldReplace(da, dms)) {
			DmsActionMsg amsg = new DmsActionMsg(da, dms, logger);
			if (amsg.isValid())
				dms_actions.put(dms, amsg);
		} else if (logger.isOpen())
			logSched(dms, "dropping " + da);
	}

	/** Check if an action should replace the current DMS action */
	private boolean shouldReplace(DmsAction da, DMSImpl dms) {
		DmsActionMsg amsg = dms_actions.get(dms);
		DmsAction o = (amsg != null) ? amsg.action : null;
		return (null == o) || da.getMsgPriority() >= o.getMsgPriority();
	}

	/** Update the DMS messages */
	private void updateDmsMessages() {
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS dms = it.next();
			if (dms instanceof DMSImpl) {
				DMSImpl dmsi = (DMSImpl) dms;
				DmsActionMsg amsg = dms_actions.get(dmsi);
				if (logger.isOpen())
					logSched(dms, "scheduling " + amsg);
				dmsi.setActionMsg(amsg);
			}
		}
	}
}
