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

	/** Camera streamer thread */
	static private final Scheduler STREAMER = new Scheduler("streamer");

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
			updateStatus();
		}
	};

	/** Timer task for updating video status */
	private final StatusUpdater stat_updater = new StatusUpdater();

	/** Stream progress timer */
	private final Timer timer = new Timer(STATUS_DELAY, stat_updater);

	/** Mouse PTZ control */
	private final MousePTZ mouse_ptz;

	/** Current video stream */
	private VideoStream stream = null;

	/** Create a mouse PTZ */
	static private MousePTZ createMousePTZ(CameraPTZ cam_ptz, Dimension sz,
		JPanel screen_pnl)
	{
		return cam_ptz != null
		     ? new MousePTZ(cam_ptz, sz, screen_pnl)
		     : null;
	}

	/** Create a new stream panel */
	public StreamPanel(VideoRequest req, CameraPTZ cam_ptz) {
		super(new GridBagLayout());
		video_req = req;
		VideoRequest.Size vsz = req.getSize();
		Dimension sz = UI.dimension(vsz.width, vsz.height);
		screen_pnl = createScreenPanel(sz);
		mouse_ptz = createMousePTZ(cam_ptz, sz, screen_pnl);
		status_pnl = createStatusPanel(vsz);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		add(screen_pnl, c);
		add(status_pnl, c);
		setPreferredSize(UI.dimension(vsz.width, vsz.height + 20));
		setMinimumSize(UI.dimension(vsz.width, vsz.height + 20));
		setMaximumSize(UI.dimension(vsz.width, vsz.height + 20));
	}

	/** Create a new stream panel */
	public StreamPanel(VideoRequest req) {
		this(req, null);
	}

	/** Create the screen panel */
	private JPanel createScreenPanel(Dimension sz) {
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createBevelBorder(
			BevelBorder.LOWERED));
		p.setPreferredSize(sz);
		p.setMinimumSize(sz);
		return p;
	}

	/** Create the status panel */
	private JPanel createStatusPanel(VideoRequest.Size vsz) {
		JPanel p = new JPanel(new BorderLayout());
		p.add(status_lbl, BorderLayout.WEST);
		p.setPreferredSize(UI.dimension(vsz.width, 20));
		p.setMinimumSize(UI.dimension(vsz.width, 20));
		return p;
	}

	/** Update stream status */
	private void updateStatus() {
		STREAMER.addJob(new Job() {
			public void perform() {
				VideoStream vs = stream;
				if(vs != null && vs.isPlaying())
					status_lbl.setText(vs.getStatus());
				else
					clearStream();
			}
		});
	}

	/** Set the camera to stream */
	public void setCamera(final Camera c) {
		STREAMER.addJob(new Job() {
			public void perform() {
				if(stream != null)
					clearStream();
				if(c != null) {
					status_lbl.setText(I18N.get(
						"camera.stream.opening"));
					requestStream(c);
				}
			}
		});
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
			return new MJPEGStream(STREAMER, video_req, c);
		case MPEG4:
			throw new IOException("No decoder");
		default:
			throw new IOException("No encoder");
		}
	}

	/** Clear the video stream */
	private void clearStream() {
		timer.stop();
		screen_pnl.removeAll();
		screen_pnl.repaint();
		VideoStream vs = stream;
		if(vs != null) {
			vs.dispose();
			stream = null;
		}
		status_lbl.setText(null);
	}

	/** Dispose of the stream panel */
	public final void dispose() {
		clearStream();
		if(mouse_ptz != null)
			mouse_ptz.dispose();
	}
}
