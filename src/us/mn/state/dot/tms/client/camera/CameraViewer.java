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
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import us.mn.state.dot.log.TmsLogFactory;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.client.TmsSelectionEvent;
import us.mn.state.dot.tms.client.TmsSelectionListener;
import us.mn.state.dot.tms.client.TmsSelectionModel;
import us.mn.state.dot.tms.client.proxy.LocationProxy;
import us.mn.state.dot.tms.client.toast.Icons;
import us.mn.state.dot.video.AbstractImageFactory;
import us.mn.state.dot.video.Camera;
import us.mn.state.dot.video.Client;
import us.mn.state.dot.video.RepeaterImageFactory;
import us.mn.state.dot.video.VideoException;
import us.mn.state.dot.video.client.VideoMonitor;

/**
 * GUI for viewing camera images
 *
 * @author Douglas Lau
 */
public final class CameraViewer extends JPanel implements TmsSelectionListener {

	/** The number of frames to process (for streaming) */
	static protected final int STREAM_DURATION = 300;

	/** Dead zone needed for too-precise joystick drivers */
	static protected final float AXIS_DEADZONE = 3f / 64;

	/** Button number to select previous camera */
	static protected final int BUTTON_PREVIOUS = 10;

	/** Button number to select next camera */
	static protected final int BUTTON_NEXT = 11;

	/** Network worker thread */
	static protected final Scheduler NETWORKER = new Scheduler("NETWORKER");

	/** Properties for configuring the video client */
	private final Properties videoProps;

	/** The base URLs of the backend video stream servers */
	private final String[] streamUrls;

	/** The video stream request parameter wrapper */
	private final Client client = new Client();

	/** Displays the name of the selected camera */
	protected final JTextField txtId = new JTextField();

	/** Camera location */
	protected final JTextField txtLocation = new JTextField();

	/** Streaming video viewer */
	protected final VideoMonitor monitor = new VideoMonitor();

	/** Button used to play videoe */
	protected final JButton play = new JButton(Icons.getIcon("play"));

	/** Button used to stop video */
	protected final JButton stop = new JButton(Icons.getIcon("stop"));

	/** Panel for controlling camera PTZ */
	protected final PTZPanel ptz_panel = new PTZPanel();

	/** Panel for the video controls */
	protected final JPanel videoControls =
		new JPanel(new FlowLayout(FlowLayout.CENTER));

	/** Device handler for camera devices */
	protected final CameraHandler handler;

	/** Currently selected camera */
	protected CameraProxy selected = null;

	/** Joystick polling thread */
	protected final JoystickThread joystick = new JoystickThread();

	/** Create a new camera viewer */
	public CameraViewer(CameraHandler h, boolean admin, Properties p) {
		super(new GridBagLayout());
		videoProps = p;
		streamUrls = AbstractImageFactory.createBackendUrls(p, 1);
		handler = h;
		handler.getSelectionModel().addTmsSelectionListener( this );
		setBorder(BorderFactory.createTitledBorder("Selected Camera"));
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 0;
		bag.insets = new Insets(2, 4, 2, 4);
		bag.anchor = GridBagConstraints.EAST;
		add(new JLabel("ID"), bag);
		add(new JLabel("Location"), bag);
		bag.gridx = 1;
		bag.gridy = 0;
		bag.fill = GridBagConstraints.HORIZONTAL;
		bag.weightx = 1;
		txtId.setEditable(false);
		add(txtId, bag);
		bag.gridy = 1;
		txtLocation.setEditable(false);
		add(txtLocation, bag);
		bag.gridx = 0;
		bag.gridy = 2;
		bag.gridwidth = 2;
		bag.anchor = GridBagConstraints.CENTER;
		bag.fill = GridBagConstraints.BOTH;
		add(monitor, bag);
		monitor.setStatusVisible(false);
		monitor.setProgressVisible(true);
		monitor.setLabelVisible(false);
		bag.gridy = 3;
		bag.fill = GridBagConstraints.NONE;
		bag.gridwidth = 2;
		play.setToolTipText("Play");
		stop.setToolTipText("Stop");
		videoControls.add(play);
		videoControls.add(stop);
		add(videoControls, bag);
		bag.gridy = 4;
//		if(admin)
//			add(ptz_panel, bag);
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
				}
			}
		});
	}

	/** Filter an axis to remove slop around the joystick dead zone */
	static protected float filter_deadzone(float v) {
		if(Math.abs(v) <= AXIS_DEADZONE)
			return 0;
		else
			return v;
	}

	/** Pan value from last poll */
	protected float pan;

	/** Tilt value from last poll */
	protected float tilt;

	/** Zoom value from last poll */
	protected float zoom;

	/** Poll the joystick and send PTZ command to server */
	protected void pollJoystick() throws RemoteException {
		CameraProxy proxy = selected;	// Avoid race
		if(proxy != null) {
			float p = filter_deadzone(joystick.getPan());
			float t = -filter_deadzone(joystick.getTilt());
			float z = filter_deadzone(joystick.getZoom());
			if(p != 0 || pan != 0 || t != 0 || tilt != 0 ||
			   z != 0 || zoom != 0)
			{
				proxy.camera.move(p, t, z);
				pan = p;
				tilt = t;
				zoom = z;
			}
		}
	}

	/** Lookup the camera by ID */
	protected CameraProxy lookupCamera(String id) {
		Map proxies = handler.getProxies();
		synchronized(proxies) {
			Object proxy = proxies.get(id);
			if(proxy instanceof CameraProxy)
				return (CameraProxy)proxy;
			else
				return null;
		}
	}

	/** Select the next camera */
	protected void selectNextCamera() {
		CameraProxy camera = selected;	// Avoid race
		if(camera != null)
			selectCamera(camera.getUID() + 1);
	}

	/** Select the previous camera */
	protected void selectPreviousCamera() {
		CameraProxy camera = selected;	// Avoid race
		if(camera != null)
			selectCamera(camera.getUID() - 1);
	}

	/** Select the camera by number */
	protected void selectCamera(int uid) {
		StringBuffer b = new StringBuffer();
		b.append(uid);
		while(b.length() < 3)
			b.insert(0, '0');
		selectCamera("C" + b);
	}

	/** Select the camera by ID */
	protected void selectCamera(String id) {
		CameraProxy proxy = lookupCamera(id);
		if(proxy != null) {
			TmsSelectionModel sel = handler.getSelectionModel();
			sel.setSelected(proxy);
		}
	}

	/** Dispose of the camera viewer */
	public void dispose() {
		removeAll();
		selected = null;
	}

	/** Set the selected camera */
	public void setSelected(CameraProxy camera) {
		selected = camera;
		pan = 0;
		tilt = 0;
		zoom = 0;
		refreshUpdate();
		refreshStatus();
	}

	/** Called whenever the selected TMS object changes */
	public void selectionChanged(TmsSelectionEvent e) {
		final TMSObject o = e.getSelected();
		if(o instanceof CameraProxy)
			setSelected((CameraProxy)o);
	}

	/** Called whenever the TMS object is updated */
	public void refreshUpdate() {
		CameraProxy camera = selected;	// Avoid NPE
		if(camera != null) {
			LocationProxy loc = (LocationProxy)camera.getLocation();
			txtId.setText(camera.getId());
			txtLocation.setText(loc.getDescription());
			play.setEnabled(camera.isActive());
			stop.setEnabled(camera.isActive());
			if(camera.isActive())
				ptz_panel.setCamera(camera);
			else
				ptz_panel.setEnabled(false);
		} else
			ptz_panel.setEnabled(false);
	}

	/** Refresh the status of the device */
	public void refreshStatus() {
		CameraProxy camera = selected;
		if(camera == null) {
			clear();
			return;
		}
		try {
			playPressed(camera);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/** Start video streaming */
	protected void playPressed(CameraProxy c) throws MalformedURLException,
		VideoException
	{
		Camera camera = new Camera();
		camera.setId(c.getId());
		client.setCamera(camera);
		monitor.setImageFactory(new RepeaterImageFactory(client,
			streamUrls[client.getArea()]), STREAM_DURATION);
	}

	/** Stop video streaming */
	protected void stopPressed() {
		monitor.setImageFactory(null, 0);
	}

	/** Clear all of the fields */
	protected void clear() {
		txtId.setText("");
		txtLocation.setText("");
		play.setEnabled(false);
		stop.setEnabled(false);
		ptz_panel.setEnabled(false);
	}
}
