/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2012  Minnesota Department of Transportation
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
import java.util.SortedSet;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.MultiParser;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.StationHelper;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Travel time estimator
 *
 * @author Douglas Lau
 */
public class TravelTimeEstimator {

	/** Calculate the maximum trip minute to display on the sign */
	static protected int maximumTripMinutes(float miles) {
		float hours = miles /
			SystemAttrEnum.TRAVEL_TIME_MIN_MPH.getInt();
		return Math.round(hours * 60);
	}

	/** Round up to the next 5 minutes */
	static protected int roundUp5Min(int min) {
		return ((min - 1) / 5 + 1) * 5;
	}

	/** Name to use for debugging */
	private final String name;

	/** Origin location */
	protected final GeoLoc origin;

	/** Mapping of station IDs to routes */
	protected final HashMap<String, Route> s_routes =
		new HashMap<String, Route>();

	/** Create a new travel time estimator */
	public TravelTimeEstimator(String n, GeoLoc o) {
		name = n;
		origin = o;
	}

	/** Replace travel time tags in a MULTI string */
	public String replaceTravelTimes(String trav) {
		TravelCallback cb = new TravelCallback();
		MultiParser.parse(trav, cb);
		if(cb.isChanged()) {
			cb.clear();
			MultiParser.parse(trav, cb);
		}
		if(cb.valid)
			return cb.toString();
		else
			return null;
	}

	/** MultiString for replacing travel time tags */
	protected class TravelCallback extends MultiString {

		/* If all routes are on the same corridor, when the
		 * "OVER X" form is used, it must be used for all
		 * destinations. So, first we must calculate the times
		 * for each destination. Then, determine if the "OVER"
		 * form should be used. After that, replace the travel
		 * time tags with the selected values. */
		protected boolean any_over = false;
		protected boolean all_over = false;

		protected boolean valid = true;

		/** Add a travel time destination */
		public void addTravelTime(String sid) {
			Route r = lookupRoute(sid);
			if(r != null)
				addTravelTime(r);
			else {
				logTravel("NO ROUTE TO " + sid);
				valid = false;
			}
		}

		/** Add a travel time for a route */
		protected void addTravelTime(Route r) {
			boolean final_dest = isFinalDest(r);
			try {
				int mn = calculateTravelTime(r, final_dest);
				int slow = maximumTripMinutes(r.getLength());
				addTravelTime(mn, slow);
			}
			catch(BadRouteException e) {
				logTravel("BAD ROUTE, " + e.getMessage());
				valid = false;
			}
		}

		/** Add a travel time */
		protected void addTravelTime(int mn, int slow) {
			boolean over = mn > slow;
			if(over) {
				any_over = true;
				mn = slow;
			}
			if(over || all_over) {
				mn = roundUp5Min(mn);
				addSpan("OVER " + String.valueOf(mn));
			} else
				addSpan(String.valueOf(mn));
		}

		/** Check if the callback has changed formatting mode */
		protected boolean isChanged() {
			all_over = any_over && isSingleCorridor();
			return all_over;
		}
	}

	/** Lookup a route by station ID */
	protected Route lookupRoute(String sid) {
		if(!s_routes.containsKey(sid)) {
			Route r = createRoute(sid);
			if(r != null)
				s_routes.put(sid, r);
		}
		return s_routes.get(sid);
	}

	/** Create one route to a travel time destination */
	protected Route createRoute(String sid) {
		Station s = StationHelper.lookup(sid);
		if(s != null)
			return createRoute(s);
		else
			return null;
	}

	/** Create one route to a travel time destination */
	protected Route createRoute(Station s) {
		GeoLoc dest = s.getR_Node().getGeoLoc();
		return createRoute(dest);
	}

	/** Create one route to a travel time destination */
	protected Route createRoute(GeoLoc dest) {
		RouteBuilder builder = new RouteBuilder(name,
			BaseObjectImpl.corridors);
		SortedSet<Route> routes = builder.findRoutes(origin, dest);
		if(routes.size() > 0)
			return routes.first();
		else
			return null;
	}

	/** Log a travel time error */
	protected void logTravel(String m) {
		if(RouteBuilder.TRAVEL_LOG.isOpen())
			RouteBuilder.TRAVEL_LOG.log(name + ": " +m);
	}

	/** Check if the given route is a final destination */
	protected boolean isFinalDest(Route r) {
		for(Route ro: s_routes.values()) {
			if(ro != r && isSameCorridor(r, ro) &&
				r.getLength() < ro.getLength())
			{
				return false;
			}
		}
		return true;
	}

	/** Are two routes confined to the same single corridor */
	protected boolean isSameCorridor(Route r1, Route r2) {
		if(r1 != null && r2 != null) {
			Corridor c1 = r1.getOnlyCorridor();
			Corridor c2 = r2.getOnlyCorridor();
			if(c1 != null && c2 != null)
				return c1 == c2;
		}
		return false;
	}

	/** Calculate the travel time for the given route */
	protected int calculateTravelTime(Route route, boolean final_dest)
		throws BadRouteException
	{
		float hours = route.getTravelTime(final_dest);
		return (int)(hours * 60) + 1;
	}

	/** Are all the routes confined to the same single corridor */
	protected boolean isSingleCorridor() {
		Corridor cor = null;
		for(Route r: s_routes.values()) {
			Corridor c = r.getOnlyCorridor();
			if(c == null)
				return false;
			if(cor == null)
				cor = c;
			else if(c != cor)
				return false;
		}
		return cor != null;
	}

	/** Clear the current routes */
	public void clear() {
		s_routes.clear();
	}
}
