/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.adectdc;

import java.io.IOException;
import us.mn.state.dot.tms.BaseHelper;
import us.mn.state.dot.tms.CommState;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to handle per vehicle messages.
 *
 * @author Douglas Lau
 */
public class OpPerVehicle extends OpTdc {

	/** Traffic data property */
	private final TrafficProperty traf = new TrafficProperty();

	/** Status faults */
	private String faults;

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
	protected Phase<TdcProperty> phaseOne() {
		return new ResetComm();
	}

	/** Phase to reset communication (FCB) */
	private class ResetComm extends Phase<TdcProperty> {

		/** Reset communication */
		protected Phase<TdcProperty> poll(CommMessage<TdcProperty> mess)
			throws IOException
		{
			ResetProperty reset = new ResetProperty();
			mess.add(reset);
			logStore(reset);
			mess.storeProps();
			setSuccess(true);
			return new VehicleEvents();
		}
	}

	/** Phase to get vehicle events */
	private class VehicleEvents extends Phase<TdcProperty> {

		/** Get vehicle events */
		protected Phase<TdcProperty> poll(CommMessage<TdcProperty> mess)
			throws IOException
		{
			mess.add(traf);
			mess.queryProps();
			logQuery(traf);
			setSuccess(true);
			String f = traf.getFaults();
			if (!BaseHelper.objectEquals(f, faults)) {
				putCtrlFaults(f, null);
				faults = f;
			}
			traf.logVehicles(controller);
			return controller.hasActiveDetector()
			      ? this
			      : null;
		}
	}

	/** Get the error retry threshold */
	@Override
	public int getRetryThreshold() {
		return controller.getRetryThreshold();
	}

	/** Update the controller operation counters */
	public void updateCounters(int p) {
		boolean s = isSuccess();
		controller.binEventData(p, s);
		controller.completeOperation(id, s);
		updateCtrlStatus();
	}
}
