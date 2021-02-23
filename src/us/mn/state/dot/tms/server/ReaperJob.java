/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2021  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.AlertInfo;
import us.mn.state.dot.tms.AlertInfoHelper;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentHelper;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;

/**
 * Job to periodically reap dead stuff.
 *
 * @author Douglas Lau
 */
public class ReaperJob extends Job {

	/** Get the incident reap time threshold (ms) */
	static private long getIncidentClearThreshold() {
		int secs = SystemAttrEnum.INCIDENT_CLEAR_SECS.getInt();
		return secs * (long) 1000;
	}

	/** Get the alert reap time threshold (ms) */
	static private long getAlertClearThreshold() {
		int secs = SystemAttrEnum.ALERT_CLEAR_SECS.getInt();
		return secs * (long) 1000;
	}

	/** Seconds to offset each poll from start of interval */
	static private final int OFFSET_SECS = 27;

	/** List of zombie sign messages */
	private final ArrayList<SignMessageImpl> zombie_msgs;

	/** List of zombie incidents */
	private final ArrayList<IncidentImpl> zombie_incs;

	/** List of zombie alerts */
	private final ArrayList<AlertInfoImpl> zombie_alerts;

	/** Create a new job to reap dead stuff */
	public ReaperJob() {
		super(Calendar.MINUTE, 1, Calendar.SECOND, OFFSET_SECS);
		zombie_msgs = new ArrayList<SignMessageImpl>();
		zombie_incs = new ArrayList<IncidentImpl>();
		zombie_alerts = new ArrayList<AlertInfoImpl>();
	}

	/** Perform the reaper job */
	@Override
	public void perform() {
		reapSignMessages();
		reapIncidents();
		reapAlerts();
		FeedBucket.purgeExpired();
	}

	/** Reap sign messages which have been unused for awhile */
	private void reapSignMessages() {
		// NOTE: there is a small race where a client could send a
		// message to a DMS just after isReferenced is called.  It can
		// only happen during a very short window about one minute
		// after the message loses its last reference.  Is it worth
		// making a fix for this unlikely scenario?
		if (zombie_msgs.isEmpty()) {
			findReapableMessages();
			removeReferencedMessages();
		} else {
			for (SignMessageImpl sm: zombie_msgs)
				reapMessage(sm);
			zombie_msgs.clear();
		}
	}

	/** Reap one sign message */
	private void reapMessage(SignMessageImpl sm) {
		// Make sure the message has not already been
		// reaped by looking it up in the namespace.
		// This is needed because objects are removed
		// asynchronously from the namespace.
		SignMessage m = SignMessageHelper.lookup(sm.getName());
		if ((m == sm) && !isReferenced(m)) {
			sm.notifyRemove();
			sm.logMsg("removed (reaper)");
		}
	}

	/** Find all reapable sign messages */
	private void findReapableMessages() {
		Iterator<SignMessage> it = SignMessageHelper.iterator();
		while (it.hasNext()) {
			SignMessage sm = it.next();
			if (sm instanceof SignMessageImpl)
				zombie_msgs.add((SignMessageImpl) sm);
		}
	}

	/** Remove referenced sign messages */
	private void removeReferencedMessages() {
		Iterator<SignMessageImpl> it = zombie_msgs.iterator();
		while (it.hasNext()) {
			SignMessage sm = it.next();
			if (isReferenced(sm))
				it.remove();
		}
	}

	/** Check if a sign message is referenced by any DMS */
	private boolean isReferenced(SignMessage sm) {
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS dms = it.next();
			if (dms instanceof DMSImpl) {
				DMSImpl dmsi = (DMSImpl) dms;
				if (dmsi.hasReference(sm))
					return true;
			}
		}
		return false;
	}

	/** Reap incidents which have been cleared for awhile */
	private void reapIncidents() {
		if (zombie_incs.isEmpty())
			findReapableIncidents();
		else {
			for (IncidentImpl inc: zombie_incs)
				reapIncident(inc);
			zombie_incs.clear();
		}
	}

	/** Find all reapable incidents */
	private void findReapableIncidents() {
		Iterator<Incident> it = IncidentHelper.iterator();
		while (it.hasNext()) {
			Incident i = it.next();
			if (i instanceof IncidentImpl) {
				IncidentImpl inc = (IncidentImpl) i;
				if (isReapable(inc))
					zombie_incs.add(inc);
			}
		}
	}

	/** Check if an incident is reapable */
	private boolean isReapable(IncidentImpl inc) {
		return inc.getCleared() &&
		    ((!inc.getConfirmed()) || isPastReapTime(inc));
	}

	/** Check if it is past the time an incident may be reaped */
	private boolean isPastReapTime(IncidentImpl inc) {
		return getReapTime(inc) < TimeSteward.currentTimeMillis();
	}

	/** Get the time when incident may be reaped */
	private long getReapTime(IncidentImpl inc) {
		return inc.getClearTime() + getIncidentClearThreshold();
	}

	/** Reap an incident */
	private void reapIncident(IncidentImpl inc) {
		// Make sure the incident has not already been
		// reaped by looking it up in the namespace.
		// This is needed because objects are removed
		// asynchronously from the namespace.
		Incident i = IncidentHelper.lookup(inc.getName());
		if ((i == inc) && isReapable(inc))
			inc.notifyRemove();
	}

	/** Reap alerts which have been cleared for awhile */
	private void reapAlerts() {
		if (zombie_alerts.isEmpty())
			findReapableAlerts();
		else {
			for (AlertInfoImpl ai: zombie_alerts)
				reapAlert(ai);
			zombie_alerts.clear();
		}
	}

	/** Find all reapable alerts */
	private void findReapableAlerts() {
		Iterator<AlertInfo> it = AlertInfoHelper.iterator();
		while (it.hasNext()) {
			AlertInfo ai = it.next();
			if (ai instanceof AlertInfoImpl) {
				AlertInfoImpl aii = (AlertInfoImpl) ai;
				if (isReapable(aii))
					zombie_alerts.add(aii);
			}
		}
	}

	/** Check if an alert is reapable */
	private boolean isReapable(AlertInfoImpl ai) {
		return isPastReapTime(ai) && isReapable(ai.getActionPlan());
	}

	/** Check if it is past the time an alert may be reaped */
	private boolean isPastReapTime(AlertInfoImpl ai) {
		return getReapTime(ai) < TimeSteward.currentTimeMillis();
	}

	/** Check if an action plan is reapable */
	private boolean isReapable(ActionPlan plan) {
		return (plan.getPhase().getName() == PlanPhase.UNDEPLOYED) ||
		       !plan.getActive();
	}

	/** Get the time when alert may be reaped */
	private long getReapTime(AlertInfoImpl ai) {
		return ai.getClearTime() + getAlertClearThreshold();
	}

	/** Reap an alert */
	private void reapAlert(AlertInfoImpl ai) {
		// Make sure the alert has not already been
		// reaped by looking it up in the namespace.
		// This is needed because objects are removed
		// asynchronously from the namespace.
		AlertInfo a = AlertInfoHelper.lookup(ai.getName());
		if ((a == ai) && isReapable(ai)) {
			if (ai.getAlertState() != AlertState.CLEARED.ordinal()){
				try {
					ai.clear();
				}
				catch (TMSException e) {
					e.printStackTrace();
				}
			} else
				ai.notifyRemove();
		}
	}
}
