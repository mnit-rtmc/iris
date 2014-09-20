/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
 * Copyright (C) 2008-2014 AHMCT, University of California
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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Icons;
import us.mn.state.dot.tms.client.widget.Widgets;

/**
 * This class creates a Swing panel for controlling camera pan, tilt, and zoom.
 *
 * @author Stephen Donecker
 * @author Michael Darter
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class PTZPanel extends JPanel {

	/** Create a flow layout */
	static private FlowLayout createLayout() {
		FlowLayout fl = new FlowLayout();
		fl.setHgap(Widgets.UI.scaled(10));
		return fl;
	}

	/** Button used to pan left */
	private final JButton left_btn;

	/** Button used to pan right */
	private final JButton right_btn;

	/** Button used to tilt up */
	private final JButton up_btn;

	/** Button used to tilt down */
	private final JButton down_btn;

	/** Button used to zoom in */
	private final JButton zoom_in_btn;

	/** Button used to zoom out */
	private final JButton zoom_out_btn;

	/** Slider used to select the pan-tilt-zoom speed */
	private final JSlider speed_sldr;

	/** Camera PTZ control */
	private final CameraPTZ cam_ptz;

	/** button preferred size */
	protected final Dimension btn_dim;

	/** button font */
	protected final Font btn_font;

	/** Pan-tilt-zoom speed */
	private float m_speed = 0.5f;

	/** Create a new PTZ panel */
	public PTZPanel(CameraPTZ cptz) {
		super(createLayout()); // ignores preferred sizes
		cam_ptz = cptz;
		btn_dim = Widgets.UI.dimension(24, 24);
		btn_font = new Font(null, Font.PLAIN, Widgets.UI.scaled(12));
		speed_sldr = createSpeedSlider();
		left_btn = createPtzButton("camera.ptz.left", -1, 0, 0);
		right_btn = createPtzButton("camera.ptz.right", 1, 0, 0);
		up_btn = createPtzButton("camera.ptz.up", 0, 1, 0);
		down_btn = createPtzButton("camera.ptz.down", 0, -1, 0);
		zoom_in_btn = createPtzButton("camera.ptz.zoom.in", 0, 0, 1);
		zoom_out_btn = createPtzButton("camera.ptz.zoom.out", 0, 0, -1);
		add(buildSpeedSliderPanel());
		add(buildPanTiltPanel());
		add(buildZoomPanel());
	}

	/** Create PTZ speed slider */
	private JSlider createSpeedSlider() {
		Dimension sz = Widgets.dimension(40, 80);
		JSlider s = new JSlider(SwingConstants.VERTICAL);
		s.setMajorTickSpacing(10);
		s.setPaintTicks(true);
		s.setSnapToTicks(true);
		s.setToolTipText(I18N.get("camera.ptz.speed.tooltip"));
		s.setPreferredSize(sz);
		s.setMinimumSize(sz);
		return s;
	}

	/** Build speed slider panel */
	private JPanel buildSpeedSliderPanel() {
		JPanel jp = new JPanel(new FlowLayout());
		jp.add(speed_sldr);
		speed_sldr.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				if(!speed_sldr.getValueIsAdjusting())
					m_speed = speed_sldr.getValue() / 100f;
			}
		});
		return jp;
	}

	/** Create a PTZ button */
	private JButton createPtzButton(String text_id, final int pan,
		final int tilt, final int zoom)
	{
		final JButton btn = new JButton(new IAction(text_id) {
			protected void doActionPerformed(ActionEvent ev) {
				cam_ptz.clearMovement();
			}
		});
		btn.setPreferredSize(btn_dim);
		btn.setMinimumSize(btn_dim);
		btn.setFont(btn_font);
		btn.setMargin(new Insets(0, 0, 0, 0));
		btn.setFocusPainted(false);
		ImageIcon icon = Icons.getIconByPropName(text_id);
		if (icon != null) {
			btn.setIcon(icon);
			btn.setHideActionText(true);
		}
		btn.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				buttonPressed(btn, pan, tilt, zoom);
			}
		});
		return btn;
	}

	/** Respond to a PTZ button pressed event */
	private void buttonPressed(JButton btn, int pan, int tilt, int zoom) {
		if(btn.getModel().isPressed()) {
			cam_ptz.sendPtz(m_speed * pan, m_speed * tilt,
				m_speed * zoom);
		}
		else {
			cam_ptz.clearMovement();
		}
	}

	/** Build panel with pan and tilt buttons */
	private JPanel buildPanTiltPanel() {
		JPanel p = new JPanel(new GridBagLayout());
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
		gbc.gridx = 1;
		gbc.gridy = 0;
		p.add(up_btn, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		p.add(left_btn, gbc);
		gbc.gridx = 2;
		gbc.gridy = 1;
		p.add(right_btn, gbc);
		gbc.gridx = 1;
		gbc.gridy = 2;
		p.add(down_btn, gbc);
		return p;
	}

	/** Build panel with zoom buttons */
	private JPanel buildZoomPanel() {
		JPanel p = new JPanel(new GridBagLayout());
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
		p.add(zoom_in_btn, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		p.add(zoom_out_btn, gbc);
		return p;
	}

	/** Set the camera control enable status */
	public void setEnabled(boolean enable) {
		speed_sldr.setEnabled(enable);
		left_btn.setEnabled(enable);
		right_btn.setEnabled(enable);
		up_btn.setEnabled(enable);
		down_btn.setEnabled(enable);
		zoom_in_btn.setEnabled(enable);
		zoom_out_btn.setEnabled(enable);
	}

}
