/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.canoga;

import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.event.EventType;

/**
 * Controller operation to collect vehicle event data
 *
 * @author Douglas Lau
 */
public class CollectEventData extends OpController {

	/** Minimum time before volume LSB can wrap */
	static protected final int VOL_COUNT_WRAP = 4 * 60 * 1000;

	/** Binary detection request */
	protected final BinaryDetectionRequest detection =
		new BinaryDetectionRequest();

	/** Create a new operation to collect detector event data */
	public CollectEventData(ControllerImpl c) {
		super(DATA_5_MIN, c, c.toString());
	}

	/** Handle a communication error */
	public void handleCommError(EventType et, String msg) {
		COMM_LOG.log(id + " " + et + ", " + msg);
		success = false;
		controller.logCommEvent(et, id, filterMessage(msg));
		if(!controller.hasActiveDetector())
			phase = null;
		switch(et) {
		case CHECKSUM_ERROR:
		case PARSING_ERROR:
			retry();
		}
		if(controller.getFailMillis() > VOL_COUNT_WRAP)
			phase = null;
	}

	/** Begin the sensor initialization operation */
	public void begin() {
		phase = new QueryCurrentEvents();
	}

	/** Phase to query the current detection events */
	protected class QueryCurrentEvents extends Phase {

		/** Synchronize the clock */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(detection);
			mess.getRequest();
			success = true;
			detection.logEvents(controller);
			if(controller.hasActiveDetector())
				return this;
			else
				return null;
		}
	}
}
