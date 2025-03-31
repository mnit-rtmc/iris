/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cap;

import java.io.IOException;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * This operation reads alerts from a CAP feed.
 *
 * @author Douglas Lau
 * @author Gordon Parikh
 */
public class OpReadCap extends OpController<CapProperty> {

	/** Alert feed name */
	private final String feed;

	/** Comm protocol */
	private final CommProtocol protocol;

	/** Create a new operation to read alert feed */
	public OpReadCap(ControllerImpl c, String fd, CommProtocol cp) {
		super(PriorityLevel.POLL_HIGH, c);
		feed = fd;
		protocol = cp;
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<CapProperty> phaseOne() {
		return new PhaseReadCap();
	}

	/** Phase to read the alert feed */
	private class PhaseReadCap extends Phase<CapProperty> {

		/** Execute the phase */
		protected Phase<CapProperty> poll(
			CommMessage<CapProperty> mess) throws IOException
		{
			CapPoller.slog("polling feed " + feed);
			mess.add(new CapProperty(feed, protocol));
			mess.queryProps();
			return null;
		}
	}

	/** Handle a communication error */
	@Override
	public void handleCommError(EventType et) {
		CapPoller.slog("ERROR: " + et);
		super.handleCommError(et);
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		CapPoller.slog("finished " + feed);
		super.cleanup();
	}
}
