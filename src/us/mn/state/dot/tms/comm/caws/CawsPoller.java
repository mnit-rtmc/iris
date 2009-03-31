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

import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.SystemAttributeHelperD10;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DiagnosticOperation;
import us.mn.state.dot.tms.comm.HttpFileMessenger;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.Messenger;
import us.mn.state.dot.tms.comm.SignPoller;
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * Caltrans D10 CAWS Poller. This class provides a Caltrans D10
 * CAWS driver, which periodically retrieves automatically
 * generated CMS messages from a specified URL.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class CawsPoller extends MessagePoller implements SignPoller
{
	/** the only valid drop address */
	static public final int VALID_DROP_ADDRESS = 1;

	/** Create a new poller */
	public CawsPoller(String n, Messenger m) {
		super(n, m);

		// System.err.println("CawsPoller.CawsPoller() called.");
		assert m instanceof HttpFileMessenger;
	}

	/** Create a new message for the specified controller, called by MessagePoller.doPoll(). */
	public AddressedMessage createMessage(ControllerImpl c) {

		// System.err.println("CawsPoller.createMessage() called.");
		return new Message(messenger);
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		return (drop == VALID_DROP_ADDRESS);
	}

	/**
	 * Perform a controller download. This method is also called
	 * when the user presses the 'download' button on the controller
	 * dialog in the status tab.
	 */
	public void download(ControllerImpl c, boolean reset, int p) {
		//System.err.println("CawsPoller.download() called, reset="
		//		   + reset);
	}

	/** Perform a sign status poll. Defined in SignPoller interface. */
	public void pollSigns(ControllerImpl c, Completer comp) {}

	/** Perform a 30-second poll */
	public void poll30Second(ControllerImpl c, Completer comp) {
		if(SystemAttributeHelperD10.isCAWSActive())
			new OpProcessCawsMsgs(c).start();
	}

	/** Perform a 5-minute poll */
	public void poll5Minute(ControllerImpl c, Completer comp) {}

	/**
	 * Start a test for the given controller.  This method is activated
	 * when the user clicks the checkbox 'test communication' on the
	 * the controller dialog in the status tab.
	 *
	 * @see us.mn.state.dot.tms.ControllerImpl#testCommunications
	 */
	public DiagnosticOperation startTest(ControllerImpl c) {
		// System.err.println("CawsPoller.startTest() called.");
		return null;
	}

	/** return name of AWS system */
	public static String awsName() {
		return I18NMessages.get("Aws.Name");
	}
}
