/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2015  AHMCT, University of California
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cohuptz;

import java.io.IOException;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Cohu PTZ operation to initiate a camera reset.
 *
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class OpResetCamera extends OpCohuPTZ {

	/**
	 * Create the operation.
	 * @param c the CameraImpl instance
	 * @param cp the CohuPTZPoller instance
	 */
	public OpResetCamera(CameraImpl c, CohuPTZPoller cp) {
		super(PriorityLevel.COMMAND, c, cp);
	}

	/** Begin the operation */
	@Override
	protected Phase<CohuPTZProperty> phaseTwo() {
		return new ResetCamera();
	}

	/** Phase to reset the camera */
	protected class ResetCamera extends Phase<CohuPTZProperty> {
		protected Phase<CohuPTZProperty> poll(
			CommMessage<CohuPTZProperty> mess)
			throws IOException
		{
			mess.add(new ResetCameraProperty());
			doStoreProps(mess);
			return null;
		}
	}
}
