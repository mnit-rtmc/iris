/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
package us.mn.state.dot.tms.server.comm.ipaws;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * This operation reads alerts from the IPAWS system and parses them for
 * storage in the IPAWS Alert Bucket.
 *
 * @author Douglas Lau
 * @author Gordon Parikh
 */
public class OpTestIpaws extends OpReadIpaws {

	/** Create a new operation to read alert feed */
	protected OpTestIpaws(ControllerImpl c, String fid) {
		super(PriorityLevel.DIAGNOSTIC, c, fid);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<IpawsProperty> phaseOne() {
		return new PhaseTestIpaws();
	}

	/** Phase to read the test alert */
	protected class PhaseTestIpaws extends Phase<IpawsProperty> {

		/** Execute the phase */
		protected Phase<IpawsProperty> poll(
			CommMessage<IpawsProperty> mess) throws IOException
		{
			// read the test alert from a file
			// TODO make this path a system attribute
			File testAlert = new File("/var/log/iris/Ipaws_Test_Alert.xml");
			InputStream is = new FileInputStream(testAlert);
			
			// call IpawsReader.readIpaws directly
			IpawsReader.readIpaws(is);
			return null;
		}
	}

	/** Handle a communication error */
	@Override
	public void handleCommError(EventType et, String msg) {
		IpawsPoller.slog("ERROR DURING TEST: " + msg);
		super.handleCommError(et, msg);
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		IpawsPoller.slog("Finished IPAWS alert test " + alertFeed);
		super.cleanup();
	}
}
