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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Icons;
import us.mn.state.dot.tms.client.widget.Widgets;

/**
 * A panel containing buttons for camera utility functions.
 * It is composed of three subpanels: a focus control panel,
 * an iris control panel, and a misc. panel.
 *
 * @author Travis Swanston
 */
public class UtilPanel extends JPanel {

	/** focus control panel */
	protected final JPanel focusPanel;

	/* focus control panel buttons */
	protected JButton focusNear_btn;
	protected JButton focusFar_btn;
	protected JButton focusManual_btn;
	protected JButton focusAuto_btn;

	/** iris control panel */
	protected final JPanel irisPanel;

	/* iris control panel buttons */
	protected JButton irisClose_btn;
	protected JButton irisOpen_btn;
	protected JButton irisManual_btn;
	protected JButton irisAuto_btn;

	/** misc. ops panel */
	protected final JPanel miscPanel;

	/* misc. ops panel buttons */
	protected JButton wipeOnce_btn;
	protected JButton camReset_btn;

	/** The CameraPTZ object. */
	protected final CameraPTZ cam_ptz;

	/** button preferred size */
	protected final Dimension btn_dim;

	/** button font */
	protected final Font btn_font;

	/** label font */
	protected final Font lbl_font;

	/** Create a camera utility panel. */
	public UtilPanel(CameraPTZ cptz) {
		super(createLayout());
		cam_ptz = cptz;
		btn_dim = Widgets.UI.dimension(20, 20);
		btn_font = new Font(null, Font.PLAIN, Widgets.UI.scaled(10));
		lbl_font = new Font(null, Font.PLAIN, Widgets.UI.scaled(11));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = Widgets.UI.insets();
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.weightx = 0.5;
		gbc.weighty = 0.5;

		gbc.gridx = 0;
		gbc.gridy = 0;
		focusPanel = buildFocusPanel();
		add(focusPanel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		miscPanel = buildMiscPanel();
		add(miscPanel, gbc);

		gbc.gridheight = 1;
		gbc.gridx = 0;
		gbc.gridy = 1;
		irisPanel = buildIrisPanel();
		add(irisPanel, gbc);
	}

	/** Create the primary layout */
	static protected GridBagLayout createLayout() {
		GridBagLayout gbl = new GridBagLayout();
		return gbl;
	}

	/**
	 * Create the focus control panel.
	 * @return The focus control panel.
	 */
	protected JPanel buildFocusPanel() {
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
		focusNear_btn = createFocusBtn("camera.util.focus.near",
			DeviceRequest.CAMERA_FOCUS_NEAR);
		jp.add(focusNear_btn, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		focusFar_btn = createFocusBtn("camera.util.focus.far",
			DeviceRequest.CAMERA_FOCUS_FAR);
		jp.add(focusFar_btn, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		focusManual_btn = createMiscOpBtn("camera.util.focus.manual",
			DeviceRequest.CAMERA_FOCUS_MANUAL);
		jp.add(focusManual_btn, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		focusAuto_btn = createMiscOpBtn("camera.util.focus.auto",
			DeviceRequest.CAMERA_FOCUS_AUTO);
		jp.add(focusAuto_btn, gbc);

		return jp;
	}

	/**
	 * Create the iris control panel.
	 * @return The iris control panel.
	 */
	protected JPanel buildIrisPanel() {
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
		irisClose_btn = createIrisBtn("camera.util.iris.close",
			DeviceRequest.CAMERA_IRIS_CLOSE);
		jp.add(irisClose_btn, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		irisOpen_btn = createIrisBtn("camera.util.iris.open",
			DeviceRequest.CAMERA_IRIS_OPEN);
		jp.add(irisOpen_btn, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		irisManual_btn = createMiscOpBtn("camera.util.iris.manual",
			DeviceRequest.CAMERA_IRIS_MANUAL);
		jp.add(irisManual_btn, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		irisAuto_btn = createMiscOpBtn("camera.util.iris.auto",
			DeviceRequest.CAMERA_IRIS_AUTO);
		jp.add(irisAuto_btn, gbc);

		return jp;
	}

	/**
	 * Create the misc. ops control panel.
	 * @return The misc. ops control panel.
	 */
	protected JPanel buildMiscPanel() {
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
		wipeOnce_btn = createMiscOpBtn("camera.util.wiper.oneshot",
			DeviceRequest.CAMERA_WIPER_ONESHOT);
		jp.add(wipeOnce_btn, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		camReset_btn = createMiscOpBtn("camera.util.reset",
			DeviceRequest.RESET_DEVICE);
		jp.add(camReset_btn, gbc);

		return jp;
	}

	/** Set enabled status for UtilPanel. */
	@Override
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);
		setEnabledFocusPanel(enable);
		setEnabledIrisPanel(enable);
		setEnabledMiscPanel(enable);
	}

	/** Set enabled status for subpanel focusPanel. */
	protected void setEnabledFocusPanel(boolean enable) {
		focusNear_btn.setEnabled(enable);
		focusFar_btn.setEnabled(enable);
		focusManual_btn.setEnabled(enable);
		focusAuto_btn.setEnabled(enable);
	}

	/** Set enabled status for subpanel irisPanel. */
	protected void setEnabledIrisPanel(boolean enable) {
		irisClose_btn.setEnabled(enable);
		irisOpen_btn.setEnabled(enable);
		irisManual_btn.setEnabled(enable);
		irisAuto_btn.setEnabled(enable);
	}

	/** Set enabled status for subpanel miscPanel. */
	protected void setEnabledMiscPanel(boolean enable) {
		wipeOnce_btn.setEnabled(enable);
		camReset_btn.setEnabled(enable);
	}

	/**
	 * Create a focus-control button.
	 * @param text_id
	 * @param dr
	 * @return The requested JButton.
	 */
	protected JButton createFocusBtn(String text_id,
		final DeviceRequest dr)
	{
		final JButton btn = new JButton(new IAction(text_id) {
			@Override
			protected void doActionPerformed(ActionEvent ev) {
			}
		});

		btn.setPreferredSize(btn_dim);
		btn.setMinimumSize(btn_dim);
		btn.setFont(btn_font);
		btn.setMargin(new Insets(0, 0, 0, 0));
		ImageIcon icon = Icons.getIconByPropName(text_id);
		if (icon != null) {
			btn.setIcon(icon);
			btn.setHideActionText(true);
		}
		btn.setFocusPainted(false);
		btn.addChangeListener(new ChangeListener() {
			boolean pressedState = false;
			@Override
			public void stateChanged(ChangeEvent ce) {
				boolean nowPressed = btn.getModel().isPressed();
				if ((!pressedState) && (nowPressed)) {
					cam_ptz.sendRequest(dr);
					pressedState = true;
				}
				else if ((pressedState) && (!nowPressed)) {
					cam_ptz.sendRequest(
						DeviceRequest.CAMERA_FOCUS_STOP);
					pressedState = false;
				}
			}
		});

		return btn;
	}

	/**
	 * Create an iris-control button.
	 * @param text_id
	 * @param dr
	 * @return The requested JButton.
	 */
	protected JButton createIrisBtn(String text_id,
		final DeviceRequest dr)
	{
		final JButton btn = new JButton(new IAction(text_id) {
			@Override
			protected void doActionPerformed(ActionEvent ev) {
			}
		});

		btn.setPreferredSize(btn_dim);
		btn.setMinimumSize(btn_dim);
		btn.setFont(btn_font);
		btn.setMargin(new Insets(0, 0, 0, 0));
		ImageIcon icon = Icons.getIconByPropName(text_id);
		if (icon != null) {
			btn.setIcon(icon);
			btn.setHideActionText(true);
		}
		btn.setFocusPainted(false);
		btn.addChangeListener(new ChangeListener() {
			boolean pressedState = false;
			@Override
			public void stateChanged(ChangeEvent ce) {
				boolean nowPressed = btn.getModel().isPressed();
				if ((!pressedState) && (nowPressed)) {
					cam_ptz.sendRequest(dr);
					pressedState = true;
				}
				else if ((pressedState) && (!nowPressed)) {
					cam_ptz.sendRequest(
						DeviceRequest.CAMERA_IRIS_STOP);
					pressedState = false;
				}
			}
		});

		return btn;
	}

	/**
	 * Create a misc-op button.
	 * These buttons are for single, stateless, parameterless camera
	 * operations, such as camera reset, single-shot wiper, auto/manual
	 * focus mode selection, auto/manual iris mode selection, etc.
	 * @param text_id
	 * @param dr
	 * @return The requested JButton.
	 */
	protected JButton createMiscOpBtn(String text_id,
		final DeviceRequest dr)
	{
		final JButton btn = new JButton(new IAction(text_id) {
			@Override
			protected void doActionPerformed(ActionEvent ev) {
			}
		});

		btn.setPreferredSize(btn_dim);
		btn.setMinimumSize(btn_dim);
		btn.setFont(btn_font);
		btn.setMargin(new Insets(0, 0, 0, 0));
		ImageIcon icon = Icons.getIconByPropName(text_id);
		if (icon != null) {
			btn.setIcon(icon);
			btn.setHideActionText(true);
		}
		btn.setFocusPainted(false);
		btn.addChangeListener(new ChangeListener() {
			boolean pressedState = false;
			@Override
			public void stateChanged(ChangeEvent ce) {
				boolean nowPressed = btn.getModel().isPressed();
				if ((!pressedState) && (nowPressed)) {
					cam_ptz.sendRequest(dr);
					pressedState = true;
				}
				else if ((pressedState) && (!nowPressed)) {
					// (nothing to stop)
					pressedState = false;
				}
			}
		});

		return btn;
	}

}
