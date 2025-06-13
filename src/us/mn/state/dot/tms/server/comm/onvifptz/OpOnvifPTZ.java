/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2015  AHMCT, University of California
 * Copyright (C) 2016-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.onvifptz;

import java.io.IOException;
import java.util.Objects;

import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * ONVIF PTZ operation.
 *
 * @author Ethan Beauclaire
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class OpOnvifPTZ extends OpDevice<OnvifProp> {

	/** Logger method */
	private void log(String s) {
		OnvifPTZPoller.slog("OpOnvifPTZ:" + s);
	}

	/** ONVIF property */
	private final OnvifProp prop;
	private final OnvifPTZPoller poller;

	/** Create a new ONVIF operation */
	protected OpOnvifPTZ(CameraImpl c, OnvifProp p) {
		super(PriorityLevel.COMMAND, c);
		prop = p;
		poller = (OnvifPTZPoller) c.getPoller();
	}

	/** Create the second phase of the operation */
	protected Phase<OnvifProp> phaseTwo() {
		return new SendProp();
	}

	/** Override to queue ONVIF ops while previous ones on a camera finish */
	@Override
	public boolean equals(Object o) {
		if ((!(o instanceof OpOnvifPTZ)) || getClass() != o.getClass())
			return false;

		OpOnvifPTZ op = (OpOnvifPTZ) o;
		return o == this || (
			   op.device == device &&
			   Objects.equals(op.prop, prop)
			);
	}

	/** Send property */
	private class SendProp extends Phase<OnvifProp> {
		protected Phase<OnvifProp> poll(CommMessage<OnvifProp> mess)
			throws IOException
		{
			String resp = prop.sendSoap(poller);
			log("Sent soap, response:\n" + resp);
			return null;
		}
	}
}
