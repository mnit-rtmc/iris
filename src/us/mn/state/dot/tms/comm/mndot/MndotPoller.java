/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.mndot;

import java.io.EOFException;
import java.util.Calendar;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.Interval;
import us.mn.state.dot.tms.LaneControlSignalImpl;
import us.mn.state.dot.tms.RampMeterImpl;
import us.mn.state.dot.tms.RampMeterType;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.tms.WarningSignImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DiagnosticOperation;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.Messenger;
import us.mn.state.dot.tms.comm.MeterPoller;
import us.mn.state.dot.tms.comm.WarningSignPoller;

/**
 * MndotPoller is a poller for the Mn/DOT 170 communication protocol,
 * revision 3, 4 or 5.
 *
 * @author Douglas Lau
 */
public class MndotPoller extends MessagePoller implements MeterPoller,
	WarningSignPoller
{
	/** Test if it is afternoon */
	static protected boolean isAfternoon() {
		return Calendar.getInstance().get(Calendar.AM_PM)== Calendar.PM;
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
		float secs_per_veh = Interval.HOUR / (float)rate;
		if(meter.getMeterType() == RampMeterType.SINGLE.ordinal())
			secs_per_veh /= 2;
		float green = SystemAttributeHelper.getMeterGreenSecs();
		float yellow = SystemAttributeHelper.getMeterYellowSecs();
		float min_red = SystemAttributeHelper.getMeterMinRedSecs();
		float red_time = secs_per_veh - (green + yellow);
		return Math.max(red_time, min_red);
	}

	/** Calculate the red time for a ramp meter.
	 * @param meter	Ramp meter to calculate red time.
	 * @return Red time (seconds) */
	static float calculateRedTime(RampMeterImpl meter) {
		return calculateRedTime(meter, meter.getRate());
	}

	/** Calculate the release rate
	 * @param red_time Red time (seconds)
	 * @return Release rate (vehicles per hour) */
	static int calculateReleaseRate(RampMeterImpl meter, float red_time) {
		float green = SystemAttributeHelper.getMeterGreenSecs();
		float yellow = SystemAttributeHelper.getMeterYellowSecs();
		float secs_per_veh = red_time + yellow + green;
		if(meter.getMeterType() == RampMeterType.SINGLE.ordinal())
			secs_per_veh *= 2;
		return Math.round(Interval.HOUR / secs_per_veh);
	}

	/** CommLink protocol (4-bit or 5-bit) */
	protected final int protocol;

	/** Create a new Mn/DOT 170 poller */
	public MndotPoller(String n, Messenger m, int p) {
		super(n, m);
		protocol = p;
	}

	/** Create a new message for the specified controller */
	public AddressedMessage createMessage(ControllerImpl c)
		throws EOFException
	{
		return new Message(messenger.getOutputStream(c),
			messenger.getInputStream(c), c.getDrop(), protocol);
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		if(drop < 1 || drop > 31)
			return false;
		if(drop > 15 && protocol != CommLink.PROTO_MNDOT_5)
			return false;
		return true;
	}

	/** Perform a controller download */
	public void download(ControllerImpl c, boolean reset, int p) {
		if(c.getActive()) {
			Download d = new Download(c, reset);
			d.setPriority(p);
			d.start();
		}
	}

	/** Perform a 30-second poll */
	public void pollSigns(ControllerImpl c, Completer comp) {
		if(c.getActive()) {
			LaneControlSignalImpl lcs = c.getActiveLcs();
			if(lcs != null)
				new LCSQuerySignal(lcs, comp).start();
			WarningSignImpl warn = c.getActiveWarningSign();
			if(warn != null)
				new WarningStatus(warn, comp).start();
		}
	}

	/** Perform a 30-second poll */
	public void poll30Second(ControllerImpl c, Completer comp) {
		if(c.hasActiveDetector())
			new Data30Second(c, comp).start();
		if(c.hasActiveMeter())
			new QueryMeterStatus(c, comp).start();
	}

	/** Perform a 5-minute poll */
	public void poll5Minute(ControllerImpl c, Completer comp) {
		if(c.hasActiveDetector() || c.hasActiveMeter())
			new Data5Minute(c, comp).start();
		if(c.hasAlarm())
			new QueryAlarms(c).start();
	}

	/** Start a test for the given controller */
	public DiagnosticOperation startTest(ControllerImpl c) {
		DiagnosticOperation test = new Diagnostic170(c);
		test.start();
		return test;
	}

	/** Send a new release rate to a ramp meter */
	public void sendReleaseRate(RampMeterImpl meter, Integer rate) {
		int n = getMeterNumber(meter);
		if(n > 0) {
			if(shouldStop(meter, rate))
				stopMetering(meter);
			else {
				float red = calculateRedTime(meter);
				int r = Math.round(red * 10);
				new RedTimeCommand(meter, n, r).start();
				if(!meter.isMetering())
					startMetering(meter);
			}
		}
	}

	/** Should we stop metering? */
	protected boolean shouldStop(RampMeterImpl meter, Integer rate) {
		// Workaround for errors in rx only (good tx)
		return rate == null ||
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
			new MeterRateCommand(meter, n, rate).start();
	}

	/** Get the appropriate rate for the deployed state */
	static protected int getDeployedRate(boolean d) {
		if(d)
			return MeterRate.CENTRAL;
		else
			return MeterRate.FORCED_FLASH;
	}

	/** Set the deployed status of the sign */
	public void setDeployed(WarningSignImpl sign, boolean d) {
		new MeterRateCommand(sign, 1, getDeployedRate(d)).start();
	}
}
