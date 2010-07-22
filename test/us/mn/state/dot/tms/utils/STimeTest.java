/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.awt.Toolkit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import junit.framework.TestCase;

/** 
 * Test cases
 * @author Michael Darter
 */
public class STimeTest extends TestCase {

	/** constructor */
	public STimeTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {

		// FIXME: these tests should be rewritten to test the STime
		//        functionality instead of testing if time zone has
		//        been set to PDT
		boolean run_dst_tests = false;

		// STime.XMLtoDate
		if(run_dst_tests) {
			// this date is within DST so should convert to GMT-7
			Date d1 = STime.XMLtoDate("2008-04-29T15:37:22Z");
			assertTrue(d1.toString().equals(
				"Tue Apr 29 08:37:22 PDT 2008"));
		}

		// STime.CalendarToXML
		if(run_dst_tests) {
			// local time
			Calendar c = new GregorianCalendar(2008, 
				4 - 1, 29, 8, 53, 31);    // local time in DST
			String xml = STime.CalendarToXML(c);
			assertTrue(xml.equals("2008-04-29T15:53:31Z")); // UTM
		}

		// DST conversions
		if(run_dst_tests) {
			Calendar c = new GregorianCalendar();
			System.err.println("Default Locale:" + Locale.getDefault());
			System.err.println("Default TimeZone:"
				 + c.getTimeZone().getDisplayName());
			System.err.println("Default TimeZone getDSTSavings():"
				 + c.getTimeZone().getDSTSavings() / 1000 / 60
				   / 60);
			System.err.println("Default DST_OFFSET:"
				 + c.get(Calendar.DST_OFFSET) / 1000 / 60 / 60
				 + " hours");
			System.err.println("Default ZONE_OFFSET:"
				 + c.get(Calendar.ZONE_OFFSET) / 1000 / 60 / 60
				 + " (hours).");

			// using default time zone
			c = new GregorianCalendar();
			c.set(2008, 4 - 1, 29, 8, 53, 31);    // within DST
			System.err.println("In default zone, local time is:"
				 + c.getTime().toString());
			assertTrue(c.getTime().toString().equals(
				"Tue Apr 29 08:53:31 PDT 2008"));

			Date local = c.getTime();

			System.err.println("As Date:" + local);
			assertTrue(local.toString().equals(
				"Tue Apr 29 08:53:31 PDT 2008"));
			System.err.println("As Date to UTC:" + local.toGMTString());
			assertTrue(local.toGMTString().toString().equals(
				"29 Apr 2008 15:53:31 GMT"));
		}
	}
}
