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
package us.mn.state.dot.tms.server.comm.natch;

import java.util.HashMap;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.User;
import us.mn.state.dot.tms.server.AlarmImpl;
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.ControllerIoImpl;
import us.mn.state.dot.tms.server.LcsImpl;
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
	/** I/O pin for first ramp meter */
	static public final int METER_1_PIN = 2;

	/** I/O pin for second ramp meter */
	static public final int METER_2_PIN = 3;

	/** Lookup a ramp meter on a controller */
	static public RampMeterImpl lookupMeter(ControllerImpl ctrl, int pin) {
		ControllerIO cio = ctrl.getIO(pin);
		return (cio instanceof RampMeterImpl)
		      ? (RampMeterImpl) cio
		      : null;
	}

	/** Counter for message IDs */
	private final Counter counter = new Counter();

	/** Mapping of all detector status operation collectors on line */
	private final HashMap<ControllerImpl, Operation> collectors =
		new HashMap<ControllerImpl, Operation>();

	/** Create a new Natch poller */
	public NatchPoller(CommLink link) {
		super(link, TCP, false);
	}

	/** Create a controller operation */
	private void createOp(String n, ControllerImpl c, OpStep s,
		PriorityLevel pl)
	{
		Operation op = new Operation(n, c, s);
		op.setPriority(pl);
		addOp(op);
	}

	/** Create a controller I/O operation */
	private void createOp(String n, ControllerIoImpl cio, OpStep s,
		PriorityLevel pl)
	{
		Controller c = cio.getController();
		if (c instanceof ControllerImpl) {
			ControllerImpl ci = (ControllerImpl) c;
			Operation op = new Operation(n, ci, cio, s);
			op.setPriority(pl);
			addOp(op);
		}
	}

	/** Send detection request to a controller.
	 * @param c Controller to poll. */
	@Override
	public void sendRequest(ControllerImpl c, DeviceRequest r) {
		switch (r) {
		case RESET_DEVICE:
			createOp("system.command.op", c,
				new OpSystemCommand(counter),
				PriorityLevel.SETTINGS);
			break;
		case SEND_SETTINGS:
			createOp("clock.status.op", c,
				new OpClockStatus(counter),
				PriorityLevel.SETTINGS);
			createOp("system.attribute.op", c,
				new OpSystemAttributes(counter),
				PriorityLevel.SETTINGS);
			createOp("detector.op.configure", c,
				new OpDetectorConfigure(counter, 0),
				PriorityLevel.SETTINGS);
			createOp("firmware.version.op", c,
				new OpFirmwareVersion(counter),
				PriorityLevel.SETTINGS);
			RampMeterImpl meter1 = lookupMeter(c, METER_1_PIN);
			if (meter1 != null) {
				sendRequest(meter1,
				DeviceRequest.SEND_SETTINGS);
			}
			RampMeterImpl meter2 = lookupMeter(c, METER_2_PIN);
			if (meter2 != null) {
				sendRequest(meter2,
				DeviceRequest.SEND_SETTINGS);
			}
			break;
		}
	}

	/** Query sample data.
	 * @param c Controller to poll.
	 * @param per_sec Sample period in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int per_sec) {
		if (c.getPollPeriodSec() == per_sec) {
			Operation ds = getDetectorStatusOp(c);
			if (ds != null)
				binEventData(c, per_sec);
		} else {
			// Long polling period, check detector configs
			createOp("detector.op.query.config", c,
				new OpQueryDetConfig(counter, 0),
				PriorityLevel.POLL_LOW);
		}
	}

	/** Bin detection event data */
	private void binEventData(ControllerImpl c, int per_sec) {
		boolean s = isConnected() && !c.isOffline();
		if (!s)
			c.logGap();
		c.binEventData(per_sec, s);
	}

	/** Get detector status operation for a controller */
	private synchronized Operation getDetectorStatusOp(ControllerImpl c) {
		final Operation ds = collectors.get(c);
		if (ds == null || ds.isDone()) {
			Operation op = new Operation("detector.op.status", c,
				new OpDetectorStatus());
			op.setPriority(PriorityLevel.IDLE);
			collectors.put(c, op);
			addOp(op);
		}
		return ds;
	}

	/** Send a device request to a ramp meter */
	@Override
	public void sendRequest(RampMeterImpl meter, DeviceRequest r) {
		switch (r) {
		case SEND_SETTINGS:
			createOp("ramp.meter.op.configure", meter,
				new OpMeterConfigure(counter, meter),
				PriorityLevel.SETTINGS);
			break;
		case QUERY_STATUS:
			createOp("ramp.meter.op.query.status", meter,
				new OpQueryMeterStatus(counter, meter),
				PriorityLevel.POLL_HIGH);
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
			new OpSendMeterStatus(counter, meter, rate),
			PriorityLevel.COMMAND);
	}

	/** Send a device request to a beacon */
	@Override
	public void sendRequest(BeaconImpl b, DeviceRequest r) {
		switch (r) {
		case QUERY_STATUS:
			createOp("beacon.op.query.state", b,
				new OpQueryBeaconState(counter, b),
				PriorityLevel.POLL_HIGH);
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
			new OpSendBeaconState(counter, b, f),
			PriorityLevel.COMMAND);
	}

	/** Send a device request to an LCS array */
	@Override
	public void sendRequest(LcsImpl lcs, DeviceRequest r) {
		// FIXME
	}

	/** Send new indications to an LCS array.
	 * @param lcs LCS array.
	 * @param ind New lane use indications.
	 * @param o User who deployed the indications. */
	@Override
	public void sendIndications(LcsImpl lcs, String lock) {
		// FIXME
	}

	/** Send a device request to an alarm */
	@Override
	public void sendRequest(AlarmImpl alarm, DeviceRequest r) {
		switch (r) {
		case QUERY_STATUS:
			createOp("alarm.op.query.state", alarm,
				new OpQueryAlarmState(counter, alarm),
				PriorityLevel.POLL_HIGH);
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
