/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2011  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.msgfeed;

import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.STime;

/**
 * This operation reads the DMS messages from the AWS generated
 * message file and sets new DMS messages.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpProcessAwsMsgs extends OpController
{
	/** This operation; needed for inner Phase classes */
	protected final OpProcessAwsMsgs operation;

	/** Create a new device operation */
	protected OpProcessAwsMsgs(ControllerImpl c) {
		super(PriorityLevel.DATA_30_SEC, c);
		operation = this;
	}

	/** Begin the operation */
	public final boolean begin() {
		phase = new PhaseReadMsgFile();
		return true;
	}

	/** Cleanup the operation */
	public void cleanup() {
		super.cleanup();
	}

	/** Phase to read the aws dms message file */
	protected class PhaseReadMsgFile extends Phase {

		/**
		 * Execute the phase.
		 * @throws IOException received from queryProps call.
		 */
		protected Phase poll(CommMessage argmess) throws IOException {
			Log.finest(
				"OpProcessAwsMsgs.PhaseReadMsgFile.poll() " +
				"called: " + 
				STime.getCurDateTimeMSString(true));
			assert argmess instanceof Message: "wrong message type";
			AwsProperty prop = new AwsProperty();
			Message mess = (Message)argmess;
			mess.add(prop);
			mess.queryProps();
			prop.activate();
			return null;
		}
	}
}
