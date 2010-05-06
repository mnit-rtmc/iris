/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss105;

import java.io.EOFException;
import java.io.PrintStream;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.SamplePoller;

/**
 * SS105Poller is a java implementation of the Wavetronix SmartSensor 105
 * serial data communication protocol.
 *
 * @author Douglas Lau
 */
public class SS105Poller extends MessagePoller implements SamplePoller {

	/** Create a new SS105 poller */
	public SS105Poller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified controller */
	public CommMessage createMessage(ControllerImpl c) throws EOFException {
		return new Message(new PrintStream(
			messenger.getOutputStream(c)),
			messenger.getInputStream(c), c);
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		return drop >= 0 && drop <= 9999;
	}

	/** Perform a controller download */
	protected void download(ControllerImpl c, PriorityLevel p) {
		if(c.getActive()) {
			OpSendSensorSettings o =
				new OpSendSensorSettings(c, true);
			o.setPriority(p);
			o.start();
		}
	}

	/** Perform a controller reset */
	public void resetController(ControllerImpl c) {
		if(c.getActive())
			new OpSendSensorSettings(c, true).start();
	}

	/** Send sample settings to a controller */
	public void sendSettings(ControllerImpl c) {
		if(c.getActive())
			new OpSendSensorSettings(c, false).start();
	}

	/** Query sample data */
	public void querySamples(ControllerImpl c, int intvl, Completer comp) {
		if(intvl == 30) {
			if(c.hasActiveDetector())
				new OpQuerySamples(c, comp).start();
		}
	}
}
