/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

//~--- non-JDK imports --------------------------------------------------------

import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.ParsingException;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.lang.IllegalArgumentException;

import java.util.LinkedList;

/**
 * DMS Lite Message. A Message represents the bytes sent and
 * received from a device. The interface is intended to be
 * flexible enough so that a single class can be used for
 * all message types. The DMSLite Message syntax uses XML.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class Message implements AddressedMessage {
    static final String DMSLITEMSGTAG = "DmsLite";    // xml tag

    // consts
    static final int TIMEOUT_DMS_MS = 1000 * 20;    // max wait time for DMS response

    // fields
    private String m_name        = "DmsLiteMsg";
    private String m_reqmsgname  = "";
    private String m_respmsgname = "";

    /** List of objects set or get with this message */
    protected final LinkedList<Object> m_objlist = new LinkedList<Object>();
    private final TokenStreamReader    m_is;
    private final OutputStream         m_os;

    /** Create a new message */
    public Message(OutputStream os, InputStream is) {
        m_os = os;
        m_is = new TokenStreamReader(is, 1024, 16384, 1000);    // buffer size, max cap, sleep time
        System.err.println("dmslite.Message.Message() called.");
    }

    /** set message name */
    public void setName(String n) {
        m_name = n;
    }

    /** get message name */
    public String getName() {
        return (m_name);
    }

    /** set request message name */
    public void setReqMsgName(String n) {
        m_reqmsgname = n;
    }

    /** get request message name */
    public String getReqMsgName() {
        return (m_reqmsgname);
    }

    /** set response message name */
    public void setRespMsgName(String n) {
        m_respmsgname = n;
    }

    /** get response message name */
    public String getRespMsgName() {
        return (m_respmsgname);
    }

    /**
     * Add an object to this message. The object must be a Pair with the car
     * being a String representing the name of the element, and the cdr being
     * the element value.
     */
    public void add(Object mo) {

        // arg check
        if (!(mo instanceof ReqRes)) {
            throw new IllegalArgumentException("dmslite.Message.add() wrong arg type.");
        }

        m_objlist.add(mo);
    }

    /** Send a get request message */
    public void getRequest(String community) throws IOException {

/*
        is.skip(is.available());
        encodeVarBindList(false);
        encodeRequestPDU(Tag.GET_REQUEST);
        encodeSNMPMessage(community);
        encoder.writeTo(m_os);
        encoder.reset();
        m_os.flush();
        decodeSNMPMessage(is, community);
        decodeResponsePDU(is);
        decodeVarBindList(is);
*/
    }

    /**
     * Send a get request message
     *
     * @throws IOException if received response is malformed.
     */
    public void getRequest() throws IOException {
        System.err.println("dmslite.Message.getRequest() called.");

        // build message
        byte[] array = this.buildReqMsg();

        // send message
        System.err.print("Writing " + array.length + " bytes to cmsserver....");
        m_os.write(array);
        m_os.flush();
        System.err.println("write done.");

        // read response
        String token;

        try {
            token = m_is.readToken(TIMEOUT_DMS_MS, "<" + DMSLITEMSGTAG + ">", "</" + DMSLITEMSGTAG + ">");
        } catch (IllegalStateException ex) {
            throw new IllegalStateException("Capacity exceeded in dmslite.Message.getRequest(): " + ex);
        } catch (IOException ex) {
            throw new IOException("CMS disconnected:" + ex);
        }

        // timed out?
        if (token == null) {
            System.err.println("dmslite.Message.getRequest(): timed out waiting for CMS (timeout is "
                               + TIMEOUT_DMS_MS / 1000 + " secs).");

            // parse response
        } else {
            System.err.println("dmslite.Message.getRequest(): found complete token:" + token);

            // fill in returned fields for each ReqRes using received xml string
            for (Object i : m_objlist) {
                assert i instanceof ReqRes;

                ReqRes rr = (ReqRes) i;

                rr.parseRes(DMSLITEMSGTAG, this.getRespMsgName(), token);    // throws IOException
            }
        }

        // getRequest(PUBLIC);
    }

    /** Send a set request message */
    public void setRequest(String community) throws IOException {

/*
        is.skip(is.available());
        encodeVarBindList(true);
        encodeRequestPDU(Tag.SET_REQUEST);
        encodeSNMPMessage(community);
        encoder.writeTo(m_os);
        encoder.reset();
        m_os.flush();
        decodeSNMPMessage(is, community);
        decodeResponsePDU(is);
        decodeVarBindList(is);
*/
    }

    /** Send an set request message */
    public void setRequest() throws IOException {

        // setRequest(PUBLIC);
    }

    /**
     * Return a send message with this format:
     *     <DmsLite><SendMsgReq>...etc...</SendMsgReq></DmsLite>
     */

/*
    public static byte[] buildSendMsg() {

    // build child tags
    StringBuilder children=new StringBuilder(256);
    Xml.addXmlTag(children,"Address","1");
    Xml.addXmlTag(children,"OnTime","1");
    Xml.addXmlTag(children,"OffTime","1");
    Xml.addXmlTag(children,"Bitmap","00112233445566778899aabbccddeeff");

    // enclose child tags in message tag
    StringBuilder msgtag=new StringBuilder(384);
    Xml.addXmlTag(msgtag,"SendMsgReq",children);

    // enclose message tag in top level doc
    StringBuilder doc=new StringBuilder(384);
    //Xml.addXmlDocHeader(doc);
    Xml.addXmlTag(doc,"DmsLite",msgtag);

    System.err.println("dmslite.Message.buildSendMsg(): message to send is "+doc.length()+" bytes, msg:"+doc.toString()+".");

    // convert to byte[]
    byte[] array=doc.toString().getBytes();

        return(array);
    }
*/

    /**
     * Return a request message with this format:
     *     <DmsLite><msg name>...etc...</msg name></DmsLite>
     */
    public byte[] buildReqMsg() {

        // build child tags
        StringBuilder children = new StringBuilder(256);

        for (Object i : m_objlist) {
            assert i instanceof ReqRes : "dmslite.Message() arg must be a ReqRes";

            // System.err.println("will add:"+i);
            Xml.addXmlTag(children, ((ReqRes) i).getReqName(), ((ReqRes) i).getReqVal());
        }

        // enclose child tags in message tag
        StringBuilder msgtag = new StringBuilder(384);

        Xml.addXmlTag(msgtag, this.getReqMsgName(), children);

        // enclose message tag in top level doc
        StringBuilder doc = new StringBuilder(384);

        Xml.addXmlTag(doc, DMSLITEMSGTAG, msgtag);
        System.err.println("dmslite.Message.buildReqMsg(): message to send is " + doc.length() + " bytes, msg:"
                           + doc.toString() + ".");

        byte[] array = doc.toString().getBytes();

        return (array);
    }
}
