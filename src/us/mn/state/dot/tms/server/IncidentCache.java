/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
import java.util.HashSet;
import java.util.Iterator;

/**
 * Cache of incidents in incident feeds.
 *
 * @author Douglas Lau
 */
public class IncidentCache {

	/** Map of active incidents */
	private final HashMap<String, IncidentImpl> incidents =
		new HashMap<String, IncidentImpl>();

	/** Map of updated incidents */
	private final HashMap<String, IncidentImpl> nxt =
		new HashMap<String, IncidentImpl>();

	/** Set of cleared incidents */
	private final HashSet<String> cleared = new HashSet<String>();

	/** Set of garbage incidents */
	private final HashSet<String> garbage = new HashSet<String>();

	/** Check if the cache contains an incident ID */
	public synchronized boolean contains(String id) {
		return incidents.containsKey(id)
		    || cleared.contains(id)
		    || garbage.contains(id);
	}

	/** Put an incident */
	public synchronized void put(String id, IncidentImpl inc) {
		nxt.put(id, inc);
	}

	/** Refresh an incident */
	public synchronized void refresh(String id) {
		IncidentImpl inc = incidents.get(id);
		if (inc != null)
			nxt.put(id, inc);
		else {
			cleared.add(id);
			garbage.remove(id);
		}
	}

	/** Update the cache */
	public synchronized void update() {
		Iterator<String> it = incidents.keySet().iterator();
		while (it.hasNext()) {
			String id = it.next();
			if (!nxt.containsKey(id)) {
				clear(id);
				it.remove();
			}
		}
		incidents.putAll(nxt);
		nxt.clear();
	}

	/** Clear one incident */
	private void clear(String id) {
		cleared.add(id);
		IncidentImpl inc = incidents.get(id);
		if (inc != null)
			clear(inc);
	}

	/** Clear one incident */
	private void clear(IncidentImpl inc) {
		inc.setClearedNotify(true);
		if (!inc.getConfirmed())
			inc.notifyRemove();
	}

	/** Purge old garbage */
	public synchronized void purge() {
		garbage.clear();
		garbage.addAll(cleared);
		cleared.clear();
	}
}
