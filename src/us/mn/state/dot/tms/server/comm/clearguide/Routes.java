/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Iteris Inc.
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
	protected Routes() {
	}

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
	 * @param jroutes Array of routes, each an object of route attribs:
	 * 	[{"route_id":34731,"speed_mph":68,"route_name":"Cliff to
	 * 	Diffley Que anal","travel_time_mins":1,"delay_ff_mins":0,
	 * 	"delay_sl_mins":0,"timestamp":1.611017443E9}, ...]
	 * @return Number of routes added */
	protected int parse(JSONArray jroutes) throws ParsingException {
		try {
			for (int i = 0; i < jroutes.length(); i++) {
				JSONObject jroute = jroutes.getJSONObject(i);
				add(new Route(
					jroute.getInt("route_id"),
					jroute.getDouble("travel_time_mins"),
					jroute.getDouble("delay_sl_mins"),
					jroute.getLong("timestamp") * 1000));
			}
		} catch(Exception ex) {
			String msg = "parseRoutes ex=" + ex;
			throw new ParsingException(msg);
		} finally {
			return size();
		}
	}

	/** Get a statistic for the specified route.
 	 * @param rid ClearGuide route id, 0 to ignore.
 	 * @param min Min statistic value from [cg] tag, 0 to ignore.
	 * @param mode Statistic to retrieve as defined by [cg] tag.
	 * @param ridx Route index, zero based.
	 * @return Specified statistic or null if not found */
	protected Integer getStat(int rid, int min, String mode, int ridx) {
		if (ridx >= 0 && ridx < cg_routes.size()) {
			Route route = cg_routes.get(ridx);
			return route.getStat(rid, min, mode);
		}
		return null;
	}

	/** To string */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(Routes:");
		for (Route cgr : cg_routes)
			sb.append(" ").append(cgr);
		sb.append(")");
		return sb.toString();
	}
}
