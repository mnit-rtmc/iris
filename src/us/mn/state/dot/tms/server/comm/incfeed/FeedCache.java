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
package us.mn.state.dot.tms.server.comm.incfeed;

import java.util.HashMap;
import us.mn.state.dot.tms.server.IncidentImpl;

/**
 * Cache of incidents in feed.
 *
 * @author Douglas Lau
 */
public class FeedCache {

	/** Map of incidents */
	private final HashMap<String, IncidentImpl> incidents =
		new HashMap<String, IncidentImpl>();

	/** Map of updated incidents */
	private final HashMap<String, IncidentImpl> nxt =
		new HashMap<String, IncidentImpl>();

	/** Check if the cache contains an incident ID */
	public boolean contains(String id) {
		return incidents.containsKey(id);
	}

	/** Put an incident */
	public void put(String id, IncidentImpl inc) {
		nxt.put(id, inc);
	}

	/** Refresh an incident */
	public void refresh(String id) {
		nxt.put(id, incidents.get(id));
	}

	/** Update the cache */
	public void update() {
		for (String id: incidents.keySet()) {
			if (!nxt.containsKey(id)) {
				IncidentImpl inc = incidents.get(id);
				if (inc != null) {
					inc.setClearedNotify(true);
					if (!inc.getConfirmed())
						inc.notifyRemove();
				}
			}
		}
		incidents.clear();
		incidents.putAll(nxt);
		nxt.clear();
	}
}
