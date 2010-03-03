/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2010  Minnesota Department of Transportation
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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.toast.Icons;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;
import us.mn.state.dot.tms.client.camera.stream.VideoRequest;
import us.mn.state.dot.tms.client.camera.stream.HttpDataSource;
import us.mn.state.dot.tms.client.camera.stream.StreamPanel;

/**
 * GUI for viewing camera images
 *
 * @author Douglas Lau
 */
public class CameraViewer extends JPanel
	implements ProxySelectionListener<Camera>
{
	/** System attribute for number of frames to process (for streaming) */
	static protected final int STREAM_DURATION =
		SystemAttrEnum.CAMERA_NUM_VIDEO_FRAMES.getInt();

	/** Dead zone needed for too-precise joystick drivers */
	static protected final float AXIS_DEADZONE = 3f / 64;

	/** The system attribute for the number of button presets */
	static protected final int NUMBER_BUTTON_PRESETS =
		SystemAttrEnum.CAMERA_NUM_PRESET_BTNS.getInt();

	/** Button number to select previous camera */
	static protected final int BUTTON_PREVIOUS = 10;

	/** Button number to select next camera */
	static protected final int BUTTON_NEXT = 11;

	/** Network worker thread */
	static protected final Scheduler NETWORKER = new Scheduler("NETWORKER");

	/** Parse the integer ID of a monitor or camera */
	static protected int parseUID(String name) {
		String id = name;
		while(!Character.isDigit(id.charAt(0)))
			id = id.substring(1);
		try {
			return Integer.parseInt(id);
		}
		catch(NumberFormatException e) {
			return 0;
		}
	}

	/** Sonar state */
	protected final SonarState state;

	/** The video stream request parameter wrapper */
	private final VideoRequest request;

	/** Displays the name of the selected camera */
	protected final JTextField txtId = new JTextField();

	/** Camera location */
	protected final JTextField txtLocation = new JTextField();

	/** Video output selection ComboBox */
	protected final JComboBox cmbOutput;

	/** Video monitor output */
	protected VideoMonitor video_monitor;

	/** Streaming video viewer */
	protected final StreamPanel s_panel = new StreamPanel();

	/** Button used to play video */
	protected final JButton play = new JButton(Icons.getIcon("play"));

	/** Button used to stop video */
	protected final JButton stop = new JButton(Icons.getIcon("stop"));

	/** Panel for controlling camera PTZ */
	protected final CameraControl ptz_panel = new CameraControl();

	/** Panel for the video controls */
	protected final JPanel videoControls =
		new JPanel(new FlowLayout(FlowLayout.CENTER));

	/** Proxy manager for camera devices */
	protected final CameraManager manager;

	/** Logged in user */
	protected final User user;

	/** Currently selected camera */
	protected Camera selected = null;

	/** Joystick polling thread */
	protected final JoystickThread joystick = new JoystickThread();

	/** Create a new camera viewer */
	public CameraViewer(Session session, CameraManager man) {
		super(new GridBagLayout());
		manager = man;
		manager.getSelectionModel().addProxySelectionListener(this);
		state = session.getSonarState();
		user = session.getUser();
		request = new VideoRequest(session.getProperties());
		request.setFrames(STREAM_DURATION);
		setBorder(BorderFactory.createTitledBorder("Selected Camera"));
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 0;
		bag.insets = new Insets(2, 4, 2, 4);
		bag.anchor = GridBagConstraints.EAST;
		add(new JLabel("ID"), bag);
		bag.gridx = 2;
		add(new JLabel("Output"), bag);
		bag.gridx = 0;
		bag.gridy = 1;
		add(new JLabel("Location"), bag);
		bag.gridx = 1;
		bag.gridy = 0;
		bag.fill = GridBagConstraints.HORIZONTAL;
		bag.weightx = 1;
		txtId.setEditable(false);
		add(txtId, bag);
		bag.gridx = 3;
		bag.weightx = 0.5;
		cmbOutput = createOutputCombo();
		add(cmbOutput, bag);
		new ActionJob(this, cmbOutput) {
			public void perform() throws Exception {
				monitorSelected();
			}
		};
		bag.gridx = 1;
		bag.gridy = 1;
		bag.weightx = 1;
		txtLocation.setEditable(false);
		add(txtLocation, bag);
		bag.gridx = 0;
		bag.gridy = 2;
		bag.gridwidth = 4;
		bag.anchor = GridBagConstraints.CENTER;
		bag.fill = GridBagConstraints.BOTH;
		add(s_panel, bag);
		bag.gridy = 3;
		bag.fill = GridBagConstraints.NONE;
		play.setToolTipText("Play");
		stop.setToolTipText("Stop");
		videoControls.add(play);
		videoControls.add(stop);
		add(videoControls, bag);
		bag.gridy = 4;
		if(SystemAttrEnum.CAMERA_PTZ_PANEL_ENABLE.getBoolean())
			add(ptz_panel, bag);
		new ActionJob(NETWORKER, play) {
			public void perform() throws Exception {
				playPressed(selected);
			}
		};
		new ActionJob(NETWORKER, stop) {
			public void perform() {
				stopPressed();
			}
		};
		clear();
		Thread t = new Thread() {
			public void run() {
				while(true) {
					try {
						pollJoystick();
						sleep(200);
					}
					catch(Exception e) {
						e.printStackTrace();
						break;
					}
				}
			}
		};
		Connection c = state.lookupConnection(state.getConnection());
		request.setSonarSessionId(c.getSessionId());
		request.setRate(30);
		t.setDaemon(true);
		t.start();
		joystick.addJoystickListener(new JoystickListener() {
			public void buttonChanged(JoystickButtonEvent ev) {
				if(ev.pressed) {
					if(ev.button == BUTTON_NEXT)
						selectNextCamera();
					else if(ev.button == BUTTON_PREVIOUS)
						selectPreviousCamera();
					else if((ev.button >= 0) && (ev.button < NUMBER_BUTTON_PRESETS))
						selectCameraPreset(ev.button + 1);
				}
			}
		});
	}

	/** Filter an axis to remove slop around the joystick dead zone */
	static protected float filter_deadzone(float v) {
		float av = Math.abs(v);
		if(av > AXIS_DEADZONE) {
			float fv = (av - AXIS_DEADZONE) / (1 - AXIS_DEADZONE);
			if(v < 0)
				return -fv;
			else
				return fv;
		} else
			return 0;
	}

	/** Pan value from last poll */
	protected float pan;

	/** Tilt value from last poll */
	protected float tilt;

	/** Zoom value from last poll */
	protected float zoom;

	/** Poll the joystick and send PTZ command to server */
	protected void pollJoystick() {
		Camera proxy = selected;	// Avoid race
		if(proxy != null) {
			float p = filter_deadzone(joystick.getPan());
			float t = -filter_deadzone(joystick.getTilt());
			float z = filter_deadzone(joystick.getZoom());
			if(p != 0 || pan != 0 || t != 0 || tilt != 0 ||
			   z != 0 || zoom != 0)
			{
				Float[] ptz = new Float[3];
				ptz[0] = new Float(p);
				ptz[1] = new Float(t);
				ptz[2] = new Float(z);
				proxy.setPtz(ptz);
				pan = p;
				tilt = t;
				zoom = z;
			}
		}
	}

	/** Select the next camera */
	protected void selectNextCamera() {
		Camera camera = selected;	// Avoid race
		if(camera != null)
			selectCamera(parseUID(camera.getName()) + 1);
	}

	/** Select the previous camera */
	protected void selectPreviousCamera() {
		Camera camera = selected;	// Avoid race
		if(camera != null)
			selectCamera(parseUID(camera.getName()) - 1);
	}

	/** Command current camera to goto preset location */
	protected void selectCameraPreset(int preset) {
		Camera proxy = selected;	// Avoid race
		if(proxy != null)
			proxy.setRecallPreset(preset);
	}

	/** Select the camera by number */
	protected void selectCamera(int uid) {
		StringBuilder b = new StringBuilder();
		b.append(uid);
		while(b.length() < 3)
			b.insert(0, '0');
		selectCamera("C" + b);
	}

	/** Select the camera by ID */
	protected void selectCamera(String id) {
		Camera proxy = CameraHelper.lookup(id);
		if(proxy != null)
			manager.getSelectionModel().setSelected(proxy);
	}

	/** Dispose of the camera viewer */
	public void dispose() {
		removeAll();
		selected = null;
	}

	/** Set the selected camera */
	public void setSelected(Camera camera) {
		selected = camera;
		pan = 0;
		tilt = 0;
		zoom = 0;
		if(camera != null) {
			txtId.setText(camera.getName());
			txtLocation.setText(GeoLocHelper.getDescription(
				camera.getGeoLoc()));
			try {
				playPressed(camera);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			if(video_monitor != null)
				video_monitor.setCamera(camera);
		}
		updateMonitorPanel(camera);
	}

	/** Called whenever a camera is added to the selection */
	public void selectionAdded(Camera c) {
		if(manager.getSelectionModel().getSelectedCount() <= 1)
			setSelected(c);
	}

	/** Called whenever a camera is removed from the selection */
	public void selectionRemoved(Camera c) {
		ProxySelectionModel<Camera> model = manager.getSelectionModel();
		if(model.getSelectedCount() == 1) {
			for(Camera cam: model.getSelected())
				setSelected(cam);
		} else if(c == selected)
			setSelected(null);
	}

	/** Update the monitor panel */
	protected void updateMonitorPanel(Camera camera) {
		if(camera != null)
			enableMonitorPanel(camera);
		else
			disableMonitorPanel();
	}

	/** Enable the monitor panel */
	protected void enableMonitorPanel(Camera camera) {
		play.setEnabled(true);
		stop.setEnabled(true);
		ptz_panel.setCamera(camera);
		ptz_panel.setEnabled(true);
	}

	/** Disable the monitor panel */
	protected void disableMonitorPanel() {
		stopPressed();
		play.setEnabled(false);
		stop.setEnabled(false);
		ptz_panel.setEnabled(false);
	}

	/** Called when a video monitor is selected */
	protected void monitorSelected() {
		Camera camera = selected;
		Object o = cmbOutput.getSelectedItem();
		if(o instanceof VideoMonitor) {
			video_monitor = (VideoMonitor)o;
			video_monitor.setCamera(camera);
		} else
			video_monitor = null;
		updateMonitorPanel(camera);
	}

	/** Start video streaming */
	protected void playPressed(Camera c) throws IOException {
		HttpDataSource source = new HttpDataSource(request.getUrl(
			c.getName()));
		s_panel.setVideoStream(source.createStream(),
			request.getFrames());
	}

	/** Stop video streaming */
	protected void stopPressed() {
		s_panel.setVideoStream(null, 0);
	}

	/** Clear all of the fields */
	protected void clear() {
		txtId.setText("");
		txtLocation.setText("");
		disableMonitorPanel();
	}

	/** Create the video output selection combo box */
	private JComboBox createOutputCombo() {
		JComboBox box = new JComboBox();
		FilteredMonitorModel m = new FilteredMonitorModel(user, state);
		box.setModel(new WrapperComboBoxModel(m));
		if(m.getSize() > 1)
			box.setSelectedIndex(1);
		return box;
	}
}
