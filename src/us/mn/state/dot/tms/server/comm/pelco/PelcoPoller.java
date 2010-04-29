/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelco;

import java.io.EOFException;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.VideoMonitorPoller;

/**
 * PecloPoller is a java implementation of the Pelco Video Switch serial
 * communication protocol
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
public class PelcoPoller extends MessagePoller implements VideoMonitorPoller {

	/** Dummy drop value for creating addressed messages */
	static protected final int PELCO_DROP = 1;

	/** Create a new Pelco line */
	public PelcoPoller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified drop address */
	public CommMessage createMessage(ControllerImpl c) throws EOFException {
		return new Message(messenger.getOutputStream(c),
			messenger.getInputStream(c));
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		return drop == PELCO_DROP;
	}

	/** Set the camera to display on the specified monitor */
	public void setMonitorCamera(ControllerImpl c, VideoMonitor m,
		String cam)
	{
		new SelectMonitorCamera(c, m, cam).start();
	}
}
