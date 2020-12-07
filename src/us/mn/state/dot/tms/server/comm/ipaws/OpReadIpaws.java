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

import java.io.IOException;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * This operation reads alerts from the IPAWS system and parses them for
 * storage in the IPAWS Alert Bucket.
 *
 * @author Douglas Lau
 * @author Gordon Parikh
 */
public class OpReadIpaws extends OpController<IpawsProperty> {

	/** Alert feed name */
	protected final String alertFeed;

	/** Create a new operation to read alert feed with default priority level.
	 */
	protected OpReadIpaws(ControllerImpl c, String fid) {
		this(PriorityLevel.DATA_30_SEC, c, fid);
	}
	
	/** Create a new operation to read alert feed with custom priority level.
	 */
	protected OpReadIpaws(PriorityLevel p, ControllerImpl c, String fid) {
		super(p, c);
		alertFeed = fid;
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<IpawsProperty> phaseOne() {
		return new PhaseReadIpaws();
	}

	/** Phase to read the alert feed */
	protected class PhaseReadIpaws extends Phase<IpawsProperty> {

		/** Execute the phase */
		protected Phase<IpawsProperty> poll(
			CommMessage<IpawsProperty> mess) throws IOException
		{
			IpawsPoller.slog("Polling IPAWS alert feed " + alertFeed);
			mess.add(new IpawsProperty(alertFeed));
			mess.queryProps();
			return null;
		}
	}

	/** Handle a communication error */
	@Override
	public void handleCommError(EventType et, String msg) {
		IpawsPoller.slog("ERROR: " + msg);
		super.handleCommError(et, msg);
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		IpawsPoller.slog("Finished IPAWS alert feed " + alertFeed);
		super.cleanup();
	}
}
