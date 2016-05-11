/*
 * IRIS -- Intelligent Roadway Information System
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
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to send a Cohu device request Cohu.
 *
 * @author Douglas Lau
 */
public class OpDeviceReq extends OpCohuPTZ {

	/** Device request */
	private final DeviceRequest dev_req;

	/** Create device request operation */
	public OpDeviceReq(CameraImpl c, CohuPTZPoller cp, DeviceRequest dr) {
		super(PriorityLevel.COMMAND, c, cp);
		dev_req = dr;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<CohuPTZProperty> phaseTwo() {
		return new SendDeviceReq();
	}

	/** Phase to send the device request */
	protected class SendDeviceReq extends Phase<CohuPTZProperty> {
		protected Phase<CohuPTZProperty> poll(
			CommMessage<CohuPTZProperty> mess)
			throws IOException
		{
			mess.add(new DeviceReqProperty(dev_req));
			doStoreProps(mess);
			return null;
		}
	}
}
