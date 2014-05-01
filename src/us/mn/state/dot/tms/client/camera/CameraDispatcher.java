/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2014  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * GUI for viewing camera images.
 *
 * @author Douglas Lau
 * @author Tim Johnson
 */
public class CameraDispatcher extends IPanel {

	/** The system attribute for the number of button presets */
	static private final int NUMBER_BUTTON_PRESETS =
		SystemAttrEnum.CAMERA_NUM_PRESET_BTNS.getInt();

	/** Button number to select previous camera */
	static private final int BUTTON_PREVIOUS = 10;

	/** Button number to select next camera */
	static private final int BUTTON_NEXT = 11;

	/** Video size */
	static private final VideoRequest.Size SIZE = VideoRequest.Size.MEDIUM;

	/** User session */
	private final Session session;

	/** Proxy manager for camera devices */
	private final CameraManager manager;

	/** Selection model */
	private final ProxySelectionModel<Camera> sel_model;

	/** Selection listener */
	private final ProxySelectionListener<Camera> sel_listener =
		new ProxySelectionListener<Camera>()
	{
		public void selectionAdded(Camera c) {
			setSelected(sel_model.getSingleSelection());
		}
		public void selectionRemoved(Camera c) {
			setSelected(sel_model.getSingleSelection());
		}
	};

	/** Camera list model */
	private final ProxyListModel<Camera> model;

	/** Camera name label */
	private final JLabel name_lbl = createValueLabel();

	/** Camera location label */
	private final JLabel location_lbl = createValueLabel();

	/** Video output selection ComboBox */
	private final JComboBox output_cbx;

	/** Selected video monitor output */
	private VideoMonitor video_monitor;

	/** Camera PTZ control */
	private final CameraPTZ cam_ptz;

	/** Streaming video panel */
	private final StreamPanel stream_pnl;

	/** PTZ panel */
	private final PTZPanel ptz_pnl;

	/** Panel for camera presets */
	private final PresetPanel preset_pnl;

	/** Currently selected camera */
	private Camera selected = null;

	/** Joystick PTZ handler */
	private final JoystickPTZ joy_ptz;

	/** Create a new camera dispatcher */
	public CameraDispatcher(Session s, CameraManager man) {
		session = s;
		manager = man;
		sel_model = manager.getSelectionModel();
		model = session.getSonarState().getCamCache().getCameraModel();
		cam_ptz = new CameraPTZ(s);
		joy_ptz = new JoystickPTZ(cam_ptz);
		ptz_pnl = new PTZPanel(cam_ptz);
		preset_pnl = new PresetPanel();
		stream_pnl = createStreamPanel();
		output_cbx = createOutputCombo();
	}

	/** Create the stream panel */
	private StreamPanel createStreamPanel() {
		VideoRequest vr = new VideoRequest(session.getProperties(),
			SIZE);
		vr.setSonarSessionId(session.getSessionId());
		vr.setRate(30);
		return new StreamPanel(vr, cam_ptz);
	}

	/** Create the video output selection combo box */
	private JComboBox createOutputCombo() {
		JComboBox box = new JComboBox();
		FilteredMonitorModel m = new FilteredMonitorModel(session);
		box.setModel(new WrapperComboBoxModel(m));
		if(m.getSize() > 1)
			box.setSelectedIndex(1);
		return box;
	}

	/** Initialize the widgets on the panel */
	public void initialize() {
		output_cbx.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				monitorSelected();
			}
		});
		joy_ptz.addJoystickListener(new JoystickListener() {
			public void buttonChanged(JoystickButtonEvent ev) {
				if(ev.pressed)
					doJoyButton(ev);
			}
		});
		setTitle(I18N.get("camera.selected"));
		add("device.name");
		add(name_lbl);
		add("camera.output");
		add(output_cbx, Stretch.LAST);
		add("location");
		add(location_lbl, Stretch.LAST);
		add(stream_pnl, Stretch.FULL);
		if(SystemAttrEnum.CAMERA_PTZ_PANEL_ENABLE.getBoolean())
			add(ptz_pnl, Stretch.FULL);
		add(preset_pnl, Stretch.CENTER);
		clear();
		sel_model.addProxySelectionListener(sel_listener);
	}

	/** Process a joystick button event */
	private void doJoyButton(JoystickButtonEvent ev) {
		if(ev.button == BUTTON_NEXT)
			selectNextCamera();
		else if(ev.button == BUTTON_PREVIOUS)
			selectPreviousCamera();
		else if(ev.button >= 0 && ev.button < NUMBER_BUTTON_PRESETS)
			selectCameraPreset(ev.button + 1);
	}

	/** Select the next camera */
	private void selectNextCamera() {
		Camera cam = model.higher(selected);
		if(cam != null)
			sel_model.setSelected(cam);
	}

	/** Select the previous camera */
	private void selectPreviousCamera() {
		Camera cam = model.lower(selected);
		if(cam != null)
			sel_model.setSelected(cam);
	}

	/** Command current camera to goto preset location */
	private void selectCameraPreset(int preset) {
		Camera proxy = selected;	// Avoid race
		if(proxy != null)
			proxy.setRecallPreset(preset);
	}

	/** Dispose of the camera viewer */
	@Override
	public void dispose() {
		sel_model.removeProxySelectionListener(sel_listener);
		joy_ptz.dispose();
		cam_ptz.setCamera(null);
		stream_pnl.dispose();
		selected = null;
		super.dispose();
	}

	/** Set the selected camera */
	public void setSelected(final Camera camera) {
		if(camera == selected)
			return;
		cam_ptz.setCamera(camera);
		selected = camera;
		if(camera != null) {
			name_lbl.setText(camera.getName());
			location_lbl.setText(GeoLocHelper.getDescription(
				camera.getGeoLoc()));
			stream_pnl.setCamera(camera);
			selectCamera();
			ptz_pnl.setEnabled(cam_ptz.canControlPtz());
			preset_pnl.setCamera(camera);
			preset_pnl.setEnabled(cam_ptz.canControlPtz());
		} else
			clear();
	}

	/** Called when a video monitor is selected */
	private void monitorSelected() {
		Object o = output_cbx.getSelectedItem();
		if(o instanceof VideoMonitor) {
			video_monitor = (VideoMonitor)o;
			selectCamera();
		} else
			video_monitor = null;
	}

	/** Select a camera on a video monitor */
	private void selectCamera() {
		VideoMonitor vm = video_monitor;
		if(vm != null)
			vm.setCamera(selected);
	}

	/** Clear all of the fields */
	private void clear() {
		name_lbl.setText("");
		location_lbl.setText("");
		stream_pnl.setCamera(null);
		ptz_pnl.setEnabled(false);
		preset_pnl.setEnabled(false);
	}
}
