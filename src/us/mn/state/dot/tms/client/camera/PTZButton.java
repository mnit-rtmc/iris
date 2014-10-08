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

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A button for controlling pan/tilt/zoom on a camera.
 *
 * @author Douglas Lau
 */
public class PTZButton extends JButton {

	/** Camera PTZ controller */
	private final CameraPTZ cam_ptz;

	/** Pan value */
	private final int pan;

	/** Tilt value */
	private final int tilt;

	/** Zoom value */
	private final int zoom;

	/** Pan-tilt-zoom speed */
	private float speed = 0.5f;

	/** Set the PTZ speed */
	public void setSpeed(float s) {
		speed = s;
	}

	/** Create a new PTZ button */
	public PTZButton(String text_id, final CameraPTZ cptz, int p, int t,
		int z)
	{
		super(new IAction(text_id) {
			protected void doActionPerformed(ActionEvent ev) {
				cptz.clearMovement();
			}
		});
		cam_ptz = cptz;
		pan = p;
		tilt = t;
		zoom = z;
		setMargin(UI.buttonInsets());
		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				pressed();
			}
		});
	}

	/** Respond to a PTZ button pressed event */
	private void pressed() {
		if (getModel().isPressed()) {
			cam_ptz.sendPtz(speed * pan, speed * tilt,
				speed * zoom);
		} else
			cam_ptz.clearMovement();
	}
}
