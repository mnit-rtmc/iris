/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.smartsensor;

import java.io.EOFException;
import java.io.PrintStream;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.DiagnosticOperation;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.SamplePoller;

/**
 * SmartSensorPoller is a java implementation of the Wavetronix SmartSensor
 * serial data communication protocol
 *
 * @author Douglas Lau
 */
public class SmartSensorPoller extends MessagePoller implements SamplePoller {

	/** Create a new SmartSensor poller */
	public SmartSensorPoller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified controller */
	public AddressedMessage createMessage(ControllerImpl c)
		throws EOFException
	{
		return new Message(new PrintStream(
			messenger.getOutputStream(c)),
			messenger.getInputStream(c), c);
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		return drop >= 0 && drop <= 9999;
	}

	/** Perform a controller download */
	protected void download(ControllerImpl c, int p) {
		if(c.getActive()) {
			InitializeSensor o = new InitializeSensor(c, true);
			o.setPriority(p);
			o.start();
		}
	}

	/** Perform a controller reset */
	public void resetController(ControllerImpl c) {
		if(c.getActive()) {
			InitializeSensor o = new InitializeSensor(c, true);
			o.start();
		}
	}

	/** Query sample data */
	public void querySamples(ControllerImpl c, int intvl, Completer comp) {
		if(intvl == 30) {
			if(c.hasActiveDetector())
				new GetBinnedSamples(c, comp).start();
		}
	}

	/** Start a test for the given controller */
	public DiagnosticOperation startTest(ControllerImpl c) {
		DiagnosticOperation test = new SensorDiagnostic(c);
		test.start();
		return test;
	}
}
