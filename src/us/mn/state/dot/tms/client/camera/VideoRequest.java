/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2011  Minnesota Department of Transportation
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

/**
 * The video stream request parameter wrapper.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
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
		SMALL(176, 120),	// Quarter SIF
		MEDIUM(352, 240),	// Full SIF
		LARGE(704, 480);	// 4x SIF
		private Size(int w, int h) {
			width = w;
			height = h;
		}
		public final int width;
		public final int height;
		public String getResolution() {
			return "" + width + 'x' + height;
		}
	}

	/** Video host property name */
	static protected final String VIDEO_HOST = "video.host";

	/** Video port property name */
	static protected final String VIDEO_PORT = "video.port";

	/** Create a url for connecting to the video server.
	 * @param p Properties */
	protected String createBaseUrl(Properties p) {
		String ip = p.getProperty(VIDEO_HOST);
		if(ip != null) {
			try {
				ip = InetAddress.getByName(ip).getHostAddress();
				String port = p.getProperty(VIDEO_PORT);
				if(port != null)
					return ip + ":" + port;
				else
					return ip;
			}
			catch(UnknownHostException uhe) {
				System.out.println("Invalid video server " +
					uhe.getMessage());
			}
		}
		return null;
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

	/** Duration of stream (seconds) */
	private int duration = 60;

	/** Get requested duration in seconds */
	public int getDuration() {
		return duration;
	}

	/** Set the stream duration in seconds */
	public void setDuration(int d) {
		duration = d;
	}

	/** Stream size */
	private final Size size;

	/** Get the stream size */
	public Size getSize() {
		return size;
	}

	/** The base URL of the video server */
	private final String base_url;

	/** Servlet type */
	private final ServletType servlet_type = ServletType.STREAM;

	/** Create a new video request */
	public VideoRequest(Properties p, Size sz) {
		base_url = createBaseUrl(p);
		size = sz;
	}

	/** Create a URL for a stream */
	public String getUrl(Camera cam) throws IOException {
		if(base_url != null)
			return getServletUrl(cam);
		else
			return getCameraUrl(cam);
	}

	/** Create a video servlet URL */
	protected String getServletUrl(Camera cam) {
		return new String("http://" + base_url +
			"/video/" + servlet_type.servlet +
			"?id=" + cam.getName() +
			"&size=" + (size.ordinal() + 1) +
			"&ssid=" + sonarSessionId);
	}

	/** Create a camera encoder URL */
	protected String getCameraUrl(Camera cam) throws IOException {
		String ip = CameraHelper.parseEncoderIp(cam);
		if(ip.length() < 1)
			throw new IOException("No Encoder IP");
		switch(getEncoderType(cam)) {
		case AXIS_MJPEG:
			return new String("http://" + ip +
				"/axis-cgi/mjpg/video.cgi" +
				"?camera=" + cam.getEncoderChannel() +
				"&resolution=" + size.getResolution());
		case AXIS_MPEG4:
			return new String("rtsp://" + ip + "/mpeg4/" +
				cam.getEncoderChannel() + "/media.amp");
		case INFINOVA_MPEG4:
			return new String("rtsp://" + ip + "/1.AMP");
		default:
			throw new IOException("No Encoder");
		}
	}

	/** Get the stream type for a camera */
	public StreamType getStreamType(Camera cam) {
		if(base_url != null)
			return StreamType.MJPEG;
		else
			return getEncoderType(cam).stream_type;
	}

	/** Get the encoder type for a camera */
	static protected EncoderType getEncoderType(Camera cam) {
		return EncoderType.fromOrdinal(cam.getEncoderType());
	}
}
