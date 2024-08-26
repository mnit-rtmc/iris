/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
		Iterator<DeviceAction> it = DeviceActionHelper.iterator();
		while (it.hasNext()) {
			DeviceAction da = it.next();
			if (da.getActionPlan() == ap)
				hashtags.add(da.getHashtag());
		}
		return hashtags;
	}

	/** Get set of beacons controlled by an action plan */
	static public TreeSet<Beacon> findBeacons(ActionPlan ap) {
		Set<String> hashtags = findHashtags(ap);
		TreeSet<Beacon> beacons = new TreeSet<Beacon>(
			new NumericAlphaComparator<Beacon>());
		Iterator<Beacon> it = BeaconHelper.iterator();
		while (it.hasNext()) {
			Beacon b = it.next();
			Hashtags tags = new Hashtags(b.getNotes());
			for (String ht: hashtags) {
				if (tags.contains(ht)) {
					beacons.add(b);
					break;
				}
			}
		}
		return beacons;
	}

	/** Get set of cameras controlled by an action plan */
	static public TreeSet<Camera> findCameras(ActionPlan ap) {
		Set<String> hashtags = findHashtags(ap);
		TreeSet<Camera> cameras = new TreeSet<Camera>(
			new NumericAlphaComparator<Camera>());
		Iterator<Camera> it = CameraHelper.iterator();
		while (it.hasNext()) {
			Camera cam = it.next();
			Hashtags tags = new Hashtags(cam.getNotes());
			for (String ht: hashtags) {
				if (tags.contains(ht)) {
					cameras.add(cam);
					break;
				}
			}
		}
		return cameras;
	}

	/** Get set of DMS controlled by an action plan */
	static public TreeSet<DMS> findDms(ActionPlan ap) {
		Set<String> hashtags = findHashtags(ap);
		TreeSet<DMS> signs = new TreeSet<DMS>(
			new NumericAlphaComparator<DMS>());
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS dms = it.next();
			Hashtags tags = new Hashtags(dms.getNotes());
			for (String ht: hashtags) {
				if (tags.contains(ht)) {
					signs.add(dms);
					break;
				}
			}
		}
		return signs;
	}

	/** Get set of lane markings controlled by an action plan */
	static public TreeSet<LaneMarking> findLaneMarkings(ActionPlan ap) {
		Set<String> hashtags = findHashtags(ap);
		TreeSet<LaneMarking> markings = new TreeSet<LaneMarking>(
			new NumericAlphaComparator<LaneMarking>());
		Iterator<LaneMarking> it = LaneMarkingHelper.iterator();
		while (it.hasNext()) {
			LaneMarking lm = it.next();
			Hashtags tags = new Hashtags(lm.getNotes());
			for (String ht: hashtags) {
				if (tags.contains(ht)) {
					markings.add(lm);
					break;
				}
			}
		}
		return markings;
	}

	/** Get set of ramp meters controlled by an action plan */
	static public TreeSet<RampMeter> findRampMeters(ActionPlan ap) {
		Set<String> hashtags = findHashtags(ap);
		TreeSet<RampMeter> meters = new TreeSet<RampMeter>(
			new NumericAlphaComparator<RampMeter>());
		Iterator<RampMeter> it = RampMeterHelper.iterator();
		while (it.hasNext()) {
			RampMeter rm = it.next();
			Hashtags tags = new Hashtags(rm.getNotes());
			for (String ht: hashtags) {
				if (tags.contains(ht)) {
					meters.add(rm);
					break;
				}
			}
		}
		return meters;
	}
}
