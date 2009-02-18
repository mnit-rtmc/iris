/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm;

import java.io.IOException;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.ErrorCounter;

/**
 * General operation to test communication to a controller
 *
 * @author Douglas Lau
 */
abstract public class DiagnosticOperation extends ControllerOperation {

	/** Number of milliseconds in one minute */
	static protected final int ONE_MINUTE = 60 * 1000;

	/** Time to stop processing this poll */
	protected long stop;

	/** Count of polls */
	protected int count = 0;

	/** Create a new diagnostic operation */
	public DiagnosticOperation(ControllerImpl c) {
		super(DIAGNOSTIC, c);
		keepTesting();
	}

	/** Keep testing the communications to the controller */
	public void keepTesting() {
		stop = System.currentTimeMillis() + ONE_MINUTE;
	}

	/** Stop testing the communications to the controller */
	public void stopTesting() {
		stop = 0;
	}

	/** Test if the operation should stop */
	protected boolean shouldStop() {
		return System.currentTimeMillis() > stop;
	}

	/** Perform a poll on a message poller */
	public void poll(AddressedMessage mess) throws IOException,
		DeviceContentionException
	{
		count++;
		if(count > 9) {
			count = 0;
//			FIXME: do some magic here ...
//			controller.notifyStatus();
		}
		if(shouldStop())
			phase = null;
		else {
			super.poll(mess);
			controller.incrementCounter(ErrorCounter.TYPE_GOOD);
		}
	}

	/** Handle an exception */
	public void handleException(IOException e) {
		errorStatus = e.getMessage();
		controller.incrementCounter(ErrorCounter.TYPE_FAIL);
	}

	/** Cleanup the operation */
	public void cleanup() {
		// FIXME: do some magic here ...
//		if(count > 0)
//			controller.notifyStatus();
	}
}
