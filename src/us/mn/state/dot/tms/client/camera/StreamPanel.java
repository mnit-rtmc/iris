/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2020  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.Timer;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.VideoRequest.Size;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A JPanel that can display a video stream. It includes a status label.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 * @author Travis Swanston
 * @author Michael Janson
 * @author Gordon Parikh
 */
public class StreamPanel extends JPanel {

	/** Status panel height */
	static private final int HEIGHT_STATUS_PNL = 20;

	/** Control panel height */
	static private final int HEIGHT_CONTROL_PNL = 40;

	/** Milliseconds between updates to the status */
	static private final int STATUS_DELAY = 1000;

	/** Camera streamer thread */
	static private final Scheduler STREAMER = new Scheduler("streamer");

	/** Video request */
	private final VideoRequest video_req;

	/** Auto-play mode */
	private final boolean autoplay;

	/** JPanel which holds the component used to render the video stream */
	private final VidPanel screen_pnl;

	/** Stream controls panel and its components */
	private final StreamControlPanel control_pnl;

	/** Current Camera */
	private Camera camera = null;

	/** Current video stream */
	private VideoStream stream = null;

	/** Most recent streaming state.  State variable for event FSM. */
	private boolean stream_state = false;

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

	/** Stream status listeners to notify on stream status change events */
	private final Set<StreamStatusListener> ssl_set =
		new HashSet<StreamStatusListener>();

	/** Camera PTZ */
	private CameraPTZ ptz;

	/**
	 * Create a new stream panel.
	 * @param req The VideoRequest object to use.
	 * @param cam_ptz An optional (null for none) CameraPTZ PTZ manager.
	 *                Mouse PTZ control is disabled if null.
	 * @param s A reference to the current Session, or null if external
	 *          viewer support not desired.
	 * @param ctrl Enable streaming control buttons?  If false, you
	 *             probably want autoplay to be true.
	 * @param auto Automatically play upon setCamera()?
	 */
	public StreamPanel(VideoRequest req, CameraPTZ cam_ptz, Session s,
		boolean ctrl, boolean auto)
	{
		super(new BorderLayout());
		video_req = req;
		autoplay = auto;
		ptz = cam_ptz;
		VideoRequest.Size vsz = req.getSize();
		Dimension sz = UI.dimension(vsz.width, vsz.height);
		screen_pnl = new VidPanel(sz);
		Dimension vpsz = screen_pnl.getPreferredSize();
		add(screen_pnl, BorderLayout.CENTER);
		if (ctrl) {
			control_pnl = new StreamControlPanel(s, this);
			add(control_pnl, BorderLayout.SOUTH);
		} else
			control_pnl = null;
		int pnlHeight = vpsz.height + (ctrl ? HEIGHT_CONTROL_PNL : 0);
		Dimension psz = new Dimension(vsz.width, pnlHeight);
		setPreferredSize(psz);
		setMinimumSize(psz);
		setMaximumSize(psz);
	}

	/**
	 * Create a new stream panel with autoplay, no stream controls, and
	 * no mouse PTZ.
	 */
	public StreamPanel(VideoRequest req) {
		this(req, null, null, false, true);
	}

	/** Scheduler streaming to stop */
	void schedStopStream() {
		STREAMER.addJob(new Job() {
			public void perform() {
				stopStream();
			}
		});
	}

	/**
	 * Stop streaming, if a stream is currently active.
	 * This is normally called from the streamer thread.
	 */
	private void stopStream() {
		if (screen_pnl != null)
			clearStream();
	}

	/** Schedule streaming to start */
	void schedPlayStream() {
		STREAMER.addJob(new Job() {
			public void perform() {
				playStream();
			}
		});
	}

	/**
	 * Start streaming from the current camera, unless null.
	 * This is normally called from the streamer thread.
	 */
	private void playStream() {
		stopStream();
		if (camera != null)
			requestStream(camera);
	}

	/** Schedule external streaming to start */
	void schedPlayExternal(final SmartDesktop desktop) {
		STREAMER.addJob(new Job() {
			public void perform() {
				playExternal(desktop);
			}
		});
	}

	/** Play stream on external player */
	private void playExternal(SmartDesktop desktop) {
		stopStream();
		desktop.showExtFrame(new VidWindow(camera, true, Size.MEDIUM));
	}

	/** Update stream status */
	private void updateStatus() {
		STREAMER.addJob(new Job() {
			public void perform() {
				updateButtonState();
			}
		});
	}

	/** Update the button state */
	private void updateButtonState() {
		if (control_pnl != null) {
			control_pnl.updateButtonState(camera != null,
				isStreaming());
		}
	}

	/**
	 * Set the Camera to use for streaming.  If a current stream exists,
	 * it is stopped.  If autoplay is enabled and Camera c can be
	 * streamed, it will be.
	 *
	 * @param c The camera to stream, or null to merely clear the current
	 *          stream.
	 */
	public void setCamera(final Camera c) {
		STREAMER.addJob(new Job() {
			public void perform() {
				stopStream();
				camera = c;
				updateButtonState();
				if (autoplay)
					playStream();
			}
		});
	}

	/** Request a new video stream */
	private void requestStream(Camera c) {
		screen_pnl.setCamera(c);
		handleStateChange();
		timer.start();
	}

	/** Clear the video stream */
	private void clearStream() {
		screen_pnl.releaseStream();
		screen_pnl.stopStatusMonitor();
		handleStateChange();
	}

	/** Dispose of the stream panel */
	public final void dispose() {
		clearStream();
	}

	/** Are we currently streaming? */
	public boolean isStreaming() {
		return screen_pnl != null && screen_pnl.isStreaming();
	}

	/**
	 * Handle a possible streaming state change.  If necessary, update
	 * stream_state, streaming control button status, and notify
	 * StreamStatusListeners, ensuring against superfluous duplicate
	 * events.
	 */
	private void handleStateChange() {
		boolean streaming = isStreaming();
		if (streaming == stream_state)
			return;
		stream_state = streaming;
		updateButtonState();
		for (StreamStatusListener ssl : ssl_set) {
			if (stream_state)
				ssl.onStreamStarted();
			else
				ssl.onStreamFinished();
		}
	}

	/** Bind a StreamStatusListener to this StreamPanel. */
	public void bindStreamStatusListener(StreamStatusListener ssl) {
		if (ssl != null)
			ssl_set.add(ssl);
	}

	/** Unbind a StreamStatusListener from this StreamPanel. */
	public void unbindStreamStatusListener(StreamStatusListener ssl) {
		if (ssl != null)
			ssl_set.remove(ssl);
	}
}
