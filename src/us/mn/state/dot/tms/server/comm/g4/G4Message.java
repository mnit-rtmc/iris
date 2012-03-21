/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.g4;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * G4 message
 *
 * @author Michael Darter
 */
public class G4Message implements CommMessage {

	/** Output stream */
	protected final DataOutputStream out_stream;

	/** Input stream */
	protected final InputStream inp_stream;

	/** Sensor id */
	final int sensor_id;

	/** Controller property */
	G4Property dev_prop;

	/** Messenger */
	Messenger messenger;

	/** Create a new G4 message */
	public G4Message(DataOutputStream os, InputStream is, 
		ControllerImpl c, Messenger m) 
	{
		out_stream = os;
		inp_stream = is;
		sensor_id = c.getDrop();
		messenger = m;
	}

	/** Add a controller property */
	public void add(ControllerProperty cp) {
		if(cp instanceof G4Property)
			dev_prop = (G4Property)cp;
	}

	/** Query the controller properties.
	 * @throws IOException For sending or receiving errors. */
	public void queryProps() throws IOException {
		G4Poller.info("G4Message.queryProps() called");
		if(dev_prop != null)
			dev_prop.doGetRequest(out_stream, inp_stream);
		else
			throw new ProtocolException("No property");
	}

	/** Store the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void storeProps() throws IOException {
		G4Poller.info("G4Message.storeProps() call ignored.");
	}
}
