/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2013  Minnesota Department of Transportation
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
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import us.mn.state.dot.sched.SwingRunner;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A JPanel that can display a video stream. It includes a status label.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
public class StreamPanel extends JPanel {

	/** Milliseconds between updates to the status */
	static private final int STATUS_DELAY = 1000;

	/** JPanel which holds the component used to render the video stream */
	protected final JPanel screen_pnl = new JPanel(new BorderLayout());

	/** JPanel which holds the status widgets */
	protected final JPanel status_pnl = new JPanel(new BorderLayout());

	/** JLabel for displaying the stream details (codec, size, framerate) */
	protected final JLabel status_lbl = new JLabel();

	/** Timer listener for updating video status */
	private class StatusUpdater implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			VideoStream vs = stream;
			if(vs != null && vs.isPlaying())
				status_lbl.setText(vs.getStatus());
			else
				clearStream();
		}
	};

	/** Timer task for updating video status */
	private final StatusUpdater stat_updater = new StatusUpdater();

	/** Stream progress timer */
	private final Timer timer = new Timer(STATUS_DELAY, stat_updater);

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
			status_lbl.setText(I18N.get("camera.stream.opening"));
			stream = createStream(req, c);
			JComponent screen = stream.getComponent();
			screen.setPreferredSize(screen_pnl.getPreferredSize());
			screen_pnl.add(screen);
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
		switch(req.getStreamType(c)) {
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
		VideoStream vs = stream;
		if(vs != null) {
			vs.dispose();
			stream = null;
		}
		status_lbl.setText(null);
		SwingRunner.invoke(new Runnable() {
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
