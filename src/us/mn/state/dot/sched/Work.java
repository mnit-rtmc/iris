/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2020  Minnesota Department of Transportation
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

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Work for a Worker to perform.
 *
 * @author Douglas Lau
 */
abstract public class Work implements Comparable<Work> {

	/** Next available work identifier */
	static private final AtomicLong next_id = new AtomicLong();

	/** Unique work identifier */
	private final long id = next_id.getAndIncrement();

	/** Flag to indicate work complete */
	private boolean is_complete = false;

	/** Create Work */
	public Work() { }

	/** Perform the work */
	final void performWork() throws Exception {
		try {
			perform();
		}
		finally {
			complete();
			setComplete();
		}
	}

	/** Do this upon completion of the work */
	protected void complete() { }

	/** Set the complete flag */
	private synchronized void setComplete() {
		is_complete = true;
		notify();
	}

	/** Actual "work" to be performed */
	abstract protected void perform() throws Exception;

	/** Compare this work with another one */
	@Override
	public int compareTo(Work other) {
		return Long.signum(id - other.id);
	}

	/** Wait for the work to be completed.
	 * @param ms Time to wait before giving up.
	 * @throws TimeoutException if timeout expires before completion. */
	public synchronized final void waitForCompletion(long ms)
		throws TimeoutException
	{
		long waited = 0;
		while (!is_complete) {
			if (waited >= ms)
				throw new TimeoutException();
			long w = Math.min(100, ms - waited);
			try {
				TimeSteward.wait(this, w);
				waited += w;
			}
			catch (InterruptedException e) {
				// keep waiting
			}
		}
	}
}
