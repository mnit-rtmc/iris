/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * A JavaPanel is responsible for managing video streams using the built-in java libraries.
 *
 * @author Tim Johnson
 */
final public class JavaPanel extends StreamPanel {

	/** Label to display video stream */
	private final JLabel screen = new JLabel();

	/** Current video stream */
	private VideoStream stream = null;

	/** Total number of frames requested */
	private int n_frames = 0;

	protected JavaPanel(){
		screenPanel.add(screen);
		thread.start();
	}

	/** Anonymous thread to read video stream */
	private final Thread thread = new Thread() {
		public void run() {
			while(true) {
				VideoStream vs = stream;
				if(vs != null)
					readStream(vs);
				else {
					synchronized(thread) {
						try {
							thread.wait();
						}
						catch(InterruptedException e) {
							// nothing to do
						}
					}
				}
			}
		}
	};

	/** Read a video stream */
	protected void readStream(final VideoStream vs) {
		try {
			while(vs == stream) {
				byte[] idata = vs.getImage();
				screen.setIcon(createIcon(idata));
				progress.setValue(vs.getFrameCount());
				if(vs.getFrameCount() >= n_frames)
					break;
			}
		}
		catch(IOException e) {
			streamLabel.setText(e.getMessage());
		}
		finally {
			try {
				vs.close();
			}
			catch(IOException e) {
				streamLabel.setText(e.getMessage());
			}
			clearVideoStream(vs);
			screen.setIcon(null);
		}
	}

	/** Clear the specified video stream */
	protected synchronized void clearVideoStream(VideoStream vs) {
		if(stream == vs) {
			stream = null;
			n_frames = 0;
			progress.setValue(0);
		}
	}

	public void requestStream(VideoRequest request){
		try {
			HttpDataSource source = new HttpDataSource(
					new URL(request.getUrlString(MJPEG)));
			setVideoStream(source.createStream(),
				request.getFrames());
		}
		catch(IOException e) {
			streamLabel.setText(e.getMessage());
		}
	}
	
	/** Clear the video stream */
	public void clearStream(){
		System.out.println("JAVA implementation of stopping a stream.");
	}

	/** Set the video stream to display */
	protected synchronized void setVideoStream(VideoStream vs, int f) {
		stream = vs;
		n_frames = f;
		progress.setMaximum(n_frames);
		progress.setValue(0);
		synchronized(thread) {
			thread.notify();
		}
	}

	/** Create an image icon from image data */
	protected ImageIcon createIcon(byte[] idata) {
		ImageIcon icon = new ImageIcon(idata);
		if(icon.getIconWidth() == imageSize.width &&
		   icon.getIconHeight() == imageSize.height)
			return icon;
		Image im = icon.getImage().getScaledInstance(
			imageSize.width, imageSize.height, Image.SCALE_FAST);
		return new ImageIcon(im);
	}
}
