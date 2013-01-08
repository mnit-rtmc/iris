/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * Mouse event handler for PTZ control.
 *
 * @author Douglas Lau
 */
public class MousePTZ extends MouseAdapter implements MouseMotionListener {

	/** Camera PTZ control */
	private final CameraPTZ cam_ptz;

	/** Size of stream panel widget */
	private final Dimension size;

	/** Left edge of dead zone */
	private final int dead_left;

	/** Right edge of dead zone */
	private final int dead_right;

	/** Upper edge of dead zone */
	private final int dead_up;

	/** Lower edge of dead zone */
	private final int dead_down;

	/** Create a new mouse PTZ handler */
	public MousePTZ(CameraPTZ cptz, Dimension sz) {
		cam_ptz = cptz;
		size = sz;
		int deadx = sz.width / 20;
		dead_left = sz.width / 2 - deadx;
		dead_right = sz.width / 2 + deadx;
		int deady = sz.height / 20;
		dead_up = sz.height / 2 - deady;
		dead_down = sz.height / 2 + deady;
	}

	/** Handle a mouse pressed event */
	public void mousePressed(MouseEvent e) {
		updatePanTilt(e);
	}

	/** Handle a moust released event */
	public void mouseReleased(MouseEvent e) {
		cancelPanTilt();
	}

	/** Handle a mouse dragged event */
	public void mouseDragged(MouseEvent e) {
		updatePanTilt(e);
	}

	/** Handle a mouse moved event */
	public void mouseMoved(MouseEvent e) { }

	/** Update the camera pan/tilt */
	private void updatePanTilt(MouseEvent e) {
		cam_ptz.sendPtz(calculatePan(e), calculateTilt(e), 0);
	}

	/** Calculate the pan value */
	private float calculatePan(MouseEvent e) {
		float x = e.getX();
		if(x < dead_left)
			return -(dead_left - x) / dead_left;
		else if(x > dead_right) {
			return (x - dead_right) /
				(size.width - dead_right);
		} else
			return 0;
	}

	/** Calculate the tilt value */
	private float calculateTilt(MouseEvent e) {
		float y = e.getY();
		if(y < dead_up)
			return (dead_up - y) / dead_up;
		else if(y > dead_down) {
			return -(y - dead_down) /
				(size.height - dead_down);
		} else
			return 0;
	}

	/** Cancel the pan/tilt action */
	private void cancelPanTilt() {
		cam_ptz.clearPtz();
	}
}
