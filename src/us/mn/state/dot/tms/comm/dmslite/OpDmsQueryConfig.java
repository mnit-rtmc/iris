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

import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;

import java.io.IOException;

/**
 * Operation to query the configuration of a DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpDmsQueryConfig extends OpDms {

    /** associated DMS */
    protected final DMSImpl dms;

    /** Create a new DMS query configuration object */
    public OpDmsQueryConfig(DMSImpl d) {
        super(DOWNLOAD, d);
        dms = d;
    }

    /** Create the first phase of the operation */
    protected Phase phaseOne() {
        return new PhaseGetConfig();
    }

    /** Cleanup the operation */
    public void cleanup() {
        if (success) {
            dms.notifyUpdate();
        }

        super.cleanup();
    }

    /** Phase to query the dms config */
    protected class PhaseGetConfig extends Phase {

        /**
         * Get the dms config.
         * Note, the type of exception throw here determines
         * if the messenger reopens the connection on failure.
         *
         * @see MessagePoller#doPoll()
         * @see Messenger#handleException()
         * @see Messenger#shouldReopen()
         */
        protected Phase poll(AddressedMessage argmess) throws IOException {

            // System.err.println("dmslite.OpDmsQueryConfig.PhaseGetConfig.poll(msg) called.");
            assert argmess instanceof Message : "wrong message type";

            Message mess = (Message) argmess;

	    // set message attributes as a function of the operation
	    setMsgAttributes(mess);

            // build req msg
            String reqname = "GetDmsConfigReqMsg";
            String resname = "GetDmsConfigRespMsg";

            mess.setName(reqname);
            mess.setReqMsgName(reqname);
            mess.setRespMsgName(resname);

            String drop = Integer.toString(dms.getController().getDrop());
            ReqRes rr   = new ReqRes("Address", drop, new String[] {
                "IsValid", "signAccess", "model", "make", "version", "type", "horizBorder", "vertBorder", "horizPitch", "vertPitch",
                "signHeight", "signWidth", "characterHeightPixels", "characterWidthPixels", "signHeightPixels",
                "signWidthPixels"
            });

            mess.add(rr);

            // send msg
            mess.getRequest();

            // parse resp msg
            boolean isValid               = false;
            String  model                 = "";
	    String  signAccess		  = "";
            String  make                  = "";
            String  version               = "";
            String  type                  = "";
            int     horizBorder           = 0;
            int     vertBorder            = 0;
            int     horizPitch            = 0;
            int     vertPitch             = 0;
            int     signHeight            = 0;
            int     signWidth             = 0;
            int     characterHeightPixels = 0;
            int     characterWidthPixels  = 0;
            int     signHeightPixels      = 0;
            int     signWidthPixels       = 0;

            try {
                isValid = new Boolean(rr.getResVal("IsValid"));

                // valid message received?
                if (isValid) {
		    signAccess		  = rr.getResVal("signAccess");
                    model                 = rr.getResVal("model");
                    make                  = rr.getResVal("make");
                    version               = rr.getResVal("version");
                    type                  = rr.getResVal("type");
                    horizBorder           = Convert.stringToInt(rr.getResVal("horizBorder"));
                    vertBorder            = Convert.stringToInt(rr.getResVal("vertBorder"));
                    horizPitch            = Convert.stringToInt(rr.getResVal("horizPitch"));
                    vertPitch             = Convert.stringToInt(rr.getResVal("vertPitch"));
                    signHeight            = Convert.stringToInt(rr.getResVal("signHeight"));
                    signWidth             = Convert.stringToInt(rr.getResVal("signWidth"));
                    characterHeightPixels = Convert.stringToInt(rr.getResVal("characterHeightPixels"));
                    characterWidthPixels  = Convert.stringToInt(rr.getResVal("characterWidthPixels"));
                    signHeightPixels      = Convert.stringToInt(rr.getResVal("signHeightPixels"));
                    signWidthPixels       = Convert.stringToInt(rr.getResVal("signWidthPixels"));

                    // System.err.println("dmslite.OpQueryDmsConfig.PhaseGetConfig.poll(msg) parsed msg values: isValid:"+
                    // isValid+", model:"+model+", make:"+make+"...etc.");
                }
            } catch (IllegalArgumentException ex) {
                System.err.println("Malformed XML received in dmslite.OpQueryDmsConfig.PhaseGetConfig.poll(msg):" + ex);

                throw ex;
            }

            // set config values
            // these values are displayed in the DMS dialog, Configuration tab
            if (isValid) {
                dms.setModel(model);
                dms.setSignAccess(signAccess);		// wizard, modem
                dms.setMake(make);
                dms.setVersion(version);
                dms.setSignType(type);
                dms.setHorizontalBorder(horizBorder);    // in mm
                dms.setVerticalBorder(vertBorder);       // in mm
                dms.setHorizontalPitch(horizPitch);
                dms.setVerticalPitch(vertPitch);

                // values not set for these
                dms.setSignLegend("sign legend");
                dms.setBeaconType("beacon type");
                dms.setSignTechnology("sign technology");

                // note, these must be defined for comboboxes
                // in the "Compose message" control to appear
                dms.setSignHeight(signHeight);    // mm
                dms.setSignWidth(signWidth);      // mm
                dms.setCharacterHeightPixels(characterHeightPixels);
                dms.setCharacterWidthPixels(characterWidthPixels);
                dms.setSignHeightPixels(signHeightPixels);
                dms.setSignWidthPixels(signWidthPixels);

                // update message graphic because sign params may have changed
                // dms.updateMessageGraphic(); //FIXME: enable this call?, but should not render from text
            }

            // done
            return null;
        }
    }
}
