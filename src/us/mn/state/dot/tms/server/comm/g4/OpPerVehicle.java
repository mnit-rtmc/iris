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
package us.mn.state.dot.tms.server.comm.g4;

import java.io.IOException;
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to handle per vehicle messages.
 *
 * @author Douglas Lau
 */
public class OpPerVehicle extends OpG4 {

	/** Maximum number of lanes */
	static private final int MAX_LANES = 12;

	/** Create a new "per vehicle" operation */
	public OpPerVehicle(ControllerImpl c) {
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
				logGap();
				mess.logError("BAD TIMESTAMP: " +
					new Date(ev.getStamp()));
				return new StoreRTC();
			}
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
			return new VehicleEvent();
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
			controller.binEventSamples(p);
		controller.completeOperation(id, isSuccess());
	}
}
