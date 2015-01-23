/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
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

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import us.mn.state.dot.geokit.Position;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.units.Distance;

/**
 * Helper class for cameras.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class CameraHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private CameraHelper() {
		assert false;
	}

	/** Lookup the camera with the specified name */
	static public Camera lookup(String name) {
		return (Camera)namespace.lookupObject(Camera.SONAR_TYPE,
			name);
	}

	/** Get a camera iterator */
	static public Iterator<Camera> iterator() {
		return new IteratorWrapper<Camera>(namespace.iterator(
			Camera.SONAR_TYPE));
	}

	/** Get the host ip for the camera's encoder */
	static public String parseEncoderIp(Camera cam) {
		String enc = cam.getEncoder();
		if(enc != null && enc.indexOf(':') >= 0)
			return enc.substring(0, enc.indexOf(':'));
		else
			return enc.trim();
	}

	/** Find the nearest cameras to a position */
	static public Collection<Camera> findNearest(Position pos, int n_count){
		TreeMap<Double, Camera> cams = new TreeMap<Double, Camera>();
		Iterator<Camera> it = iterator();
		while(it.hasNext()) {
			Camera cam = it.next();
			GeoLoc loc = cam.getGeoLoc();
			Distance d = GeoLocHelper.distanceTo(loc, pos);
			if(d != null) {
				cams.put(d.m(), cam);
				while(cams.size() > n_count)
					cams.pollLastEntry();
			}
		}
		return cams.values();
	}

	/**
	 * Does the camera require an external viewer?
	 * @param cam The camera
	 * @return Whether cam requires an external viewer,
	 *         or false if cam is null.
	 */
	static public boolean needsExternalViewer(Camera cam) {
		if (cam == null)
			return false;
		return EncoderType.fromOrdinal(cam.getEncoderType())
			.ext_viewer_only;
	}
}
