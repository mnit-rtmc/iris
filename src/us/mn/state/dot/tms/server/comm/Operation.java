/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.IDebugLog;

/**
 * An operation to be performed on a field controller.  Each message
 * poller maintains a prioritized queue of all outstanding operations. When an
 * operation gets to the head of the queue, the next phase is performed by
 * calling the poll method.
 *
 * @author Douglas Lau
 */
abstract public class Operation {

	/** Operation error log */
	static protected final IDebugLog OP_LOG = new IDebugLog("operation");

	/** Priority of the operation */
	protected PriorityLevel priority;

	/** Get the priority of the operation.
	 * @return Priority of the operation (@see PriorityLevel) */
	public PriorityLevel getPriority() {
		return priority;
	}

	/** Set the priority of the operation */
	public void setPriority(PriorityLevel p) {
		if(p.ordinal() < priority.ordinal())
			priority = p;
	}

	/** Current phase of the operation */
	private Phase phase;

	/** Create a new I/O operation */
	public Operation(PriorityLevel prio) {
		priority = prio;
		if(OP_LOG.isOpen())
			OP_LOG.log(getOpName() + " created");
	}

	/** Create the first phase of the operation.  This method cannot be
	 * called in the Operation constructor, because the object may not
	 * have been fully constructed yet (subclass initialization). */
	abstract protected Phase phaseOne();

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

	/** Get the operation name */
	protected String getOpName() {
		String name = getClass().getName();
		int i = name.lastIndexOf('.');
		if(i >= 0)
			return name.substring(i + 1);
		else
			return name;
	}

	/** Success or failure of operation */
	private boolean success = true;

	/** Check if the operation succeeded */
	public boolean isSuccess() {
		return success;
	}

	/** Set the success flag */
	public void setSuccess(boolean s) {
		success = s;
	}

	/** Set the operation to failed */
	public void setFailed() {
		setSuccess(false);
		phase = null;
	}

	/** Set the operation to succeeded */
	public void setSucceeded() {
		setSuccess(true);
		phase = null;
	}

	/** Begin the operation */
	public boolean begin() {
		phase = phaseOne();
		return true;
	}

	/** Cleanup the operation */
	public void cleanup() {
		OP_LOG.log(getOpName() + " cleanup");
	}

	/** Handle a communication error */
	public void handleCommError(EventType et, String msg) {
		setFailed();
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
	public void poll(CommMessage mess) throws IOException,
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
		abstract protected Phase poll(CommMessage mess)
			throws IOException, DeviceContentionException;
	}

	/** Get a description of the operation */
	public String getOperationDescription() {
		return getOpName();
	}
}
