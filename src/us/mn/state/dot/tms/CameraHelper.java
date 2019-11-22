/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2019  Minnesota Department of Transportation
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

	/** Invalid URI needed because URI.toURL throws
	 * IllegalArgumentException when scheme is empty. */
	static private final URI INVALID_URI = URIUtil.HTTP;

	/** Don't allow instances to be created */
	private CameraHelper() {
		assert false;
	}

	/** Lookup the camera with the specified name */
	static public Camera lookup(String name) {
		return (Camera) namespace.lookupObject(Camera.SONAR_TYPE,
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

	/** Find a camera with the specific number */
	static private Camera findUID(int uid) {
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
		Integer id = VideoMonitorHelper.parseUID(uid);
		return (id != null) ? findUID(id) : null;
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
		return findUID(cam_num);
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

	/** Find a camera with the specified number */
	static public Camera findNum(String cam) {
		Integer cn = parseInt(cam);
		return (cn != null) ? findNum(cn) : null;
	}

	/** Parse an integer */
	static private Integer parseInt(String num) {
		try {
			return Integer.parseInt(num);
		}
		catch (NumberFormatException e) {
			return null;
		}
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

	/** Get an encoder stream for a camera.
	 * @param c Camera for stream.
	 * @param q Preferred encoding quality.
	 * @param allow_mcast Allow multicast streams. */
	static public EncoderStream getStream(Camera c, EncodingQuality q,
		boolean allow_mcast)
	{
		if (c != null) {
			EncoderType et = c.getEncoderType();
			if (et != null) {
				boolean mcast = allow_mcast &&
					(c.getEncMcast() != null);
				return EncoderStreamHelper.find(et, q, mcast);
			}
		}
		return null;
	}

	/** Create a camera encoder URI */
	static public URI encoderUri(Camera c, EncoderStream es) {
		if (c != null && es != null) {
			if (EncoderStreamHelper.isMcast(es))
				return mcastUri(es, c);
			else
				return ucastUri(es, c);
		} else
			return INVALID_URI;
	}

	/** Create a multicast URI */
	static private URI mcastUri(EncoderStream es, Camera c) {
		Integer port = es.getMcastPort();
		String madr = c.getEncMcast();
		return (madr != null && port != null)
		      ? URIUtil.create(URIUtil.UDP, madr + ":" + port)
		      : INVALID_URI;
	}

	/** Create a unicast URI */
	static private URI ucastUri(EncoderStream es, Camera c) {
		String sch = es.getUriScheme();
		String path = es.getUriPath();
		String addr = c.getEncAddress();
		if (sch != null && path != null && addr != null) {
			URI scheme = URIUtil.createScheme(sch);
			String auth = getAuth(c);
			Integer port = c.getEncPort();
			if (port != null)
				addr = addr + ":" + port;
			Integer chan = c.getEncChannel();
			return URIUtil.create(scheme, auth + addr + buildPath(
				path, chan));
		} else
			return INVALID_URI;
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
	static private String buildPath(String path, Integer chan) {
		return (chan != null)
		      ? path.replace("{chan}", Integer.toString(chan))
		      : path;
	}

	/** Check if camera is blank */
	static public boolean isBlank(Camera c) {
		return (null == c) || isCameraNumBlank(c.getCamNum());
	}

	/** Check if a camera number is "blank" */
	static private boolean isCameraNumBlank(Integer cn) {
		return (cn != null) && (cameraNumBlank() == cn);
	}

	/** Get the "blank" camera number */
	static private int cameraNumBlank() {
		return SystemAttrEnum.CAMERA_NUM_BLANK.getInt();
	}

	/** Get a camera number (or name) */
	static public String getCameraNum(Camera cam) {
		if (cam != null) {
			Integer num = cam.getCamNum();
			return (num != null)
			      ? "#" + num.toString()
			      : cam.getName();
		} else
			return "";
	}
}
