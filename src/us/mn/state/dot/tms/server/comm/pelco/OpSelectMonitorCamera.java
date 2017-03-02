/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2017  Minnesota Department of Transportation
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

/**
 * Pelco operation to select the camera for one monitor.
 *
 * @author Douglas Lau
 * @author Timothy Johnson
 */
public class OpSelectMonitorCamera extends OpPelco {

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
			OpSelectMonitorCamera op = (OpSelectMonitorCamera) o;
			return monitor == op.monitor &&
			       cam_num == op.cam_num;
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
			mess.add(new SelectMonitorProperty(
				monitor.getMonNum()));
			mess.add(new SelectCameraProperty(cam_num));
			mess.storeProps();
			return null;
		}
	}
}
