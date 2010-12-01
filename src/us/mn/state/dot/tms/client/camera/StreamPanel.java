/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2010  Minnesota Department of Transportation
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

/**
 * A JPanel that can display a video stream.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
public class StreamPanel extends JPanel {

	/** Size of a quarter SIF */
	static protected final Dimension SIF_QUARTER = new Dimension(176, 120);

	/** Size of a full SIF */
	static protected final Dimension SIF_FULL = new Dimension(352, 240);

	/** Size of 4 x SIF */
	static protected final Dimension SIF_4X = new Dimension(704, 480);

	/** Label to display video stream */
	private final JLabel screen = new JLabel();

	/** Current video stream */
	private VideoStream stream = null;

	/** Progress bar for duration */
	private final JProgressBar progress = new JProgressBar(0, 100);

	/** Total number of frames requested */
	private int n_frames = 0;

	/** Size of video image */
	private Dimension imageSize = new Dimension(SIF_FULL);

	/** Create a new stream panel */
	public StreamPanel() {
		super(new BorderLayout());
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		p.add(screen, c);
		p.add(progress, c);
		add(p);
		setVideoSize(imageSize);
		screen.setBorder(BorderFactory.createBevelBorder(
			BevelBorder.LOWERED));
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
			progress.setString(e.getMessage());
			progress.setStringPainted(true);
		}
		finally {
			try {
				vs.close();
			}
			catch(IOException e) {
				progress.setString(e.getMessage());
				progress.setStringPainted(true);
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

	/** Set the dimensions of the video stream */
	protected void setVideoSize(Dimension d) {
		imageSize = d;
		screen.setPreferredSize(d);
		screen.setMinimumSize(d);
	}

	/** Request a new video stream */
	public void requestStream(VideoRequest request, String cid) {
		try {
			HttpDataSource source = new HttpDataSource(
				request.getUrl(cid));
			setVideoStream(source.createStream(),
				request.getFrames());
		}
		catch(IOException e) {
			progress.setString(e.getMessage());
			progress.setStringPainted(true);
		}
	}

	/** Set the video stream to display */
	protected synchronized void setVideoStream(VideoStream vs, int f) {
		stream = vs;
		n_frames = f;
		progress.setMaximum(n_frames);
		progress.setValue(0);
		progress.setStringPainted(false);
		synchronized(thread) {
			thread.notify();
		}
	}

	/** Clear the video stream */
	public void clearStream() {
		setVideoStream(null, 0);
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
