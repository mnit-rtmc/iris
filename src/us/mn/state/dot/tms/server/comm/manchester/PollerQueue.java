/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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

import java.util.HashMap;
import java.util.LinkedList;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.CameraImpl;

/**
 * A static thread which is responsible for streaming Manchester PTZ commands.
 *
 * @author Timothy A. Johnson
 * @author Douglas Lau
 */
public class PollerQueue extends Thread {

	/** Operation timeout in miliseconds */
	static protected final int OP_TIMEOUT_MS = 30000;

	/** The number of milliseconds between commands */
	protected static final int CMD_INTERVAL = 60;

	/** Mapping of camera names to current PTZ commands */
	protected final HashMap<String, OpMoveCamera> commands =
		new HashMap<String, OpMoveCamera>();

	/** List of expired camera IDs */
	protected final LinkedList<String> expired = new LinkedList<String>();

	/** Add a PTZ command to the queue.
	 * cmd The OpMoveCamera command to be added.
	 */
	public synchronized void addCommand(CameraImpl c, OpMoveCamera cmd) {
		if(cmd.isStopCmd()) {
			cmd.start();
			commands.remove(c.getName());
		} else
			commands.put(c.getName(), cmd);
	}

	/** Run method for thread */
	public void run() {
		while(true) {
			sendCommands();
			try {
				Thread.sleep(CMD_INTERVAL);
			}
			catch(InterruptedException e) {
				// ignore
			}
		}
	}

	/** Send all current commands to cameras */
	protected synchronized void sendCommands() {
		long now = TimeSteward.currentTimeMillis();
		for(String cid: commands.keySet()) {
			OpMoveCamera cmd = commands.get(cid);
			if(cmd.stamp + OP_TIMEOUT_MS < now)
				cmd.start();
			else
				expired.add(cid);
		}
		for(String cid: expired)
			commands.remove(cid);
		expired.clear();
	}
}
