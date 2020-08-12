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
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.UserProperty;
import us.mn.state.dot.tms.client.camera.VideoRequest.Size;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Icons;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;
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
	private final JPanel control_pnl;

	/** Stop button */
	private final JButton stop_btn;

	/** Play button */
	private final JButton play_btn;

	/** Play external button */
	private final JButton playext_btn;

	/** Layout ComboBox */
	private JComboBox<String> layout_list;

	/** Layout ComboBox model */
	private DefaultComboBoxModel<String> layout_list_model;

	/** Save layout button */
	private JButton save_layout_btn;

	/** Delete layout button */
	private JButton delete_layout_btn;

	/** Restore layout button */
	private JButton restore_layout_btn;

	/** JLabel for displaying the stream details (codec, size, framerate) */
	private final JLabel status_lbl = new JLabel();

	/** Stream control commands */
	static private enum StreamCommand {
		STOP("camera.stream.stop"),
		PLAY("camera.stream.play"),
		PLAY_EXTERNAL("camera.stream.playext"),
		SAVE_LAYOUT("camera.template.save.layout"),
		DELETE_LAYOUT("camera.template.delete.layout"),
		RESTORE_LAYOUT("camera.template.restore.layout");

		/** Command I18n text */
		private final String text_id;

		/** Create a stream command */
		private StreamCommand(String tid) {
			text_id = tid;
		}

		/** Create a stream command button */
		private JButton createButton(final StreamPanel pnl) {
			IAction ia = new IAction(text_id) {
				@Override
				protected void doActionPerformed(ActionEvent ev) {
					STREAMER.addJob(new Job() {
						public void perform() {
							handleButton(pnl);
						}
					});
				}
			};
			final JButton btn = new JButton(ia);
			btn.setMargin(new Insets(0, 0, 0, 0));
			ImageIcon icon = Icons.getIconByPropName(text_id);
			if (icon != null) {
				btn.setIcon(icon);
				btn.setHideActionText(true);
			}
			btn.setFocusPainted(false);
			return btn;
		}

		/** Handle control button press */
		private void handleButton(StreamPanel pnl) {
			switch (this) {
			case STOP:
				pnl.stopStream();
				break;
			case PLAY:
				pnl.playStream();
				break;
			case PLAY_EXTERNAL:
				pnl.playExternal();
				break;
			case SAVE_LAYOUT:
				pnl.saveLayout();
				break;
			case DELETE_LAYOUT:
				pnl.deleteLayout();
				break;
			case RESTORE_LAYOUT:
				pnl.restoreLayout();
				break;
			}
		}
	}

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

	/** User session */
	private final Session session;

	/** User properties */
	private final Properties props;

	/** Smart desktop */
	private SmartDesktop desktop;

	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			updateEditMode();
		}
	};

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
		session = s;
		props = session.getProperties();
		stop_btn = StreamCommand.STOP.createButton(this);
		play_btn = StreamCommand.PLAY.createButton(this);
		playext_btn = StreamCommand.PLAY_EXTERNAL.createButton(this);
		VideoRequest.Size vsz = req.getSize();
		Dimension sz = UI.dimension(vsz.width, vsz.height);
		screen_pnl = new VidPanel(sz);
		Dimension vpsz = screen_pnl.getPreferredSize();
		control_pnl = createControlPanel(vsz);
		add(screen_pnl, BorderLayout.CENTER);
		if (ctrl)
			add(control_pnl, BorderLayout.SOUTH);

		int pnlHeight = vpsz.height + (ctrl ? HEIGHT_CONTROL_PNL : 0);
		Dimension psz = new Dimension(vsz.width, pnlHeight);
		setPreferredSize(psz);
		setMinimumSize(psz);
		setMaximumSize(psz);

		updateButtonState();
		if (session != null) {
			desktop = session.getDesktop();
			session.addEditModeListener(edit_lsnr);
			updateLayoutList();
		}
	}

	/**
	 * Create a new stream panel with autoplay, no stream controls, and
	 * no mouse PTZ.
	 */
	public StreamPanel(VideoRequest req) {
		this(req, null, null, false, true);
	}

	/** Create the control panel */
	private JPanel createControlPanel(VideoRequest.Size vsz) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER,
			UI.hgap, UI.vgap));

		// Use an editable ComboBox for saving more than one layout
		layout_list_model = new DefaultComboBoxModel<String>();
		layout_list = new JComboBox<String>(layout_list_model);
		layout_list.setToolTipText(I18N.get("camera.template.layout"));
		layout_list.setPreferredSize(UI.dimension(120, 28));

		save_layout_btn = StreamCommand.SAVE_LAYOUT.createButton(this);
		delete_layout_btn = StreamCommand.DELETE_LAYOUT
			.createButton(this);
		restore_layout_btn = StreamCommand.RESTORE_LAYOUT
			.createButton(this);

		p.add(stop_btn);
		p.add(play_btn);
		p.add(playext_btn);
		p.add(Box.createHorizontalStrut(10));
		p.add(layout_list);
		p.add(save_layout_btn);
		p.add(delete_layout_btn);
		p.add(restore_layout_btn);
		p.setPreferredSize(UI.dimension(vsz.width + 50,
			HEIGHT_CONTROL_PNL));
		p.setMinimumSize(UI.dimension(vsz.width + 50, HEIGHT_CONTROL_PNL));
		return p;
	}

	/** Save the current layout */
	private void saveLayout() {
		UserProperty.saveStreamLayout(props, getLayoutName());
		updateLayoutList();
	}

	/** Restore the saved layout */
	private void restoreLayout() {
		StreamLayout layout = new StreamLayout(props, getLayoutName());
		layout.restoreFrames(desktop);
		updateLayoutList();
	}

	/** Delete the selected layout */
	private void deleteLayout() {
		UserProperty.deleteStreamLayout(props, getLayoutName());
		updateLayoutList();
	}

	/** Get the selected layout name */
	private String getLayoutName() {
		String name = (String) layout_list.getSelectedItem();
		return (name != null && !name.isEmpty())
		      ?	name
		      : UserProperty.getNextStreamLayoutName(props);
	}

	/** Update the list of layouts based on the current properties */
	private void updateLayoutList() {
		String layoutName = (String) layout_list.getSelectedItem();

		// clear and update, then try to re-select
		layout_list_model.removeAllElements();
		ArrayList<String> layoutNames =
			UserProperty.getStreamLayoutNames(props);
		for (String ln: layoutNames)
			layout_list_model.addElement(ln);
		layout_list.setSelectedItem(layoutName);
	}

	/**
	 * Stop streaming, if a stream is currently active.
	 * This is normally called from the streamer thread.
	 */
	private void stopStream() {
		if (screen_pnl != null)
			clearStream();
	}

	/**
	 * Start streaming from the current camera, unless null.
	 * This is normally called from the streamer thread.
	 */
	private void playStream() {
		stopStream();
		if (camera == null) {
			setStatusText(null);
			return;
		}
		setStatusText(I18N.get("camera.stream.opening"));
		requestStream(camera);
	}

	/** Play stream on external player */
	private void playExternal() {
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
				setStatusText(null);
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
		if (session != null)
			session.removeEditModeListener(edit_lsnr);
		save_layout_btn.setEnabled(false);
	}

	/** Set the status label. */
	private void setStatusText(String s) {
		status_lbl.setText(s);
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

	/** Update the button state */
	private void updateButtonState() {
		if (camera == null) {
			stop_btn.setEnabled(false);
			play_btn.setEnabled(false);
			playext_btn.setEnabled(false);
			return;
		}
		boolean streaming = isStreaming();
		stop_btn.setEnabled(streaming);
		play_btn.setEnabled(!streaming);
		playext_btn.setEnabled(true);
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

	/** Update the edit mode */
	public void updateEditMode() {
		boolean editMode = session.getEditMode();
		save_layout_btn.setEnabled(editMode);
		delete_layout_btn.setEnabled(editMode);
		layout_list.setEditable(editMode);
	}
}
