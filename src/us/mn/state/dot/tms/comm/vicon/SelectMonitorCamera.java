/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.vicon;

import java.io.IOException;
import us.mn.state.dot.tms.CameraImpl;
import us.mn.state.dot.tms.VideoMonitorImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;

/**
 * Vicon operation to select the camera for one monitor.
 *
 * @author Douglas Lau
 */
public class SelectMonitorCamera extends ViconOperation {

	/** Create a new select monitor camera operation */
	public SelectMonitorCamera(VideoMonitorImpl m, CameraImpl c) {
		super(COMMAND, m, c);
	}

	/** Begin the operation */
	public Phase phaseOne() {
		return new Select();
	}

	/** Phase to select the monitor and camera */
	protected class Select extends Phase {

		/** Command controller to move the camera */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new SelectMonitorRequest(monitor.getUID()));
			mess.add(new SelectCameraRequest(camera.getUID()));
			mess.setRequest();
			return null;
		}
	}
}
