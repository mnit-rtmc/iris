/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
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
import javax.swing.JPanel;
import us.mn.state.dot.tms.DeviceRequest;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel containing miscellaneous camera buttons.
 *
 * @author Douglas Lau
 */
public class MiscPanel extends JPanel {

	/** Wiper button */
	private final JButton wiper_btn;

	/** Camera reset button */
	private final JButton reset_btn;

	/** Create a miscellaneous camera panel */
	public MiscPanel(CameraPTZ cptz) {
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
		setLayout(gl);
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup hg = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		hg.addComponent(wiper_btn);
		hg.addComponent(reset_btn);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		vg.addComponent(wiper_btn);
		vg.addGap(UI.vgap);
		vg.addComponent(reset_btn);
		return vg;
	}

	/** Set enabled status */
	@Override
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);
		wiper_btn.setEnabled(enable);
		reset_btn.setEnabled(enable);
	}
}
