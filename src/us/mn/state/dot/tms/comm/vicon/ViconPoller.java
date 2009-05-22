/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.vicon;

import java.io.EOFException;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DiagnosticOperation;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.Messenger;
import us.mn.state.dot.tms.comm.VideoMonitorPoller;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * ViconPoller is a java implementation of the Vicon Video Switch serial
 * communication protocol
 *
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @author Douglas Lau
 */
public class ViconPoller extends MessagePoller implements VideoMonitorPoller {

	/** Dummy drop value for creating addressed messages */
	static protected final int VICON_DROP = 1;

	/** Create a new Vicon line */
	public ViconPoller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified drop address */
	public AddressedMessage createMessage(ControllerImpl c)
		throws EOFException
	{
		return new Message(messenger.getOutputStream(c),
			messenger.getInputStream(c));
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		return drop == VICON_DROP;
	}

	/** Perform a controller download */
	public void download(ControllerImpl c, boolean reset, int p) {
		// Nothing to do here
	}

	/** Perform a 30-second poll */
	public void poll30Second(ControllerImpl c, Completer comp) {
		// Nothing to do here
	}

	/** Perform a 5-minute poll */
	public void poll5Minute(ControllerImpl c, Completer comp) {
		// Nothing to do here
	}

	/** Start a test for the given controller */
	public DiagnosticOperation startTest(ControllerImpl c) {
		return null; // no diagmnostic testing can be done
	}

	/** Set the camera to display on the specified monitor */
	public void setMonitorCamera(ControllerImpl c, VideoMonitor m,
		String cam)
	{
		new SelectMonitorCamera(c, m, cam).start();
	}
}
