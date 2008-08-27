/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2008  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package us.mn.state.dot.tms.comm.caws;

import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.ControllerOperation;
import us.mn.state.dot.tms.comm.HttpFileMessenger;

import java.io.IOException;

/**
 * This operation reads the DMS messages from the CAWS generated
 * message file and sets new DMS messages.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpProcessCawsMsgs extends ControllerOperation
{
	/** This operation; needed for inner Phase classes */
	protected final OpProcessCawsMsgs operation;

	/** Create a new device operation */
	protected OpProcessCawsMsgs(ControllerImpl c) {
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

	/** Phase to read the caws dms message file */
	protected class PhaseReadMsgFile extends Phase
	{
		/**
		 * Execute the phase.
		 * @throws IOException received from getRequest call.
		 */
		protected Phase poll(AddressedMessage argmess)
			throws IOException {
			System.err.println(
			    "OpProcessCawsMsgs.PhaseReadMsgFile.poll() called.");
			assert argmess instanceof Message : "wrong message type";

			Message mess = (Message) argmess;

			// send msg
			mess.getRequest();

			// parse the response
			byte[] bmsgs = mess.getDmsMsgs();

			// nothing?
			if((bmsgs == null) || (bmsgs.length <= 0)) {
				System.err.println(
				    "OpProcessCawsMsgs.PhaseReadMsgFile.poll(), missing or zero length caws file.");
				return null;
			}

			// create and activate messages
			System.err.println(
			    "OpProcessCawsMsgs.PhaseReadMsgFile.poll(), received "
			    + bmsgs.length + " bytes of cms messages.");
			new D10CmsMsgs(bmsgs).activate();
			return null;
		}
	}
}
