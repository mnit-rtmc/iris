/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Controller operation to query vehicle event samples
 *
 * @author Douglas Lau
 */
public class OpQueryEventSamples extends OpCanoga {

	/** Minimum time before volume LSB can wrap */
	static protected final int VOL_COUNT_WRAP = 4 * 60 * 1000;

	/** Binary detection request */
	protected final BinaryDetectionProperty detection =
		new BinaryDetectionProperty();

	/** Create a new operation to query detector event samples */
	public OpQueryEventSamples(ControllerImpl c) {
		super(PriorityLevel.DATA_5_MIN, c);
	}

	/** Handle a communication error */
	public void handleCommError(EventType et, String msg) {
		COMM_LOG.log(id + " " + et + ", " + msg);
		success = false;
		controller.logCommEvent(et, id, filterMessage(msg));
		if(!controller.hasActiveDetector())
			setFailed();
		switch(et) {
		case CHECKSUM_ERROR:
		case PARSING_ERROR:
			retry();
		}
		if(controller.getFailMillis() > VOL_COUNT_WRAP)
			setFailed();
	}

	/** Create the first phase of the operation */
	protected Phase phaseOne() {
		return new QueryCurrentEvents();
	}

	/** Phase to query the current detection events */
	protected class QueryCurrentEvents extends Phase {

		/** Query the current detection events */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(detection);
			mess.queryProps();
			if(CANOGA_LOG.isOpen())
				detection.debug(CANOGA_LOG, id);
			success = true;
			detection.logEvents(controller);
			if(controller.hasActiveDetector())
				return this;
			else
				return null;
		}
	}

	/** Cleanup the operation.  For this operation, cleanup gets called
	 * every 30 seconds even though the operation continues. */
	public void cleanup() {
		controller.binEventSamples();
		super.cleanup();
	}
}
