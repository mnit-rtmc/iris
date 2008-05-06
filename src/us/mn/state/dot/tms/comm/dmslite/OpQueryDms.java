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

import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.ntcip.DmsMessageMemoryType;
import us.mn.state.dot.tms.comm.ntcip.DmsMessageMultiString;
import us.mn.state.dot.tms.comm.ntcip.DmsMessageStatus;
import us.mn.state.dot.tms.comm.ntcip.DmsMessageTimeRemaining;

import java.io.IOException;
import java.util.Date;

/**
 * Operation to query the current message on a DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpQueryDms extends OpDms {

    /** Create a new DMS query status object */
    public OpQueryDms(DMSImpl d) {
        super(DEVICE_DATA, d);
    }

    /** Create the first real phase of the operation */
    protected Phase phaseOne() {

        System.err.println("dmslite.OpQueryDms.phaseOne() called.: m_dms.getSignWidthPixels()="+m_dms.getSignWidthPixels());

        // has getConfig() been called yet? If not, don't do anything
        // FIXME: there must be a better way to check for this condition
        if (m_dms.getSignWidthPixels()<=0) {
            return null;
        }

        return new PhaseQueryCurrentMessage();
    }

    /** 
     * Create a SignMessage.
     *
     * @params argmulti A String in the MultiString format.
     * @returns A SignMessage that contains the text of the message and a rendered bitmap.
     */
    private SignMessage createSignMessage(String owner,String argmulti,byte[] argbitmap) {
        System.err.println("OpQueryDms.createSignMessage() called: m_dms.width="+m_dms.getSignWidthPixels()+", argbitmap.len="+argbitmap.length+".");

        assert owner!=null;
        assert argmulti!=null;
        assert argbitmap!=null;

        int numpages=new MultiString(argmulti).getNumPages();
        System.err.println("OpQueryDms.createSignMessage(): numpages="+numpages);

        // force bitmap to be 1 page
        // FIXME: remove this? 
        if (numpages>1) {
            byte[] nbm=new byte[300];
            System.arraycopy(argbitmap,0,nbm,0,300);
            argbitmap=nbm;
        }

        // sanity check
        // FIXME: remove this?
        int height=m_dms.getSignHeightPixels();
        int width=m_dms.getSignWidthPixels();
		int len=((width * height + 7)/8)*numpages;
        if (len!=argbitmap.length) {
            System.err.println("OpQueryDms.createSignMessage(): ERROR, length mismatch: len="+len+",length()="+argbitmap.length+", numpages="+numpages);
        }

        // create SignMessage
		MultiString multi = new MultiString(argmulti);
		BitmapGraphic bitmap = new BitmapGraphic(width,height);
        try {
            bitmap.setBitmap(argbitmap);
        } catch (IndexOutOfBoundsException ex) {
            System.err.println("OpQueryDms.createSignMessage(): ERROR, could not set the bitmap, bitmaplen="+argbitmap.length);
        }
		SignMessage sm=new SignMessage(owner, multi, bitmap,SignMessage.DURATION_INFINITE); //FIXME: duration from cmsserver?

		return sm;
    }

    /**
     * Phase to get current message
     * Note, the type of exception throw here determines
     * if the messenger reopens the connection on failure.
     *
     * @see MessagePoller#doPoll()
     * @see Messenger#handleException()
     * @see Messenger#shouldReopen()
     */
    protected class PhaseQueryCurrentMessage extends Phase {

        /** Query current message */
        protected Phase poll(AddressedMessage argmess) throws IOException {
            System.err.println("OpQueryDms.PhaseQueryCurrentMessage.poll(msg) called.");
            assert argmess instanceof Message : "wrong message type";

            Message mess = (Message) argmess;

            // build req msg and expected response
            mess.setName("StatusReqMsg");
            mess.setReqMsgName("StatusReqMsg");
            mess.setRespMsgName("StatusRespMsg");
            String addr=new Integer((int)m_dms.getController().getDrop()).toString();
            ReqRes rr = new ReqRes("Address",addr,
                new String[] { "IsValid", "MsgTextAvailable", "MsgText", "Owner", "UseOnTime", "OnTime", 
                    "UseOffTime", "OffTime", "UseBitmap", "Bitmap" });

            // send msg
            mess.add(rr);
            mess.getRequest();

            // parse resp msg
            boolean valid = false;
            boolean msgtextavailable=false;
            String  msgtext="";
            String owner="";
            boolean useont=false;
            Date    ont=new Date();
            boolean useofft=false;
            Date    offt=new Date();
            boolean usebitmap=false;
            String  bitmap = "";

            // get valid flag
            try {
                valid = new Boolean(rr.getResVal("IsValid"));
                if (valid) {
                    msgtextavailable=new Boolean(rr.getResVal("MsgTextAvailable"));
                    msgtext=rr.getResVal("MsgText");
                    owner=rr.getResVal("Owner");

                    // ontime
                    useont=new Boolean(rr.getResVal("UseOnTime"));
                    if (useont) {
                        ont=Time.XMLtoDate(rr.getResVal("OnTime"));
                    }

                    // offtime
                    useofft=new Boolean(rr.getResVal("UseOffTime"));
                    if (useofft) {
                        offt=Time.XMLtoDate(rr.getResVal("OffTime"));
                    }

                    // bitmap
                    usebitmap=new Boolean(rr.getResVal("UseBitmap"));
                    bitmap = rr.getResVal("Bitmap");

                    System.err.println(
                        "OpQueryDms.PhaseQueryCurrentMessage.poll(msg) parsed msg values: IsValid:" + valid
                        +", MsgTextAvailable:"+msgtextavailable+", MsgText:"+msgtext+ ", OnTime:" + ont + ", OffTime:" + offt + ", bitmap:" + bitmap);
                }
            } catch (IllegalArgumentException ex) {
                System.err.println("OpQueryDms.PhaseQueryCurrentMessage: Malformed XML received:" + ex);
                throw ex;
            }

            // process response
            if (valid) {
                //System.err.println("OpQueryDms: valid response from cmsserver received.");

                // have text
                if (msgtextavailable) {
                    m_dms.setMessageFromController(msgtext, SignMessage.DURATION_INFINITE); //FIXME: calculate duration using on/off time

                // don't have text
                // note, the MsgText field is still assumed to contain a multistring with a message 
                // indicating a missing one or two page message.
                } else {
                    SignMessage sm;

                    // have bitmap
                    if (usebitmap) {
                        byte[] bm = Convert.hexStringToByteArray(bitmap);

                        //System.err.println("OpQueryDms: hex string length=" + bitmap.length() + ", byte[] length=" + bm.length);
                        BitmapGraphic bmg = new BitmapGraphic(96, 25);  //FIXME: use sign dims
                        //FIXME: calc duration
                        sm=createSignMessage(owner,msgtext, bm); 

                    // don't have bitmap, therefore CMS is blank
                    } else {
		                MultiString multi = new MultiString();
		                BitmapGraphic bbm = new BitmapGraphic(m_dms.getSignWidthPixels(),
			                m_dms.getSignHeightPixels());
		                sm=new SignMessage(owner, multi, bbm, 0);
                    }
                    
                    // set new message
                    m_dms.setActiveMessage(sm);
                }

            } else {
                System.err.println("OpQueryDms: response from cmsserver received, ignored because Xml valid field is false.");
            }

            // this operation is complete
            return null;
        }
    }
}
