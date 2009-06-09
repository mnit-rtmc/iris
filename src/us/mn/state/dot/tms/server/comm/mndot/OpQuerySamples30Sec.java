/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.IOException;
import java.util.Calendar;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;

/**
 * Operation to query 30-second sample data
 *
 * @author Douglas Lau
 */
public class OpQuerySamples30Sec extends OpQuerySamples {

	/** 30-Second completer */
	protected final Completer completer;

	/** Create a new 30-second data operation */
	public OpQuerySamples30Sec(ControllerImpl c, Completer comp) {
		super(DATA_30_SEC, c);
		completer = comp;
	}

	/** Begin the operation */
	public void begin() {
		completer.up();
		phase = new QuerySample30Sec();
	}

	/** Phase to query the 30-second sample data */
	protected class QuerySample30Sec extends Phase {

		/** Query 30-second sample data */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] r = new byte[72];
			mess.add(new MemoryRequest(
				Address.DATA_BUFFER_30_SECOND, r));
			mess.getRequest();
			processData(r);
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		Calendar stamp = completer.getStamp();
		controller.storeData30Second(stamp, FIRST_DETECTOR_PIN, volume,
			scans, null);
		completer.down();
		super.cleanup();
	}
}
