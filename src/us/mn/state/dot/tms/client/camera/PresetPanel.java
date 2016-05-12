/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.BeaconHelper;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.CameraPresetHelper;
import static us.mn.state.dot.tms.CameraPreset.MAX_PRESET;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.client.widget.Icons;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A panel containing controls for recalling and storing camera presets.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class PresetPanel extends JPanel {

	/** Get tooltip text for the preset buttons.
	 * @param state Store button state. */
	static private String presetButtonTooltip(int state) {
		return (state == ItemEvent.SELECTED)
		      ? I18N.get("camera.preset.store.tooltip")
		      : I18N.get("camera.preset.recall.tooltip");
	}

	/** Get tooltip text for the store button.
	 * @param state Store button state. */
	static private String storeButtonTooltip(int state) {
		return (state == ItemEvent.SELECTED)
		      ? I18N.get("camera.preset.store.active.tooltip")
		      : I18N.get("camera.preset.store.inactive.tooltip");
	}

	/** Camera PTZ */
	private final CameraPTZ cam_ptz;

	/** Array of buttons used to select presets */
	private final JButton[] preset_btn = new JButton[MAX_PRESET];

	/** Button used to store presets */
	private final JToggleButton store_btn;

	/** Create a preset panel */
	public PresetPanel(CameraPTZ cptz) {
		cam_ptz = cptz;
		for (int i = 0; i < preset_btn.length; i++)
			preset_btn[i] = createPresetButton(i + 1);
		store_btn = createStoreButton();
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
		gl.linkSize(preset_btn[0], preset_btn[1], preset_btn[2],
		            preset_btn[3]);
		gl.linkSize(preset_btn[4], preset_btn[5], preset_btn[6],
		            preset_btn[7]);
		gl.linkSize(preset_btn[8], preset_btn[9], preset_btn[10],
		            preset_btn[11]);
		setLayout(gl);
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup hg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup g0 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		g0.addComponent(preset_btn[0]);
		g0.addComponent(preset_btn[1]);
		g0.addComponent(preset_btn[2]);
		g0.addComponent(preset_btn[3]);
		hg.addGroup(g0);
		hg.addGap(UI.hgap);
		GroupLayout.ParallelGroup g1 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		g1.addComponent(preset_btn[4]);
		g1.addComponent(preset_btn[5]);
		g1.addComponent(preset_btn[6]);
		g1.addComponent(preset_btn[7]);
		hg.addGroup(g1);
		hg.addGap(UI.hgap);
		GroupLayout.ParallelGroup g2 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		g2.addComponent(preset_btn[8]);
		g2.addComponent(preset_btn[9]);
		g2.addComponent(preset_btn[10]);
		g2.addComponent(preset_btn[11]);
		hg.addGroup(g2);
		hg.addGap(UI.hgap);
		hg.addComponent(store_btn);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup g0 = gl.createParallelGroup();
		g0.addComponent(preset_btn[0]);
		g0.addComponent(preset_btn[4]);
		g0.addComponent(preset_btn[8]);
		vg.addGroup(g0);
		vg.addGap(UI.vgap);
		GroupLayout.ParallelGroup g1 = gl.createParallelGroup();
		g1.addComponent(preset_btn[1]);
		g1.addComponent(preset_btn[5]);
		g1.addComponent(preset_btn[9]);
		g1.addComponent(store_btn);
		vg.addGroup(g1);
		vg.addGap(UI.vgap);
		GroupLayout.ParallelGroup g2 = gl.createParallelGroup();
		g2.addComponent(preset_btn[2]);
		g2.addComponent(preset_btn[6]);
		g2.addComponent(preset_btn[10]);
		vg.addGroup(g2);
		vg.addGap(UI.vgap);
		GroupLayout.ParallelGroup g3 = gl.createParallelGroup();
		g3.addComponent(preset_btn[3]);
		g3.addComponent(preset_btn[7]);
		g3.addComponent(preset_btn[11]);
		vg.addGroup(g3);
		return vg;
	}

	/** Create a preset button */
	private JButton createPresetButton(final int num) {
		JButton btn = new JButton(new IAction("camera.preset") {
			protected void doActionPerformed(ActionEvent e) {
				handlePresetBtnPress(num);
			}
		});
		btn.setMargin(UI.buttonInsets());
		btn.setText(Integer.toString(num));
		btn.setToolTipText(presetButtonTooltip(ItemEvent.DESELECTED));
		return btn;
	}

	/** Handle a preset button press */
	private void handlePresetBtnPress(int num) {
		if (store_btn.isSelected()) {
			cam_ptz.storePreset(num);
			store_btn.setSelected(false);
		} else
			cam_ptz.recallPreset(num);
	}

	/** Create the store button */
	private JToggleButton createStoreButton() {
		JToggleButton btn = new JToggleButton();
		ImageIcon icon = Icons.getIcon("camera_preset_store_inactive");
		ImageIcon iconSel = Icons.getIcon("camera_preset_store_active");
		if (icon != null && iconSel != null) {
			btn.setIcon(icon);
			btn.setSelectedIcon(iconSel);
		} else
			btn.setText(I18N.get("camera.preset.store"));
		btn.setMargin(UI.buttonInsets());
		btn.setToolTipText(storeButtonTooltip(ItemEvent.DESELECTED));
		btn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				toggleStoreButton(ie);
			}
		});
		return btn;
	}

	/** Toggle the store button state */
	private void toggleStoreButton(ItemEvent ie) {
		int state = ie.getStateChange();
		String tt = presetButtonTooltip(state);
		for (JButton btn: preset_btn)
			btn.setToolTipText(tt);
		store_btn.setToolTipText(storeButtonTooltip(state));
	}

	/** Set enabled status */
	@Override
	public void setEnabled(boolean e) {
		super.setEnabled(e);
		store_btn.setEnabled(false);
		store_btn.setSelected(false);
		updatePresetButtons(cam_ptz.getCamera());
		for (JButton btn: preset_btn)
			btn.setEnabled(e && cam_ptz.canRecallPreset());
		store_btn.setEnabled(e && cam_ptz.canStorePreset());
	}

	/** Update preset button text */
	private void updatePresetButtons(Camera c) {
		for (int i = 0; i < preset_btn.length; i++)
			updatePresetButton(preset_btn[i], c, i + 1);
	}

	/** Update preset button text */
	private void updatePresetButton(JButton btn, Camera c, int pn) {
		btn.setText(presetText(c, pn));
	}

	/** Get preset button text */
	private String presetText(Camera c, int pn) {
		if (c != null) {
			CameraPreset cp = CameraPresetHelper.lookup(c, pn);
			if (cp != null)
				return presetText(cp);
		}
		return "<html><font color=#aaaaaa>" + Integer.toString(pn) +
			"</html>";
	}

	/** Get preset button text */
	private String presetText(CameraPreset cp) {
		Device d = devicePreset(cp);
		if (d != null)
			return d.getName();
		Direction dir = Direction.fromOrdinal(cp.getDirection());
		if (dir != Direction.UNKNOWN)
			return dir.det_dir;
		else
			return Integer.toString(cp.getPresetNum());
	}

	/** Get a device associated with a preset */
	private Device devicePreset(CameraPreset cp) {
		Device d = beaconPreset(cp);
		if (d != null)
			return d;
		d = dmsPreset(cp);
		if (d != null)
			return d;
		d = meterPreset(cp);
		if (d != null)
			return d;
		return null;
	}

	/** Get a beacon associated with a preset */
	private Device beaconPreset(CameraPreset cp) {
		Iterator<Beacon> it = BeaconHelper.iterator();
		while (it.hasNext()) {
			Beacon b = it.next();
			if (b.getPreset() == cp)
				return b;
		}
		return null;
	}

	/** Get a DMS associated with a preset */
	private Device dmsPreset(CameraPreset cp) {
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS d = it.next();
			if (d.getPreset() == cp)
				return d;
		}
		return null;
	}

	/** Get a ramp meter associated with a preset */
	private Device meterPreset(CameraPreset cp) {
		Iterator<RampMeter> it = RampMeterHelper.iterator();
		while (it.hasNext()) {
			RampMeter m = it.next();
			if (m.getPreset() == cp)
				return m;
		}
		return null;
	}
}
