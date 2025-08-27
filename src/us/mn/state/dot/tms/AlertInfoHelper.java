/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021-2025  Minnesota Department of Transportation
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Helper class for AlertInfo.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class AlertInfoHelper extends BaseHelper {

	/** Don't instantiate */
	private AlertInfoHelper() {
		assert false;
	}

	/** Lookup the AlertInfo with the specified name */
	static public AlertInfo lookup(String name) {
		return (AlertInfo) namespace.lookupObject(AlertInfo.SONAR_TYPE,
			name);
	}

	/** Get an AlertInfo object iterator */
	static public Iterator<AlertInfo> iterator() {
		return new IteratorWrapper<AlertInfo>(namespace.iterator(
			AlertInfo.SONAR_TYPE));
	}

	/** Find all active signs */
	static public Set<DMS> findActiveSigns(AlertInfo ai) {
		HashSet<DMS> signs = new HashSet<DMS>();
		ActionPlan ap = ai.getActionPlan();
		Iterator<DeviceAction> it = DeviceActionHelper.iterator(ap);
		while (it.hasNext()) {
			DeviceAction da = it.next();
			String ht = da.getHashtag();
			Iterator<DMS> dit = DMSHelper.iterator();
			while (dit.hasNext()) {
				DMS d = dit.next();
				String n = d.getNotes();
				if (new Hashtags(n).contains(ht))
					signs.add(d);
			}
		}
		return signs;
	}
}
