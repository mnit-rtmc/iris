/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2022  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.clearguide;

import java.util.LinkedList;
import org.json.JSONObject;
import org.json.JSONArray;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * ClearGuide Routes
 *
 * @author Michael Darter
 */
public class Routes {

	/** Write a log message */
	static private void log(String msg) {
		ClearGuidePoller.slog("Routes." + msg);
	}

	/** Routes associated with a single DMS */
	private final LinkedList<Route> cg_routes = new LinkedList<Route>();

	/** Constructor */
	protected Routes() { }

	/** Get number of routes in container */
	protected int size() {
		return cg_routes.size();
	}

	/** Add a new route to the end of list.
	 * @return True if the collection changed */
	protected Routes add(Route r) {
		if (!cg_routes.add(r))
			log("addRoute: add route failure rid=" + r.gcr_id);
		return this;
	}

	/** Parse routes from JSON array.
	 * @param jroutes Array of routes, each an object of route attribs.
	 * @return Number of routes added */
	protected int parse(JSONArray jroutes) throws ParsingException {
		for (int i = 0; i < jroutes.length(); i++) {
			JSONObject jroute = jroutes.getJSONObject(i);
			add(new Route(jroute));
		}
		return size();
	}

	/** Get a statistic for the specified route.
	 * @param wid ClearGuide workzone ID, 0 to ignore.
	 * @param mode Statistic to retrieve as defined by [cg] tag.
	 * @param idx Workzone index, zero based.
	 * @return Specified statistic or null if not found */
	protected Integer getStat(int wid, String mode, int idx) {
		if (idx >= 0 && idx < cg_routes.size()) {
			Route route = cg_routes.get(idx);
			return route.getStat(wid, mode);
		}
		return null;
	}

	/** To string */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(Routes:");
		for (Route cgr : cg_routes)
			sb.append(" ").append(cgr);
		sb.append(")");
		return sb.toString();
	}
}
