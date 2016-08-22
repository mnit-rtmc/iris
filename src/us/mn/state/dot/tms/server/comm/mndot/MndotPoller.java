/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.AlarmImpl;
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.LaneMarkingImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.AlarmPoller;
import us.mn.state.dot.tms.server.comm.BeaconPoller;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.LaneMarkingPoller;
import us.mn.state.dot.tms.server.comm.LCSPoller;
import us.mn.state.dot.tms.server.comm.MeterPoller;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.SamplePoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * MndotPoller is a poller for the MnDOT 170 communication protocol,
 * revision 4 or 5.
 *
 * @author Douglas Lau
 */
public class MndotPoller extends DevicePoller<MndotProperty>
	implements LCSPoller, AlarmPoller, MeterPoller, SamplePoller,
	BeaconPoller, LaneMarkingPoller
{
	/** MnDOT 170 debug log */
	static private final DebugLog MNDOT_LOG = new DebugLog("mndot170");

	/** Communication protocol */
	private final CommProtocol protocol;

	/** Create a new MnDOT 170 poller.
	 * @param n Comm link name. */
	public MndotPoller(String n, CommProtocol cp) {
		super(n, TCP, MNDOT_LOG);
		protocol = cp;
	}

	/** Perform a controller reset */
	@Override
	public void resetController(ControllerImpl c) {
		addOp(new OpReset170(c));
	}

	/** Send sample settings to a controller */
	@Override
	public void sendSettings(ControllerImpl c) {
		addOp(new OpSendSampleSettings(c));
	}

	/** Respond to a download request from a controller */
	@Override
	public void sendSettings(ControllerImpl c, PriorityLevel p) {
		addOp(new OpSendSampleSettings(p, c));
		BeaconImpl beacon = c.getActiveBeacon();
		if (beacon != null)
			addOp(new OpSendBeaconSettings(p, beacon));
		RampMeterImpl meter1 = Op170.lookupMeter1(c);
		if (meter1 != null)
			addOp(new OpSendMeterSettings(p, meter1));
		RampMeterImpl meter2 = Op170.lookupMeter2(c);
		if (meter2 != null)
			addOp(new OpSendMeterSettings(p, meter2));
	}

	/** Query sample data.
 	 * @param c Controller to poll.
 	 * @param p Sample period in seconds. */
	@Override
	public void querySamples(ControllerImpl c, int p) {
		switch (p) {
		case OpQuerySamples30Sec.SAMPLE_PERIOD_SEC:
			addOp(new OpQuerySamples30Sec(c));
			break;
		case OpQuerySamples5Min.SAMPLE_PERIOD_SEC:
			addOp(new OpQuerySamples5Min(c));
			break;
		}
	}

	/** Send a device request to a ramp meter */
	@Override
	public void sendRequest(RampMeterImpl meter, DeviceRequest r) {
		switch (r) {
		case SEND_SETTINGS:
			addOp(new OpSendMeterSettings(meter));
			break;
		case QUERY_STATUS:
			addOp(new OpQueryMeterStatus(meter));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send a new release rate to a ramp meter */
	@Override
	public void sendReleaseRate(RampMeterImpl meter, Integer rate) {
		addOp(new OpSendMeterRate(meter, rate));
	}

	/** Send a device request to a beacon */
	@Override
	public void sendRequest(BeaconImpl beacon, DeviceRequest r) {
		switch (r) {
		case SEND_SETTINGS:
			addOp(new OpSendBeaconSettings(beacon));
			break;
		case QUERY_STATUS:
			addOp(new OpQueryBeaconState(beacon));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Set the flashing state of a beacon */
	@Override
	public void setFlashing(BeaconImpl b, boolean f) {
		addOp(new OpSendBeaconState(b, f));
	}

	/** Send a device request to an LCS array */
	@Override
	public void sendRequest(LCSArrayImpl lcs_array, DeviceRequest r) {
		switch (r) {
		case SEND_SETTINGS:
			addOp(new OpSendLCSSettings(lcs_array));
			break;
		case QUERY_MESSAGE:
			addOp(new OpQueryLCSIndications(lcs_array));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Set the deployed status of a lane marking */
	@Override
	public void setDeployed(LaneMarkingImpl dev, boolean d) {
		addOp(new OpDeployLaneMarking(dev, d));
	}

	/** Send new indications to an LCS array.
	 * @param lcs_array LCS array.
	 * @param ind New lane use indications.
	 * @param o User who deployed the indications. */
	@Override
	public void sendIndications(LCSArrayImpl lcs_array, Integer[] ind,
		User o)
	{
		addOp(new OpSendLCSIndications(lcs_array, ind, o));
	}

	/** Send a device request to an alarm */
	@Override
	public void sendRequest(AlarmImpl alarm, DeviceRequest r) {
		switch (r) {
		case QUERY_STATUS:
			Controller c = alarm.getController();
			if (c instanceof ControllerImpl) {
				ControllerImpl ci = (ControllerImpl) c;
				addOp(new OpQueryAlarms(ci));
			}
			break;
		default:
			// Ignore other requests
			break;
		}
	}
}
