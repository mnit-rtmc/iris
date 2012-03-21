/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.g4;

import java.io.EOFException;
import java.io.IOException;
import java.io.DataOutputStream;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.IDebugLog;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.SamplePoller;

/**
 * G4Poller is a java implementation of the EIS G4 MVDS protocol.
 * serial data communication protocol.
 *
 * @author Michael Darter
 */
public class G4Poller extends MessagePoller implements SamplePoller {

	/** Debug log */
	static private final IDebugLog G4_LOG = new IDebugLog("g4");

	/** Create a new G4 poller */
	public G4Poller(String n, Messenger m) {
		super(n, m, OpenMode.PER_OP);
		G4Poller.info("G4Poller.G4Poller("+n+","+m+")");
	}

	/** Log a message */
	static protected void info(String m) {
		G4_LOG.log("info: " + m);
	}

	/** Log a message */
	static protected void warn(String m) {
		G4_LOG.log("WARN: " + m);
	}

	/** Create a new message for the specified controller. Called by
 	 * MessagePoller.doPoll. */
	public CommMessage createMessage(ControllerImpl c) throws EOFException {
		G4Poller.info("G4Poller.createMessage(" + c + ")");
		return new G4Message(
			new DataOutputStream(messenger.getOutputStream(c)),
			messenger.getInputStream(c), c, messenger);
	}

	/** Check if a sensor id is valid */
	public boolean isAddressValid(int drop) {
		return drop >= 0 && drop < 65536;
	}

	/** Perform a controller download */
	protected void download(ControllerImpl c, PriorityLevel p) {}

	/** Perform a controller reset */
	public void resetController(ControllerImpl c) {}

	/** Send sample settings to a controller. Called on startup. */
	public void sendSettings(ControllerImpl c) {}

	/** Query sample data, called every 30 seconds.
 	 * @param c Controller, may not be null.
 	 * @param intvl Query interval in seconds.
 	 * @param comp Job completer.  */
	public void querySamples(ControllerImpl c, int intvl, Completer comp) {
		G4Poller.info("G4Poller.querySamples(" + c + "," + intvl + ") called");
		G4Poller.info("G4Poller.querySamples(): activeDet=" + 
			c.hasActiveDetector() + ", id=" + c.getDrop());
		if(!c.hasActiveDetector())
			return;
		if(intvl == 30) {
			G4Poller.info("G4Poller.querySamples(): " +
				"creating new OpQueryStats");
			addOperation(new OpQueryStats(c, comp));
		} else
			G4Poller.info("interval=" + intvl + " not supported");
	}

	/** Sleep */
	static protected void sleepy(int ms) {
		try {
			Thread.sleep(ms);
		} catch(Exception e) {}
	}
}
