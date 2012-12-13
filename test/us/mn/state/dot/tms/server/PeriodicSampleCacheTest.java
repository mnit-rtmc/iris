/*
 * IRIS -- Intelligent Roadway Information System
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
package us.mn.state.dot.tms.server;

import java.util.Calendar;
import java.util.Iterator;
import junit.framework.TestCase;

/** 
 * Periodic Sample Cache test cases
 * @author Doug Lau
 */
public class PeriodicSampleCacheTest extends TestCase {

	public PeriodicSampleCacheTest(String name) {
		super(name);
	}

	public void testVolume() {
		PeriodicSampleCache cache = new PeriodicSampleCache(
			PeriodicSampleType.VOLUME);
		assertTrue(isEmpty(cache));
		Calendar cal = Calendar.getInstance();
		cal.set(2012, Calendar.JANUARY, 1, 0, 0, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 2));
		assertFalse(isEmpty(cache));
		cal.set(2012, Calendar.JANUARY, 1, 0, 1, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 60, 4));
		assertFalse(isEmpty(cache));
		assertTrue(areSamplesEqual(cache, 2));
	}

	public void testOccupancy() {
		PeriodicSampleCache cache = new PeriodicSampleCache(
			PeriodicSampleType.OCCUPANCY);
		assertTrue(isEmpty(cache));
		Calendar cal = Calendar.getInstance();
		cal.set(2012, Calendar.JANUARY, 1, 0, 0, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 5));
		assertFalse(isEmpty(cache));
		cal.set(2012, Calendar.JANUARY, 1, 0, 1, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 60, 5));
		assertFalse(isEmpty(cache));
		assertTrue(areSamplesEqual(cache, 5));
	}

	public void testSpeed() {
		PeriodicSampleCache cache = new PeriodicSampleCache(
			PeriodicSampleType.SPEED);
		assertTrue(isEmpty(cache));
		Calendar cal = Calendar.getInstance();
		cal.set(2012, Calendar.JANUARY, 1, 0, 0, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 10));
		cal.set(2012, Calendar.JANUARY, 1, 0, 1, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 15));
		cal.set(2012, Calendar.JANUARY, 1, 0, 1, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 20));
		cal.set(2012, Calendar.JANUARY, 1, 0, 2, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 25));
		// missing sample 30 @ (2012-01-01 00:02:30)
		// missing sample 30 @ (2012-01-01 00:03:00)
		cal.set(2012, Calendar.JANUARY, 1, 0, 3, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 35));
		cal.set(2012, Calendar.JANUARY, 1, 0, 4, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 40));
		cal.set(2012, Calendar.JANUARY, 1, 0, 4, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 45));
		cal.set(2012, Calendar.JANUARY, 1, 0, 5, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 50));
		cal.set(2012, Calendar.JANUARY, 1, 0, 5, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 300, 30));
		assertFalse(isEmpty(cache));
		Iterator<PeriodicSample> it = cache.iterator();
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 10);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 15);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 20);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 25);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 30);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 30);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 35);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 40);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 45);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 50);
		assertFalse(it.hasNext());
	}

	public void testScan() {
		PeriodicSampleCache cache = new PeriodicSampleCache(
			PeriodicSampleType.SCAN);
		assertTrue(isEmpty(cache));
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);
		cal.set(2012, Calendar.JANUARY, 1, 0, 0, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 100));
		cal.set(2012, Calendar.JANUARY, 1, 0, 1, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 150));
		cal.set(2012, Calendar.JANUARY, 1, 0, 1, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 200));
		cal.set(2012, Calendar.JANUARY, 1, 0, 2, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 250));
		cal.set(2012, Calendar.JANUARY, 1, 0, 2, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 300));
		cal.set(2012, Calendar.JANUARY, 1, 0, 3, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 350));
		cal.set(2012, Calendar.JANUARY, 1, 0, 3, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 400));
		cal.set(2012, Calendar.JANUARY, 1, 0, 4, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 450));
		cal.set(2012, Calendar.JANUARY, 1, 0, 4, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 500));
		cal.set(2012, Calendar.JANUARY, 1, 0, 5, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 550));
		assertFalse(isEmpty(cache));
		cal.set(2012, Calendar.JANUARY, 1, 0, 3, 0);
		cache.purge(cal.getTimeInMillis());
		Iterator<PeriodicSample> it = cache.iterator();
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 350);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 400);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 450);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 500);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 550);
		assertFalse(it.hasNext());
	}

	private boolean isEmpty(PeriodicSampleCache cache) {
		return !cache.iterator().hasNext();
	}

	private boolean areSamplesEqual(PeriodicSampleCache cache, int val) {
		Iterator<PeriodicSample> it = cache.iterator();
		while(it.hasNext()) {
			PeriodicSample ps = it.next();
			if(ps.value != val)
				return false;
		}
		return true;
	}
}
