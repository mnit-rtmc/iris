/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.g4;

import java.io.IOException;
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.CommState;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to handle per vehicle messages.
 *
 * @author Douglas Lau
 */
public class OpPerVehicle extends OpG4 {

	/** Create a new "per vehicle" operation */
	public OpPerVehicle(ControllerImpl c) {
		super(PriorityLevel.IDLE, c);
		setSuccess(false);
	}

	/** Handle a communication state */
	@Override
	public void handleCommState(CommState cs) {
		setSuccess(false);
		super.handleCommState(cs);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<G4Property> phaseOne() {
		return new VehicleEvent();
	}

	/** Phase to get a vehicle event */
	private class VehicleEvent extends Phase<G4Property> {

		/** Get a vehicle event */
		protected Phase<G4Property> poll(CommMessage<G4Property> mess)
			throws IOException
		{
			VehicleEventProperty ev = new VehicleEventProperty();
			mess.add(ev);
			mess.queryProps();
			setSuccess(true);
			if (ev.isValidStamp()) {
				ev.logVehicle(controller);
				return controller.hasActiveDetector()
				      ? this
				      : null;
			} else {
				controller.logGap();
				mess.logError("BAD TIMESTAMP: " +
					new Date(ev.getStamp()));
				return new StoreRTC();
			}
		}
	}

	/** Phase to set the clock */
	private class StoreRTC extends Phase<G4Property> {

		/** Set the clock */
		protected Phase<G4Property> poll(
			CommMessage<G4Property> mess) throws IOException
		{
			RTCProperty rtc = new RTCProperty();
			rtc.setStamp(TimeSteward.currentTimeMillis());
			mess.add(rtc);
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
		boolean s = isSuccess();
		if (!s)
			controller.logGap();
		controller.binEventData(p, s);
		controller.completeOperation(id, s);
	}
}
