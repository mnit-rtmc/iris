/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.units.Distance;

/**
 * Helper class for cameras.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class CameraHelper extends BaseHelper {

	/** Get basic authentication string */
	static private String getAuth() {
		String user = SystemAttrEnum.CAMERA_AUTH_USERNAME.getString();
		String pass = SystemAttrEnum.CAMERA_AUTH_PASSWORD.getString();
		return (user.length() > 0 && pass.length() > 0)
		      ? user + ':' + pass + '@' : "";
	}

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

	/** Find a camera with the specific UID */
	static public Camera findUID(String uid) {
		Integer id = parseUID(uid);
		if (id != null) {
			Iterator<Camera> it = iterator();
			while (it.hasNext()) {
				Camera cam = it.next();
				Integer cid = parseUID(cam.getName());
				if (id.equals(cid))
					return cam;
			}
		}
		return null;
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

	/** Create a camera encoder URI */
	static public String encoderUri(Camera c, String opt) {
		if (c != null) {
			switch (EncoderType.fromOrdinal(c.getEncoderType())) {
			case AXIS:
				return axisUri(c, opt);
			case INFINOVA:
				return infinovaUri(c);
			default:
				return c.getEncoder();
			}
		} else
			return "";
	}

	/** Create a URI for an Axis encoder */
	static private String axisUri(Camera c, String opt) {
		String auth = getAuth();
		String enc = c.getEncoder();
		int chan = c.getEncoderChannel();
		switch (StreamType.fromOrdinal(c.getStreamType())) {
		case MJPEG:
			/* showlength parameter needed to force ancient (2401)
			 * servers to provide Content-Length headers */
			return "http://" + auth + enc +
			       "/axis-cgi/mjpg/video.cgi" +
			       "?camera=" + chan +
			       opt + "&showlength=1";
		case MPEG4:
			return "rtsp://" + auth + enc +
			       "/mpeg4/" + chan + "/media.amp";
		default:
			return "";
		}
	}

	/** Create a URI for an Infinova encoder */
	static private String infinovaUri(Camera c) {
		String auth = getAuth();
		String enc = c.getEncoder();
		return "rtsp://" + auth + enc + "/1.AMP";
	}
}
