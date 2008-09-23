/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2008  Minnesota Department of Transportation
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.security.IrisUser;
import us.mn.state.dot.tms.client.sonar.ProxySelectionListener;
import us.mn.state.dot.tms.client.sonar.ProxySelectionModel;
import us.mn.state.dot.tms.client.toast.Icons;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;
import us.mn.state.dot.video.AbstractDataSource;
import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.HttpDataSource;
import us.mn.state.dot.video.VideoException;

/**
 * GUI for viewing camera images
 *
 * @author Douglas Lau
 */
public class CameraViewer extends JPanel
	implements ProxySelectionListener<Camera>
{
	/** The default number of frames to process (for streaming) */
	static protected final String DEFAULT_STREAM_DURATION = "900";

	/** The property name for the number of frames to process */
	static protected final String PROPERTY_STREAM_DURATION = "stream.duration";

	/** Dead zone needed for too-precise joystick drivers */
	static protected final float AXIS_DEADZONE = 3f / 64;

	/** Button number to select preset 1 */
	static protected final int BUTTON_PRESET_1 = 0;

	/** Button number to select preset 2 */
	static protected final int BUTTON_PRESET_2 = 1;

	/** Button number to select preset 3 */
	static protected final int BUTTON_PRESET_3 = 2;

	/** Button number to select preset 4*/
	static protected final int BUTTON_PRESET_4 = 3;

	/** Button number to select preset 5 */
	static protected final int BUTTON_PRESET_5= 4;

	/** Button number to select previous camera */
	static protected final int BUTTON_PREVIOUS = 10;

	/** Button number to select next camera */
	static protected final int BUTTON_NEXT = 11;
	
	/** The property value for true */
	static protected final String PROPERTY_TRUE = "true";
	
	/** The property name for on screen ptz control */
	static protected final String PROPERTY_ON_SCREEN_PTZ = "on.screen.ptz";

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

	/** Properties for configuring the video client */
	private final Properties videoProps;

	/** Message logger */
	protected final Logger logger;

	/** Sonar state */
	protected final SonarState state;

	/** The base URLs of the backend video stream servers */
	private final String[] streamUrls;

	/** The video stream request parameter wrapper */
	private final Client client = new Client();

	/** Displays the name of the selected camera */
	protected final JTextField txtId = new JTextField();

	/** Camera location */
	protected final JTextField txtLocation = new JTextField();

	/** Video output selection ComboBox */
	protected final JComboBox cmbOutput;

	/** Video monitor output */
	protected us.mn.state.dot.tms.VideoMonitor video_monitor;
	
	/** Streaming video viewer */
	protected final us.mn.state.dot.video.client.VideoMonitor monitor =
		new us.mn.state.dot.video.client.VideoMonitor();

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
	protected final IrisUser user;

	/** Currently selected camera */
	protected Camera selected = null;

	/** Joystick polling thread */
	protected final JoystickThread joystick = new JoystickThread();
	
	/** Create a new camera viewer */
	public CameraViewer(CameraManager m, Properties p, Logger l,
		SonarState st, IrisUser u)
	{
		super(new GridBagLayout());
		manager = m;
		manager.getSelectionModel().addProxySelectionListener(this);
		videoProps = p;
		logger = l;
		state = st;
		user = u;
		streamUrls = AbstractDataSource.createBackendUrls(p, 1);
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
		add(monitor, bag);
		monitor.setStatusVisible(false);
		monitor.setProgressVisible(true);
		monitor.setLabelVisible(false);
		bag.gridy = 3;
		bag.fill = GridBagConstraints.NONE;
		play.setToolTipText("Play");
		stop.setToolTipText("Stop");
		videoControls.add(play);
		videoControls.add(stop);
		add(videoControls, bag);
		bag.gridy = 4;
		if (videoProps.getProperty(PROPERTY_ON_SCREEN_PTZ, "").equalsIgnoreCase(PROPERTY_TRUE)) {
			add(ptz_panel, bag);
		}
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
		client.setSonarSessionId(c.getSessionId());
		client.setRate(30);
		t.setDaemon(true);
		t.start();
		joystick.addJoystickListener(new JoystickListener() {
			public void buttonChanged(JoystickButtonEvent ev) {
				if(ev.pressed) {
					if(ev.button == BUTTON_NEXT)
						selectNextCamera();
					else if(ev.button == BUTTON_PREVIOUS)
						selectPreviousCamera();
					else if(ev.button == BUTTON_PRESET_1)
						selectCameraPreset(1);
					else if(ev.button == BUTTON_PRESET_2)
						selectCameraPreset(2);
					else if(ev.button == BUTTON_PRESET_3)
						selectCameraPreset(3);
					else if(ev.button == BUTTON_PRESET_4)
						selectCameraPreset(4);
					else if(ev.button == BUTTON_PRESET_5)
						selectCameraPreset(5);
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
			proxy.setGoToPreset(preset);
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
		Camera proxy = state.lookupCamera(id);
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
				if(video_monitor != null)
					video_monitor.setCamera(camera);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
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
		if(o instanceof us.mn.state.dot.tms.VideoMonitor) {
			video_monitor =
				(us.mn.state.dot.tms.VideoMonitor)o;
			video_monitor.setCamera(camera);
		} else
			video_monitor = null;
		updateMonitorPanel(camera);
	}

	/** Start video streaming */
	protected void playPressed(Camera c) throws MalformedURLException,
		VideoException
	{
		us.mn.state.dot.video.Camera camera =
			new us.mn.state.dot.video.Camera();
		camera.setId(c.getName());
		client.setCamera(camera);
		monitor.setDataSource(new HttpDataSource(client,
			new URL(streamUrls[client.getArea()] + "?id=" +
			client.getCameraId() + "&ssid=" +
			client.getSonarSessionId())), Integer.parseInt(
			videoProps.getProperty(PROPERTY_STREAM_DURATION,
			DEFAULT_STREAM_DURATION)));
	}

	/** Stop video streaming */
	protected void stopPressed() {
		monitor.setDataSource(null, 0);
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
		FilteredMonitorModel m = new FilteredMonitorModel(
			state.lookupUser(user.getName()), state);
		box.setModel(new WrapperComboBoxModel(m));
		if(m.getSize() > 1)
			box.setSelectedIndex(1);
		return box;
	}
}
