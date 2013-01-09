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
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sched.SwingRunner;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.utils.I18N;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A JPanel that can display a video stream. It includes a status label.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
public class StreamPanel extends JPanel {

	/** Milliseconds between updates to the status */
	static private final int STATUS_DELAY = 1000;

	/** Network worker thread */
	static private final Scheduler NETWORKER = new Scheduler("networker");

	/** Video request */
	private final VideoRequest video_req;

	/** JPanel which holds the component used to render the video stream */
	private final JPanel screen_pnl;

	/** JPanel which holds the status widgets */
	private final JPanel status_pnl;

	/** JLabel for displaying the stream details (codec, size, framerate) */
	private final JLabel status_lbl = new JLabel();

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

	/** Camera PTZ control */
	private final CameraPTZ cam_ptz;

	/** Mouse PTZ control */
	private final MousePTZ mouse_ptz;

	/** Current video stream */
	private VideoStream stream = null;

	/** Create a new stream panel */
	public StreamPanel(CameraPTZ cptz, VideoRequest req) {
		super(new GridBagLayout());
		cam_ptz = cptz;
		VideoRequest.Size vsz = req.getSize();
		Dimension sz = UI.dimension(vsz.width, vsz.height);
		mouse_ptz = new MousePTZ(cam_ptz, sz);
		video_req = req;
		screen_pnl = createScreenPanel(sz);
		mouse_ptz.setComponent(screen_pnl);
		status_pnl = createStatusPanel(sz);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		add(screen_pnl, c);
		add(status_pnl, c);
	}

	/** Create the screen panel */
	private JPanel createScreenPanel(Dimension sz) {
		JPanel p = new JPanel(new BorderLayout());
		p.addMouseListener(mouse_ptz);
		p.addMouseMotionListener(mouse_ptz);
		p.addMouseWheelListener(mouse_ptz);
		p.setBorder(BorderFactory.createBevelBorder(
			BevelBorder.LOWERED));
		p.setPreferredSize(sz);
		return p;
	}

	/** Create the status panel */
	private JPanel createStatusPanel(Dimension sz) {
		JPanel p = new JPanel(new BorderLayout());
		p.add(status_lbl, BorderLayout.WEST);
		p.setPreferredSize(new Dimension(sz.width, 20));
		return p;
	}

	/** Set the camera to stream */
	public void setCamera(final Camera c) {
		if(stream != null)
			clearStream();
		if(c != null) {
			status_lbl.setText(I18N.get("camera.stream.opening"));
			NETWORKER.addJob(new Job() {
				public void perform() {
					requestStream(c);
				}
			});
		}
	}

	/** Request a new video stream */
	private void requestStream(Camera c) {
		try {
			stream = createStream(c);
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
	private VideoStream createStream(Camera c) throws IOException {
		switch(video_req.getStreamType(c)) {
		case MJPEG:
			return new MJPEGStream(video_req, c);
		case MPEG4:
			try {
				Class.forName("org.gstreamer.Gst");
				Class.forName("com.sun.jna.Library");
				return new GstStream(video_req, c);
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
	private void clearStream() {
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
	public final void dispose() {
		clearStream();
		mouse_ptz.dispose();
	}
}
