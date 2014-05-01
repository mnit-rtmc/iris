/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
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

package us.mn.state.dot.tms.server.comm.cohuptz;

import java.io.EOFException;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.CameraPoller;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;

/**
 * Poller for the Cohu PTZ protocol
 *
 * @author Travis Swanston
 */
public class CohuPTZPoller extends MessagePoller implements CameraPoller {

	/** Cohu camera address range constants */
	static public final int COHU_ADDR_MIN = 1;
	static public final int COHU_ADDR_MAX = 223;

	/** Create a new Cohu PTZ poller */
	public CohuPTZPoller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified drop address */
	public CommMessage createMessage(ControllerImpl c)
		throws EOFException
	{
		return new Message(messenger.getOutputStream(c), c.getDrop());
	}

	/** Check drop address validity */
	public boolean isAddressValid(int drop) {
		return ((drop >= COHU_ADDR_MIN) && (drop <= COHU_ADDR_MAX));
	}

	/** Send a "PTZ camera move" command */
	@Override
	public void sendPTZ(CameraImpl c, float p, float t, float z) {
		addOperation(new OpMoveCamera(c, p, t, z));
	}

	/** Send a "store camera preset" command */
	@Override
	public void sendStorePreset(CameraImpl c, int preset) {
		addOperation(new OpStorePreset(c, preset));
	}

	/** Send a "recall camera preset" command */
	@Override
	public void sendRecallPreset(CameraImpl c, int preset) {
		addOperation(new OpRecallPreset(c, preset));
	}

}
