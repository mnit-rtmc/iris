/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.vicon;

import java.io.IOException;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;

/**
 * Vicon operation to select the camera for one monitor.
 *
 * @author Douglas Lau
 */
public class SelectMonitorCamera extends ViconOperation {

	/** Parse the integer ID of a monitor or camera */
	static protected int parseUID(String name) throws IOException {
		String id = name;
		while(id.length() > 0 && !Character.isDigit(id.charAt(0)))
			id = id.substring(1);
		try {
			return Integer.parseInt(id);
		}
		catch(NumberFormatException e) {
			throw new IOException("Invalid UID: " + name);
		}
	}

	/** Create a new select monitor camera operation */
	public SelectMonitorCamera(ControllerImpl c, VideoMonitor m,
		String cam)
	{
		super(COMMAND, c, m, cam);
	}

	/** Begin the operation */
	public Phase phaseOne() {
		return new Select();
	}

	/** Phase to select the monitor and camera */
	protected class Select extends Phase {

		/** Command controller to move the camera */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new SelectMonitorRequest(parseUID(
				monitor.getName())));
			mess.add(new SelectCameraRequest(parseUID(camera)));
			mess.setRequest();
			return null;
		}
	}
}
