/*
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
package us.mn.state.dot.tms.client.camera.stream;

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

	/** Constant for small sized images */
	static public final int SMALL = 1;

	/** Constant for medium sized images */
	static public final int MEDIUM = 2;

	/** Constant for large sized images */
	static public final int LARGE = 3;

	/** Sonar session identifier for authenticating to the video system */
	private long sonarSessionId = -1;

	/** Get the SONAR session ID */
	public long getSonarSessionId() {
		return sonarSessionId;
	}

	/** Set the SONAR session ID */
	public void setSonarSessionId(long sonarSessionId) {
		this.sonarSessionId = sonarSessionId;
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
	public void setRate(int rate) {
		this.rate = rate;
	}

	private int frames = 60 * 30;

	public int getFrames() {
		return frames;
	}

	public void setFrames(int f) {
		frames = f;
	}

	private int size = 2;

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		if(size >= SMALL && size <= LARGE)
			this.size = size;
	}

	/** The base URLs of the backend video stream servers */
	private final String[] streamUrls;

	/** Create a new video request */
	public VideoRequest(Properties p) {
		streamUrls = createBackendUrls(p, 1);
	}

	/** Create an array of urls for connecting to the backend servers.
	 * @param p
	 * @param type Stream (1) or Still (2)
	 * @return */
	static public String[] createBackendUrls(Properties p, int type) {
		LinkedList<String> urls = new LinkedList<String>();
		int id = 0;
		while(true) {
			String ip = p.getProperty("video.backend.host" + id);
			if(ip == null)
				break;
			try {
				ip = InetAddress.getByName(ip).getHostAddress();
			}
			catch(UnknownHostException uhe) {
				System.out.println("Invalid backend server " +
					id + " " + uhe.getMessage());
				break;
			}
			String port = p.getProperty("video.backend.port" + id,
				p.getProperty("video.backend.port" + 0));
			String servletName = "";
			if(type == 1)
				servletName = "stream";
			if(type == 2)
				servletName = "image";
			urls.add("http://" + ip + ":" + port +
				"/video/" + servletName);
			id++;
		}
		return (String[])urls.toArray(new String[0]);
	}

	/** Get the URL for the request */
	public URL getUrl(String cid) {
		try {
			if(area >= 0 && area < streamUrls.length)
				return createURL(cid);
		}
		catch(MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** Create the URL for the request */
	protected URL createURL(String cid) throws MalformedURLException {
		return new URL(streamUrls[area] + "?id=" + cid + "&ssid=" +
			sonarSessionId);
	}
}
