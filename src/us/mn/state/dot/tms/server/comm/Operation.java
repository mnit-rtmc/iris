/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import java.io.IOException;
import us.mn.state.dot.tms.server.event.EventType;

/**
 * An operation to be performed on a field controller.  Each message
 * poller maintains a prioritized queue of all outstanding operations. When an
 * operation gets to the head of the queue, the next phase is performed by
 * calling the poll method.
 *
 * @author Douglas Lau
 */
abstract public class Operation {

	/** Constant definition for urgent priority (system shut-down) */
	static public final int URGENT = 0;

	/** Constant definition for command priority (overrides, etc.) */
	static public final int COMMAND = 1;

	/** Constant definition for 30-second data priority */
	static public final int DATA_30_SEC = 2;

	/** Constant definition for download priority */
	static public final int DOWNLOAD = 3;

	/** Constant definition for 5-minute data priority */
	static public final int DATA_5_MIN = 4;

	/** Constant definition for device data priority */
	static public final int DEVICE_DATA = 5;

	/** Constant definition for diagnostic priority */
	static public final int DIAGNOSTIC = 6;

	/** Priority of the operation */
	protected int priority;

	/** Get the priority of the operation.
	 * @return Priority of the operation (0 is highest priority) */
	public int getPriority() {
		return priority;
	}

	/** Set the priority of the operation */
	public void setPriority(int p) {
		if(p < priority)
			priority = p;
	}

	/** Current phase of the operation */
	protected Phase phase;

	/** Create a new I/O operation */
	public Operation(int prio) {
		priority = prio;
		phase = null;
	}

	/** Get a string description of the operation */
	public String toString() {
		String name;
		Phase p = phase;
		if(p != null)
			name = p.getClass().getName();
		else
			name = getClass().getName();
		int i = name.lastIndexOf('.');
		if(i >= 0)
			return name.substring(i + 1);
		else
			return name;
	}

	/** Success or failure of operation */
	protected boolean success = true;

	/** Begin the operation */
	abstract public void begin();

	/** Cleanup the operation */
	public void cleanup() {}

	/** Handle a communication error */
	public void handleCommError(EventType et, String msg) {
		setFailed();
	}

	/** Set the operation to failed */
	public void setFailed() {
		success = false;
		phase = null;
	}

	/** Check if the operation is done */
	public boolean isDone() {
		return phase == null;
	}

	/** 
	  * Perform a poll with an addressed message. Called by 
	  * MessagePoller.doPoll(). Processing stops when phase is
	  * assigned null.
	  * @see MessagePoller.performOperations
	  */
	public void poll(AddressedMessage mess) throws IOException,
		DeviceContentionException
	{
		final Phase p = phase;
		if(p != null)
			phase = p.poll(mess);
	}

	/** Base class for operation phases */
	abstract protected class Phase {

		/** Perform a poll.
		 * @return The next phase of the operation */
		abstract protected Phase poll(AddressedMessage mess)
			throws IOException, DeviceContentionException;
	}

	/** 
	  * Get a human readable description of the operation, which
	  * is the name of the operation class. 
	  */
	public String getOperationDescription() {
		String name = this.getClass().getName();
		int i = name.lastIndexOf('.');
		if(i >= 0)
			return name.substring(i + 1);
		return name;
	}
}
