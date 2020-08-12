/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2020  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
 * Copyright (C) 2020 SRF Consulting Group
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
import java.awt.Frame;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A JPanel that can display a video stream. It includes a status label.
 *
 * Derived from the StreamPanel class.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 * @author Travis Swanston
 * @author John L. Stanley - SRF Consulting Group
 */
public class VidWindow extends AbstractForm {

	/** Status panel height */
	static private final int HEIGHT_STATUS_PNL = 40;

	/** Control panel height */
	static private final int HEIGHT_CONTROL_PNL = 30;

	/** Frame title */
	static private final String TITLE = "Stream Panel";

	/** Get a window title */
	static public String getWindowTitle(Camera cam) {
		return TITLE + ": " + cam.getName();
	}

	/** Check if a frame is a video window */
	static public boolean isFrame(Frame f) {
		return f.getTitle().startsWith(TITLE) && f.isVisible();
	}

	/** JPanel that renders the video stream */
	private final VidPanel videoPanel;

	/** Most recent streaming state.  State variable for event FSM. */
	private boolean stream_state = false;

	/** Stream status listeners to notify on stream status change events */
	private final Set<StreamStatusListener> ssl_set =
		new HashSet<StreamStatusListener>();

	/** Create a video window */
	public VidWindow(Camera cam, Boolean ctrl, VideoRequest.Size vsz) {
		this(cam, ctrl, UI.dimension(vsz.width,
				vsz.height + HEIGHT_STATUS_PNL
				+ (ctrl ? HEIGHT_CONTROL_PNL : 0)), 0);
	}

	/** Create a video window */
	public VidWindow(Camera cam, Boolean ctrl, Dimension pdm, int strm_num)
	{
		super(getWindowTitle(cam), true);

		setLayout(new BorderLayout());

		Session s = Session.getCurrent();
		int vidHeight = pdm.height - HEIGHT_STATUS_PNL
			- (ctrl ? HEIGHT_CONTROL_PNL : 0);

		int vidWidth = pdm.width;

		Dimension sz = UI.dimension(vidWidth, vidHeight);
		CameraPTZ cam_ptz = new CameraPTZ(s);
		cam_ptz.setCamera(cam);

		videoPanel = new VidPanel(sz, strm_num);
		add(videoPanel, BorderLayout.CENTER);

		if (ctrl)
			add(new PopoutCamControlPanel(cam_ptz), BorderLayout.SOUTH);

		setPreferredSize(UI.dimension(pdm.width, pdm.height));
		setMinimumSize(UI.dimension(pdm.width, pdm.height));
		setMaximumSize(UI.dimension(pdm.width, pdm.height));

		videoPanel.addChangeListener(new StateChangeListener());
		setCamera(cam);
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
		videoPanel.setCamera(c);
	}

	/** Are we currently streaming? */
	public boolean isStreaming() {
		VidPanel vp = videoPanel;
		if (vp != null)
			return vp.isStreaming();
		return false;
	}

	class StateChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			handleStateChange();
		}
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
		for (StreamStatusListener ssl : ssl_set) {
			if (stream_state)
				ssl.onStreamStarted();
			else
				ssl.onStreamFinished();
		}
	}

	/**
	 * Bind a StreamStatusListener to this StreamPanel.
	 */
	public void bindStreamStatusListener(StreamStatusListener ssl) {
		if (ssl != null)
			ssl_set.add(ssl);
	}

	/**
	 * Unbind a StreamStatusListener from this StreamPanel.
	 */
	public void unbindStreamStatusListener(StreamStatusListener ssl) {
		if (ssl != null)
			ssl_set.remove(ssl);
	}
}
