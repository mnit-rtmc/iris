/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.manchester;

import java.io.EOFException;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.CameraPoller;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;

/**
 * ManchesterPoller is a java implementation of the Manchester (American
 * Dynamics) camera control communication protocol
 *
 * @author Douglas Lau
 */
public class ManchesterPoller extends MessagePoller implements CameraPoller {

	/** The thread responsible for sending PTZ commands */
	static protected final PollerQueue queue =
		new PollerQueue();
	
	/** Highest allowed address for Manchester protocol */
	static protected final int ADDRESS_MAX = 1024;

	/** Create a new Manchester poller */
	public ManchesterPoller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified drop address */
	public AddressedMessage createMessage(ControllerImpl c)
		throws EOFException
	{
		return new Message(messenger.getOutputStream(c), c.getDrop());
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		return drop >= 1 && drop <= ADDRESS_MAX;
	}

	/** Send a PTZ camera move command */
	public void sendPTZ(CameraImpl c, float p, float t, float z) {
		PollerQueue.addCommand(c, new MoveCamera(c, p, t, z));
		if(!queue.isAlive())
			queue.start();
	}

	/** Send a PTZ set camera preset command */
	public void sendSetPreset(CameraImpl c, int preset) {
		// FIXME
	}

	/** Send a PTZ goto camera preset command */
	public void sendGoToPreset(CameraImpl c, int preset) {
		// FIXME
	}
}
