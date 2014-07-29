/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.widget.Icons;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A panel containing controls for recalling and storing camera presets.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class PresetPanel extends JPanel {

	/** User session */
	private final Session session;

	/** Tooltip for "store" button when store mode is inactive */
	static protected final String TOOLTIP_STORE_INACTIVE
		= I18N.get("camera.preset.store.inactive.tooltip");

	/** Tooltip for "store" button when store mode is active */
	static protected final String TOOLTIP_STORE_ACTIVE
		= I18N.get("camera.preset.store.active.tooltip");

	/** Number of preset buttons to display in preset grid */
	static protected final int NUM_PRESET_BTNS =
		SystemAttrEnum.CAMERA_NUM_PRESET_BTNS.getInt();

	/** Number of columns to use in the preset grid */
	static protected final int PRESET_GRID_COLUMNS =
		SystemAttrEnum.CAMERA_PRESET_PANEL_COLUMNS.getInt();

	/** Array of buttons used to select presets */
	protected final JButton[] preset_btn = new JButton[NUM_PRESET_BTNS];

	/** Button used to store presets */
	protected final JToggleButton store_btn;

	/** Selected camera */
	protected Camera camera = null;

	/** Button preferred size */
	protected final Dimension btn_dim;

	/** Button font */
	protected final Font btn_font;

	/** Create a preset panel */
	public PresetPanel(Session s) {
		super(new GridBagLayout());
		session = s;
		btn_dim = Widgets.UI.dimension(24, 24);
		btn_font = new Font(Font.SANS_SERIF, Font.BOLD,
			Widgets.UI.scaled(10));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = Widgets.UI.insets();
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.weightx = 0.5;
		gbc.weighty = 0.5;

		gbc.gridx = 0;
		gbc.gridy = 0;
		add(buildGridPanel(), gbc);

		store_btn = createStoreButton();
		if (SystemAttrEnum.CAMERA_PRESET_STORE_ENABLE.getBoolean()) {
			gbc.gridx = 1;
			gbc.gridy = 0;
			add(store_btn, gbc);
		}
	}

	/**
	 * Create the preset array panel.
	 * @return The preset array panel.
	 */
	protected JPanel buildGridPanel() {
		GridBagLayout gbl = new GridBagLayout();
		JPanel jp = new JPanel(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		for (int i=0; i < NUM_PRESET_BTNS; i++) {
			preset_btn[i] = createPresetButton(i + 1);
			jp.add(preset_btn[i], gbc);
			if (++gbc.gridx % PRESET_GRID_COLUMNS == 0) {
				gbc.gridx=0;
				gbc.gridy++;
			}
		}
		return jp;
	}

	/** Create a preset button */
	protected JButton createPresetButton(final int num) {
		JButton btn = new JButton(new IAction("camera.preset") {
			protected void doActionPerformed(ActionEvent e) {
				handlePresetBtnPress(num);
			}
		});
		btn.setPreferredSize(btn_dim);
		btn.setMinimumSize(btn_dim);
		btn.setFont(btn_font);
		btn.setMargin(new Insets(0, 0, 0, 0));
		btn.setText(Integer.toString(num));
		btn.setFocusPainted(false);
		return btn;
	}

	protected void handlePresetBtnPress(int num) {
		if (camera == null) return;
		if (store_btn.isSelected()) {
			camera.setStorePreset(num);
			store_btn.setSelected(false);
		}
		else {
			camera.setRecallPreset(num);
		}
	}

	/** Create the store button */
	protected JToggleButton createStoreButton() {
		JToggleButton btn = new JToggleButton();
		btn.setPreferredSize(btn_dim);
		btn.setMinimumSize(btn_dim);
		btn.setFont(btn_font);
		btn.setMargin(new Insets(0, 0, 0, 0));
		ImageIcon icon = Icons.getIcon("camera_preset_store_inactive");
		ImageIcon iconSel = Icons.getIcon("camera_preset_store_active");
		if ((icon != null) && (iconSel != null)) {
			btn.setIcon(icon);
			btn.setSelectedIcon(iconSel);
		}
		else {
			btn.setText("ST");
		}
		btn.setToolTipText(TOOLTIP_STORE_INACTIVE);
		btn.setFocusPainted(false);
		btn.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				int state = ie.getStateChange();
				if (state == ItemEvent.SELECTED) {
					store_btn.setToolTipText(
						TOOLTIP_STORE_ACTIVE);
				} else {
					store_btn.setToolTipText(
						TOOLTIP_STORE_INACTIVE);
				}
			}
		});
		return btn;
	}

	/** Set the camera */
	public void setCamera(Camera c) {
		camera = c;
		updateStoreButtonStatus();
	}

	/**
	 * Update enabled status of store button based on current panel
	 * status, camera selection, and user permissions.
	 */
	protected void updateStoreButtonStatus() {
		if ( (this.isEnabled())
			&& (camera != null)
			&& (session.isUpdatePermitted(camera,
			"storePreset")) )
				store_btn.setEnabled(true);
			else
				store_btn.setEnabled(false);
	}

	/** Set enabled status for PresetPanel and its components. */
	public void setEnabled(boolean enable) {
		for(JButton b: preset_btn)
			b.setEnabled(enable);
		super.setEnabled(enable);
		updateStoreButtonStatus();
	}

}
