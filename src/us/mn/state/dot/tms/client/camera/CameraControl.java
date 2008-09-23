/*
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
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Insets;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.ButtonModel;
import javax.swing.JSlider;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import us.mn.state.dot.tms.Camera;

/**
 * This class creates a Swing panel for controlling camera pan, tilt, and zoom.
 *
 * @author Stephen Donecker
 * @company University of California, Davis
 * @created June 26, 2008
 */
public class CameraControl extends JPanel implements ChangeListener, ActionListener {

	/** Button used to pan left */
	protected final PTZButton m_panLeft = new PTZButton("\u2190", "Pan left", new int[] {-1, 0, 0});

	/** Button used to pan right */
	protected final PTZButton m_panRight = new PTZButton("\u2192", "Pan right", new int[] {1, 0, 0});

	/** Button used to tilt up */
	protected final PTZButton m_tiltUp = new PTZButton("\u2191", "Tilt up", new int[] {0, 1, 0});

	/** Button used to tilt down */
	protected final PTZButton m_tiltDown = new PTZButton("\u2193", "Tilt down", new int[] {0, -1, 0});

	/** Button used to pan left and tilt up */
	protected final PTZButton m_panLeftTiltUp = new PTZButton("\u2196", "Pan left and tilt up", new int[] {-1, 1, 0});

	/** Button used to pan right and tilt up */
	protected final PTZButton m_panRightTiltUp = new PTZButton("\u2197", "Pan right and tilt up", new int[] {1, 1, 0});

	/** Button used to pan left and tilt down */
	protected final PTZButton m_panLeftTiltDown = new PTZButton("\u2199", "Pan left and tilt down", new int[] {-1, -1, 0});

	/** Button used to pan right and tilt down */
	protected final PTZButton m_panRightTiltDown = new PTZButton("\u2198", "Pan right and tilt down", new int[] {1, -1, 0});

	/** Button used to zoom in */
	protected final PTZButton m_zoomIn = new PTZButton("+", "Zoom in", new int[] {0, 0, 1});

	/** Button used to zoom out */
	protected final PTZButton m_zoomOut = new PTZButton("-", "Zoom out", new int[] {0, 0, -1});

	/** Button used to go to preset location 1 */
	private final PresetButton m_preset1 = new PresetButton(1, "Preset 1");

	/** Button used to go to preset location 2 */
	private final PresetButton m_preset2 = new PresetButton(2, "Preset 2");
	
	/** Button used to go to preset location 3 */
	private final PresetButton m_preset3 = new PresetButton(3, "Preset 3");

	/** Button used to go to preset location 4 */
	private final PresetButton m_preset4 = new PresetButton(4, "Preset 4");

	/** Button used to go to preset location 5 */
	private final PresetButton m_preset5 = new PresetButton(5, "Preset 5");

	/** The preferred size of the slider */
	static protected final Dimension SLIDER_SIZE = new Dimension(150, 30);

	/** The slider used to select the pan-tilt-zoom speed */
	private final JSlider m_ptzSpeed = new JSlider();
	
	/** Pan-tilt-zoom speed */
	private float m_speed = (float) 0.5;

	/** The camera proxy for the current camera to control */
	private Camera m_cameraProxy = null;

	/** Constructor */
	public CameraControl() {
		
		// add controls 
		super(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		
		//c.gridx = 0;
		//c.gridy = 0;
		//add(m_panLeftTiltUp, c);
		//c.gridx = 2;
		//c.gridy = 0;
		//add(m_panRightTiltUp, c);
		//c.gridx = 0;
		//c.gridy = 2;
		//add(m_panLeftTiltDown, c);
		//c.gridx = 2;
		//c.gridy = 2;
		//add(m_panRightTiltDown, c);
		
		c.gridx = 1;
		c.gridy = 0;
		add(m_tiltUp, c);
		c.gridx = 0;
		c.gridy = 1;
		add(m_panLeft, c);
		c.gridx = 2;
		c.gridy = 1;
		add(m_panRight, c);
		c.gridx = 1;
		c.gridy = 2;
		add(m_tiltDown, c);
		c.gridx = 4;
		c.gridy = 1;
		add(m_zoomIn, c);
		c.gridx = 4;
		c.gridy = 2;
		add(m_zoomOut, c);
	
		c.gridwidth = 5;
		c.insets = new Insets(15,0,0,0);
		c.gridx = 0;
		c.gridy = 3;
		add(m_ptzSpeed, c);
		
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 4;
		add(m_preset1, c);
		c.gridx = 1;
		c.gridy = 4;
		add(m_preset2, c);
		c.gridx = 2;
		c.gridy = 4;
		add(m_preset3, c);
		c.gridx = 3;
		c.gridy = 4;
		add(m_preset4, c);
		c.gridx = 4;
		c.gridy = 4;
		add(m_preset5, c);

		// configure slider
		m_ptzSpeed.setMajorTickSpacing(10);
		m_ptzSpeed.setPaintTicks(true);
		m_ptzSpeed.setSnapToTicks(true);
		m_ptzSpeed.setToolTipText("Speed");
		m_ptzSpeed.setMinimumSize(SLIDER_SIZE);
		m_ptzSpeed.setPreferredSize(SLIDER_SIZE);

		// add change listeners for button down event
		m_panLeft.addChangeListener(this);
		m_panRight.addChangeListener(this);
		m_tiltUp.addChangeListener(this);
		m_tiltDown.addChangeListener(this);
		m_panLeftTiltUp.addChangeListener(this);
		m_panRightTiltUp.addChangeListener(this);
		m_panLeftTiltDown.addChangeListener(this);
		m_panRightTiltDown.addChangeListener(this);
		m_zoomIn.addChangeListener(this);
		m_zoomOut.addChangeListener(this);

		// add action listeners for button up event
		m_panLeft.addActionListener(this);
		m_panRight.addActionListener(this);
		m_tiltUp.addActionListener(this);
		m_tiltDown.addActionListener(this);
		m_panLeftTiltUp.addActionListener(this);
		m_panRightTiltUp.addActionListener(this);
		m_panLeftTiltDown.addActionListener(this);
		m_panRightTiltDown.addActionListener(this);
		m_zoomIn.addActionListener(this);
		m_zoomOut.addActionListener(this);
		m_preset1.addActionListener(this);
		m_preset2.addActionListener(this);
		m_preset3.addActionListener(this);
		m_preset4.addActionListener(this);
		m_preset5.addActionListener(this);

		// add change listener for speed slider
		m_ptzSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				if (!m_ptzSpeed.getValueIsAdjusting()) {
					m_speed = (float) m_ptzSpeed.getValue()/100;
				}
			}
		});

	}

	/** Process the button change event (button pressed) */
	public void stateChanged(ChangeEvent ce) {
		PTZButton button = (PTZButton) ce.getSource();
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
			m_cameraProxy.setGoToPreset(pbutton.getPreset());
		} else
			m_cameraProxy.setPtz(new Float[] { 0f, 0f, 0f } );
	}
	
	/** Set the camera to control */
	public void setCamera(Camera proxy) {

		// check preconditions
		assert proxy != null : "CameraControl.setCamera: The camera proxy is null";

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
		m_preset1.setEnabled(enable);
		m_preset2.setEnabled(enable);
		m_preset3.setEnabled(enable);
		m_preset4.setEnabled(enable);
		m_preset5.setEnabled(enable);
	}
}
