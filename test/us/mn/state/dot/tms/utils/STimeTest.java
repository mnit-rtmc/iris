/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
 * @created 04/09/09
 */
public class STimeTest extends TestCase {

	/** constructor */
	public STimeTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {

		// testing DST methods requires that the zone be set 
		// to support DST
		boolean run_dst_tests = false;

		// does default TZ support DST?
		System.err.println("The default time zone supports DST: " + 
			STime.verifyTimeZoneSupportsDST());

		// verify default time zone is specified correctly 
		// via command line arg.
		// note: for the ant test cases, use the jvmarg statement to 
		//       specify the time zone for the jvm that runs the junit
		//       tests.
		if(run_dst_tests) {
			if(!STime.verifyDefaultTimeZone("Pacific Standard Time")) {
				System.err.println(
					"Specify the correct time zone using the " +
					"command line switch: -Duser.timezone=" +
					"America/Los_Angeles");
				assertTrue(false);
			}
		}

		// STime.subtract and STime.add
		{
			Date d1 = STime.getCurTime();
			Date d2 = STime.subtract(d1, 5 * 1000);
			assertTrue(d2.before(d1));
			long delta = d1.getTime() - d2.getTime();
			assertTrue(delta == 5 * 1000);
		}

		// STime.XMLtoDate
		if(run_dst_tests) {
			// this date is within DST so should convert to GMT-7
			Date d1 = STime.XMLtoDate("2008-04-29T15:37:22Z");
			assertTrue(d1.toString().equals(
				"Tue Apr 29 08:37:22 PDT 2008"));
		}

		// STime.toXMLDateTime
		if(run_dst_tests) {
			// note, this time is within DST so is GMT-7
			Date d = new Date(2008 - 1900, 4 - 1, 29, 8, 53, 31);
			String xml = STime.toXMLDateTime(d);
			assertTrue(xml, xml.equals("2008-04-29T15:53:31Z"));
		}

		// STime.XMLtoCalendar
		{
			Calendar c1 =
				STime.XMLtoCalendar("2008-04-29T15:37:22Z");
			assertTrue(c1.get(Calendar.DAY_OF_MONTH) == 29);
			assertTrue(c1.get(Calendar.HOUR_OF_DAY) == 15);
			assertTrue(c1.get(Calendar.MINUTE) == 37);
			assertTrue(c1.get(Calendar.YEAR) == 2008);
			assertTrue(c1.get(Calendar.MONTH) == 4 - 1);
			assertTrue(c1.get(Calendar.SECOND) == 22);
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

		// STime.dateToString
		{
			String s = STime.dateToString(STime.getCurTime(), true);
			assertTrue(s, s.length() == 19);
		}

		// STime.stringToDate
		{
			Date d = STime.stringToDate("2009-03-07 15:51:08", true);
			assertTrue(d.getYear()==2009 - 1900);
			assertTrue(d.getMonth()==3-1);
			assertTrue(d.getDate()==7);
			assertTrue(d.getHours()==15);
			assertTrue(d.getMinutes()==51);
			assertTrue(d.getSeconds()==8);
		}

		// STime.secondsDiff
		{
			Date d1 = new Date();
			STime.sleep(1600);
			Date d2 = new Date();
			assertTrue(STime.secondsDiff(d1, d2) == 2);
			assertTrue(STime.secondsDiff(d2, d1) == 2);
			assertTrue(STime.secondsDiff(d2, d2) == 0);
		}

		// STime.secondsDiffMin
		{
			Date d1 = new Date();
			STime.sleep(1600);
			Date d2 = new Date();
			assertTrue(STime.secondsDiffMin(d1, d2) == 1);
			assertTrue(STime.secondsDiffMin(d2, d1) == 1);
			assertTrue(STime.secondsDiffMin(d1, d1) == 0);
		}

		// STime.secondsDiffMax
		{
			Date d1 = new Date();
			STime.sleep(1100);
			Date d2 = new Date();
			assertTrue(STime.secondsDiffMax(d1, d2) == 2);
			assertTrue(STime.secondsDiffMax(d1, d1) == 0);
		}
	}
}
