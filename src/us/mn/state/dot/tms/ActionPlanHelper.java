/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
import java.util.TreeSet;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * Helper class for action plans.
 *
 * @author Douglas Lau
 */
public class ActionPlanHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private ActionPlanHelper() {
		assert false;
	}

	/** Lookup the action plan with the specified name */
	static public ActionPlan lookup(String name) {
		return (ActionPlan) namespace.lookupObject(ActionPlan.SONAR_TYPE,
			name);
	}

	/** Get an action plan iterator */
	static public Iterator<ActionPlan> iterator() {
		return new IteratorWrapper<ActionPlan>(namespace.iterator(
			ActionPlan.SONAR_TYPE));
	}

	/** Find all hashtags associated with an action plan */
	static public Set<String> findHashtags(ActionPlan ap) {
		HashSet<String> hashtags = new HashSet<String>();
		Iterator<DeviceAction> it = DeviceActionHelper.iterator(ap);
		while (it.hasNext()) {
			DeviceAction da = it.next();
			hashtags.add(da.getHashtag());
		}
		return hashtags;
	}

	/** Get count of beacons controlled by an action plan */
	static public int countBeacons(ActionPlan ap) {
		int n_count = 0;
		Set<String> hashtags = findHashtags(ap);
		Iterator<Beacon> it = BeaconHelper.iterator();
		while (it.hasNext()) {
			Beacon b = it.next();
			if (ControllerHelper.isActive(b.getController())) {
				Hashtags tags = new Hashtags(b.getNotes());
				if (tags.containsAny(hashtags))
					n_count++;
			}
		}
		return n_count;
	}

	/** Get count of cameras controlled by an action plan */
	static public int countCameras(ActionPlan ap) {
		int n_count = 0;
		Set<String> hashtags = findHashtags(ap);
		Iterator<Camera> it = CameraHelper.iterator();
		while (it.hasNext()) {
			Camera cam = it.next();
			if (ControllerHelper.isActive(cam.getController())) {
				Hashtags tags = new Hashtags(cam.getNotes());
				if (tags.containsAny(hashtags))
					n_count++;
			}
		}
		return n_count;
	}

	/** Get count of DMS controlled by an action plan */
	static public int countDms(ActionPlan ap) {
		int n_count = 0;
		Set<String> hashtags = findHashtags(ap);
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS dms = it.next();
			if (ControllerHelper.isActive(dms.getController())) {
				Hashtags tags = new Hashtags(dms.getNotes());
				if (tags.containsAny(hashtags))
					n_count++;
			}
		}
		return n_count;
	}

	/** Get count of gate arms controlled by an action plan */
	static public int countGateArms(ActionPlan ap) {
		int n_count = 0;
		Set<String> hashtags = findHashtags(ap);
		Iterator<GateArm> it = GateArmHelper.iterator();
		while (it.hasNext()) {
			GateArm ga = it.next();
			if (ControllerHelper.isActive(ga.getController())) {
				Hashtags tags = new Hashtags(ga.getNotes());
				if (tags.containsAny(hashtags))
					n_count++;
			}
		}
		return n_count;
	}

	/** Get count of ramp meters controlled by an action plan */
	static public int countRampMeters(ActionPlan ap) {
		int n_count = 0;
		Set<String> hashtags = findHashtags(ap);
		Iterator<RampMeter> it = RampMeterHelper.iterator();
		while (it.hasNext()) {
			RampMeter rm = it.next();
			if (ControllerHelper.isActive(rm.getController())) {
				Hashtags tags = new Hashtags(rm.getNotes());
				if (tags.containsAny(hashtags))
					n_count++;
			}
		}
		return n_count;
	}

	/** Get set of DMS controlled by an action plan */
	static public TreeSet<DMS> findDms(ActionPlan ap) {
		Set<String> hashtags = findHashtags(ap);
		TreeSet<DMS> signs = new TreeSet<DMS>(
			new NumericAlphaComparator<DMS>());
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS dms = it.next();
			if (ControllerHelper.isActive(dms.getController())) {
				Hashtags tags = new Hashtags(dms.getNotes());
				if (tags.containsAny(hashtags))
					signs.add(dms);
			}
		}
		return signs;
	}
}
