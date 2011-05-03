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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;
import us.mn.state.dot.tms.Camera;

/**
 * The video stream request parameter wrapper.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
public class VideoRequest {

	/** Stream type servlet enum */
	static public enum StreamType {
		STREAM("stream"), STILL("image");

		private final String servlet;
		private StreamType(String srv) {
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

	private String videoHost = null;
	private String videoPort = null;

	/** Create a url for connecting to the video server.
	 * @param p Properties
	 * @param st Servlet type */
	protected String createBaseUrl(Properties p, StreamType st) {
		videoHost = p.getProperty(VIDEO_HOST);
		String url = null;
		if(videoHost != null) {
			try {
				videoHost = InetAddress.getByName(
					videoHost).getHostAddress();
				videoPort = p.getProperty(VIDEO_PORT);
				url = "http://" + videoHost + ":" + videoPort +
					"/video/";
			}
			catch(UnknownHostException uhe) {
				System.out.println("Invalid video server " +
					uhe.getMessage());
			}
		}
		if(url != null)
			url = url + st.servlet;
		return url;
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

	/** Number of frames requested */
	private int frames = 60 * 30;

	/** Get the number of frames */
	public int getFrames() {
		return frames;
	}

	/** Set the number of frames */
	public void setFrames(int f) {
		frames = f;
	}

	/** Stream size */
	private final Size size;

	/** Get the stream size */
	public Size getSize() {
		return size;
	}

	/** The base URL of the video server */
	private final String base_url;

	/** Create a new video request */
	public VideoRequest(Properties p, Size sz) {
		base_url = createBaseUrl(p, StreamType.STREAM);
		size = sz;
	}

	/** Check if the video host and video port properties have been set. */
	private boolean useProxyServer() {
		return (videoHost != null) && (videoPort != null);
	}

	/** Create a URL for an MPEG4 stream */
	public URL getMPEG4Url(Camera cam) throws MalformedURLException {
		if(useProxyServer()) {
			System.out.println("Using proxy server for MPEG-4");
			//mpeg4 is not supported on the proxy server yet.
			return new URL("rtsp://" + videoHost + ":" + videoPort +
					"/video/stream?id=" + cam.getName());
		} else {
			return new URL("rtsp://" + getCameraIp(cam) +
				":554/mpeg4/1/media.amp");
		}
	}

	/** Create a URL for a MJPEG stream */
	public URL getMJPEGUrl(Camera cam) throws MalformedURLException {
		if(useProxyServer()) {
			System.out.println("Using proxy server for MJPEG");
			return new URL(base_url +
					"?id=" + cam.getName() +
					"&size=" + (size.ordinal() + 1) +
					"&ssid=" + sonarSessionId);
		} else {
			return new URL("http://" +
					cam.getEncoder() +
					"/axis-cgi/mjpg/video.cgi?" +
					"resolution=" + size.getResolution());
		}
	}

	/** Get the host ip for the stream.
	 * If the video.host property has been set, then use the video host.
	 * Otherwise, use the ip address of the camera itself.
	 * @return
	 */
	static private String getCameraIp(Camera cam) {
		String encoder = cam.getEncoder();
		if(encoder == null)
			return null;
		if(encoder.indexOf(':') == -1)
			return encoder;
		return encoder.substring(0, encoder.indexOf(':'));
	}
}
