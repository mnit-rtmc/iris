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
import us.mn.state.dot.tms.server.comm.OpDevice;

/**
 * Poller for the Cohu PTZ protocol
 *
 * @author Travis Swanston
 */
public class CohuPTZPoller extends MessagePoller implements CameraPoller {

	/** Cohu camera address range constants */
	static public final int ADDR_MIN = 1;
	static public final int ADDR_MAX = 223;

	/** Minimum amount of delay (ms) between Cohu commands */
	static protected final int MIN_CMD_INTERVAL_MS = 10;

	/** Timestamp of most recent operation by this poller. */
	protected long lastOpTime = 0;

	/** Current pan value */
	protected float curPan  = 0.0F;

	/** Current tilt value */
	protected float curTilt = 0.0F;

	/** Current zoom value */
	protected float curZoom = 0.0F;


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
		return ((drop >= ADDR_MIN) && (drop <= ADDR_MAX));
	}

	/** Send a "PTZ camera move" command */
	@Override
	public void sendPTZ(CameraImpl c, float p, float t, float z) {
		if (p != curPan)  {
			addOperation(new OpPanCamera(c, p));
			curPan = p;
		}
		if (t != curTilt) {
			addOperation(new OpTiltCamera(c, t));
			curTilt = t;
		}
		if (z != curZoom) {
			addOperation(new OpZoomCamera(c, z));
			curZoom = z;
		}
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

	/**
	 * Add an operation to the message poller, ensuring that sufficient
	 * time has passed since the last operation was issued (Cohu devices
	 * require a short pause between commands).
	 */
	protected void addOperation(OpDevice op) {
		pauseIfNeeded();
		super.addOperation(op);
		lastOpTime = System.currentTimeMillis();
	}

	/**
	 * If MIN_CMD_INTERVAL_MS milliseconds have not passed since the
	 * previous operation, simply sleep MIN_CMD_INTERVAL_MS milliseconds.
	 */
	protected void pauseIfNeeded() {
		long curTime = System.currentTimeMillis();
		if ((curTime - lastOpTime) < MIN_CMD_INTERVAL_MS) {
			try {
				Thread.sleep(MIN_CMD_INTERVAL_MS);
			}
			catch (InterruptedException e) {
			}
		}
	}

}
