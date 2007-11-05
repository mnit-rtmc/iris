/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2006  Minnesota Department of Transportation
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

import java.rmi.RemoteException;
import javax.swing.Action;
import us.mn.state.dot.tms.client.device.TrafficDeviceAction;

/**
 * Sends a PTZ command to a camera.
 *
 * @author Douglas Lau
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class CameraMoveAction extends TrafficDeviceAction {

	/** The direction (and speed) to pan the camera */
	protected final int pan;

	/** The direction (and speed) to tilt the camera */
	protected final int tilt;

	/** The direction to zoom the camera */
	protected final int zoom;

	/** Create a new action to move the camera. */
	public CameraMoveAction(CameraProxy c, String name, String description,
		int p, int t, int z)
	{
		super(c);
		putValue(Action.NAME, name);
		putValue(Action.SHORT_DESCRIPTION, description);
		putValue(Action.LONG_DESCRIPTION, description);
		pan = p;
		tilt = t;
		zoom = z;
	}
	
	/** Actually perform the action */
	protected void do_perform() throws RemoteException {
		CameraProxy p = (CameraProxy)proxy;
		p.camera.move(pan, tilt, zoom);
	}
}
