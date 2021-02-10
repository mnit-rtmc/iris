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
		CapResponseType rt, CapUrgency urg)
	{
		ArrayList<AlertConfig> cfgs = new ArrayList<AlertConfig>();
		Iterator<AlertConfig> it = iterator();
		while (it.hasNext()) {
			AlertConfig cfg = it.next();
			CapEvent event = CapEvent.fromCode(cfg.getEvent());
			CapResponseType response_type = CapResponseType
				.fromOrdinal(cfg.getResponseType());
			CapUrgency urgency = CapUrgency.fromOrdinal(
				cfg.getUrgency());
			if (ev == event && rt == response_type &&
			    urg == urgency)
				cfgs.add(cfg);
		}
		return cfgs;
	}
}
