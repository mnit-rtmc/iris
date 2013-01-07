/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2013  Minnesota Department of Transportation
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

import javax.swing.JComponent;
import us.mn.state.dot.tms.Camera;

/**
 * A video stream displays a video on a swing component.
 *
 * @author Douglas Lau
 * @author Timothy Johnson
 */
public interface VideoStream {

	/** Get a component for displaying the video stream */
	JComponent getComponent();

	/** Get the status of the stream */
	String getStatus();

	/** Test if the video is playing */
	boolean isPlaying();

	/** Dispose of the video stream */
	void dispose();
}
