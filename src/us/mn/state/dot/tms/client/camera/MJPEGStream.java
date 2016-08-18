/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2013  Minnesota Department of Transportation
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

import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.StreamType;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.Base64;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A video stream which reads an MJPEG source.
 *
 * @author Douglas Lau
 * @author Timothy Johnson
 */
public class MJPEGStream implements VideoStream {

	/** Default timeout for direct URL Connections */
	static protected final int TIMEOUT_DIRECT = 5 * 1000;

	/** Label to display video stream */
	private final JLabel screen = new JLabel();

	/** URL of the data source */
	private final URL url;

	/** Requested video size */
	private final Dimension size;

	/** Input stream to read */
	private final InputStream stream;

	/** Count of rendered frames */
	private int n_frames = 0;

	/** Flag to continue running stream */
	private boolean running = true;

	/** Stream error message */
	private String error_msg = null;

	/** Set the stream error message */
	protected void setErrorMsg(String e) {
		if(error_msg == null)
			error_msg = e;
	}

	/** Create a new MJPEG stream */
	public MJPEGStream(Scheduler s, VideoRequest req, Camera c)
		throws IOException
	{
		url = new URL(req.getUrl(c));
		size = UI.dimension(req.getSize().width, req.getSize().height);
		stream = createInputStream();
		s.addJob(job);
	}

	/** Create an input stream from an HTTP connection */
	protected InputStream createInputStream() throws IOException {
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		if (!SystemAttrEnum.CAMERA_AUTH_USERNAME.getString().equals("")){
			String userauth = SystemAttrEnum.CAMERA_AUTH_USERNAME.getString() + ":" + SystemAttrEnum.CAMERA_AUTH_PASSWORD.getString();
			String basicAuth = "Basic " + new String(new Base64().encode(userauth.getBytes()));
			c.setRequestProperty ("Authorization", basicAuth);
		}
		HttpURLConnection.setFollowRedirects(true);
		c.setConnectTimeout(TIMEOUT_DIRECT);
		c.setReadTimeout(TIMEOUT_DIRECT);
		int resp = c.getResponseCode();
		if (resp != HttpURLConnection.HTTP_OK) {
			throw new IOException(c.getResponseMessage());
		}
		return c.getInputStream();
	}

	/** Anonymous thread to read video stream */
	private final Job job = new Job(Calendar.MILLISECOND, 1) {
		public void perform() {
			if(running)
				readStream();
		}
		public boolean isRepeating() {
			return running;
		}
	};

	/** Read a video stream */
	private void readStream() {
		try {
			byte[] idata = getImage();
			screen.setIcon(createIcon(idata));
		}
		catch(IOException e) {
			setErrorMsg(e.getMessage());
			screen.setIcon(null);
			running = false;
		}
	}

	/** Get the next image in the mjpeg stream */
	protected byte[] getImage() throws IOException {
		int n_size = getImageSize();
		byte[] image = new byte[n_size];
		int n_bytes = 0;
		while(n_bytes < n_size) {
			int r = stream.read(image, n_bytes, n_size - n_bytes);
			if(r >= 0)
				n_bytes += r;
			else
				throw new IOException("End of stream");
		}
		n_frames++;
		return image;
	}

	/** Create an image icon from image data */
	protected ImageIcon createIcon(byte[] idata) {
		ImageIcon icon = new ImageIcon(idata);
		if(icon.getIconWidth() == size.width &&
		   icon.getIconHeight() == size.height)
			return icon;
		Image im = icon.getImage().getScaledInstance(size.width,
			size.height, Image.SCALE_FAST);
		return new ImageIcon(im);
	}

	/** Get the length of the next image */
	private int getImageSize() throws IOException {
		for(int i = 0; i < 100; i++) {
			String s = readLine();
			if(s.toLowerCase().indexOf("content-length") > -1) {
				// throw away an empty line after the
				// content-length header
				readLine();
				return parseContentLength(s);
			}
		}
		throw new IOException("Missing content-length");
	}

	/** Parse the content-length header */
	private int parseContentLength(String s) throws IOException {
		s = s.substring(s.indexOf(":") + 1);
		s = s.trim();
		try {
			return Integer.parseInt(s);
		}
		catch(NumberFormatException e) {
			throw new IOException("Invalid content-length");
		}
	}

	/** Read the next line of text */
	private String readLine() throws IOException {
		StringBuilder b = new StringBuilder();
		while(true) {
			int ch = stream.read();
			if(ch < 0) {
				if(b.length() == 0)
					throw new IOException("End of stream");
				else
					break;
			}
			b.append((char)ch);
			if(ch == '\n')
				break;
		}
		return b.toString();
	}

	/** Get a component for displaying the video stream */
	public JComponent getComponent() {
		return screen;
	}

	/** Get the status of the stream */
	public String getStatus() {
		String e = error_msg;
		if(e != null)
			return e;
		else
			return StreamType.MJPEG.toString();
	}

	/** Test if the video is playing */
	public boolean isPlaying() {
		return running;
	}

	/** Dispose of the video stream */
	public void dispose() {
		running = false;
		try {
			stream.close();
		}
		catch(IOException e) {
			setErrorMsg(e.getMessage());
		}
		screen.setIcon(null);
	}
}
