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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
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

	/** Current video stream */
	private VideoStream stream = null;

	/** Create a new stream panel */
	public StreamPanel(VideoRequest req) {
		super(new GridBagLayout());
		VideoRequest.Size vsz = req.getSize();
		Dimension sz = UI.dimension(vsz.width, vsz.height);
		video_req = req;
		screen_pnl = createScreenPanel(sz);
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
		MousePanTilt pt = new MousePanTilt(sz);
		p.addMouseListener(pt);
		p.addMouseMotionListener(pt);
		p.setBorder(BorderFactory.createBevelBorder(
			BevelBorder.LOWERED));
		p.setPreferredSize(sz);
		return p;
	}

	/** Mouse event handler */
	private final class MousePanTilt extends MouseAdapter
		implements MouseMotionListener
	{
		private final Dimension size;
		private final int dead_left;
		private final int dead_right;
		private final int dead_up;
		private final int dead_down;
		private float pan = 0;
		private float tilt = 0;
		private MousePanTilt(Dimension sz) {
			size = sz;
			int deadx = sz.width / 20;
			dead_left = sz.width / 2 - deadx;
			dead_right = sz.width / 2 + deadx;
			int deady = sz.height / 20;
			dead_up = sz.height / 2 - deady;
			dead_down = sz.height / 2 + deady;
		}
		public void mousePressed(MouseEvent e) {
			updatePanTilt(e);
		}
		public void mouseReleased(MouseEvent e) {
			cancelPanTilt();
		}
		public void mouseDragged(MouseEvent e) {
			updatePanTilt(e);
		}
		public void mouseMoved(MouseEvent e) { }
		private void updatePanTilt(MouseEvent e) {
			sendPtz(calculatePan(e), calculateTilt(e), 0);
		}
		private float calculatePan(MouseEvent e) {
			float x = e.getX();
			if(x < dead_left)
				return -(dead_left - x) / dead_left;
			else if(x > dead_right) {
				return (x - dead_right) /
					(size.width - dead_right);
			} else
				return 0;
		}
		private float calculateTilt(MouseEvent e) {
			float y = e.getY();
			if(y < dead_up)
				return (dead_up - y) / dead_up;
			else if(y > dead_down) {
				return -(y - dead_down) /
					(size.height - dead_down);
			} else
				return 0;
		}
		private void cancelPanTilt() {
			sendPtz(0, 0, 0);
		}
	}

	/** Send PTZ command to camera */
	private void sendPtz(float p, float t, float z) {
		VideoStream vs = stream;
		if(vs != null) {
			Float[] ptz = new Float[3];
			ptz[0] = new Float(p);
			ptz[1] = new Float(t);
			ptz[2] = new Float(z);
			vs.getCamera().setPtz(ptz);
		}
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
	private VideoStream createStream(Camera c)
		throws IOException
	{
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
	protected final void dispose() {
		clearStream();
	}
}
