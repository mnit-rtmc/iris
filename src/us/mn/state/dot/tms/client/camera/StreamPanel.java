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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

import us.mn.state.dot.tms.Camera;

/**
 * A JPanel that can display a video stream. It includes a progress bar and methods to
 * set the size of the video. Implementations of this class are responsible for handling
 * the stream including connecting, stopping and processing.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
abstract public class StreamPanel extends JPanel {

	/** Constant for MPEG-4 codec */
	protected static final String MPEG4 = "MPEG-4";
	
	/** Constant for MotionJPEG codec */
	protected static final String MJPG = "MotionJPEG";

	/** JPanel which holds the component used to render the video stream */
	protected final JPanel screenPanel = new JPanel(new BorderLayout());

	/** JPanel which holds the status widgets */
	protected final JPanel statusPanel = new JPanel(new BorderLayout());

	/** JLabel for displaying the stream details (codec, size, framerate) */
	protected final JLabel streamLabel = new JLabel();
	
	/** Size of a quarter SIF */
	static protected final Dimension SIF_QUARTER = new Dimension(176, 120);

	/** Size of a full SIF */
	static protected final Dimension SIF_FULL = new Dimension(352, 240);

	/** Size of 4 x SIF */
	static protected final Dimension SIF_4X = new Dimension(704, 480);

	/** Progress bar for duration */
	protected final JProgressBar progress = new JProgressBar(0, 100);

	/** Size of video image */
	protected Dimension imageSize = new Dimension(SIF_FULL);

	/** Create a new stream panel */
	public StreamPanel() {
		super(new BorderLayout());
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		p.add(screenPanel, c);
		statusPanel.add(streamLabel, BorderLayout.WEST);
		statusPanel.add(progress, BorderLayout.EAST);
		p.add(statusPanel, c);
		add(p);
		setVideoSize(imageSize);
		progress.setBorder(null);
		progress.setOpaque(true);
		progress.setBackground(Color.BLUE);
		statusPanel.setBorder(BorderFactory.createBevelBorder(
				BevelBorder.LOWERED));
		screenPanel.setBorder(BorderFactory.createBevelBorder(
			BevelBorder.LOWERED));
	}

	static public StreamPanel getInstance(){
		try{
			Class.forName("org.gstreamer.Gst");
			Class.forName("com.sun.jna.Library");
			return new GstPanel();
		}catch(ClassNotFoundException cnfe){
			return new JavaPanel();
		}catch(NoClassDefFoundError ncdfe){
			return new JavaPanel();
		}
	}
	
	abstract void requestStream(VideoRequest req, Camera c);

	abstract void clearStream();

	/** Set the dimensions of the video stream */
	protected void setVideoSize(Dimension d) {
		imageSize = d;
		screenPanel.setPreferredSize(d);
	}
	
	final protected void dispose(){
		clearStream();
	}
}