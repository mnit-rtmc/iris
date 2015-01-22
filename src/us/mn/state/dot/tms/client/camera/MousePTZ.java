/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2014  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.Timer;
import us.mn.state.dot.tms.client.widget.Icons;

/**
 * Mouse event handler for PTZ control.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class MousePTZ {

	/** Milliseconds between zoom ticks */
	static private final int ZOOM_TICK = 250;

	/** Mouse cursor for "pt up" */
	static private final Cursor pt_up = Icons.getCursor("pt_up", 12, 1);

	/** Mouse cursor for "pt down" */
	static private final Cursor pt_down = Icons.getCursor("pt_down", 12,23);

	/** Mouse cursor for "pt left" */
	static private final Cursor pt_left = Icons.getCursor("pt_left", 1, 12);

	/** Mouse cursor for "pt right" */
	static private final Cursor pt_right = Icons.getCursor("pt_right", 23,
		12);

	/** Mouse cursor for "pt left up" */
	static private final Cursor pt_left_up = Icons.getCursor("pt_left_up",
		4, 4);

	/** Mouse cursor for "pt left down" */
	static private final Cursor pt_left_down = Icons.getCursor(
		"pt_left_down", 4, 20);

	/** Mouse cursor for "pt right up" */
	static private final Cursor pt_right_up =Icons.getCursor("pt_right_up",
		20, 4);

	/** Mouse cursor for "pt right down" */
	static private final Cursor pt_right_down = Icons.getCursor(
		"pt_right_down", 20, 20);

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

	/** Pan value from last update */
	private float pan = 0;

	/** Tilt value from last update */
	private float tilt = 0;

	/** Zoom value from last update */
	private float zoom = 0;

	/** Count of wheel ticks */
	private int n_zoom = 0;

	/** Timer listener for zoom timer */
	private class ZoomTimer implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			float z = zoom;
			if(n_zoom > 0)
				n_zoom--;
			else if(n_zoom < 0)
				n_zoom++;
			zoom = calculateZoom();
			if(z != zoom)
				cam_ptz.sendPtz(pan, tilt, zoom);
		}
	};

	/** Zoom timer */
	private final Timer timer = new Timer(ZOOM_TICK, new ZoomTimer());

	/** Create a new mouse PTZ handler */
	public MousePTZ(CameraPTZ cptz, Dimension sz, Component c) {
		cam_ptz = cptz;
		size = sz;
		int deadx = sz.width / 12;
		dead_left = sz.width / 2 - deadx;
		dead_right = sz.width / 2 + deadx;
		int deady = sz.height / 12;
		dead_up = sz.height / 2 - deady;
		dead_down = sz.height / 2 + deady;
		setComponent(c);
		timer.start();
	}

	/** Set the component for mouse events */
	private void setComponent(final Component c) {
		MouseAdapter mouser = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				cancelPanTilt();
			}
			@Override
			public void mousePressed(MouseEvent e) {
				updatePanTilt(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				cancelPanTilt();
			}
			@Override
			public void mouseExited(MouseEvent e) {
				cancelPanTilt();
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				updatePanTilt(e);
				c.setCursor(getCursor(e));
			}
			@Override
			public void mouseMoved(MouseEvent e) {
				c.setCursor(getCursor(e));
			}
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				n_zoom -= e.getWheelRotation();
				zoom = calculateZoom();
				cam_ptz.sendPtz(pan, tilt, zoom);
			}
		};
		c.addMouseListener(mouser);
		c.addMouseMotionListener(mouser);
		c.addMouseWheelListener(mouser);
	}

	/** Dispose of the mouse PTZ handler */
	public void dispose() {
		timer.stop();
	}

	/** Get the appropriate cursor */
	private Cursor getCursor(MouseEvent e) {
		if ((!cam_ptz.isControlEnabled())
			|| (!cam_ptz.isCameraSelected()))
		{
			return Cursor.getPredefinedCursor(
				Cursor.DEFAULT_CURSOR);
		}
		int x = e.getX();
		int y = e.getY();
		if(x < dead_left) {
			if(y < dead_up)
				return pt_left_up;
			else if(y > dead_down)
				return pt_left_down;
			else
				return pt_left;
		} else if(x > dead_right) {
			if(y < dead_up)
				return pt_right_up;
			else if(y > dead_down)
				return pt_right_down;
			else
				return pt_right;
		} else {
			if(y < dead_up)
				return pt_up;
			else if(y > dead_down)
				return pt_down;
			else {
				return Cursor.getPredefinedCursor(
					Cursor.MOVE_CURSOR);
			}
		}
	}

	/** Update the camera pan/tilt */
	private void updatePanTilt(MouseEvent e) {
		pan = calculatePan(e);
		tilt = calculateTilt(e);
		cam_ptz.sendPtz(pan, tilt, zoom);
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
		pan = 0;
		tilt = 0;
		cam_ptz.sendPtz(pan, tilt, zoom);
	}

	/** Calculate the zoom value */
	private float calculateZoom() {
		if(n_zoom > 0)
			return 1;
		else if(n_zoom < 0)
			return -1;
		else
			return 0;
	}
}
