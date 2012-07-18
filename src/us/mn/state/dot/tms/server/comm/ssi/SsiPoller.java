/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ssi;

import us.mn.state.dot.tms.server.IDebugLog;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.HttpFileMessenger;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * SSI RWIS poller, which periodically reads SSI data via http.
 *
 * @author Michael Darter
 */
public class SsiPoller extends MessagePoller {

	/** SSI logger */
	static public final IDebugLog LOG = new IDebugLog("ssi");

	/** Log an SSI message */
	static public void log(String msg) {
		LOG.log(msg);
	}

	/** HTTP file messenger */
	private final HttpFileMessenger http_messenger;

	/** Create a new poller */
	public SsiPoller(String n, HttpFileMessenger m) {
		super(n, m);
		http_messenger = m;
	}

	/** Create a new message for the specified controller, 
	 *  called by MessagePoller.doPoll(). */
	public CommMessage createMessage(ControllerImpl c) {
		return new SsiMessage(http_messenger);
	}

	/** Drop address is always valid */
	public boolean isAddressValid(int drop) {
		return true;
	}

	/** Read the file, called periodically */
	public void readEvent() {
		addOperation(new OpRead());
	}
}
