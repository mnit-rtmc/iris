/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

import java.io.EOFException;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.SamplePoller;

/**
 * SS125Poller is an implementation of the Wavetronix SmartSensor HD serial
 * data communication protocol.
 *
 * @author Douglas Lau
 */
public class SS125Poller extends MessagePoller implements SamplePoller {

	/** Create a new SS125 poller */
	public SS125Poller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified controller */
	public CommMessage createMessage(ControllerImpl c)
		throws EOFException
	{
		return new Message(messenger.getOutputStream(c),
			messenger.getInputStream(c), c);
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		return drop > 0 && drop < 65536;
	}

	/** Perform a controller download */
	protected void download(ControllerImpl c, PriorityLevel p) {
		if(c.getActive()) {
			OpSendSensorSettings o =
				new OpSendSensorSettings(c, true);
			o.setPriority(p);
			addOperation(o);
		}
	}

	/** Perform a controller reset */
	public void resetController(ControllerImpl c) {
		if(c.getActive())
			addOperation(new OpSendSensorSettings(c, true));
	}

	/** Send sample settings to a controller */
	public void sendSettings(ControllerImpl c) {
		if(c.getActive())
			addOperation(new OpSendSensorSettings(c, false));
	}

	/** Query sample data */
	public void querySamples(ControllerImpl c, int intvl, Completer comp) {
		if(intvl == 30) {
			if(c.hasActiveDetector())
				addOperation(new OpQuerySamples(c, comp));
		}
	}
}
