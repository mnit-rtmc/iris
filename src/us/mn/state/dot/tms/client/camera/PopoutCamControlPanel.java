/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2016  Minnesota Department of Transportation
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

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.CameraPresetHelper;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Camera control panel.
 *
 * @author Douglas Lau
 */
public class PopoutCamControlPanel extends JPanel {

	/** User session */
	private final Session session;

	/** Panel for zoom control */
	private final ZoomPanel zoom_pnl;

	/** Camera PTZ control */
	private final CameraPTZ camera_ptz;
	
	/** Video output selection ComboBox */
	private final JComboBox<VideoMonitor> monitor_cbx;
	
	/** Selected video monitor output */
	private VideoMonitor video_monitor;
	
	/** Video monitor watcher */
	private final ProxyWatcher<VideoMonitor> watcher;
	
	/** Video monitor view */
	private final ProxyView<VideoMonitor> vm_view =
		new ProxyView<VideoMonitor>()
	{
		public void enumerationComplete() { }
		public void update(VideoMonitor vm, String a) {
			System.out.println("Updating monitor...");
			video_monitor = vm;
		}
		public void clear() {
			video_monitor = null;
		}
	};
	
	/** Cache of video monitor objects */
	private final TypeCache<VideoMonitor> vm_cache;
	
	/** Camera preset combo box */
	private final JComboBox<CameraPreset> preset_cbx =
		new JComboBox<CameraPreset>();

	/** Camera preset combo box model */
	private final DefaultComboBoxModel<CameraPreset> preset_mdl;
	
	/** Camera preset action */
	private final IAction preset_act = new IAction("camera.preset") {
		protected void doActionPerformed(ActionEvent e) {
			CameraPreset cp = (CameraPreset) preset_cbx.getSelectedItem();
			if (cp != null)
				camera_ptz.recallPreset(cp.getPresetNum());
		}
	};
	
	/** Video monitor action */
	private final IAction monitor_act = new IAction("video.monitor") {
		protected void doActionPerformed(ActionEvent e) {
			monitorSelected();
		}
	};

	/** Create a new camera control panel */
	public PopoutCamControlPanel(CameraPTZ cam_ptz) {
		session = Session.getCurrent();
		camera_ptz = cam_ptz;
		zoom_pnl = new ZoomPanel(cam_ptz);
	
		preset_mdl = createPresetModel(camera_ptz);
		preset_cbx.setModel(preset_mdl);
		preset_cbx.setAction(preset_act);
		preset_cbx.setRenderer(new PresetComboRendererLong());
		
		monitor_cbx = createMonitorCombo();
		monitor_cbx.setRenderer(new MonComboRendererLong());
		monitor_cbx.setAction(monitor_act);
		vm_cache = session.getSonarState().getCamCache()
                .getVideoMonitors();
		watcher = new ProxyWatcher<VideoMonitor>(vm_cache,vm_view,true);
		watcher.initialize();
		layoutPanel();


	}
	/** Create the video output selection combo box */
	private JComboBox<VideoMonitor> createMonitorCombo() {
		JComboBox<VideoMonitor> box = new JComboBox<VideoMonitor>();
		DefaultComboBoxModel<VideoMonitor> cbxm =
				new DefaultComboBoxModel<VideoMonitor>();
		
		// sort the monitor list first based on monitor number and permissions
		TypeCache<VideoMonitor> vms = session.getSonarState()
				.getCamCache().getVideoMonitors();
		ArrayList<VideoMonitor> lst = new ArrayList<VideoMonitor>();
		Iterator<VideoMonitor> it = vms.iterator();
		while (it.hasNext()) {
			lst.add(it.next());
		}
		lst.sort(new Comparator<VideoMonitor>() {
			@Override
			public int compare(VideoMonitor vm0, VideoMonitor vm1) {
				Integer n0 = vm0.getMonNum();
				Integer n1 = vm1.getMonNum();
				int c = n0.compareTo(n1);
				if (c!= 0)
					return c;
				else {
					boolean p0 = session.isWritePermitted(vm0, "camera");
					boolean p1 = session.isWritePermitted(vm1, "camera");
					return Boolean.compare(p0, p1);
				}
			}
		});
		
		// now put stuff in the model
		// add a blank element at the beginning
		cbxm.addElement(null);
		
		for (VideoMonitor vm: lst)
			cbxm.addElement(vm);
		
		box.setModel(cbxm);
		return box;
	}
	
	
	/** Create the camera preset model */
	private DefaultComboBoxModel<CameraPreset> createPresetModel(
			CameraPTZ cam_ptz) {
		Camera c = cam_ptz.getCamera();
		DefaultComboBoxModel<CameraPreset> cbxm =
				new DefaultComboBoxModel<CameraPreset>();
		
		// add a blank element at the beginning
		cbxm.addElement(null);
		
		if (c != null) {
			cam_ptz.setCamera(c);
			for (int i = 1; i <= CameraPreset.MAX_PRESET; ++i) {
				CameraPreset cp = CameraPresetHelper.lookup(c, i);
				if (cp != null)
					cbxm.addElement(cp);
			}
		}
		return cbxm;
	}


	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHorizontalGroup(createHorizontalGroup(gl));
		gl.setVerticalGroup(createVerticalGroup(gl));
		setLayout(gl);
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup hg = gl.createSequentialGroup();
		hg.addComponent(zoom_pnl);
		hg.addGap(UI.hgap);
		hg.addComponent(preset_cbx);
		hg.addGap(UI.hgap);
		hg.addComponent(monitor_cbx);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup vg = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		vg.addComponent(zoom_pnl);
		vg.addComponent(preset_cbx);
		vg.addComponent(monitor_cbx);
		return vg;
	}

	/** Dispose of the camera control panel */
	public void dispose() {
		removeAll();
	}

	/** Set enabled status */
	@Override
	public void setEnabled(boolean e) {
		super.setEnabled(e);
		zoom_pnl.setEnabled(e);
		preset_cbx.setEnabled(e);
		monitor_cbx.setEnabled(e);
	}
	
	/** Called when a video monitor is selected */
	private void monitorSelected() {
		VideoMonitor vm = getSelectedOutput();
		String vmn = (vm != null) ? vm.getName() : "null";
		System.out.println("Setting monitor output to " + vmn);
		watcher.setProxy(vm);
		if (vm != null) {
			Camera c = camera_ptz.getCamera();
			vm.setCamera(c);
		}
	}

	/** Get the selected video monitor from UI */
	private VideoMonitor getSelectedOutput() {
		Object o = monitor_cbx.getSelectedItem();
		return (o instanceof VideoMonitor) ? (VideoMonitor) o : null;
	}


}
