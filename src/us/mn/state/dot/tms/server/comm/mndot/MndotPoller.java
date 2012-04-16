/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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

import java.io.EOFException;
import java.util.Calendar;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.DeviceRequest;
import static us.mn.state.dot.tms.Interval.HOUR;
import us.mn.state.dot.tms.RampMeterType;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.LaneMarkingImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.WarningSignImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.AlarmPoller;
import us.mn.state.dot.tms.server.comm.LaneMarkingPoller;
import us.mn.state.dot.tms.server.comm.LCSPoller;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.MeterPoller;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.SamplePoller;
import us.mn.state.dot.tms.server.comm.WarningSignPoller;

/**
 * MndotPoller is a poller for the Mn/DOT 170 communication protocol,
 * revision 3, 4 or 5.
 *
 * @author Douglas Lau
 */
public class MndotPoller extends MessagePoller implements AlarmPoller,LCSPoller,
	MeterPoller, SamplePoller, WarningSignPoller, LaneMarkingPoller
{
	/** Test if it is afternoon */
	static protected boolean isAfternoon() {
		return TimeSteward.getCalendarInstance().get(Calendar.AM_PM) ==
		       Calendar.PM;
	}

	/** Get the meter number on the controller. This does not belong in the
	 * RampMeterImpl class because it only applies to the Mndot protocol. */
	static protected int getMeterNumber(RampMeterImpl meter) {
		if(meter.isActive()) {
			int pin = meter.getPin();
			if(pin == 2)
				return 1;
			if(pin == 3)
				return 2;
		}
		return 0;
	}

	/** Calculate the red time for a ramp meter.
	 * @param meter	Ramp meter to calculate red time.
	 * @param rate Release rate (vehicles per hour).
	 * @return Red time (seconds) */
	static float calculateRedTime(RampMeterImpl meter, int rate) {
		float secs_per_veh = HOUR / (float)rate;
		if(meter.getMeterType() == RampMeterType.SINGLE.ordinal())
			secs_per_veh /= 2;
		float green = SystemAttrEnum.METER_GREEN_SECS.getFloat();
		float yellow = SystemAttrEnum.METER_YELLOW_SECS.getFloat();
		float min_red = SystemAttrEnum.METER_MIN_RED_SECS.getFloat();
		float red_time = secs_per_veh - (green + yellow);
		return Math.max(red_time, min_red);
	}

	/** Calculate the release rate
	 * @param red_time Red time (seconds)
	 * @return Release rate (vehicles per hour) */
	static int calculateReleaseRate(RampMeterImpl meter, float red_time) {
		float green = SystemAttrEnum.METER_GREEN_SECS.getFloat();
		float yellow = SystemAttrEnum.METER_YELLOW_SECS.getFloat();
		float secs_per_veh = red_time + yellow + green;
		if(meter.getMeterType() == RampMeterType.SINGLE.ordinal())
			secs_per_veh *= 2;
		return Math.round(HOUR / secs_per_veh);
	}

	/** CommProtocol (4-bit or 5-bit) */
	protected final CommProtocol protocol;

	/** Create a new Mn/DOT 170 poller */
	public MndotPoller(String n, Messenger m, CommProtocol p) {
		super(n, m);
		protocol = p;
	}

	/** Create a new message for the specified controller */
	public CommMessage createMessage(ControllerImpl c) throws EOFException {
		return new Message(messenger.getOutputStream(c),
			messenger.getInputStream(c), c.getDrop(), protocol);
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		if(drop < 1 || drop > 31)
			return false;
		if(drop > 15 && protocol != CommProtocol.MNDOT_5)
			return false;
		return true;
	}

	/** Respond to a download request from a controller */
	protected void download(ControllerImpl c, PriorityLevel p) {
		OpSendSampleSettings ss = new OpSendSampleSettings(c);
		ss.setPriority(p);
		addOperation(ss);
		WarningSignImpl warn = c.getActiveWarningSign();
		if(warn != null) {
			OpSendWarningSettings s =
				new OpSendWarningSettings(warn);
			s.setPriority(p);
			addOperation(s);
		}
		RampMeterImpl meter1 = Op170.lookupMeter1(c);
		if(meter1 != null) {
			OpSendMeterSettings s = new OpSendMeterSettings(meter1);
			s.setPriority(p);
			addOperation(s);
		}
		RampMeterImpl meter2 = Op170.lookupMeter2(c);
		if(meter2 != null) {
			OpSendMeterSettings s = new OpSendMeterSettings(meter2);
			s.setPriority(p);
			addOperation(s);
		}
	}

	/** Perform a controller reset */
	public void resetController(ControllerImpl c) {
		if(c.getActive())
			addOperation(new OpReset170(c));
	}

	/** Send sample settings to a controller */
	public void sendSettings(ControllerImpl c) {
		if(c.getActive())
			addOperation(new OpSendSampleSettings(c));
	}

	/** Query sample data */
	public void querySamples(ControllerImpl c, int intvl, Completer comp) {
		switch(intvl) {
		case 30:
			if(c.hasActiveDetector())
				addOperation(new OpQuerySamples30Sec(c, comp));
			// This should happen on a meter QUERY_STATUS, but
			// green detectors need to be queried also...
			if(c.hasActiveMeter())
				addOperation(new OpQueryMeterStatus(c, comp));
			break;
		case 300:
			if(c.hasActiveDetector() || c.hasActiveMeter())
				addOperation(new OpQuerySamples5Min(c, comp));
			break;
		}
	}

	/** Query the status of alarms */
	public void queryAlarms(ControllerImpl c) {
		if(c.hasAlarm())
			addOperation(new OpQueryAlarms(c));
	}

	/** Send a device request to a ramp meter */
	public void sendRequest(RampMeterImpl meter, DeviceRequest r) {
		switch(r) {
		case SEND_SETTINGS:
			addOperation(new OpSendMeterSettings(meter));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send a new release rate to a ramp meter */
	public void sendReleaseRate(RampMeterImpl meter, Integer rate) {
		int n = getMeterNumber(meter);
		if(n > 0) {
			if(shouldStop(meter, rate))
				stopMetering(meter);
			else {
				float red = calculateRedTime(meter, rate);
				int r = Math.round(red * 10);
				addOperation(new OpSendMeterRedTime(meter,
					n, r));
				if(!meter.isMetering())
					startMetering(meter);
			}
		}
	}

	/** Should we stop metering? */
	protected boolean shouldStop(RampMeterImpl meter, Integer rate) {
		// Workaround for errors in rx only (good tx)
		return rate == null || rate == 0 ||
		       meter.getFailMillis() > COMM_FAIL_THRESHOLD_MS;
	}

	/** Start metering */
	protected void startMetering(RampMeterImpl meter) {
		if(!meter.isFailed())
			sendMeteringRate(meter, MeterRate.CENTRAL);
	}

	/** Stop metering */
	protected void stopMetering(RampMeterImpl meter) {
		sendMeteringRate(meter, MeterRate.FORCED_FLASH);
	}

	/** Send a new metering rate */
	protected void sendMeteringRate(RampMeterImpl meter, int rate) {
		int n = getMeterNumber(meter);
		if(n > 0)
			addOperation(new OpSendMeterRate(meter, n, rate));
	}

	/** Send a device request to a warning sign */
	public void sendRequest(WarningSignImpl sign, DeviceRequest r) {
		switch(r) {
		case SEND_SETTINGS:
			addOperation(new OpSendWarningSettings(sign));
			break;
		case QUERY_STATUS:
			addOperation(new OpQueryWarningStatus(sign));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Set the deployed status of the sign */
	public void setDeployed(WarningSignImpl sign, boolean d) {
		addOperation(new OpSendWarningCommand(sign, d));
	}

	/** Send a device request to an LCS array */
	public void sendRequest(LCSArrayImpl lcs_array, DeviceRequest r) {
		switch(r) {
		case SEND_SETTINGS:
			addOperation(new OpSendLCSSettings(lcs_array));
			break;
		case QUERY_MESSAGE:
			addOperation(new OpQueryLCSIndications(lcs_array));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Set the deployed status of a lane marking */
	public void setDeployed(LaneMarkingImpl dev, boolean d) {
		addOperation(new OpDeployLaneMarking(dev, d));
	}

	/** Send new indications to an LCS array.
	 * @param lcs_array LCS array.
	 * @param ind New lane use indications.
	 * @param o User who deployed the indications. */
	public void sendIndications(LCSArrayImpl lcs_array, Integer[] ind,
		User o)
	{
		addOperation(new OpSendLCSIndications(lcs_array, ind, o));
	}
}
