/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Insets;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.ButtonModel;
import javax.swing.JSlider;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * This class creates a Swing panel for controlling camera pan, tilt, and zoom.
 *
 * @author Stephen Donecker
 * @author Michael Darter
 */
public class CameraControl extends JPanel implements ChangeListener,
	ActionListener
{
	/** Number of buttons used to go to preset location */
	static private final int NUMBER_PRESET_BUTTONS =
		SystemAttrEnum.CAMERA_NUM_PRESET_BTNS.getInt();

	/** The preferred size of the slider */
	static private final Dimension SLIDER_SIZE = new Dimension(40, 110);

	/** Button used to pan left */
	protected final PTZButton m_panLeft =
		new PTZButton("\u2190", "Pan left", new int[] {-1, 0, 0});

	/** Button used to pan right */
	protected final PTZButton m_panRight =
		new PTZButton("\u2192", "Pan right", new int[] {1, 0, 0});

	/** Button used to tilt up */
	protected final PTZButton m_tiltUp =
		new PTZButton("\u2191", "Tilt up", new int[] {0, 1, 0});

	/** Button used to tilt down */
	protected final PTZButton m_tiltDown =
		new PTZButton("\u2193", "Tilt down", new int[] {0, -1, 0});

	/** Button used to pan left and tilt up */
	protected final PTZButton m_panLeftTiltUp = new PTZButton("\u2196",
		"Pan left and tilt up", new int[] {-1, 1, 0});

	/** Button used to pan right and tilt up */
	protected final PTZButton m_panRightTiltUp = new PTZButton("\u2197",
		"Pan right and tilt up", new int[] {1, 1, 0});

	/** Button used to pan left and tilt down */
	protected final PTZButton m_panLeftTiltDown = new PTZButton("\u2199",
		"Pan left and tilt down", new int[] {-1, -1, 0});

	/** Button used to pan right and tilt down */
	protected final PTZButton m_panRightTiltDown = new PTZButton("\u2198",
		"Pan right and tilt down", new int[] {1, -1, 0});

	/** Button used to zoom in */
	protected final PTZButton m_zoomIn = new PTZButton("+", "Zoom in",
		new int[] {0, 0, 1});

	/** Button used to zoom out */
	protected final PTZButton m_zoomOut = new PTZButton("-", "Zoom out",
		new int[] {0, 0, -1});

	/** Array of buttons used to go to preset locations */
	private final PresetButton[] m_preset =
		new PresetButton[NUMBER_PRESET_BUTTONS];

	/** The slider used to select the pan-tilt-zoom speed */
	private final JSlider m_ptzSpeed = 
		new JSlider(SwingConstants.VERTICAL);

	/** Pan-tilt-zoom speed */
	private float m_speed = 0.5f;

	/** The camera proxy for the current camera to control */
	private Camera m_cameraProxy = null;

	/** Constructor */
	public CameraControl() {
		super(new FlowLayout()); // ignores preferred sizes
		((FlowLayout)getLayout()).setHgap(24);
		add(buildSpeedSliderPanel());
		add(buildPanTiltPanel());
		add(buildZoomPanel());
		add(buildPresetPanel());
	}

	/** Build speed slider panel */
	private JPanel buildSpeedSliderPanel() {
		// configure
		m_ptzSpeed.setMajorTickSpacing(10);
		m_ptzSpeed.setPaintTicks(true);
		m_ptzSpeed.setSnapToTicks(true);
		m_ptzSpeed.setToolTipText("Speed");
		m_ptzSpeed.setMinimumSize(SLIDER_SIZE);
		m_ptzSpeed.setPreferredSize(SLIDER_SIZE);

		// build panel
		JPanel jp = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 1;
		c.gridheight = 3;
		c.gridx = 0;
		c.gridy = 0;
		jp.add(m_ptzSpeed, c);

		// add change listener for speed slider
		m_ptzSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				if(!m_ptzSpeed.getValueIsAdjusting())
					m_speed = m_ptzSpeed.getValue() / 100f;
			}
		});

		return jp;
	}

	/** Build panel with pan and tilt buttons */
	private JPanel buildPanTiltPanel() {
		// configure
		final Dimension zbs = new Dimension(30, 30);
		m_tiltUp.setPreferredSize(zbs);
		m_tiltUp.setMinimumSize(zbs);
		m_tiltDown.setPreferredSize(zbs);
		m_tiltDown.setMinimumSize(zbs);
		m_panLeft.setPreferredSize(zbs);
		m_panLeft.setMinimumSize(zbs);
		m_panRight.setPreferredSize(zbs);
		m_panRight.setMinimumSize(zbs);

		// add change listeners for button down event
		m_panLeft.addChangeListener(this);
		m_panRight.addChangeListener(this);
		m_tiltUp.addChangeListener(this);
		m_tiltDown.addChangeListener(this);
		m_panLeftTiltUp.addChangeListener(this);
		m_panRightTiltUp.addChangeListener(this);
		m_panLeftTiltDown.addChangeListener(this);
		m_panRightTiltDown.addChangeListener(this);

		// add action listeners for button up event
		m_panLeft.addActionListener(this);
		m_panRight.addActionListener(this);
		m_tiltUp.addActionListener(this);
		m_tiltDown.addActionListener(this);
		m_panLeftTiltUp.addActionListener(this);
		m_panRightTiltUp.addActionListener(this);
		m_panLeftTiltDown.addActionListener(this);
		m_panRightTiltDown.addActionListener(this);

		// build panel
		JPanel jp = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 2;
		c.gridy = 0;
		jp.add(m_tiltUp, c);
		c.gridx = 1;
		c.gridy = 1;
		jp.add(m_panLeft, c);
		c.gridx = 3;
		c.gridy = 1;
		jp.add(m_panRight, c);
		c.gridx = 2;
		c.gridy = 2;
		jp.add(m_tiltDown, c);
		return jp;
	}

	/** Build panel with zoom buttons */
	private JPanel buildZoomPanel() {
		// configure
		final Dimension zbs = new Dimension(30, 30);
		m_zoomIn.setPreferredSize(zbs);
		m_zoomIn.setMinimumSize(zbs);
		m_zoomOut.setPreferredSize(zbs);
		m_zoomOut.setMinimumSize(zbs);

		// add change listeners for button down event
		m_zoomIn.addChangeListener(this);
		m_zoomOut.addChangeListener(this);
		// add action listeners for button up event
		m_zoomIn.addActionListener(this);
		m_zoomOut.addActionListener(this);

		// build panel
		JPanel jp = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 4;
		c.gridy = 0;
		jp.add(m_zoomIn, c);
		c.gridx = 4;
		c.gridy = 1;
		jp.add(m_zoomOut, c);
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
			m_preset[i] = new PresetButton(i + 1,
				new String("Preset " + (i + 1)));
			c.gridx = 5 + i % PRESET_BUTTONS_PER_ROW;
			c.gridy = 0 + i / PRESET_BUTTONS_PER_ROW;
			jp.add(m_preset[i], c);
		}

		// add action listeners for button up event
		for(PresetButton b: m_preset)
			b.addActionListener(this);

		return jp;
	}

	/** Process the button change event (button pressed) */
	public void stateChanged(ChangeEvent ce) {
		PTZButton button = (PTZButton)ce.getSource();
		ButtonModel model = button.getModel();
		if(model.isPressed()) {
			Float[] ptz = new Float[] {
				m_speed * button.getUnitVector()[0],
				m_speed * button.getUnitVector()[1],
				m_speed * button.getUnitVector()[2]
			};
			m_cameraProxy.setPtz(ptz);
		}
	}

	/** Process the button action event (button released) */
	public void actionPerformed(ActionEvent ae) {
		JButton button = (JButton) ae.getSource();
		if(button instanceof PresetButton) {
			PresetButton pbutton = (PresetButton)button;
			m_cameraProxy.setRecallPreset(pbutton.getPreset());
		} else
			m_cameraProxy.setPtz(new Float[] { 0f, 0f, 0f } );
	}

	/** Set the camera to control */
	public void setCamera(Camera proxy) {
		assert proxy != null;
		m_cameraProxy = proxy;
	}

	/** Set the camera control enable status */
	public void setEnabled(boolean enable) {
		m_panLeft.setEnabled(enable);
		m_panRight.setEnabled(enable);
		m_tiltUp.setEnabled(enable);
		m_tiltDown.setEnabled(enable);
		m_panLeftTiltUp.setEnabled(enable);
		m_panRightTiltUp.setEnabled(enable);
		m_panLeftTiltDown.setEnabled(enable);
		m_panRightTiltDown.setEnabled(enable);
		m_zoomIn.setEnabled(enable);
		m_zoomOut.setEnabled(enable);
		m_ptzSpeed.setEnabled(enable);
		for(PresetButton b: m_preset)
			b.setEnabled(enable);
	}
}
