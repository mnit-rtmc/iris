/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.CommunicationLine;
import us.mn.state.dot.tms.Completer;
import us.mn.state.dot.tms.Controller170Impl;
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
	/** CommunicationLine protocol (4-bit or 5-bit) */
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
		if(drop > 15 && protocol != CommunicationLine.PROTO_MNDOT_5)
			return false;
		return true;
	}

	/** Check that a controller is an active 170 controller */
	static protected Controller170Impl check170(ControllerImpl c) {
		if(c instanceof Controller170Impl) {
			Controller170Impl c170 = (Controller170Impl)c;
			if(c170.isActive())
				return c170;
		}
		return null;
	}

	/** Perform a controller download */
	public void download(ControllerImpl c, boolean reset, int p) {
		Controller170Impl c170 = check170(c);
		if(c170 != null) {
			Download d = new Download(c170, reset);
			d.setPriority(p);
			d.start();
		}
	}

	/** Perform a 30-second poll */
	public void pollSigns(ControllerImpl c, Completer comp) {
		Controller170Impl c170 = check170(c);
		if(c170 == null)
			return;
		TrafficDeviceImpl device = (TrafficDeviceImpl)c170.getDevice();
		if(device != null) {
			if(device instanceof LaneControlSignalImpl) {
				LaneControlSignalImpl lcs =
					(LaneControlSignalImpl)device;
				new LCSQuerySignal(lcs, comp).start();
			}
			else if(device instanceof WarningSignImpl) {
				WarningSignImpl warn = (WarningSignImpl)device;
				new WarningStatus(warn, comp).start();
			}
		}
	}

	/** Perform a 30-second poll */
	public void poll30Second(ControllerImpl c, Completer comp) {
		Controller170Impl c170 = check170(c);
		if(c170 == null)
			return;
		if(c170.hasActiveDetector())
			new Data30Second(c170, comp).start();
		if(c170.hasActiveMeter())
			new QueryMeterStatus(c170, comp).start();
	}

	/** Perform a 5-minute poll */
	public void poll5Minute(ControllerImpl c, Completer comp) {
		Controller170Impl c170 = check170(c);
		if(c170 == null)
			return;
		if(c170.hasActiveDetector() || c170.hasActiveMeter())
			new Data5Minute(c170, comp).start();
		if(c170.hasAlarm())
			new QueryAlarms(c170).start();
	}

	/** Start a test for the given controller */
	public DiagnosticOperation startTest(ControllerImpl c) {
		DiagnosticOperation test = new Diagnostic170(c);
		test.start();
		return test;
	}

	/** Send a new metering rate */
	protected void sendMeteringRate(RampMeterImpl meter, int rate) {
		int n = meter.getMeterNumber();
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

	/** Set the meter to local metering mode */
	protected void setLocal(RampMeterImpl meter) {
		sendMeteringRate(meter, MeterRate.FLASH);
	}

	/** Send a new release rate (vehicles per hour) */
	public void sendReleaseRate(RampMeterImpl meter, int rate) {
		int n = meter.getMeterNumber();
		if(n > 0) {
			// Workaround for errors in rx only (good tx)
			if(meter.isFailedBeyondThreshold())
				setLocal(meter);
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
