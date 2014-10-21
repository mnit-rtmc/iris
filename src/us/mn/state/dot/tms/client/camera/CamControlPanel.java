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
import javax.swing.JPanel;
import us.mn.state.dot.tms.SystemAttrEnum;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Camera control panel.
 *
 * @author Douglas Lau
 */
public class CamControlPanel extends JPanel {

	/** PTZ panel enabled flag */
	private final boolean ptz;

	/** Util panel enabled flag */
	private final boolean util;

	/** Preset panel enabled flag */
	private final boolean preset;

	/** Pan-tilt panel */
	private final PanTiltPanel pt_pnl;

	/** PTZ panel */
	private final PTZPanel ptz_pnl;

	/** Panel for lens control */
	private final LensPanel lens_pnl;

	/** Panel for camera utilities */
	private final UtilPanel util_pnl;

	/** Panel for camera presets */
	private final PresetPanel preset_pnl;

	/** Panel for camera presets */
	private final PresetPanel2 preset2_pnl;

	/** Create a new camera control panel */
	public CamControlPanel(CameraPTZ cam_ptz) {
		ptz = SystemAttrEnum.CAMERA_PTZ_PANEL_ENABLE.getBoolean();
		util = SystemAttrEnum.CAMERA_UTIL_PANEL_ENABLE.getBoolean();
		preset = SystemAttrEnum.CAMERA_PRESET_PANEL_ENABLE.getBoolean();
		pt_pnl = new PanTiltPanel(cam_ptz);
		ptz_pnl = new PTZPanel(cam_ptz);
		lens_pnl = new LensPanel(cam_ptz);
		util_pnl = new UtilPanel(cam_ptz);
		preset_pnl = new PresetPanel(cam_ptz);
		preset2_pnl = new PresetPanel2(cam_ptz);
		layoutPanel();
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
		if (ptz)
			hg.addComponent(ptz_pnl);
		else
			hg.addComponent(pt_pnl);
		hg.addGap(UI.hgap);
		if (util)
			hg.addComponent(util_pnl);
		else
			hg.addComponent(lens_pnl);
		if (preset)
			hg.addComponent(preset_pnl);
		else
			hg.addComponent(preset2_pnl);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup vg = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		if (ptz)
			vg.addComponent(ptz_pnl);
		else
			vg.addComponent(pt_pnl);
		if (util)
			vg.addComponent(util_pnl);
		else
			vg.addComponent(lens_pnl);
		if (preset)
			vg.addComponent(preset_pnl);
		else
			vg.addComponent(preset2_pnl);
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
		ptz_pnl.setEnabled(e);
		pt_pnl.setEnabled(e);
		lens_pnl.setEnabled(e);
		util_pnl.setEnabled(e);
		preset_pnl.setEnabled(e);
		preset2_pnl.setEnabled(e);
	}
}
