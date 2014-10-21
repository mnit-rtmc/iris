/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.DeviceRequest;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A panel for camera lens control.
 *
 * @author Douglas Lau
 */
public class LensPanel extends JPanel {

	/** Zoom label */
	private final JLabel zoom_lbl = new JLabel(I18N.get(
		"camera.lens.zoom"));

	/** Button used to zoom in */
	private final JButton zoom_in_btn;

	/** Button used to zoom out */
	private final JButton zoom_out_btn;

	/** Focus label */
	private final JLabel focus_lbl = new JLabel(I18N.get(
		"camera.lens.focus"));

	/** Focus near button */
	private final JButton focus_near_btn;

	/** Focus far button */
	private final JButton focus_far_btn;

	/** Iris label */
	private final JLabel iris_lbl = new JLabel(I18N.get(
		"camera.lens.iris"));

	/** Iris open button */
	private final JButton iris_open_btn;

	/** Iris close button */
	private final JButton iris_close_btn;

	/** Blank label */
	private final JLabel blank_lbl = new JLabel();

	/** Wiper button */
	private final JButton wiper_btn;

	/** Camera reset button */
	private final JButton reset_btn;

	/** Camera PTZ */
	private final CameraPTZ cam_ptz;

	/** Create a new camera lens panel */
	public LensPanel(CameraPTZ cptz) {
		cam_ptz = cptz;
		zoom_in_btn = new PTZButton("camera.lens.zoom.in", cptz,
			0, 0, 1);
		zoom_out_btn = new PTZButton("camera.lens.zoom.out", cptz,
			0, 0, -1);
		focus_near_btn = new DeviceReqButton("camera.lens.focus.near",
			cptz, DeviceRequest.CAMERA_FOCUS_NEAR,
			DeviceRequest.CAMERA_FOCUS_STOP);
		focus_far_btn = new DeviceReqButton("camera.lens.focus.far",
			cptz, DeviceRequest.CAMERA_FOCUS_FAR,
			DeviceRequest.CAMERA_FOCUS_STOP);
		iris_open_btn = new DeviceReqButton("camera.lens.iris.open",
			cptz, DeviceRequest.CAMERA_IRIS_OPEN,
			DeviceRequest.CAMERA_IRIS_STOP);
		iris_close_btn = new DeviceReqButton("camera.lens.iris.close",
			cptz, DeviceRequest.CAMERA_IRIS_CLOSE,
			DeviceRequest.CAMERA_IRIS_STOP);
		wiper_btn = new DeviceReqButton("camera.util.wiper.oneshot",
			cptz, DeviceRequest.CAMERA_WIPER_ONESHOT);
		reset_btn = new DeviceReqButton("camera.util.reset", cptz,
			DeviceRequest.RESET_DEVICE);
		layoutPanel();
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(true);
		gl.setHorizontalGroup(createHorizontalGroup(gl));
		gl.setVerticalGroup(createVerticalGroup(gl));
		gl.linkSize(zoom_in_btn, zoom_out_btn,
		            focus_near_btn, focus_far_btn,
		            iris_open_btn, iris_close_btn,
		            wiper_btn, reset_btn);
		setLayout(gl);
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup hg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup g0 = gl.createParallelGroup(
			GroupLayout.Alignment.TRAILING);
		g0.addComponent(zoom_lbl);
		g0.addComponent(focus_lbl);
		g0.addComponent(iris_lbl);
		g0.addComponent(blank_lbl);
		hg.addGroup(g0);
		hg.addGap(UI.hgap);
		GroupLayout.ParallelGroup g1 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		g1.addComponent(zoom_in_btn);
		g1.addComponent(focus_near_btn);
		g1.addComponent(iris_open_btn);
		g1.addComponent(wiper_btn);
		hg.addGroup(g1);
		hg.addGap(UI.hgap);
		GroupLayout.ParallelGroup g2 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		g2.addComponent(zoom_out_btn);
		g2.addComponent(focus_far_btn);
		g2.addComponent(iris_close_btn);
		g2.addComponent(reset_btn);
		hg.addGroup(g2);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup g0 = gl.createParallelGroup(
			GroupLayout.Alignment.BASELINE);
		g0.addComponent(zoom_lbl);
		g0.addComponent(zoom_in_btn);
		g0.addComponent(zoom_out_btn);
		vg.addGroup(g0);
		vg.addGap(UI.vgap);
		GroupLayout.ParallelGroup g1 = gl.createParallelGroup(
			GroupLayout.Alignment.BASELINE);
		g1.addComponent(focus_lbl);
		g1.addComponent(focus_near_btn);
		g1.addComponent(focus_far_btn);
		vg.addGroup(g1);
		vg.addGap(UI.vgap);
		GroupLayout.ParallelGroup g2 = gl.createParallelGroup(
			GroupLayout.Alignment.BASELINE);
		g2.addComponent(iris_lbl);
		g2.addComponent(iris_open_btn);
		g2.addComponent(iris_close_btn);
		vg.addGroup(g2);
		vg.addGap(UI.vgap);
		GroupLayout.ParallelGroup g3 = gl.createParallelGroup(
			GroupLayout.Alignment.BASELINE);
		g3.addComponent(blank_lbl);
		g3.addComponent(wiper_btn);
		g3.addComponent(reset_btn);
		vg.addGroup(g3);
		return vg;
	}

	/** Set enabled status */
	@Override
	public void setEnabled(boolean e) {
		super.setEnabled(e);
		zoom_lbl.setEnabled(e);
		zoom_in_btn.setEnabled(e);
		zoom_out_btn.setEnabled(e);
		focus_lbl.setEnabled(e);
		focus_near_btn.setEnabled(e);
		focus_far_btn.setEnabled(e);
		iris_lbl.setEnabled(e);
		iris_open_btn.setEnabled(e);
		iris_close_btn.setEnabled(e);
		wiper_btn.setEnabled(e);
		reset_btn.setEnabled(e);
	}
}
