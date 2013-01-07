/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2013  Minnesota Department of Transportation
 * Copyright (C) 2008-2010 AHMCT, University of California
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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Widgets;

/**
 * This class creates a Swing panel for controlling camera pan, tilt, and zoom.
 *
 * @author Stephen Donecker
 * @author Michael Darter
 * @author Douglas Lau
 */
public class CameraControl extends JPanel {

	/** Number of buttons used to go to preset location */
	static private final int NUMBER_PRESET_BUTTONS =
		SystemAttrEnum.CAMERA_NUM_PRESET_BTNS.getInt();

	/** PTZ array to stop movement */
	static private final Float[] PTZ_STOP = new Float[] { 0f, 0f, 0f };

	/** The preferred insets for buttons */
	static private final Insets INSETS = new Insets(0, 0, 0, 0);

	/** Button used to pan left */
	private final JButton left_btn;

	/** Button used to pan right */
	private final JButton right_btn;

	/** Button used to tilt up */
	private final JButton up_btn;

	/** Button used to tilt down */
	private final JButton down_btn;

	/** Button used to pan left and tilt up */
	private final JButton up_left_btn;

	/** Button used to pan right and tilt up */
	private final JButton up_right_btn;

	/** Button used to pan left and tilt down */
	private final JButton down_left_btn;

	/** Button used to pan right and tilt down */
	private final JButton down_right_btn;

	/** Button used to zoom in */
	private final JButton zoom_in_btn;

	/** Button used to zoom out */
	private final JButton zoom_out_btn;

	/** Array of buttons used to go to preset locations */
	private final JButton[] preset_btn =
		new JButton[NUMBER_PRESET_BUTTONS];

	/** Slider used to select the pan-tilt-zoom speed */
	private final JSlider speed_sldr;

	/** Pan-tilt-zoom speed */
	private float m_speed = 0.5f;

	/** The camera proxy for the current camera to control */
	private Camera camera = null;

	/** Create a new camera control panel */
	public CameraControl() {
		super(new FlowLayout()); // ignores preferred sizes
		((FlowLayout)getLayout()).setHgap(24);
		speed_sldr = createSpeedSlider();
		left_btn = createPtzButton("camera.ptz.left", -1, 0, 0);
		right_btn = createPtzButton("camera.ptz.right", 1, 0, 0);
		up_btn = createPtzButton("camera.ptz.up", 0, 1, 0);
		down_btn = createPtzButton("camera.ptz.down", 0, -1, 0);
		up_left_btn = createPtzButton("camera.ptz.up.left", -1, 1, 0);
		up_right_btn = createPtzButton("camera.ptz.up.right", 1, 1, 0);
		down_left_btn = createPtzButton("camera.ptz.down.left",-1,-1,0);
		down_right_btn = createPtzButton("camera.ptz.down.right", 1,
			-1, 0);
		zoom_in_btn = createPtzButton("camera.ptz.zoom.in", 0, 0, 1);
		zoom_out_btn = createPtzButton("camera.ptz.zoom.out", 0, 0, -1);
		add(buildSpeedSliderPanel());
		add(buildPanTiltPanel());
		add(buildZoomPanel());
		add(buildPresetPanel());
	}

	/** Create PTZ speed slider */
	private JSlider createSpeedSlider() {
		Dimension sz = Widgets.dimension(40, 110);
		JSlider s = new JSlider(SwingConstants.VERTICAL);
		s.setMajorTickSpacing(10);
		s.setPaintTicks(true);
		s.setSnapToTicks(true);
		s.setToolTipText(I18N.get("camera.ptz.speed.tooltip"));
		s.setMinimumSize(sz);
		s.setPreferredSize(sz);
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

	/** Initialize a button */
	private void initButton(JButton btn) {
		Font f = btn.getFont();
		btn.setFont(f.deriveFont(2f * f.getSize2D()));
		int sz = Math.round(3f * f.getSize2D());
		Dimension size = new Dimension(sz, sz);
		btn.setPreferredSize(size);
		btn.setMinimumSize(size);
		btn.setMargin(INSETS);
	}

	/** Create a PTZ button */
	private JButton createPtzButton(String text_id, final int pan,
		final int tilt, final int zoom)
	{
		final JButton btn = new JButton(new IAction(text_id) {
			protected void do_perform() {
				Camera c = camera;
				if(c != null)
					c.setPtz(PTZ_STOP);
			}
		});
		initButton(btn);
		btn.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				buttonPressed(btn, pan, tilt, zoom);
			}
		});
		return btn;
	}

	/** Respond to a PTZ button pressed event */
	private void buttonPressed(JButton btn, int pan, int tilt, int zoom) {
		Camera c = camera;
		if(c != null && btn.getModel().isPressed()) {
			Float[] ptz = new Float[] {
				m_speed * pan,
				m_speed * tilt,
				m_speed * zoom
			};
			c.setPtz(ptz);
		}
	}

	/** Build panel with pan and tilt buttons */
	private JPanel buildPanTiltPanel() {
		JPanel jp = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 2;
		c.gridy = 0;
		jp.add(up_btn, c);
		c.gridx = 1;
		c.gridy = 1;
		jp.add(left_btn, c);
		c.gridx = 3;
		c.gridy = 1;
		jp.add(right_btn, c);
		c.gridx = 2;
		c.gridy = 2;
		jp.add(down_btn, c);
		return jp;
	}

	/** Build panel with zoom buttons */
	private JPanel buildZoomPanel() {
		JPanel jp = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 4;
		c.gridy = 0;
		jp.add(zoom_in_btn, c);
		c.gridx = 4;
		c.gridy = 1;
		jp.add(zoom_out_btn, c);
		return jp;
	}

	/** Build panel with preset buttons */
	private JPanel buildPresetPanel() {
		JPanel jp = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 1;
		c.gridheight = 1;
		final int PRESET_BUTTONS_PER_ROW = 5;
		for(int i = 0; i < NUMBER_PRESET_BUTTONS; i++) {
			preset_btn[i] = createPresetButton(i + 1);
			c.gridx = 5 + i % PRESET_BUTTONS_PER_ROW;
			c.gridy = 0 + i / PRESET_BUTTONS_PER_ROW;
			jp.add(preset_btn[i], c);
		}
		return jp;
	}

	/** Create a preset button */
	private JButton createPresetButton(final int num) {
		JButton btn = new JButton(new IAction("camera.preset") {
			protected void do_perform() {
				Camera c = camera;
				if(c != null)
					c.setRecallPreset(num);
			}
		});
		btn.setText(Integer.toString(num));
		initButton(btn);
		return btn;
	}

	/** Set the camera to control */
	public void setCamera(Camera proxy) {
		camera = proxy;
	}

	/** Set the camera control enable status */
	public void setEnabled(boolean enable) {
		speed_sldr.setEnabled(enable);
		left_btn.setEnabled(enable);
		right_btn.setEnabled(enable);
		up_btn.setEnabled(enable);
		down_btn.setEnabled(enable);
		up_left_btn.setEnabled(enable);
		up_right_btn.setEnabled(enable);
		down_left_btn.setEnabled(enable);
		down_right_btn.setEnabled(enable);
		zoom_in_btn.setEnabled(enable);
		zoom_out_btn.setEnabled(enable);
		for(JButton b: preset_btn)
			b.setEnabled(enable);
	}
}
