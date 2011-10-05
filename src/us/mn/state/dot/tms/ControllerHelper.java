/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
 * Copyright (C) 2011  Berkeley Transportation Systems Inc.
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

import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.geokit.Position;

/**
 * Helper for controllers.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class ControllerHelper extends BaseHelper {

	/** Disallow instantiation */
	private ControllerHelper() {
		assert false;
	}

	/** Find a controller using a Checker */
	static public Controller find(final Checker<Controller> checker) {
		return (Controller)namespace.findObject(Controller.SONAR_TYPE,
			checker);
	}

	/** Find a controller associated with the comm link that uses the
	 * specified protocol.
	 * @return The controller or null if one is not defined. */
	static public Controller getController(final CommProtocol proto) {
		final CommLink cl = CommLinkHelper.getCommLink(proto);
		if(cl == null)
			return null;
		return find(new Checker<Controller>() {
			public boolean check(Controller c) {
				return c.getCommLink() == cl;
			}
		});
	}

	/** Get the geo location of a controller or null */
	static public GeoLoc getGeoLoc(Controller ctrl) {
		Cabinet cab = ctrl.getCabinet();
		if(cab != null)
			return cab.getGeoLoc();
		else
			return null;
	}

	/** Get a controller location or an empty string */
	static public String getLocation(Controller ctrl) {
		GeoLoc loc = getGeoLoc(ctrl);
		if(loc != null)
			return GeoLocHelper.getDescription(loc);
		else
			return "";
	}

	/** Get a controller position or null */
	static public Position getPosition(Controller ctrl) {
		GeoLoc gl = getGeoLoc(ctrl);
		if(gl != null)
			return GeoLocHelper.getWgs84Position(gl);
		else
			return null;
	}

	/** Test if a controller is active */
	static public boolean isActive(Controller ctrl) {
		return ctrl != null && ctrl.getActive();
	}

	/** Check if a controller is failed */
	static public boolean isFailed(Controller ctrl) {
		return isActive(ctrl) && ctrl.getFailTime() != null;
	}
}
