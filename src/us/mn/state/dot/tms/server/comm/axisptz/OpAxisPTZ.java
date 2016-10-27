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
package us.mn.state.dot.tms.server.comm.axisptz;

import java.io.IOException;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Axis VAPIX PTZ operation.
 *
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class OpAxisPTZ extends OpDevice<AxisProp> {

	/** Axis property */
	private final AxisProp prop;

	/** Create a new operation.
	 *
	 * @param c CameraImpl instance.
	 * @param p Axis property.
	 */
	protected OpAxisPTZ(CameraImpl c, AxisProp p) {
		super(PriorityLevel.COMMAND, c);
		prop = p;
	}

	/** Create the second phase of the operation */
	protected Phase<AxisProp> phaseTwo() {
		return new SendProp();
	}

	/** Send property */
	private class SendProp extends Phase<AxisProp> {
		protected Phase<AxisProp> poll(CommMessage<AxisProp> mess)
			throws IOException
		{
			mess.add(prop);
			mess.storeProps();
			return null;
		}
	}
}
