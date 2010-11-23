/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2010  Minnesota Department of Transportation
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
import java.net.UnknownHostException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Properties;

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
		SMALL, MEDIUM, LARGE;
	}

	/** Video backend host property name */
	static protected final String VIDEO_BACKEND_HOST =
		"video.backend.host";

	/** Video backend port property name */
	static protected final String VIDEO_BACKEND_PORT =
		"video.backend.port";

	/** Create an array of urls for connecting to the backend servers.
	 * @param p Properties
	 * @param st Servlet type */
	static protected String[] createUrls(Properties p, StreamType st) {
		LinkedList<String> urls = new LinkedList<String>();
		for(int id = 0; ; id++) {
			String url = createUrl(p, id);
			if(url != null)
				urls.add(url + st.servlet);
			else
				break;
		}
		return (String[])urls.toArray(new String[0]);
	}

	/** Create one backend url */
	static protected String createUrl(Properties p, int id) {
		String ip = p.getProperty(VIDEO_BACKEND_HOST + id);
		if(ip != null) {
			try {
				ip = InetAddress.getByName(ip).getHostAddress();
				String port = p.getProperty(VIDEO_BACKEND_PORT +
					id,p.getProperty(VIDEO_BACKEND_PORT+0));
				return "http://" + ip + ":" + port + "/video/";
			}
			catch(UnknownHostException uhe) {
				System.out.println("Invalid backend server " +
					id + " " + uhe.getMessage());
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

	/** Area number */
	private int area = 0;

	/** Get the area number */
	public int getArea() {
		return area;
	}

	/** Set the area number */
	public void setArea(int area) {
		this.area = area;
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
	private Size size = Size.MEDIUM;

	/** Get the stream size */
	public Size getSize() {
		return size;
	}

	/** Set the stream size */
	public void setSize(Size sz) {
		size = sz;
	}

	/** The base URLs of the backend video stream servers */
	private final String[] area_urls;

	/** Create a new video request */
	public VideoRequest(Properties p) {
		area_urls = createUrls(p, StreamType.STREAM);
	}

	/** Get the URL for the request */
	public URL getUrl(String cid) throws MalformedURLException {
		if(area >= 0 && area < area_urls.length)
			return createURL(cid);
		else
			return null;
	}

	/** Create the URL for the request */
	protected URL createURL(String cid) throws MalformedURLException {
		return new URL(area_urls[area] +
			"?id=" + cid +
			"&size=" + (size.ordinal() + 1) +
			"&ssid=" + sonarSessionId);
	}
}
