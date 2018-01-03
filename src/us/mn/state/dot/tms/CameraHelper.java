/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2018  Minnesota Department of Transportation
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.utils.URIUtil;
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

	/** Find the nearest cameras to a position */
	static public Collection<Camera> findNearest(Position pos, int n_count){
		TreeMap<Double, Camera> cams = new TreeMap<Double, Camera>();
		Iterator<Camera> it = iterator();
		while (it.hasNext()) {
			Camera cam = it.next();
			GeoLoc loc = cam.getGeoLoc();
			Distance d = GeoLocHelper.distanceTo(loc, pos);
			if (d != null) {
				cams.put(d.m(), cam);
				while (cams.size() > n_count)
					cams.pollLastEntry();
			}
		}
		return cams.values();
	}

	/** Find a camera with the specified name or number */
	static public Camera find(String cam) {
		Camera c = lookup(cam);
		return (c != null) ? c : findUID(cam);
	}

	/** Find a camera with the specific number */
	static public Camera findUID(int uid) {
		Iterator<Camera> it = iterator();
		while (it.hasNext()) {
			Camera cam = it.next();
			Integer cn = cam.getCamNum();
			if (cn != null && cn.equals(uid))
				return cam;
		}
		return null;
	}

	/** Find a camera with the specific UID */
	static public Camera findUID(String uid) {
		Integer id = parseUID(uid);
		return (id != null) ? findUID(id) : null;
	}

	/** Parse the integer ID of a camera */
	static public Integer parseUID(String uid) {
		String id = stripNonDigitPrefix(uid);
		try {
			return Integer.parseInt(id);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** Strip non-digit prefix from a string */
	static private String stripNonDigitPrefix(String v) {
		int i = 0;
		for (i = 0; i < v.length(); i++) {
			if (Character.isDigit(v.charAt(i)))
				break;
		}
		return v.substring(i);
	}

	/** Find a camera by number */
	static public Camera findNum(int cam_num) {
		// First, lookup a guessed name for camera
		Camera c = lookup(buildCamName(cam_num));
		if (c != null) {
			// Is the camera number correct?
			Integer cn = c.getCamNum();
			if (cn != null && cn == cam_num)
				return c;
		}
		// Do a linear search for camera number
		Camera nc = findUID(cam_num);
		return (nc != null) ? nc : c;
	}

	/** Build a camera name guess */
	static private String buildCamName(int cam_num) {
		StringBuilder sb = new StringBuilder();
		sb.append('C');
		sb.append(cam_num);
		while (sb.length() < 4)
			sb.insert(1, '0');
		return sb.toString();
	}

	/** Find previous camera below a given number */
	static private Camera findPrev(int cam_num) {
		Camera cam = null;
		int n = 0;	// previous camera number
		Iterator<Camera> it = iterator();
		while (it.hasNext()) {
			Camera c = it.next();
			Integer cn = c.getCamNum();
			if (cn != null && cn < cam_num) {
				if (0 == n || n < cn) {
					cam = c;
					n = cn;
				}
			}
		}
		return cam;
	}

	/** Find camera with highest number */
	static private Camera findLast() {
		Camera cam = null;
		int n = 0;	// highest camera number
		Iterator<Camera> it = iterator();
		while (it.hasNext()) {
			Camera c = it.next();
			Integer cn = c.getCamNum();
			if (cn != null && cn > n) {
				cam = c;
				n = cn;
			}
		}
		return cam;
	}

	/** Find previous (or last) camera */
	static public Camera findPrevOrLast(int cam_num) {
		Camera c = findPrev(cam_num);
		return (c != null) ? c : findLast();
	}

	/** Find next camera above a given number */
	static private Camera findNext(int cam_num) {
		Camera cam = null;
		int n = 0;	// next camera number
		Iterator<Camera> it = iterator();
		while (it.hasNext()) {
			Camera c = it.next();
			Integer cn = c.getCamNum();
			if (cn != null && cn > cam_num) {
				if (0 == n || n > cn) {
					cam = c;
					n = cn;
				}
			}
		}
		return cam;
	}

	/** Find camera with lowest number */
	static private Camera findFirst() {
		Camera cam = null;
		Integer n = null;	// lowest camera number
		Iterator<Camera> it = iterator();
		while (it.hasNext()) {
			Camera c = it.next();
			Integer cn = c.getCamNum();
			if (cn != null) {
				if ((null == n) || cn < n) {
					cam = c;
					n = cn;
				}
			}
		}
		return cam;
	}

	/** Find next (or first) camera */
	static public Camera findNextOrFirst(int cam_num) {
		Camera c = findNext(cam_num);
		return (c != null) ? c : findFirst();
	}

	/** Create a camera encoder URI */
	static public URI encoderUri(Camera c, String query) {
		if (c != null) {
			EncoderType et = c.getEncoderType();
			if (et != null)
				return encoderUri(c, et, query);
		}
		// URI.toURL throws IllegalArgumentException with empty scheme
		return URIUtil.HTTP;
	}

	/** Create a camera encoder URI */
	static private URI encoderUri(Camera c, EncoderType et, String query) {
		assert c != null;
		String enc = c.getEncoder();
		int chan = c.getEncoderChannel();
		URI scheme = URIUtil.createScheme(et.getUriScheme());
		String auth = getAuth(c);
		return URIUtil.create(scheme, auth + enc + buildPath(
			et.getUriPath(), chan) + query);
	}

	/** Get camera encoder auth string */
	static private String getAuth(Camera c) {
		assert c != null;
		Controller ctrl = c.getController();
		if (ctrl != null) {
			String pwd = ctrl.getPassword();
			if (pwd != null && pwd.length() > 0)
				return "//" + pwd + '@';
		}
		return "";
	}

	/** Build URI path */
	static private String buildPath(String path, int chan) {
		return path.replace("{chan}", Integer.toString(chan));
	}
}
