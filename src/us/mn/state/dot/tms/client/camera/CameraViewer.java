/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2013  Minnesota Department of Transportation
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * GUI for viewing camera images
 *
 * @author Douglas Lau
 * @author Tim Johnson
 */
public class CameraViewer extends JPanel
	implements ProxySelectionListener<Camera>
{
	/** The system attribute for the number of button presets */
	static protected final int NUMBER_BUTTON_PRESETS =
		SystemAttrEnum.CAMERA_NUM_PRESET_BTNS.getInt();

	/** Button number to select previous camera */
	static protected final int BUTTON_PREVIOUS = 10;

	/** Button number to select next camera */
	static protected final int BUTTON_NEXT = 11;

	/** Video size */
	static private final VideoRequest.Size SIZE = VideoRequest.Size.MEDIUM;

	/** User session */
	private final Session session;

	/** Sonar state */
	protected final SonarState state;

	/** Logged in user */
	protected final User user;

	/** Video request */
	private final VideoRequest video_req;

	/** Displays the name of the selected camera */
	protected final JTextField txtId = new JTextField();

	/** Camera location */
	protected final JTextField txtLocation = new JTextField();

	/** Video output selection ComboBox */
	protected final JComboBox cmbOutput;

	/** Video monitor output */
	protected VideoMonitor video_monitor;

	/** Camera PTZ control */
	private final CameraPTZ cam_ptz;

	/** Streaming video panel */
	private final StreamPanel s_panel;

	/** Panel for controlling camera PTZ */
	private final CameraControl ptz_panel;

	/** Proxy manager for camera devices */
	protected final CameraManager manager;

	/** Currently selected camera */
	protected Camera selected = null;

	/** Joystick PTZ handler */
	private final JoystickPTZ joy_ptz;

	/** Create a new camera viewer */
	public CameraViewer(Session s, CameraManager man) {
		super(new GridBagLayout());
		manager = man;
		manager.getSelectionModel().addProxySelectionListener(this);
		session = s;
		cam_ptz = new CameraPTZ(s);
		joy_ptz = new JoystickPTZ(cam_ptz);
		ptz_panel = new CameraControl(cam_ptz);
		state = session.getSonarState();
		user = session.getUser();
		video_req = new VideoRequest(session.getProperties(), SIZE);
		Connection c = state.lookupConnection(state.getConnection());
		video_req.setSonarSessionId(c.getSessionId());
		video_req.setRate(30);
		s_panel = new StreamPanel(cam_ptz, video_req);
		setBorder(BorderFactory.createTitledBorder(
			I18N.get("camera.selected")));
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 0;
		bag.insets = new Insets(2, 4, 2, 4);
		bag.anchor = GridBagConstraints.EAST;
		add(new ILabel("device.name"), bag);
		bag.gridx = 2;
		add(new ILabel("camera.output"), bag);
		bag.gridx = 0;
		bag.gridy = 1;
		add(new ILabel("location"), bag);
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
			public void perform() {
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
		if(SystemAttrEnum.CAMERA_PTZ_PANEL_ENABLE.getBoolean())
			add(ptz_panel, bag);
		clear();
		joy_ptz.addJoystickListener(new JoystickListener() {
			public void buttonChanged(JoystickButtonEvent ev) {
				if(ev.pressed)
					doJoyButton(ev);
			}
		});
	}

	/** Process a joystick button event */
	protected void doJoyButton(JoystickButtonEvent ev) {
		if(ev.button == BUTTON_NEXT)
			selectNextCamera();
		else if(ev.button == BUTTON_PREVIOUS)
			selectPreviousCamera();
		else if(ev.button >= 0 && ev.button < NUMBER_BUTTON_PRESETS)
			selectCameraPreset(ev.button + 1);
	}

	/** Select the next camera */
	protected void selectNextCamera() {
		Camera cam = state.getCamCache().getCameraModel().higher(
			selected);
		if(cam != null)
			manager.getSelectionModel().setSelected(cam);
	}

	/** Select the previous camera */
	protected void selectPreviousCamera() {
		Camera cam = state.getCamCache().getCameraModel().lower(
			selected);
		if(cam != null)
			manager.getSelectionModel().setSelected(cam);
	}

	/** Command current camera to goto preset location */
	protected void selectCameraPreset(int preset) {
		Camera proxy = selected;	// Avoid race
		if(proxy != null)
			proxy.setRecallPreset(preset);
	}

	/** Dispose of the camera viewer */
	public void dispose() {
		removeAll();
		joy_ptz.dispose();
		cam_ptz.setCamera(null);
		s_panel.dispose();
		selected = null;
	}

	/** Set the selected camera */
	public void setSelected(final Camera camera) {
		if(camera == selected)
			return;
		cam_ptz.setCamera(camera);
		selected = camera;
		if(camera != null) {
			txtId.setText(camera.getName());
			txtLocation.setText(GeoLocHelper.getDescription(
				camera.getGeoLoc()));
			s_panel.setCamera(camera);
			if(video_monitor != null)
				video_monitor.setCamera(camera);
			ptz_panel.setCamera(camera);
			ptz_panel.setEnabled(cam_ptz.canControlPtz());
		} else
			clear();
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

	/** Called when a video monitor is selected */
	protected void monitorSelected() {
		Camera camera = selected;
		Object o = cmbOutput.getSelectedItem();
		if(o instanceof VideoMonitor) {
			video_monitor = (VideoMonitor)o;
			video_monitor.setCamera(camera);
		} else
			video_monitor = null;
	}

	/** Clear all of the fields */
	private void clear() {
		txtId.setText("");
		txtLocation.setText("");
		s_panel.setCamera(null);
		ptz_panel.setEnabled(false);
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
