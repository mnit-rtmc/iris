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
package us.mn.state.dot.tms.server.comm.natch;

import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.AlarmImpl;
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.AlarmPoller;
import us.mn.state.dot.tms.server.comm.BasePoller;
import us.mn.state.dot.tms.server.comm.BeaconPoller;
import us.mn.state.dot.tms.server.comm.LCSPoller;
import us.mn.state.dot.tms.server.comm.MeterPoller;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.SamplePoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * NatchPoller is a poller for the Natch protocol.
 *
 * @author Douglas Lau
 */
public class NatchPoller extends BasePoller implements AlarmPoller,
	BeaconPoller, LCSPoller, MeterPoller, SamplePoller
{
	/** Counter for message IDs */
	private final Counter counter = new Counter();

	/** Create a new Natch poller */
	public NatchPoller(CommLink link) {
		super(link, TCP, false);
	}

	/** Create an operation */
	private void createOp(String n, ControllerIO cio, OpStep s) {
		Controller c = cio.getController();
		if (c instanceof ControllerImpl) {
			ControllerImpl ci = (ControllerImpl) c;
			Operation op = new Operation(n, ci, s);
			op.setPriority(PriorityLevel.SHORT_POLL);
			addOp(op);
		}
	}

	/** Perform a controller reset */
	@Override
	public void resetController(ControllerImpl c) {
		// FIXME
	}

	/** Send settings to a controller */
	@Override
	public void sendSettings(ControllerImpl c) {
		createSettingsOp("clock.status.op", c,
			new OpClockStatus(counter));
		createSettingsOp("system.attribute.op", c,
			new OpSystemAttributes(counter));
		createSettingsOp("detector.op.configure", c,
			new OpDetectorConfigure(counter));
	}

	/** Create a settings operation */
	private void createSettingsOp(String n, ControllerImpl c, OpStep s) {
		Operation op = new Operation(n, c, s);
		op.setPriority(PriorityLevel.DOWNLOAD);
		addOp(op);
	}

	/** Query sample data.
 	 * @param c Controller to poll.
 	 * @param p Sample period in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int p) {
		if (c.getPollPeriodSec() == p) {
			Operation op = new Operation("detector.op.query.data",
				c, new OpDetectorStatus(counter));
			op.setPriority(PriorityLevel.DEVICE_DATA);
			addOp(op);
		}
	}

	/** Send a device request to a ramp meter */
	@Override
	public void sendRequest(RampMeterImpl meter, DeviceRequest r) {
		switch (r) {
		case SEND_SETTINGS:
			createOp("ramp.meter.op.configure", meter,
				new OpMeterConfigure(counter, meter));
			break;
		case QUERY_STATUS:
			createOp("ramp.meter.op.query.status", meter,
				new OpQueryMeterStatus(counter, meter));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send a new release rate to a ramp meter */
	@Override
	public void sendReleaseRate(RampMeterImpl meter, Integer rate) {
		createOp("ramp.meter.op.send.status", meter,
			new OpSendMeterStatus(counter, meter, rate));
	}

	/** Send a device request to a beacon */
	@Override
	public void sendRequest(BeaconImpl b, DeviceRequest r) {
		switch (r) {
		case QUERY_STATUS:
			createOp("beacon.op.query.state", b,
				new OpQueryBeaconState(counter, b));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Set the flashing state of a beacon */
	@Override
	public void setFlashing(BeaconImpl b, boolean f) {
		createOp("beacon.op.send.state", b,
			new OpSendBeaconState(counter, b, f));
	}

	/** Send a device request to an LCS array */
	@Override
	public void sendRequest(LCSArrayImpl lcs_array, DeviceRequest r) {
		// FIXME
	}

	/** Send new indications to an LCS array.
	 * @param lcs_array LCS array.
	 * @param ind New lane use indications.
	 * @param o User who deployed the indications. */
	@Override
	public void sendIndications(LCSArrayImpl lcs_array, Integer[] ind,
		User o)
	{
		// FIXME
	}

	/** Send a device request to an alarm */
	@Override
	public void sendRequest(AlarmImpl alarm, DeviceRequest r) {
		switch (r) {
		case QUERY_STATUS:
			createOp("alarm.op.query.state", alarm,
				new OpQueryAlarmState(counter, alarm));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Start communication test */
	@Override
	public void startTesting(ControllerImpl c) {
		// FIXME
	}
}
