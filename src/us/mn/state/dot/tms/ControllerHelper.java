/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2022  Minnesota Department of Transportation
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

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.tms.geo.Position;

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

	/** Lookup the controller with the specified name */
	static public Controller lookup(String name) {
		return (Controller) namespace.lookupObject(
			Controller.SONAR_TYPE, name);
	}

	/** Get a controller iterator */
	static public Iterator<Controller> iterator() {
		return new IteratorWrapper<Controller>(namespace.iterator(
			Controller.SONAR_TYPE));
	}

	/** Get the geo location of a controller or null */
	static public GeoLoc getGeoLoc(Controller ctrl) {
		return (ctrl != null) ? ctrl.getGeoLoc() : null;
	}

	/** Get a controller location or an empty string */
	static public String getLocation(Controller ctrl) {
		GeoLoc loc = getGeoLoc(ctrl);
		return (loc != null) ? GeoLocHelper.getLocation(loc) : "";
	}

	/** Get a controller position or null */
	static public Position getPosition(Controller ctrl) {
		GeoLoc gl = getGeoLoc(ctrl);
		return (gl != null) ? GeoLocHelper.getWgs84Position(gl) : null;
	}

	/** Test if a controller is active */
	static public boolean isActive(Controller ctrl) {
		return (ctrl != null)
		    && (ctrl.getCondition() == CtrlCondition.ACTIVE.ordinal())
		    && CommLinkHelper.getPollEnabled(ctrl.getCommLink());
	}

	/** Check if a controller is failed */
	static public boolean isFailed(Controller ctrl) {
		return isActive(ctrl)
		    && ctrl.getFailTime() != null;
	}

	/** Check if a controller needs maintenance */
	static public boolean needsMaintenance(Controller ctrl) {
		return isActive(ctrl) && !isFailed(ctrl) &&
		       !(ctrl.getStatus().isEmpty() &&
		         ctrl.getMaint().isEmpty());
	}

	/** Get controller communication status */
	static public String getStatus(Controller ctrl) {
		return (ctrl != null)
		      ? ctrl.getStatus()
		      : ItemStyle.NO_CONTROLLER.toString();
	}

	/** Get controller maintenance status */
	static public String getMaintenance(Controller ctrl) {
		return (ctrl != null)
		      ? ctrl.getMaint()
		      : ItemStyle.NO_CONTROLLER.toString();
	}

	/** Get controller setup data */
	static public String getSetup(Controller ctrl, String key) {
		String setup = (ctrl != null) ? ctrl.getSetup() : null;
		if (setup != null) {
			try {
				JSONObject jo = new JSONObject(setup);
				return jo.optString(key, "");
			}
			catch (JSONException e) {
				// malformed JSON
				e.printStackTrace();
			}
		}
		return "";
	}

	/** Get controller setup array data.
	 *
	 * Finds a value in the "setup" field.  The last JSON object in the
	 * "arr" array will be found, and the value of "key" returned.
	 * If not found, "UNKNOWN" will be returned.
	 */
	static public String getSetup(Controller ctrl, String arr, String key)
	{
		String setup = (ctrl != null) ? ctrl.getSetup() : null;
		if (setup != null) {
			try {
				JSONObject jo = new JSONObject(setup);
				JSONArray ja = jo.optJSONArray(arr);
				if (ja != null && ja.length() > 0) {
					int idx = ja.length() - 1;
					JSONObject o = ja.optJSONObject(idx);
					if (o != null) {
						return o.optString(key,
							"UNKNOWN");
					}
				}
			}
			catch (JSONException e) {
				// malformed JSON
				e.printStackTrace();
			}
		}
		return "UNKNOWN";
	}
}
