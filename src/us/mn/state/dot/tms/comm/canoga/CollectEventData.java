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
package us.mn.state.dot.tms.comm.canoga;

import java.io.IOException;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.ControllerOperation;
import us.mn.state.dot.tms.comm.SerialIOException;

/**
 * Controller operation to collect vehicle event data
 *
 * @author Douglas Lau
 */
public class CollectEventData extends ControllerOperation {

	/** Minimum time before volume LSB can wrap */
	static protected final int VOL_COUNT_WRAP = 4 * 60 * 1000;

	/** Binary detection request */
	protected final BinaryDetectionRequest detection =
		new BinaryDetectionRequest();

	/** Create a new operation to collect detector event data */
	public CollectEventData(ControllerImpl c) {
		super(DATA_5_MIN, c, c.toString());
	}

	/** Handle an exception */
	public void handleException(IOException e) {
		success = false;
		controller.logException(id, e.getMessage());
		if(!controller.hasActiveDetector())
			phase = null;
		if(e instanceof SerialIOException)
			controller.retry(id);
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
