/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2017  Minnesota Department of Transportation
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.StreamType;
import static us.mn.state.dot.tms.utils.URIUtil.create;
import static us.mn.state.dot.tms.utils.URIUtil.HTTP;

/**
 * The video stream request parameter wrapper.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class VideoRequest {

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
			// FIXME: Don't fall back to direct access when DNS
			//        lookup fails -- after video servlet is
			//        available to field network.
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

	/** Create a URI for a stream */
	public URI getUri(Camera c) {
		return (base_url != null) ? getServletUri(c) : getCameraUri(c);
	}

	/** Create a video servlet URI */
	private URI getServletUri(Camera cam) {
		return create(HTTP, base_url +
		                    "/video/" + servlet_type.servlet +
		                    "/" + district +
		                    "/" + cam.getName() +
		                    "?size=" + size.code +
		                    "&ssid=" + sonarSessionId);
	}

	/** Create a camera encoder URI */
	public URI getCameraUri(Camera cam) {
		return CameraHelper.encoderUri(cam, "", getQuery(cam));
	}

	/** Create camera encoder URI query part */
	private String getQuery(Camera cam) {
		/* NOTE: query only required for older Axis encoders */
		if (isAxisEncoder(cam)) {
			/* NOTE: showlength needed to force ancient (2401)
			 *       servers to provide Content-Length headers */
			return "?camera=" + cam.getEncoderChannel()
			     + "&resolution=" + size.getResolution()
			     + "&showlength=1";
		} else
			return "";
	}

	/** Check if encoder type is AXIS */
	private boolean isAxisEncoder(Camera cam) {
		EncoderType et = cam.getEncoderType();
		return (et != null)
		      ? et.getName().toUpperCase().contains("AXIS")
		      : false;
	}

	/** Check if stream type is MJPEG.
	 * @param c Camera.
	 * @return true if stream type is motion JPEG. */
	public boolean hasMJPEG(Camera c) {
		return (c != null) && (getStreamType(c) == StreamType.MJPEG);
	}

	/** Get the stream type for a camera */
	private StreamType getStreamType(Camera c) {
		StreamType st = StreamType.fromOrdinal(c.getStreamType());
		if (st != StreamType.UNKNOWN)
			return (base_url != null) ? StreamType.MJPEG : st;
		else
			return StreamType.UNKNOWN;
	}
}
