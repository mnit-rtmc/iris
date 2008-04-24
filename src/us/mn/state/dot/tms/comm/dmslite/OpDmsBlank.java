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

import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.comm.AddressedMessage;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 * Operation to blank the DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpDmsBlank extends OpDms {

    /** associated DMS */
    private final DMSImpl m_dms;

    /** blank message, which contains owner, duration */
    private final SignMessage m_mess;

    /** Create a new DMS query configuration object */
    public OpDmsBlank(DMSImpl d, SignMessage mess) {
        super(DOWNLOAD, d);
        m_dms  = d;
        m_mess = mess;
    }

    /** Create the first phase of the operation */
    protected Phase phaseOne() {
        return new PhaseSetBlank();
    }

    /** Cleanup the operation */
    public void cleanup() {
        if (success) {
            m_dms.notifyUpdate();
        }

        super.cleanup();
    }

    /**
     * Phase to query the dms config
     * Note, the type of exception throw here determines
     * if the messenger reopens the connection on failure.
     *
     * @see MessagePoller#doPoll()
     * @see Messenger#handleException()
     * @see Messenger#shouldReopen()
     */
    protected class PhaseSetBlank extends Phase {

        /** Query the number of modules */
        protected Phase poll(AddressedMessage argmess) throws IOException {

            System.err.println("dmslite.OpDmsBlank.PhaseGetConfig.poll(msg) called. m_mess.duration="+m_mess.getDuration());
            assert argmess instanceof Message : "wrong message type";

            Message mess = (Message) argmess;

            // sanity check
            if (m_mess.getDuration() > 0) {
                System.err.println("Bogus duration received in OpDmsBlank.PhaseSetBlank().");

                return (null);
            }

            // build message: <DmsLite><SetBlankMsgReqMsg><Address>1</Address><Owner>bob</Owner></SetBlankMsgReqMsg></DmsLite>

            // build req msg
            String reqname = "SetBlankMsgReqMsg";
            String resname = "SetBlankMsgRespMsg";

            mess.setName(reqname);
            mess.setReqMsgName(reqname);
            mess.setRespMsgName(resname);

            String drop = Integer.toString(m_dms.getController().getDrop());
            ReqRes rr1   = new ReqRes("Address", drop, new String[] { "IsValid" });
            mess.add(rr1);
            ReqRes rr2   = new ReqRes("Owner", m_mess.getOwner(), new String[0]);
            mess.add(rr2);

            // send msg
            mess.getRequest();

            // response: <DmsLite><SetBlankMsgRespMsg><IsValid>true</IsValid></SetBlankMsgRespMsg></DmsLite>

            // parse resp msg
            boolean isValid = false;

            try {
                isValid = new Boolean(rr1.getResVal("IsValid"));

                // valid resp received?
                System.err.println("dmslite.OpDmsBlank.PhaseSetBlank.poll(): success="+isValid);
            } catch (IllegalArgumentException ex) {
                System.err.println("Malformed XML received in dmslite.OpDmsBlank.PhaseSetBlank.poll(msg):" + ex);
                throw ex;
            }

            // update dms
            if (isValid) {
                String owner="bubba";        //FIXME
//mtod here
                m_dms.setMessage(m_mess);
                //m_dms.clearMessage(owner);
                //m_dms.setMessage(m_mess);
            }

            // done
            return null;
        }
    }
}
