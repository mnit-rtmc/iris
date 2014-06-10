/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
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
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Cohu PTZ operation to initiate a focus movement.
 *
 * @author Travis Swanston
 */
public class OpMoveFocus extends OpCohuPTZ {

	protected final DeviceRequest devReq;

	/**
	 * Create the operation.
	 * @param c the CameraImpl instance
	 * @param cp the CohuPTZPoller instance
	 * @param dr the DeviceRequest representing the desired op
	 */
	public OpMoveFocus(CameraImpl c, CohuPTZPoller cp, DeviceRequest dr) {
		super(PriorityLevel.COMMAND, c, cp);
		devReq = dr;
	}

	/** Begin the operation. */
	@Override
	protected Phase phaseTwo() {
		return new MoveFocus();
	}

	/** Main phase. */
	protected class MoveFocus extends Phase {
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(new MoveFocusProperty(devReq));
			doStoreProps(mess);
			return null;
		}
	}

}
