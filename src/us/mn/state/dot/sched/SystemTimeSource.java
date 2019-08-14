/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.sched;

/**
 * The system Time Source provides the current system time.
 *
 * @author Douglas Lau
 */
public class SystemTimeSource implements TimeSource {

	/** Get the current time */
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	/** Sleep for the specified number of milliseconds */
	public void sleep(long millis) throws InterruptedException {
		Thread.sleep(millis);
	}

	/** Wait until an object is notified, or timeout expires */
	public void wait(Object monitor, long ms) throws InterruptedException {
		monitor.wait(ms);
	}
}
