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

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SystemPolicy;
import us.mn.state.dot.tms.TMSObjectImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 * Operation to download default values to a DMS
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpDmsDefaultDownload extends OpDms {

    /** Create a new DMS default download object */
    public OpDmsDefaultDownload(DMSImpl d) {
        super(DOWNLOAD, d);
        controller.setSetup("OK");
    }

    /** Create the first real phase of the operation */
    protected Phase phaseOne() {
        return new QueryCurrentMessage();
    }

    /** Cleanup the operation */
    public void cleanup() {
        if (success) {
            m_dms.notifyUpdate();
        } else {
            controller.setSetup(null);
        }

        super.cleanup();
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
    protected class QueryCurrentMessage extends Phase {

        /** Query current message */
        protected Phase poll(AddressedMessage argmess) throws IOException {
            assert argmess instanceof Message : "Wrong Message type received in OpDmsDefaultDownload";

            Message mess = (Message) argmess;

            System.err.println("OpDmsDefaultDownload.QueryCurrentMessage.poll() called.");

            // build message to send
            mess.setName("StatusReqMsg");
            mess.add(new Pair("Date", "03/12/08 09:24:49"));

            // send message
            mess.getRequest();

            /*
             *           DmsIllumMaxPhotocellLevel level =
             *                   new DmsIllumMaxPhotocellLevel();
             *           mess.add(level);
             *           DmsIllumNumBrightLevels levels =
             *                   new DmsIllumNumBrightLevels();
             *           mess.add(levels);
             *           mess.getRequest();
             *           m_dms.setMaxPhotocellLevel(level.getInteger());
             *           m_dms.setBrightnessLevels(levels.getInteger());
             *           return new BrightnessTable();
             */

            // this operation is complete
            return null;
        }
    }
}
