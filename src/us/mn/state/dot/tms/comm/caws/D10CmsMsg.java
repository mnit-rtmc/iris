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



package us.mn.state.dot.tms.comm.caws;

//~--- JDK imports ------------------------------------------------------------

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.rmi.RemoteException;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.BitmapGraphic;

/**
 * CAWS D10CmsMsg. This is a single CMS message.
 *
 * @author Michael Darter
 */
public class D10CmsMsg {

    // types
    public enum MSGTYPE {BLANK,ONEPAGEMSG,TWOPAGEMSG};

    // consts
    final String DESC_ONEPAGENORM = "1 Page (Normal)";
    final String DESC_TWOPAGENORM = "2 Page (Normal)";
    final String DESC_BLANK   = "Blank";
    final String SINGLESTROKE = "Single Stroke";
    final String DOUBLESTROKE = "Double Stroke";

    // fields
    private int     m_cmsid  = -1;       // cms ID
    private Date    m_date   = null;     // message date and time
    private String  m_desc   = "";       // this has predefined valid values
    private String  m_msg    = "";
    private double  m_ontime = 0;
    private boolean m_valid  = false;    // is message valid?

    /**
     * constructor
     *
     * @param line A single line from the CAWS message file. e.g.
     *             "20080403085910;25;Blank;Single Stroke;Single Stroke;;;;;;;0.0;"
     */
    public D10CmsMsg(String line) throws IllegalArgumentException {
        this.parse(line);
    }

    /**
     * parse a string that contains a single CMS message
     *
     * @param argline a single CMS message, fields delimited with ';'
     */
    private void parse(String argline) throws IllegalArgumentException {

        // add a space between successive delimiters. This is done so the
        // tokenizer doesn't skip over delimeters with nothing between them.
        // System.err.println("D10CmsMsg.D10CmsMsg() called, argline="+argline);
        String line = argline.replace(";;", "; ;");

        // System.err.println("D10CmsMsg.D10CmsMsg() called, line1="+line);
        line = line.replace(";;", "; ;");

        // System.err.println("D10CmsMsg.D10CmsMsg() called, line2="+line);
        // verify syntax
        StringTokenizer tok = new StringTokenizer(line, ";");

        // validity check
        int numtoks = tok.countTokens();

        if (numtoks != 13) {
            throw new IllegalArgumentException("Bogus CMS message format (" + argline + ").");
        }

        // date: 20080403085910
        m_date = convertDate(tok.nextToken());

        // id: 39
        m_cmsid = stringToInt(tok.nextToken());

        // message description
        String f02 = tok.nextToken();

        if (!f02.equals(DESC_BLANK) &&!f02.equals(DESC_ONEPAGENORM) &&!f02.equals(DESC_TWOPAGENORM)) {    // FIXME: verify possibilities
            System.err.println("WARNING: unknown message description received in D10CmsMsg.parse(): " + f02);
        }

        m_desc = f02;

        // pg 1 font
        String f03 = tok.nextToken();

        if (!f03.equals(SINGLESTROKE) &&!f03.equals(DOUBLESTROKE)) {
            System.err.println("WARNING: unknown pg 1 font received in D10CmsMsg.parse(): " + f03);
        }

        // pg 2 font
        String f04 = tok.nextToken();

        if (!f04.equals(SINGLESTROKE) &&!f04.equals(DOUBLESTROKE)) {
            System.err.println("WARNING: unknown pg 2 font received in D10CmsMsg.parse(): " + f04);
        }

        // rows of text
        {
            String row1 = tok.nextToken().trim().toUpperCase();
            String row2 = tok.nextToken().trim().toUpperCase();
            String row3 = tok.nextToken().trim().toUpperCase();
            String row4 = tok.nextToken().trim().toUpperCase();
            String row5 = tok.nextToken().trim().toUpperCase();
            String row6 = tok.nextToken().trim().toUpperCase();

            // create message-pg1
            StringBuilder m=new StringBuilder();
            m.append(row1);
            m.append("[nl]");
            m.append(row2);
            m.append("[nl]");
            m.append(row3);
            m.append("[nl]");
            // pg2
            if (row4.length()+row5.length()+row6.length()>0) {
                m.append("[np]");
                m.append(row4);
                m.append("[nl]");
                m.append(row5);
                m.append("[nl]");
                m.append(row6);
            }
            m_msg=m.toString();
        }

        // on time: 0.0
        m_ontime = stringToDouble(tok.nextToken());

        // ignore this field, nothing there
        String f12 = tok.nextToken();

        if (!m_desc.equals(DESC_BLANK)) {
            System.err.println("D10CmsMsg.D10CmsMsg():" + m_date+","+m_cmsid+","+m_msg+","+m_ontime);
        }
        this.checkValid();
    }

    /**
     *  Convert a local time date string from the d10 cms file to a Date.
     *
     *  @params date String date/time in the format "20080403085910" which is local time.
     *                                               01234567890123
     *  @returns A Date cooresponding to the argument.
     *  @throws IllegalArgumentException if the argument is bogus.
     */
    private static Date convertDate(String argdate) throws IllegalArgumentException {

        // sanity check
        if ((argdate == null) || (argdate.length() != 14)) {
            throw new IllegalArgumentException("Bogus date string received: " + argdate);
        }

        // note the column range is: inclusive, exclusive
        // year
        int y = stringToInt(argdate.substring(0, 4));

        if (y < 2008) {
            throw new IllegalArgumentException("Bogus year received:" + argdate + "," + y);
        }

        // month
        int m = stringToInt(argdate.substring(4, 6)) - 1;    // zero based

        if ((m < 0) || (m > 11)) {
            throw new IllegalArgumentException("Bogus month received:" + argdate + "," + m);
        }

        // day
        int d = stringToInt(argdate.substring(6, 8));

        if ((d < 1) || (d > 31)) {
            throw new IllegalArgumentException("Bogus day received:" + argdate + "," + d);
        }

        // hour
        int h = stringToInt(argdate.substring(8, 10));

        if ((h < 0) || (h > 23)) {
            throw new IllegalArgumentException("Bogus hour received:" + argdate + "," + h);
        }

        // min
        int mi = stringToInt(argdate.substring(10, 12));

        if ((mi < 0) || (mi > 59)) {
            throw new IllegalArgumentException("Bogus minute received:" + argdate + "," + mi);
        }

        // sec
        int s = stringToInt(argdate.substring(12, 14));

        if ((s < 0) || (s > 59)) {
            throw new IllegalArgumentException("Bogus second received:" + argdate + "," + s);
        }

        // create Date
        Calendar c = new GregorianCalendar(y, m, d, h, mi, s);

        // c.setTimeZone(TimeZone.getTimeZone("UTC"));  // sets UTC time zone
        Date date = c.getTime();

        return (date);
    }

    /** determine if message is valid */
    private boolean checkValid() {
        m_valid = true;

        // check
        m_valid = m_valid && true;    // FIXME: add checks

        return (m_valid);
    }

    /** return true if the CMS message is valid else false */
    public boolean getValid() {
        return (m_valid);
    }

    /** get message type */
    public MSGTYPE getMsgType() {

        MSGTYPE ret=MSGTYPE.BLANK;

        if (m_desc.equals(DESC_BLANK)) {
            ret=MSGTYPE.BLANK;
        }
        else if (m_desc.equals(DESC_ONEPAGENORM)) {
            ret=MSGTYPE.ONEPAGEMSG;
        }
        else if (m_desc.equals(DESC_TWOPAGENORM)) {
            ret=MSGTYPE.TWOPAGEMSG;
        }
        else {
            System.err.println("Warning: unknown D10 message description encountered ("+m_desc+").");
            ret=MSGTYPE.BLANK;
        }
        
        return(ret);
    }

    /** activate the message */
    public void activate(DMSImpl dms) {
        System.err.println("D10CmsMsg.activate("+dms+") called, msg="+this);

        MSGTYPE msgtype=this.getMsgType();

        // blank the message
        if (msgtype==MSGTYPE.BLANK) {
            System.err.println("D10CmsMsg.activate(): will blank CMS "+this.getIrisCmsId()+" for DMS="+dms+".");
            dms.clearMessage("CAWS");

        // set message
        } else if(msgtype==MSGTYPE.ONEPAGEMSG) {
            System.err.println("D10CmsMsg.activate(): will activate CMS "+this.getIrisCmsId()+":"+this);
            try {
                //dms.setMessage(owner,"MSG TEXT",SignMessage.DURATION_INFINITE); //FIXME: use actual msg txt
                dms.setMessage(this.toSignMessage(dms));
                dms.updateMessageGraphic(); //FIXME: move to dmsimpl called method?
            } catch (InvalidMessageException e) {
                System.err.println("D10CmsMsg.activate(): exception:"+e);
            }

        // set message
        } else if(msgtype==MSGTYPE.TWOPAGEMSG) {
            //FIXME

        // error
        } else {
            assert false : "D10CmsMsg.activate(): ERROR--unknown MSGTYPE.";
        }

    }

    /** 
     * toSignMessage builds a SignMessage version of this message.
     *
     * @params dms The associated DMS.
     * @returns A SignMessage that contains the text of the message and a rendered bitmap.
     */
    public SignMessage toSignMessage(DMSImpl dms) {
        System.err.println("D10CmsMsg.toSignMessage() called: dms.SignWidth="+dms.getSignWidthPixels()+
            ", getId()="+dms.getId()+", dms.SignHeight="+dms.getSignHeightPixels());

        // create multistring
		MultiString multi = new MultiString(m_msg);

        // create bitmap
		//BitmapGraphic bitmap = new BitmapGraphic(dms.getSignWidthPixels(),dms.getSignHeightPixels());
		BitmapGraphic bitmap = dms.createPixelMap(multi);

        // create signmessage
        String owner="CAWS";
		SignMessage sm=new SignMessage(owner, multi, bitmap,SignMessage.DURATION_INFINITE);
		return sm;
    }

    /** tostring */
    public String toString() {
        String s = "";

        s += "Message: " + m_msg + ", ";
        s += "On time: " + m_ontime;

        return (s);
    }

    /**
     * convert String to int.
     */
    private static int stringToInt(String s) {
        if (s == null) {
            return (0);
        }

        int i = 0;

        try {
            i = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            i = 0;
        }

        return (i);
    }

    /**
     * convert String to double.
     */
    public static double stringToDouble(String s) {
        if (s == null) {
            return (0);
        }

        double d = 0;

        try {
            d = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            d = 0;
        }

        return (d);
    }

    /** get the CMS id */
    public int getCmsId() {
        return(m_cmsid);
    }

    /** get the CMS id in IRIS form, e.g. "V39" */
    public String getIrisCmsId() {
        Integer id=this.getCmsId();
        return("V"+id.toString());
    }

}
