/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.LaneControlSignalImpl;
import us.mn.state.dot.tms.RampMeterImpl;
import us.mn.state.dot.tms.TrafficDeviceImpl;
import us.mn.state.dot.tms.WarningSignImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DiagnosticOperation;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.Messenger;
import us.mn.state.dot.tms.comm.MessengerException;
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
	/** CommLink protocol (4-bit or 5-bit) */
	protected final int protocol;

	/** Create a new Mn/DOT 170 poller */
	public MndotPoller(String n, Messenger m, int p) {
		super(n, m);
		protocol = p;
	}

	/** Create a new message for the specified controller */
	public AddressedMessage createMessage(ControllerImpl c)
		throws MessengerException
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

	/** Send a new metering rate */
	protected void sendMeteringRate(RampMeterImpl meter, int rate) {
		int n = getMeterNumber(meter);
		if(n > 0)
			new SetMeterRate(meter, n, rate).start();
	}

	/** Start metering */
	public void startMetering(RampMeterImpl meter) {
		if(!meter.isFailed()) {
			sendReleaseRate(meter, meter.getReleaseRate());
			sendMeteringRate(meter, MeterRate.CENTRAL);
		}
	}

	/** Stop metering */
	public void stopMetering(RampMeterImpl meter) {
		sendMeteringRate(meter, MeterRate.FORCED_FLASH);
	}

	/** Send a new release rate (vehicles per hour) */
	public void sendReleaseRate(RampMeterImpl meter, int rate) {
		int n = getMeterNumber(meter);
		if(n > 0) {
			// Workaround for errors in rx only (good tx)
			if(meter.getFailMillis() > COMM_FAIL_THRESHOLD_MS)
				stopMetering(meter);
			else {
				int r = meter.calculateRedTime(rate);
				new SetRedTime(meter, n, r).start();
			}
		}
	}

	/** Get the appropriate rate for the deployed state */
	protected int getDeployedRate(boolean d) {
		if(d)
			return MeterRate.CENTRAL;
		else
			return MeterRate.FORCED_FLASH;
	}

	/** Set the deployed status of the sign */
	public void setDeployed(WarningSignImpl sign, boolean d) {
		new SetMeterRate(sign, 1, getDeployedRate(d)).start();
	}
}
