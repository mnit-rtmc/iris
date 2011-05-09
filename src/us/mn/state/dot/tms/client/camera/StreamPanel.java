/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2011  Minnesota Department of Transportation
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import us.mn.state.dot.tms.Camera;

/**
 * A JPanel that can display a video stream. It includes a progress bar and
 * methods to set the size of the video. Implementations of this class are
 * responsible for handling the stream including connecting, stopping and
 * processing.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
abstract public class StreamPanel extends JPanel {

	/** JPanel which holds the component used to render the video stream */
	protected final JPanel screenPanel = new JPanel(new BorderLayout());

	/** JPanel which holds the status widgets */
	protected final JPanel statusPanel = new JPanel(new BorderLayout());

	/** JLabel for displaying the stream details (codec, size, framerate) */
	protected final JLabel streamLabel = new JLabel();

	/** Size of video image */
	protected final Dimension imageSize;

	/** Progress bar for duration */
	private final JProgressBar progress = new JProgressBar(0, 100);

	/** Milliseconds between updates to the progress */
	static protected final int TIMER_DELAY = 1000;

	/** Timer listener for updating video progress */
	protected class ProgressTimer implements ActionListener {
		protected int seconds = 0;
		public void start(int n_seconds) {
			seconds = 0;
			progress.setValue(0);
			progress.setMaximum(n_seconds);
		}
		public void stop() {
			progress.setValue(0);
		}
		public void actionPerformed(ActionEvent evt) {
			progress.setValue(++seconds);
			if(seconds > progress.getMaximum())
				clearStream();
		}
	};

	/** Timer task for updating video progress */
	protected final ProgressTimer progress_timer = new ProgressTimer();

	/** Stream progress timer */
	protected final Timer timer = new Timer(TIMER_DELAY, progress_timer);

	/** Create a new stream panel */
	public StreamPanel(Dimension sz) {
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
		imageSize = new Dimension(sz);
		statusPanel.setBorder(BorderFactory.createBevelBorder(
			BevelBorder.LOWERED));
		screenPanel.setBorder(BorderFactory.createBevelBorder(
			BevelBorder.LOWERED));
		screenPanel.setPreferredSize(imageSize);
	}

	static public StreamPanel getInstance(Dimension sz) {
		try {
			Class.forName("org.gstreamer.Gst");
			Class.forName("com.sun.jna.Library");
			return new GstPanel(sz);
		}
		catch(ClassNotFoundException cnfe) {
			return new NoGstPanel(sz);
		}
		catch(NoClassDefFoundError ncdfe) {
			return new NoGstPanel(sz);
		}
	}

	/** Request a new video stream */
	protected void requestStream(VideoRequest req, Camera c) {
		progress_timer.start(req.getDuration());
		timer.start();
	}

	/** Clear the video stream */
	protected void clearStream() {
		timer.stop();
		progress_timer.stop();
	}

	final protected void dispose() {
		clearStream();
	}
}
