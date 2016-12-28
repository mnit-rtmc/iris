/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2016  Minnesota Department of Transportation
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

	/** Binary detection request */
	private final BinaryDetectionProperty detection =
		new BinaryDetectionProperty();

	/** Create a new operation to query detector event samples */
	public OpQueryEventSamples(ControllerImpl c) {
		super(PriorityLevel.DATA_5_MIN, c);
		setSuccess(false);
	}

	/** Handle a communication error */
	@Override
	public void handleCommError(EventType et, String msg) {
		setSuccess(false);
		super.handleCommError(et, msg);
	}

	/** Get the error retry threshold */
	@Override
	public int getRetryThreshold() {
		if (controller.hasActiveDetector())
			return Integer.MAX_VALUE;
		else
			return super.getRetryThreshold();
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<CanogaProperty> phaseOne() {
		return new QueryCurrentEvents();
	}

	/** Phase to query the current detection events */
	protected class QueryCurrentEvents extends Phase<CanogaProperty> {

		/** Query the current detection events */
		protected Phase<CanogaProperty> poll(
			CommMessage<CanogaProperty> mess) throws IOException
		{
			mess.add(detection);
			mess.queryProps();
			setSuccess(true);
			detection.logEvents(controller);
			if (controller.hasActiveDetector())
				return this;
			else
				return null;
		}
	}

	/** Store event data samples as binned data */
	public void binSamples() {
		controller.binEventSamples();
	}

	/** Update the controller operation counters */
	public void updateCounters() {
		controller.completeOperation(id, isSuccess());
	}
}
