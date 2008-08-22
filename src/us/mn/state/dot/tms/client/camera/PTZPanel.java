/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2008  Minnesota Department of Transportation
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
import javax.swing.JButton;
import javax.swing.JPanel;
import us.mn.state.dot.tms.Camera;

/**
 * GUI for controlling camera PTZ
 *
 * @author Tim Johnson
 * @author Douglas Lau
 */
public class PTZPanel extends JPanel {

	/** The preferred size of the control widgets */
	static protected final Dimension SIZE = new Dimension(30, 30);

	/** The preferred insets for the control widgets */
	static protected final Insets INSETS = new Insets(0, 0, 0, 0);

	/** Font to use for pan/tilt widgets */
	static protected final Font FONT = new Font(null, Font.PLAIN, 20);

	/** Button used to pan left */
	protected final PTZButton left = new PTZButton("\u2190", "Pan left",
		-60, 0, 0);

	/** Button used to pan right */
	protected final PTZButton right = new PTZButton("\u2192", "Pan right",
		60, 0, 0);

	/** Button used to tilt up */
	protected final PTZButton up = new PTZButton("\u2191", "Tilt up",
		0, 60, 0);

	/** Button used to tilt down */
	protected final PTZButton down = new PTZButton("\u2193", "Tilt down",
		0, -60, 0);

	/** Button used to tilt up and pan left */
	protected final PTZButton up_left = new PTZButton("\u2196",
		"Pan left and up", -60, 60, 0);

	/** Button used to tilt up and pan right */
	protected final PTZButton up_right = new PTZButton("\u2197",
		"Pan right and up", 60, 60, 0);

	/** Button used to tilt down and pan left */
	protected final PTZButton down_left = new PTZButton("\u2199",
		"Pan left and down", -60, -60, 0);

	/** Button used to tilt down and pan right */
	protected final PTZButton down_right = new PTZButton("\u2198",
		"Pan right and down", 60, -60, 0);

	/** Button used to zoom in */
	protected final PTZButton zoom_in = new PTZButton("\u21BB", "Zoom in",
		0, 0, 1);

	/** Button used to zoom out */
	protected final PTZButton zoom_out = new PTZButton("\u21BA", "Zoom out",
		0, 0, -1);

	/** Button for one PTZ action */
	static protected class PTZButton extends JButton {

		protected final String name;
		protected final String description;
		protected final int pan;
		protected final int tilt;
		protected final int zoom;

		/** Create a new PTZ button */
		protected PTZButton(String n, String d, int p, int t, int z) {
			super(n);
			name = n;
			description = d;
			pan = p;
			tilt = t;
			zoom = z;
			setFont(FONT);
			setPreferredSize(SIZE);
			setMinimumSize(SIZE);
			setMargin(INSETS);
			setEnabled(false);
		}

		/** Set the camera to move */
		public void setCamera(Camera cam) {
			setAction(new CameraMoveAction(cam, name, description,
				pan, tilt, zoom));
		}
	}

	/** Create a new PTZ panel */
	public PTZPanel() {
		super(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		add(zoom_in, c);
		c.gridx = 1;
		c.gridy = 0;
		add(up_left, c);
		c.gridx = 2;
		add(up, c);
		c.gridx = 3;
		add(up_right, c);
		c.gridx = 1;
		c.gridy = 1;
		add(left, c);
		c.gridx = 3;
		add(right, c);
		c.gridx = 1;
		c.gridy = 2;
		add(down_left, c);
		c.gridx = 2;
		add(down, c);
		c.gridx = 3;
		add(down_right, c);
		c.gridx = 4;
		c.gridy = 1;
		add(zoom_out, c);
	}

	/** Set the enabled status of the PTZ panel */
	public void setEnabled(boolean enabled) {
		left.setEnabled(enabled);
		right.setEnabled(enabled);
		up.setEnabled(enabled);
		down.setEnabled(enabled);
		up_left.setEnabled(enabled);
		up_right.setEnabled(enabled);
		down_left.setEnabled(enabled);
		down_right.setEnabled(enabled);
		zoom_in.setEnabled(enabled);
		zoom_out.setEnabled(enabled);
	}

	/** Set a new camera to control */
	public void setCamera(Camera cam) {
		left.setCamera(cam);
		right.setCamera(cam);
		up.setCamera(cam);
		down.setCamera(cam);
		up_left.setCamera(cam);
		up_right.setCamera(cam);
		down_left.setCamera(cam);
		down_right.setCamera(cam);
		zoom_in.setCamera(cam);
		zoom_out.setCamera(cam);
	}
}
