/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

import java.io.IOException;
import java.util.Date;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to get event data from a SS125 device
 *
 * @author Douglas Lau
 */
public class OpQueryEvents extends OpSS125 {

	/** Maximum number of lanes */
	static private final int MAX_LANES = 10;

	/** Create a new "query events" operation */
	public OpQueryEvents(ControllerImpl c) {
		super(PriorityLevel.DATA_30_SEC, c);
		setSuccess(false);
	}

	/** Handle a communication error */
	@Override
	public void handleCommError(EventType et, String msg) {
		setSuccess(false);
		super.handleCommError(et, msg);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<SS125Property> phaseOne() {
		return new GetActiveEvent();
	}

	/** Phase to get the active event */
	private class GetActiveEvent extends Phase<SS125Property> {

		/** Get the active event data */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			ActiveEventProperty ev = new ActiveEventProperty();
			mess.add(ev);
			mess.queryProps();
			setSuccess(true);
			if (ev.isValidEvent()) {
				if (ev.isValidStamp())
					ev.logVehicle(controller);
				else {
					logGap();
					mess.logError("BAD TIMESTAMP: " +
						new Date(ev.getTime()));
					return new SendDateTime();
				}
			}
			return controller.hasActiveDetector()
			      ? this
			      : null;
		}
	}

	/** Log a vehicle detection gap */
	private void logGap() {
		for (int i = 0; i < MAX_LANES; i++) {
			DetectorImpl det = controller.getDetectorAtPin(i + 1);
			if (det != null)
				det.logGap(0);
		}
	}

	/** Phase to send the date and time */
	private class SendDateTime extends Phase<SS125Property> {

		/** Send the date and time */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			DateTimeProperty date_time = new DateTimeProperty();
			mess.add(date_time);
			mess.storeProps();
			return new ClearEvents();
		}
	}

	/** Phase to clear the event FIFO */
	private class ClearEvents extends Phase<SS125Property> {

		/** Clear the event FIFO */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			ClearEventsProperty clear = new ClearEventsProperty();
			mess.add(clear);
			mess.storeProps();
			return phaseOne();
		}
	}

	/** Get the error retry threshold */
	@Override
	public int getRetryThreshold() {
		return Integer.MAX_VALUE;
	}

	/** Update the controller operation counters */
	public void updateCounters(int p) {
		if (isSuccess())
			controller.binEventData(p);
		controller.completeOperation(id, isSuccess());
	}
}
