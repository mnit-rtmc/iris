/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * Helper class for alert configurations.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class AlertConfigHelper extends BaseHelper {

	/** Name creator */
	static private final UniqueNameCreator UNC = new UniqueNameCreator(
		"alert_cfg_%d", 20, (n)->lookup(n));

	/** Create a unique record name */
	static public String createUniqueName() {
		return UNC.createUniqueName();
	}

	/** Don't instantiate */
	private AlertConfigHelper() {
		assert false;
	}

	/** Lookup the alert config with the specified name */
	static public AlertConfig lookup(String name) {
		return (AlertConfig) namespace.lookupObject(
			AlertConfig.SONAR_TYPE, name);
	}

	/** Get an alert config object iterator */
	static public Iterator<AlertConfig> iterator() {
		return new IteratorWrapper<AlertConfig>(namespace.iterator(
			AlertConfig.SONAR_TYPE));
	}

	/** Find matching alert configs */
	static public List<AlertConfig> findMatching(CapEvent ev,
		CapResponseType rt, CapUrgency urg, CapSeverity sev,
		CapCertainty cer)
	{
		ArrayList<AlertConfig> cfgs = new ArrayList<AlertConfig>();
		Iterator<AlertConfig> it = iterator();
		while (it.hasNext()) {
			AlertConfig cfg = it.next();
			CapEvent event = CapEvent.fromCode(cfg.getEvent());
			if (checkResponse(cfg, rt) && checkUrgency(cfg, urg) &&
			    checkSeverity(cfg, sev) && checkCertainty(cfg, cer))
				cfgs.add(cfg);
		}
		return cfgs;
	}

	/** Check a config for a matching response type */
	static private boolean checkResponse(AlertConfig cfg,
		CapResponseType rt)
	{
		switch (rt) {
		case SHELTER: return cfg.getResponseShelter();
		case EVACUATE: return cfg.getResponseEvacuate();
		case PREPARE: return cfg.getResponsePrepare();
		case EXECUTE: return cfg.getResponseExecute();
		case AVOID: return cfg.getResponseAvoid();
		case MONITOR: return cfg.getResponseMonitor();
		case ALLCLEAR: return cfg.getResponseAllClear();
		case NONE: return cfg.getResponseNone();
		default: return false;
		}
	}

	/** Check a config for a matching urgency */
	static private boolean checkUrgency(AlertConfig cfg,
		CapUrgency urg)
	{
		switch (urg) {
		case UNKNOWN: return cfg.getUrgencyUnknown();
		case PAST: return cfg.getUrgencyPast();
		case FUTURE: return cfg.getUrgencyFuture();
		case EXPECTED: return cfg.getUrgencyExpected();
		case IMMEDIATE: return cfg.getUrgencyImmediate();
		default: return false;
		}
	}

	/** Check a config for a matching severity */
	static private boolean checkSeverity(AlertConfig cfg,
		CapSeverity sev)
	{
		switch (sev) {
		case UNKNOWN: return cfg.getSeverityUnknown();
		case MINOR: return cfg.getSeverityMinor();
		case MODERATE: return cfg.getSeverityModerate();
		case SEVERE: return cfg.getSeveritySevere();
		case EXTREME: return cfg.getSeverityExtreme();
		default: return false;
		}
	}

	/** Check a config for a matching certainty */
	static private boolean checkCertainty(AlertConfig cfg,
		CapCertainty cer)
	{
		switch (cer) {
		case UNKNOWN: return cfg.getCertaintyUnknown();
		case UNLIKELY: return cfg.getCertaintyUnlikely();
		case POSSIBLE: return cfg.getCertaintyPossible();
		case LIKELY: return cfg.getCertaintyLikely();
		case OBSERVED: return cfg.getCertaintyObserved();
		default: return false;
		}
	}

	/** Get the set of all signs for an alert configuration */
	static public Set<DMS> getAllSigns(AlertConfig cfg) {
		TreeSet<DMS> all_dms = new TreeSet<DMS>();
		for (QuickMessage qm: cfg.getQuickMessages()) {
			SignGroup sg = qm.getSignGroup();
			if (sg != null)
				all_dms.addAll(SignGroupHelper.getAllSigns(sg));
		}
		return all_dms;
	}
}
