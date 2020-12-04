/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.utils.Base64;

/**
 * Stream manager for an MJPEG Stream.
 *
 * (Note:  Major portions of this class were
 *  adopted from an earlier Iris MJPEGStream
 *  class created by Douglas Lau and Timothy
 *  Johnson.)
 *
 * @author John L. Stanley - SRF Consulting Group
 */
public class VidStreamMgrMJPEG extends VidStreamMgr {

	/** Component to display video stream */
	private JLabel vcomponentLabel = new JLabel();

	/** Input stream to read */
	private InputStream stream;
	
	//-------------------------------------------

	/** Create an MJPEG stream manager.
	 * @param vp The VideoPanel to use. 
	 * @param vr The StreamReq to use.
	 */
	public VidStreamMgrMJPEG(VidPanel vp, VidStreamReq vr) {
		super(vp, vr);
	}

	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------

	@Override
	public boolean isStreaming() {
		return (stream != null);
	}

	@Override
	/** Start the stream.
	 * (Queued job called from the STREAM_SCHED thread.) */
	protected void doStartStream() {
		vcomponentLabel.removeAll();
		stream = createInputStream();
		if (stream != null)
			STREAM_SCHED.addJob(jobReadStream);
	}

	@Override
	/** Stop the stream.
	 * (Queued job called from the STREAM_SCHED thread.) */
	protected void doStopStream() {
		if (stream != null) {
			setComponent(null);
			STREAM_SCHED.removeJob(jobReadStream);
			try {
				stream.close();
			} catch (Exception e) {
				// ignore
			}
			stream = null;
		}
	}
	
	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------

	/** Does config appear to be OK for this video manager? */
	public static boolean isOkConfig(String config) {
		try {
			URL url = new URL(config);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}

	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------
	// Handle MJPEG video stream
	
	/** Create an input stream from an HTTP connection */
	protected InputStream createInputStream() {
		URL url;
		URLConnection c;
		String errMsg;
		try {
			url = new URL(vreq.getConfig());
			c = url.openConnection();
			String upass = url.getUserInfo();
			if (upass != null) {
				String auth = "Basic " + new String(Base64.encode(
					upass.getBytes()));
				c.setRequestProperty("Authorization", auth);
			}
			c.setConnectTimeout(TIMEOUT_DIRECT);
			c.setReadTimeout(TIMEOUT_DIRECT);
			if (c instanceof HttpURLConnection) {
				HttpURLConnection hc = (HttpURLConnection) c;
				if (hc.getResponseCode() != HttpURLConnection.HTTP_OK)
					throw new IOException(hc.getResponseMessage());
			}
			return c.getInputStream();
		}
		catch (MalformedURLException e) {
			setErrorMsg(e, "Malformed URL Error");
		}
		catch (IOException e) {
			setErrorMsg(e, "Generic IO Error");
		}
		return null;
	}

	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------

	/** Job to read the mjpeg stream */
	private final Job jobReadStream = new Job(Calendar.MILLISECOND, 1) {
		public void perform() {
			try {
				if (videoPanel.getStreamMgr() != VidStreamMgrMJPEG.this) {
					queueStopStream();
					return;
				}
				if (stream != null)
					readStream();
			}
			catch (IOException e) {
				setErrorMsg(e, "Generic IO Error");
				vcomponentLabel.setIcon(null);
				queueStopStream();
			}
		}
		public boolean isRepeating() {
			return (stream != null);
		}
	};

	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------

	/**
	 * @param image
	 */
	private void readStream() throws IOException {
		byte[] idata = getImage();
		ImageIcon icon = createIcon(idata);
		JLabel lbl = new JLabel(icon);
		setComponent(lbl);
		streamingStarted();
	}

	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------

	/** Get the next image in the mjpeg stream */
	protected byte[] getImage() throws IOException {
		int n_size = getImageSize();
		byte[] image = new byte[n_size];
		int n_bytes = 0;
		while (n_bytes < n_size) {
			int r = stream.read(image, n_bytes, n_size - n_bytes);
			if (r < 0)
				throw new IOException("End of stream");
			n_bytes += r;
		}
		incReceivedFrameCount();
		return image;
	}

	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------

	/** Create an image icon from image data */
	//TODO: change to rescale to current VideoPanel size
	protected ImageIcon createIcon(byte[] idata) {
		ImageIcon icon = new ImageIcon(idata);
		int vWidth  = videoPanel.getWidth();
		int vHeight = videoPanel.getHeight();
		if (icon.getIconWidth() == vWidth &&
		    icon.getIconHeight() == vHeight)
			return icon;
		Image im = icon.getImage().getScaledInstance(
				vWidth,	vHeight,
				Image.SCALE_FAST);
		return new ImageIcon(im);
	}

	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------

	/** Get the length of the next image */
	private int getImageSize() throws IOException {
		for(int i = 0; i < 100; i++) {
			String s = readLine();
			if (s.toLowerCase().indexOf("content-length") > -1) {
				// throw away an empty line after the
				// content-length header
				readLine();
				return parseContentLength(s);
			}
		}
		throw new IOException("Missing content-length");
	}

	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------

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

	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------

	/** Read the next line of text */
	private String readLine() throws IOException {
		StringBuilder b = new StringBuilder();
		while (true) {
			int ch = stream.read();
			if (ch < 0) {
				if (b.length() == 0)
					throw new IOException("End of stream");
				else
					break;
			}
			b.append((char)ch);
			if (ch == '\n')
				break;
		}
		return b.toString();
	}

	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------
	//-------------------------------------------

//	/** Dispose of the video stream */
//	public void dispose() {
//		try {
//			stream.close();
//		}
//		catch(IOException e) {
//			// ignore
//		}
//		vcomponent.setIcon(null);
//	}
}
