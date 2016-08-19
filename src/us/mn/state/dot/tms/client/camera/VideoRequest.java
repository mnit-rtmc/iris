/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2015  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
 * Copyright (C) 2015  SRF Consulting Group
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
package us.mn.state.dot.tms.client.camera;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.StreamType;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.URIUtils;

/**
 * The video stream request parameter wrapper.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class VideoRequest {

	/** Get basic authentication string */
	static private String getAuth() {
		String user = SystemAttrEnum.CAMERA_AUTH_USERNAME.getString();
		String pass = SystemAttrEnum.CAMERA_AUTH_PASSWORD.getString();
		return (user.length() > 0 && pass.length() > 0)
		      ? user + ':' + pass + '@' : "";
	}

	/** Servlet type enum */
	static public enum ServletType {
		STREAM("stream"), STILL("image");

		private final String servlet;
		private ServletType(String srv) {
			servlet = srv;
		}
	}

	/** Video stream size enum */
	static public enum Size {
		THUMBNAIL(132, 90, 's'),	// Thumbnail
		SMALL(176, 120, 's'),		// Quarter SIF
		MEDIUM(352, 240, 'm'),		// Full SIF
		LARGE(704, 480, 'l');		// 4x SIF
		private Size(int w, int h, char c) {
			width = w;
			height = h;
			code = c;
		}
		public final int width;
		public final int height;
		public final char code;
		/* For Axis Q1602, if the resolution is not between 160x90 and
		 * 768x576 we will get HTTP error 400 (Bad Request) */
		private int getWidthReq() {
			return Math.max(160, Math.min(width, 768));
		}
		private int getHeightReq() {
			return Math.max(90, Math.min(height, 576));
		}
		public String getResolution() {
			return "" + getWidthReq() + 'x' + getHeightReq();
		}
	}

	/** Video host property name */
	static private final String VIDEO_HOST = "video.host";

	/** Video port property name */
	static private final String VIDEO_PORT = "video.port";

	/** Create a url for connecting to the video server.
	 * @param p Properties */
	private String createBaseUrl(Properties p) {
		String host = p.getProperty(VIDEO_HOST);
		String port = p.getProperty(VIDEO_PORT);
		return (host != null) ? createBaseUrl(host, port) : null;
	}

	/** Create a url for connecting to the video server */
	private String createBaseUrl(String host, String port) {
		try {
			String ip =InetAddress.getByName(host).getHostAddress();
			return (port != null) ? (ip + ":" + port) : ip;
		}
		catch (UnknownHostException e) {
			System.out.println("Invalid video server " +
				e.getMessage());
			return null;
		}
	}

	/** Sonar session identifier for authenticating to the video system */
	private long sonarSessionId = -1;

	/** Get the SONAR session ID */
	public long getSonarSessionId() {
		return sonarSessionId;
	}

	/** Set the SONAR session ID */
	public void setSonarSessionId(long ssid) {
		sonarSessionId = ssid;
	}

	/** Frame rate (per second) */
	private int rate = 30;

	/** Get the frame rate (per second) */
	public int getRate() {
		return rate;
	}

	/** Set the frame rate (per second) */
	public void setRate(int rt) {
		rate = rt;
	}

	/** Stream size */
	private final Size size;

	/** Get the stream size */
	public Size getSize() {
		return size;
	}

	/** The base URL of the video server */
	private final String base_url;

	/** District ID */
	private final String district;

	/** Servlet type */
	private final ServletType servlet_type = ServletType.STREAM;

	/** Create a new video request */
	public VideoRequest(Properties p, Size sz) {
		base_url = createBaseUrl(p);
		district = p.getProperty("district", "tms");
		size = sz;
	}

	/** Create a URL for a stream */
	public String getUrl(Camera c) throws IOException {
		return (base_url != null) ? getServletUrl(c) : getCameraUrl(c);
	}

	/** Create a video servlet URL */
	private String getServletUrl(Camera cam) {
		return new String("http://" + base_url +
			"/video/" + servlet_type.servlet +
			"/" + district +
			"/" + cam.getName() +
			"?size=" + size.code +
			"&ssid=" + sonarSessionId);
	}

	/** Create a camera encoder URL */
	public String getCameraUrl(Camera cam) throws IOException {
		String enc = cam.getEncoder();
		int chan = cam.getEncoderChannel();
		String auth = getAuth();
		switch (CameraHelper.getEncoderType(cam)) {
		case AXIS_MJPEG:
			/* showlength parameter needed to force ancient (2401)
			 * servers to provide Content-Length headers */
			return "http://" + auth + enc +
			       "/axis-cgi/mjpg/video.cgi" +
			       "?camera=" + chan +
			       "&resolution=" + size.getResolution() +
			       "&showlength=1";
		case AXIS_MPEG4:
			return "rtsp://" + auth + enc +
			       "/mpeg4/" + chan + "/media.amp";
		case INFINOVA_MPEG4:
			return "rtsp://" + auth + enc + "/1.AMP";
		case AXIS_MP4_AXRTSP:
			return "axrtsp://" + auth + enc +
			       "/mpeg4/" + chan + "/media.amp";
		case AXIS_MP4_AXRTSPHTTP:
			return "axrtsphttp://" + auth + enc +
			       "/mpeg4/" + chan + "/media.amp";
		case GENERIC_MMS:
			if (!URIUtils.checkScheme(enc, "mms"))
				throw new IOException("Invalid encoder field");
			return enc;
		default:
			throw new IOException("Unsupported Encoder");
		}
	}

	/** Check if stream type is MJPEG.
	 * @param c Camera.
	 * @return true if stream type is motion JPEG. */
	public boolean hasMJPEG(Camera c) {
		return (c != null) && (getStreamType(c) == StreamType.MJPEG);
	}

	/** Get the stream type for a camera */
	private StreamType getStreamType(Camera c) {
		EncoderType et = CameraHelper.getEncoderType(c);
		if (et != EncoderType.NONE) {
			return (base_url != null)
			      ? StreamType.MJPEG
			      : et.direct_stream;
		} else
			return StreamType.UNKNOWN;
	}
}
