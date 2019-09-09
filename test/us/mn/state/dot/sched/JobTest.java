/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2019  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.Calendar;
import junit.framework.TestCase;

/** 
 * Job tests
 *
 * @author Doug Lau
 */
public class JobTest extends TestCase {

	protected final Scheduler scheduler = new Scheduler();

	public JobTest(String name) {
		super(name);
	}

	public void testRepeating() {
		final ArrayList<Long> times = new ArrayList<Long>();
		Job job = new Job(Calendar.SECOND, 5) {
			public void perform() {
				times.add(System.currentTimeMillis());
			}
		};
		scheduler.addJob(job);
		while (times.size() < 2) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				assertTrue(false);
			}
		}
		long first = times.get(0);
		long second = times.get(1);
		long elapsed = second - first;
		System.out.println("Elapsed: " + elapsed);
		assertTrue(elapsed >= (5000 - 1));
	}
}
