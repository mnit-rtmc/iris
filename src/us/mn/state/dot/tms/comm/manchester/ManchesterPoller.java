/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.manchester;

import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.CameraImpl;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.TrafficDeviceImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.CameraPoller;
import us.mn.state.dot.tms.comm.DiagnosticOperation;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.Messenger;
import us.mn.state.dot.tms.comm.MessengerException;

/**
 * ManchesterPoller is a java implementation of the Manchester (American
 * Dynamics) camera control communication protocol
 *
 * @author Douglas Lau
 */
public class ManchesterPoller extends MessagePoller implements CameraPoller {

	/** Highest allowed address for Manchester protocol */
	static protected final int ADDRESS_MAX = 1024;

	/** Create a new Manchester poller */
	public ManchesterPoller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified drop address */
	public AddressedMessage createMessage(ControllerImpl c)
		throws MessengerException
	{
		return new Message(messenger.getOutputStream(c), c.getDrop());
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		return drop >= 1 && drop <= ADDRESS_MAX;
	}

	/** Perform a controller download */
	public void download(ControllerImpl c, boolean reset, int p) {
		if(c.isActive()) {
			TrafficDeviceImpl d = (TrafficDeviceImpl)c.getDevice();
			if(d != null)
				c.resetErrorCounter(d.getId());
		}
	}

	/** Perform a 30-second poll */
	public void poll30Second(ControllerImpl c, Completer comp) {
		// Nothing to do here
	}

	/** Perform a 5-minute poll */
	public void poll5Minute(ControllerImpl c, Completer comp) {
		// Nothing to do here
	}

	/** Send a PTZ camera move command */
	public void sendPTZ(CameraImpl c, int p, int t, int z) {
		new MoveCamera(c, p, t, z).start();
	}

	/** Start a test for the given controller */
	public DiagnosticOperation startTest(ControllerImpl c) {
		return null; // no diagmnostic testing can be done
	}
}
