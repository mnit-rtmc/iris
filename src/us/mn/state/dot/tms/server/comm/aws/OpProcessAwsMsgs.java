/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.aws;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.ControllerOperation;
import us.mn.state.dot.tms.server.comm.HttpFileMessenger;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.STime;

/**
 * This operation reads the DMS messages from the AWS generated
 * message file and sets new DMS messages.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpProcessAwsMsgs extends ControllerOperation
{
	/** This operation; needed for inner Phase classes */
	protected final OpProcessAwsMsgs operation;

	/** Create a new device operation */
	protected OpProcessAwsMsgs(ControllerImpl c) {
		super(DATA_30_SEC, c);
		operation = this;
	}

	/** Begin the operation */
	public final void begin() {
		phase = new PhaseReadMsgFile();
	}

	/** Cleanup the operation */
	public void cleanup() {
		super.cleanup();
	}

	/** Phase to read the aws dms message file */
	protected class PhaseReadMsgFile extends Phase
	{
		/**
		 * Execute the phase.
		 * @throws IOException received from getRequest call.
		 */
		protected Phase poll(AddressedMessage argmess)
			throws IOException 
		{
			Log.finest(
				"OpProcessAwsMsgs.PhaseReadMsgFile.poll() " +
				"called: " + 
				STime.getCurDateTimeMSString(true));
			assert argmess instanceof Message : "wrong message type";

			Message mess = (Message) argmess;

			// send msg
			mess.getRequest();

			// parse the response
			byte[] bmsgs = mess.getDmsMsgs();

			// nothing?
			if((bmsgs == null) || (bmsgs.length <= 0)) {
				Log.finest(
					"OpProcessAwsMsgs.poll(): " +
					" missing or zero length AWS file.");
				return null;
			}

			// create and activate messages
			Log.finest(
			    "OpProcessAwsMsgs.PhaseReadMsgFile.poll(), received "
			    + bmsgs.length + " bytes of DMS messages.");
			new AwsMsgs(bmsgs).activate();
			return null;
		}
	}
}
