/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelcod;

import java.io.EOFException;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.CameraPoller;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;

/**
 * PelcoDPoller is a java implementation of the Pelco D camera control
 * communication protocol
 *
 * @author Douglas Lau
 */
public class PelcoDPoller extends MessagePoller implements CameraPoller {

	/** Highest allowed address for Pelco D protocol */
	static public final int ADDRESS_MAX = 254;

	/** Create a new Pelco poller */
	public PelcoDPoller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified drop address */
	public CommMessage createMessage(ControllerImpl c) throws EOFException {
		return new Message(messenger.getOutputStream(c), c.getDrop());
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		return drop >= 1 && drop <= ADDRESS_MAX;
	}

	/** Send a PTZ camera move command */
	public void sendPTZ(CameraImpl c, float p, float t, float z) {
		addOperation(new OpMoveCamera(c, p, t, z));
	}

	/** Send a store camera preset command */
	public void sendStorePreset(CameraImpl c, int preset) {
		addOperation(new OpStorePreset(c, preset));
	}

	/** Send a recall camera preset command */
	public void sendRecallPreset(CameraImpl c, int preset) {
		addOperation(new OpRecallPreset(c, preset));
	}
}
