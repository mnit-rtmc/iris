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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import us.mn.state.dot.tms.geo.Position;
import static us.mn.state.dot.tms.utils.URIUtil.create;
import static us.mn.state.dot.tms.utils.URIUtil.EMPTY_URI;
import static us.mn.state.dot.tms.utils.URIUtil.HTTP;
import static us.mn.state.dot.tms.utils.URIUtil.RTSP;
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
	static public URI encoderUri(Camera c, String opt) {
		if (c != null) {
			int et = c.getEncoderType();
			switch (EncoderType.fromOrdinal(et)) {
			case GENERIC:
				return genericUri(c);
			case AXIS:
				return axisUri(c, opt);
			case INFINOVA:
				return infinovaUri(c);
			}
		}
		return EMPTY_URI;
	}

	/** Create a URI for a generic encoder */
	static private URI genericUri(Camera c) {
		String auth = getAuth();
		String enc = c.getEncoder();
		switch (StreamType.fromOrdinal(c.getStreamType())) {
		case MJPEG:
			return create(HTTP, auth + enc);
		case MPEG4:
		case H264:
			return create(RTSP, auth + enc);
		default:
			return EMPTY_URI;
		}
	}

	/** Create a URI for an Axis encoder */
	static private URI axisUri(Camera c, String opt) {
		String auth = getAuth();
		String enc = c.getEncoder();
		int chan = c.getEncoderChannel();
		switch (StreamType.fromOrdinal(c.getStreamType())) {
		case MJPEG:
			/* showlength parameter needed to force ancient (2401)
			 * servers to provide Content-Length headers */
			return create(HTTP, auth + enc +
			              "/axis-cgi/mjpg/video.cgi" +
			              "?camera=" + chan +
			              opt + "&showlength=1");
		case MPEG4:
			return create(RTSP, auth + enc +
			              "/mpeg4/" + chan + "/media.amp");
		case H264:
			return create(RTSP, auth + enc +
			              "/axis-media/media.amp");
		default:
			return EMPTY_URI;
		}
	}

	/** Create a URI for an Infinova encoder */
	static private URI infinovaUri(Camera c) {
		String auth = getAuth();
		String enc = c.getEncoder();
		switch (StreamType.fromOrdinal(c.getStreamType())) {
		case MPEG4:
			return create(RTSP, auth + enc + "/1.AMP");
		case H264:
			return create(RTSP, auth + enc + "/1/h264major");
		default:
			return EMPTY_URI;
		}
	}
}
