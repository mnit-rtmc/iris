/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2015  Minnesota Department of Transportation
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

import java.util.Calendar;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;
import us.mn.state.dot.tms.units.Distance;

/**
 * Job to send wiper commands to CCTV cameras.
 *
 * @author Douglas Lau
 */
public class CameraWiperJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static private final int OFFSET_SECS = 22;

	/** Wiper device request ordinal */
	static private final int WIPER =
		DeviceRequest.CAMERA_WIPER_ONESHOT.ordinal();

	/** Threshold for determine whether a camera is near a weather sensor */
	static private final double SENSOR_PROXIMITY_M = 4000;

	/** Get the precipitation rate for activating wipers in mm/hr */
	static public int getWiperPrecipRate() {
		return SystemAttrEnum.CAMERA_WIPER_PRECIP_MM_HR.getInt();
 	}

	/** Create a new camera wiper job */
	public CameraWiperJob() {
		super(Calendar.MINUTE, 1, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the camera wiper job */
	public void perform() {
		Set<GeoLoc> locs = precipLocations();
		if (locs.size() > 0)
			activateWipers(locs);
	}

	/** Get a set of locations with high precipitation rates */
	private Set<GeoLoc> precipLocations() {
		int wpr = getWiperPrecipRate();
		HashSet<GeoLoc> locs = new HashSet<GeoLoc>();
		Iterator<WeatherSensor> it = WeatherSensorHelper.iterator();
		while (it.hasNext()) {
			WeatherSensor ws = it.next();
			Integer pr = WeatherSensorHelper.getPrecipRate(ws);
			if (pr != null && pr >= wpr)
				locs.add(ws.getGeoLoc());
		}
		return locs;
	}

	/** Activate wipers near the specified locations */
	private void activateWipers(Set<GeoLoc> locs) {
		Iterator<Camera> it = CameraHelper.iterator();
		while (it.hasNext()) {
			Camera c = it.next();
			if (c instanceof CameraImpl) {
				CameraImpl cam = (CameraImpl)c;
				if (isNear(cam, locs))
					cam.setDeviceRequest(WIPER);
			}
		}
	}

	/** Check if a camera is near one of a  */
	private boolean isNear(Camera cam, Set<GeoLoc> locs) {
		GeoLoc loc = cam.getGeoLoc();
		for (GeoLoc ws: locs) {
			Distance d = GeoLocHelper.distanceTo(loc, ws);
			if (d != null && d.m() <= SENSOR_PROXIMITY_M)
				return true;
		}
		return false;
	}
}
