/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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



package us.mn.state.dot.tms.comm.dmslite;

//~--- JDK imports ------------------------------------------------------------

import java.util.Calendar;
import java.util.Date;

//import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 *  Common Time class, provides convenience methods.
 */
public class Time {

    /*
     * test Time methods. This method helps define procedural contracts for each method.
     */
    public static boolean test() {
        boolean ok = true;

        // Time.subtract
        System.err.println("Time.subtract():" + ok);

        Date d = Time.getCurTime();

        System.err.println("          Current Time (UTC):" + Time.getCurTimeSparseString(d, false));
        System.err.println("          Current Time (loc):" + Time.getCurTimeSparseString(d, true));
        System.err.println("  Current Time minus 20 mins:"
                           + Time.getCurTimeSparseString(Time.subtract(d, 20 * 60 * 1000), false));
        System.err.println("  Current Time minus 60 mins:"
                           + Time.getCurTimeSparseString(Time.subtract(d, 60 * 60 * 1000), false));
        System.err.println("    Current Time minus 1 day:"
                           + Time.getCurTimeSparseString(Time.subtract(d, 24 * 60 * 60 * 1000), false));
        System.err.println("Time.subtract():" + ok);

        // Time.toXMLDateTime
        System.err.println("Time.toXMLDateTime():" + ok);

        String xmlt = Time.toXMLDateTime(d, false);

        System.err.println("     Current time as XML date (UTC):" + xmlt);
        System.err.println("   Current time as XML date (local):" + Time.toXMLDateTime(d, true));

        Date xmldate_utc = Time.XMLtoDate(xmlt);

        if (xmldate_utc == null) {
            ok = false;
        } else {
            System.err.println("Time.XMLUTCtoDate(),UTC: " + xmldate_utc.toString());
            System.err.println("                  d,UTC: " + xmldate_utc.toString());

            String xmldate_string = Time.toXMLDateTime(xmldate_utc, false);

            ok = ok && xmldate_string.equals(xmlt);
            ok = ok && xmldate_utc.toString().equals(d.toString());
        }

        System.err.println("Time.toXMLDateTime():" + ok);
        System.err.println("Time.test():" + ok);

        return (ok);
    }

    /*
     * given a java.util.Date, return the number of
     * milliseconds UTC since Jan 1st 1970 00:00:00.
     */
    public static long getUTCinMillis(java.util.Date d) {
        long ms = d.getTime();

        return (ms);
    }

    /**
     *  get current time in MS (UTC) since Jan 1st 1970 00:00:00.
     */
    public static long getCurTimeUTCinMillis() {
        java.util.Date d = Time.getCurTime();
        long           t = Time.getUTCinMillis(d);

        return (t);
    }

    /**
     *  get current time as java.util.Date
     */
    public static Date getCurTime() {
        Calendar       cal = new GregorianCalendar();
        java.util.Date d   = cal.getTime();

        return (d);
    }

    /**
     *      calc time difference between now (UTC since 1970)
     *  and given start time in MS.
     */
    public static long calcTimeDeltaMS(long startInUTC) {
        long d = Time.getCurTimeUTCinMillis() - startInUTC;

        return (d);
    }

    /**
     *      get current time as string in either local or UTC.
     *  e.g.: '14:23:09.342'
     */
    public static String getCurTimeString(boolean local) {

        // Date d=Time.getCurTimeDate();
        // d.get
        Calendar cal = new GregorianCalendar();

        if (!local) {
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        int    hour24 = cal.get(Calendar.HOUR_OF_DAY);    // 0..23
        int    min    = cal.get(Calendar.MINUTE);         // 0..59
        int    sec    = cal.get(Calendar.SECOND);         // 0..59
        int    ms     = cal.get(Calendar.MILLISECOND);    // 0..999
        String t      = "";

        t += SString.intToString(hour24, 2) + ":";
        t += SString.intToString(min, 2) + ":";
        t += SString.intToString(sec, 2) + ".";
        t += ms;

        return (t);
    }

    /**
     *      get current time as short string in either UTC or local time.
     *  e.g.: '23:98:74'
     */
    public static String getCurTimeShortString(boolean local) {
        Calendar cal = new GregorianCalendar();

        if (!local) {
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        int    hour24 = cal.get(Calendar.HOUR_OF_DAY);    // 0..23
        int    min    = cal.get(Calendar.MINUTE);         // 0..59
        int    sec    = cal.get(Calendar.SECOND);         // 0..59
        String t      = "";

        t += SString.intToString(hour24, 2) + ":";
        t += SString.intToString(min, 2) + ":";
        t += SString.intToString(sec, 2);

        return (t);
    }

    /**
     *  get current date as string in either UTC or local time.
     *  e.g. '2007-02-13 17:11:25.338'
     */
    public static String getCurDateTimeMSString(boolean local) {
        String                     DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
        java.text.SimpleDateFormat sdf         = new java.text.SimpleDateFormat(DATE_FORMAT);
        Calendar                   c           = new GregorianCalendar();

        if (!local) {
            c.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        String dt = sdf.format(c.getTime());

        return (dt);
    }

    /**
     *      get current date and time as string in either UTC or local time.
     *  e.g. '2006-10-09 19:48:48'
     */
    public static String getCurDateTimeString(boolean local) {
        String                     DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        java.text.SimpleDateFormat sdf         = new java.text.SimpleDateFormat(DATE_FORMAT);
        Calendar                   c           = new GregorianCalendar();

        if (!local) {
            c.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        String dt = sdf.format(c.getTime());

        return (dt);
    }

    /**
     *      get current date and time as string that can be used in a file name.
     *  the arg controls if it is in UTC or local time.
     *      e.g. '102106160533' for 10/21/06, 16:05:33
     */
    public static String getCurTimeSparseString(boolean local) {
        java.util.Date d  = Time.getCurTime();
        String         dt = Time.getCurTimeSparseString(d, local);

        return (dt);
    }

    /**
     *  given a date, format and return the date as a string
     *  that can be used in a file name, formated in UTC.
     *  e.g. 102106160533 for 10/21/06, 16:05:33
     */
    public static String getCurTimeSparseString(java.util.Date d, boolean local) {
        String                     DATE_FORMAT = "MMddyyHHmmss";
        java.text.SimpleDateFormat df          = new java.text.SimpleDateFormat(DATE_FORMAT);

        if (!local) {
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        String dt = df.format(d);

        return (dt);
    }

    /**
     *  given Date return a string date in XML Schema either local or UTC.
     *  format 'YYYY-MM-DDThh:mm:ssZ'.
     *     e.g. 2008-03-22T02:04:21Z
     *          01234567890123456789
     */
    public static String toXMLDateTime(java.util.Date d, boolean local) {
        String                     DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        java.text.SimpleDateFormat sdf         = new java.text.SimpleDateFormat(DATE_FORMAT);

        if (!local) {
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        String dt   = sdf.format(d);
        String xmld = dt.substring(0, 10) + "T" + dt.substring(11) + "Z";

        assert xmld.length() == 20 : "Time.toXMLDateTime";

        return (xmld);
    }

    /**
     *  given a date in XML time in UTC, return a Date.
     *  this method only handles times in the format:
     * 
     *  'YYYY-MM-DDThh:mm:ssZ'
     *   01234567890123456789
     * 
     *  @throws IllegalArgumentException if an illegal date string is received.
     */
    public static Date XMLtoDate(String xml) throws IllegalArgumentException {

        // sanity check
        if ((xml == null) || (xml.length() != 20)) {
            throw new IllegalArgumentException("Bogus XML date string received: " + xml);
        }

        if ((xml.charAt(4) != '-') && (xml.charAt(7) != '-') && (xml.charAt(13) != ':') && (xml.charAt(16) != ':')) {
            throw new IllegalArgumentException("Bogus XML date string received: " + xml);
        }

        int      y  = SString.stringToInt(xml.substring(0, 4));
        int      m  = SString.stringToInt(xml.substring(5, 7)) - 1;    // month is zero based
        int      d  = SString.stringToInt(xml.substring(8, 10));
        int      h  = SString.stringToInt(xml.substring(11, 13));
        int      mi = SString.stringToInt(xml.substring(14, 16));
        int      s  = SString.stringToInt(xml.substring(17, 19));
        Calendar c  = new GregorianCalendar(y, m, d, h, mi, s);

        c.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date date = c.getTime();

        return (date);
    }

    /**
     *  given Date return a new Date subtracted by arg MS.
     */
    public static Date subtract(java.util.Date d, long ms) {

        // build a Date ms earlier than current
        long     now     = d.getTime();
        long     newtime = now - ms;    // subtract ms
        Calendar cal     = new GregorianCalendar();

        cal.setTimeInMillis(newtime);    // set time in millis UTC from start of epoch

        Date newd = cal.getTime();

        return (newd);
    }

    /**
     *  return true if the 2nd specified arg is after the 1st.
     */
    public static boolean after(Date d1, Date d2) {
        long l1 = d1.getTime();
        long l2 = d2.getTime();

        return (l1 < l2);
    }

    /**
     *  given a date in the following format, return a Date.
     *  an arg specifies if a UTC or local time zone.
     * 
     *     e.g. '111606181951'
     *           MMDDYYhhmmss
     *           012345678901
     */
    public static Date ShortStringToDate(String xml, boolean local) {
        if ((xml == null) || (xml.length() != 12)) {
            return (null);
        }

        int      m  = SString.stringToInt(xml.substring(0, 2)) - 1;    // month is zero based
        int      d  = SString.stringToInt(xml.substring(2, 4));
        int      y  = SString.stringToInt(xml.substring(4, 6)) + 2000;
        int      h  = SString.stringToInt(xml.substring(6, 8));
        int      mi = SString.stringToInt(xml.substring(8, 10));
        int      s  = SString.stringToInt(xml.substring(10, 12));
        Calendar c  = new GregorianCalendar(y, m, d, h, mi, s);

        if (!local) {
            c.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        Date date = c.getTime();

        return (date);
    }
}
