/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.incfeed;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.tms.IncidentHelper;
import us.mn.state.dot.tms.server.IncidentImpl;

/**
 * Cache of incidents in an incident feed.
 *
 * @author Douglas Lau
 */
public class IncidentCache {

	/** Map of active incidents */
	private final HashMap<String, IncidentImpl> incidents =
		new HashMap<String, IncidentImpl>();

	/** Set of refreshed incidents since last update */
	private final HashSet<String> nxt = new HashSet<String>();

	/** Flag to incidate cache has been updated */
	private boolean updated = false;

	/** Check if the cache is empty */
	public boolean isUpdated() {
		return updated;
	}

	/** Check if the cache contains an incident ID */
	public boolean contains(String id) {
		return incidents.containsKey(id);
	}

	/** Put an incident into the cache. */
	public void put(String id, IncidentImpl inc) {
		incidents.put(id, inc);
		nxt.add(id);
	}

	/** Refresh an incident.  This prevents the incident from being cleared
	 * on the next update. */
	public void refresh(String id) {
		nxt.add(id);
	}

	/** Update the cache.  Any incidents which have not been refreshed
	 * since this was last called will be cleared. */
	public void update() {
		Iterator<Map.Entry<String, IncidentImpl>> it =
			incidents.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, IncidentImpl> ent = it.next();
			String id = ent.getKey();
			if (!nxt.contains(id)) {
				clear(ent.getValue());
				it.remove();
			}
		}
		nxt.clear();
		updated = true;
	}

	/** Clear one incident */
	private void clear(IncidentImpl inc) {
		if (isActive(inc))
			inc.setClearedNotify(true);
	}

	/** Check if an incident is active */
	private boolean isActive(IncidentImpl inc) {
		return inc != null
		    && IncidentHelper.lookup(inc.getName()) == inc;
	}
}
