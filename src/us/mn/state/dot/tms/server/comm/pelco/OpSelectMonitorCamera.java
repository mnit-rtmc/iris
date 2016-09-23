/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelco;

import java.io.IOException;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Pelco operation to select the camera for one monitor.
 *
 * @author Douglas Lau
 * @author Timothy Johnson
 */
public class OpSelectMonitorCamera extends OpPelco {

	/** Parse the integer ID of a monitor or camera */
	static private int parseUID(String name) throws ProtocolException {
		String id = name;
		while (id.length() > 0 && !Character.isDigit(id.charAt(0)))
			id = id.substring(1);
		try {
			return Integer.parseInt(id);
		}
		catch (NumberFormatException e) {
			throw new ProtocolException("BAD UID: " + name);
		}
	}

	/** Create a new select monitor camera operation */
	public OpSelectMonitorCamera(ControllerImpl c, VideoMonitor m,
		CameraImpl cam)
	{
		super(c, m, cam);
	}

	/** Operation equality test */
	@Override
	public boolean equals(Object o) {
		if (o instanceof OpSelectMonitorCamera) {
			OpSelectMonitorCamera op = (OpSelectMonitorCamera)o;
			return monitor == op.monitor &&
			       camera.equals(op.camera);
		} else
			return false;
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<PelcoProperty> phaseOne() {
		return new Select();
	}

	/** Phase to select the monitor and camera */
	protected class Select extends Phase<PelcoProperty> {

		/** Command controller to move the camera */
		protected Phase<PelcoProperty> poll(
			CommMessage<PelcoProperty> mess) throws IOException
		{
			mess.add(new SelectMonitorProperty(parseUID(
				monitor.getName())));
			mess.add(new SelectCameraProperty(parseUID(camera)));
			mess.storeProps();
			return null;
		}
	}
}
