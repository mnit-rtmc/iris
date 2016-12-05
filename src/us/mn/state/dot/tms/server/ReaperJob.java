/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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

import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentHelper;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Job to periodically reap dead stuff.
 *
 * @author Douglas Lau
 */
public class ReaperJob extends Job {

	/** Get the incident clear time threshold (ms) */
	static private long getIncidentClearThreshold() {
		int secs = SystemAttrEnum.INCIDENT_CLEAR_SECS.getInt();
		return secs * (long) 1000;
	}

	/** Seconds to offset each poll from start of interval */
	static private final int OFFSET_SECS = 27;

	/** List of reapable sign messages */
	private final LinkedList<SignMessageImpl> reapable;

	/** Create a new job to reap dead stuff */
	public ReaperJob() {
		super(Calendar.MINUTE, 1, Calendar.SECOND, OFFSET_SECS);
		reapable = new LinkedList<SignMessageImpl>();
	}

	/** Perform the reaper job */
	@Override
	public void perform() {
		reapSignMessages();
		reapIncidents();
		FeedBucket.purgeExpired();
	}

	/** Reap incidents which have been cleared for awhile */
	private void reapIncidents() {
		Iterator<Incident> it = IncidentHelper.iterator();
		while (it.hasNext()) {
			Incident inc = it.next();
			if (inc instanceof IncidentImpl)
				reapIncident((IncidentImpl) inc);
		}
	}

	/** Reap an incident */
	private void reapIncident(IncidentImpl inc) {
		if (isReapable(inc))
			inc.notifyRemove();
	}

	/** Check if an incident is reapable */
	private boolean isReapable(IncidentImpl inc) {
		return inc.getCleared()
		    && isPastClearThreshold(inc)
		    && !isReferenced(inc);
	}

	/** Check if an incident is past the clear threshold */
	private boolean isPastClearThreshold(IncidentImpl inc) {
		return inc.getClearTime() + getIncidentClearThreshold() <
			    TimeSteward.currentTimeMillis();
	}

	/** Check if an incident is referenced by any sign message */
	private boolean isReferenced(IncidentImpl inc) {
		Iterator<SignMessage> it = SignMessageHelper.iterator();
		while (it.hasNext()) {
			SignMessage sm = it.next();
			if (sm.getIncident() == inc)
				return true;
		}
		return false;
	}

	/** Reap sign messages which have been unused for awhile */
	private void reapSignMessages() {
		// NOTE: there is a small race where a client could send a
		// message to a DMS just after isReferenced is called.  It can
		// only happen during a very short window about one minute
		// after the message loses its last reference.  Is it worth
		// making a fix for this unlikely scenario?
		if (reapable.isEmpty()) {
			findReapableMessages();
			removeReferencedMessages();
		} else {
			for (SignMessageImpl sm: reapable)
				reapMessage(sm);
			reapable.clear();
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
				reapable.add((SignMessageImpl) sm);
		}
	}

	/** Remove referenced sign messages */
	private void removeReferencedMessages() {
		Iterator<SignMessageImpl> it = reapable.iterator();
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
}
