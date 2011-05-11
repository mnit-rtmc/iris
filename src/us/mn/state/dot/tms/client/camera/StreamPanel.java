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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.EncoderType;

/**
 * A JPanel that can display a video stream. It includes a progress bar and
 * status label.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
public class StreamPanel extends JPanel {

	/** JPanel which holds the component used to render the video stream */
	protected final JPanel screen_pnl = new JPanel(new BorderLayout());

	/** JPanel which holds the status widgets */
	protected final JPanel status_pnl = new JPanel(new BorderLayout());

	/** JLabel for displaying the stream details (codec, size, framerate) */
	protected final JLabel status_lbl = new JLabel();

	/** Progress bar for duration */
	private final JProgressBar progress = new JProgressBar(0, 100);

	/** Milliseconds between updates to the progress */
	static protected final int TIMER_DELAY = 1000;

	/** Timer listener for updating video progress */
	protected class ProgressTimer implements ActionListener {
		protected int seconds = 0;
		protected int duration = 0;
		public void start(int n_seconds) {
			seconds = 0;
			duration = n_seconds;
			progress.setValue(0);
			progress.setMaximum(n_seconds);
		}
		public void stop() {
			progress.setValue(0);
		}
		public void actionPerformed(ActionEvent evt) {
			seconds++;
			VideoStream vs = stream;
			if(vs != null) {
				progress.setValue(seconds);
				if(seconds > duration || !vs.isPlaying())
					clearStream();
				if(seconds <= duration)
					status_lbl.setText(vs.getStatus());
			}
		}
	};

	/** Timer task for updating video progress */
	protected final ProgressTimer progress_timer = new ProgressTimer();

	/** Stream progress timer */
	protected final Timer timer = new Timer(TIMER_DELAY, progress_timer);

	/** Current video stream */
	private VideoStream stream = null;

	/** Create a new stream panel */
	public StreamPanel(Dimension sz) {
		super(new BorderLayout());
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		p.add(screen_pnl, c);
		status_pnl.add(status_lbl, BorderLayout.WEST);
		status_pnl.add(progress, BorderLayout.EAST);
		p.add(status_pnl, c);
		add(p);
		screen_pnl.setBorder(BorderFactory.createBevelBorder(
			BevelBorder.LOWERED));
		screen_pnl.setPreferredSize(sz);
		status_pnl.setPreferredSize(new Dimension(sz.width, 20));
	}

	/** Request a new video stream */
	public void requestStream(VideoRequest req, Camera c) {
		if(stream != null)
			clearStream();
		try {
			status_lbl.setText("Opening stream");
			stream = createStream(req, c);
			JComponent screen = stream.getComponent();
			screen.setPreferredSize(screen_pnl.getPreferredSize());
			screen_pnl.add(screen);
			progress_timer.start(req.getDuration());
			timer.start();
		}
		catch(IOException e) {
			status_lbl.setText(e.getMessage());
		}
	}

	/** Create a new video stream */
	protected VideoStream createStream(VideoRequest req, Camera c)
		throws IOException
	{
		switch(EncoderType.fromOrdinal(c.getEncoderType()).stream_type){
		case MJPEG:
			return new MJPEGStream(req, c);
		case MPEG4:
			try {
				Class.forName("org.gstreamer.Gst");
				Class.forName("com.sun.jna.Library");
				return new GstStream(req, c);
			}
			catch(ClassNotFoundException cnfe) {
				throw new IOException("Missing gstreamer");
			}
			catch(NoClassDefFoundError ncdfe) {
				throw new IOException("Missing gstreamer");
			}
		default:
			throw new IOException("No encoder");
		}
	}

	/** Clear the video stream */
	protected void clearStream() {
		screen_pnl.removeAll();
		timer.stop();
		progress_timer.stop();
		VideoStream vs = stream;
		if(vs != null) {
			vs.dispose();
			stream = null;
		}
		status_lbl.setText(null);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				screen_pnl.repaint();
			}
		});
	}

	/** Dispose of the stream panel */
	protected final void dispose() {
		clearStream();
	}
}
