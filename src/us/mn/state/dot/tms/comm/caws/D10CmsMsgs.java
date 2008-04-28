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

import java.util.LinkedList;
import java.util.StringTokenizer;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.DMSListImpl;
import us.mn.state.dot.tms.TMSObjectImpl;
import us.mn.state.dot.tms.TMSObject;

/**
 * CAWS D10CmsMsgs. This is a collection of CMS messages.
 *
 * @author Michael Darter
 */
public class D10CmsMsgs {

    // fields
    LinkedList<D10CmsMsg> m_msgs = null;

    /** constructor */
    public D10CmsMsgs(byte[] bmsgs) {
        System.err.println("D10CmsMsgs.D10CmsMsgs() called.");
        this.parse(bmsgs);
    }

    /**
     * parse a byte array of messages and add each cms message to the container.
     */
    private void parse(byte[] argmsgs) {
        m_msgs = new LinkedList<D10CmsMsg>();

        // cycle through each line, which is terminated by '\n'
        String          msgs    = byteArrayToString(argmsgs);
        StringTokenizer lineTok = new StringTokenizer(msgs, "\n");

        while (lineTok.hasMoreTokens()) {
            String    line   = lineTok.nextToken();
            D10CmsMsg cmsmsg = new D10CmsMsg(line);

            this.m_msgs.add(cmsmsg);
        }
    }

    /**
     * activate the messages.
     */
    public void activate() {
        System.err.println("D10CmsMsgs.activate() called.");
        System.err.println("D10CmsMsgs.activate(). list="+TMSObjectImpl.dmsList);

        // sanity check
        if (m_msgs == null) {
            return;
        }

        // activate each msg
        DMSListImpl list=TMSObjectImpl.dmsList;     // list of all DMS
        for (D10CmsMsg m : m_msgs) {

            // get the iris cms id, e.g. "V30"
            String irisCmsId=m.getIrisCmsId();
            TMSObject tmsobj=list.getElement(irisCmsId);
            if (tmsobj==null) {
                //System.err.println("D10CmsMsgs.activate(): did not find DMSImpl for CMS id from CAWS ("+irisCmsId+"). CAWS CMS message ignored.");
                continue;
            }
            assert tmsobj instanceof DMSImpl : "Expected DMSImpl, received:"+tmsobj;
            DMSImpl dms=(DMSImpl)tmsobj;
            System.err.println("D10CmsMsgs.activate(): irisCmsId="+irisCmsId+", dms.getId()="+dms.getId()+", getPin()="+dms.getPin()+",notes="+dms.getNotes());

            // activate message
            m.activate(dms);
        }
    }

    /**
     * convert byte[] to char[] using specific encoding.
     *
     * @returns An empty string on error.
     */
    public static String byteArrayToString(byte[] b) {
        String s = "";

        if (b == null) {
            return (s);
        }

        int len = b.length;

        try {
            s = new String(b, 0, len, "ISO-8859-1");
        } catch (Exception UnsupportedEncodingException) {
            s = "";
        }

        return (s);
    }
}
