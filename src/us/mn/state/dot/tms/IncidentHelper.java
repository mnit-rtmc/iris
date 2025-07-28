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
package us.mn.state.dot.tms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import us.mn.state.dot.sched.TimeSteward;

/**
 * Helper class for Incident.
 *
 * @author Douglas Lau
 */
public class IncidentHelper extends BaseHelper {

	/** Don't instantiate */
	private IncidentHelper() {
		assert false;
	}

	/** Lookup the Incident with the specified name */
	static public Incident lookup(String name) {
		return (Incident) namespace.lookupObject(Incident.SONAR_TYPE,
			name);
	}

	/** Get an incident iterator */
	static public Iterator<Incident> iterator() {
		return new IteratorWrapper<Incident>(namespace.iterator(
			Incident.SONAR_TYPE));
	}

	/** Lookup an incident by the original name */
	static public Incident lookupByOriginal(String name) {
		if (null == name)
			return null;
		Incident inc = lookup(name);
		if (inc != null)
			return inc;
		Iterator<Incident> it = iterator();
		while (it.hasNext()) {
			inc = it.next();
			if (name.equals(inc.getReplaces()))
				return inc;
		}
		return null;
	}

	/** Get original incident name */
	static public String getOriginalName(Incident inc) {
		String rep = inc.getReplaces();
		return (rep != null) ? rep : inc.getName();
	}

	/** Lookup the camera for an incident */
	static public Camera getCamera(Incident inc) {
		return (inc != null) ? inc.getCamera() : null;
	}

	/** Create a unique incident name */
	static public String createUniqueName() {
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String name = f.format(TimeSteward.currentTimeMillis());
		return name.substring(0, 16);
	}

	/** Get the severity of an incident */
	static public IncSeverity getSeverity(Incident inc) {
		LaneCode lc = LaneCode.fromCode(inc.getLaneCode());
		return IncImpact.severity(inc, lc);
	}

	/** Get the sign message priority for an incident */
	static public SignMsgPriority getPriority(Incident inc) {
		if (inc != null) {
			if (inc.getCleared())
				return SignMsgPriority.low_sys;
			IncSeverity sev = getSeverity(inc);
			if (sev != null)
				return sev.priority;
		}
		return null;
	}

	/** Get list of signs deployed for an incident */
	static public ArrayList<DMS> getDeployedSigns(Incident inc) {
		String orig_name = getOriginalName(inc);
		ArrayList<DMS> signs = new ArrayList<DMS>();
		Iterator<DMS> dit = DMSHelper.iterator();
		while (dit.hasNext()) {
			DMS dms = dit.next();
			DmsLock lk = new DmsLock(dms.getLock());
			String oi = lk.optIncident();
			if (oi != null && oi.equals(orig_name))
				signs.add(dms);
		}
		return signs;
	}

	/** Get count of signs deployed for an incident */
	static public int getDeployedCount(Incident inc) {
		String orig_name = getOriginalName(inc);
		int n_signs = 0;
		Iterator<DMS> dit = DMSHelper.iterator();
		while (dit.hasNext()) {
			DMS dms = dit.next();
			DmsLock lk = new DmsLock(dms.getLock());
			String oi = lk.optIncident();
			if (oi != null && oi.equals(orig_name))
				n_signs++;
		}
		return n_signs;
	}
}
