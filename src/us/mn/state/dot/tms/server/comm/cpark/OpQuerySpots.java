/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cpark;

import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * This operation reads the current available parking spots.
 *
 * @author Douglas Lau
 */
public class OpQuerySpots extends OpController<CParkProp> {

	/** Starting pin for controller I/O */
	static private final int START_PIN = 1;

	/** Maximum scan count for occupancy calculation.
	 * Scans are 1-bit (on/off) values. */
	static private final int MAX_SCANS = 1;

	/** Central park property */
	private final CParkProp prop;

	/** Create a new query spots operation */
	public OpQuerySpots(ControllerImpl c, int p) {
		super(PriorityLevel.POLL_HIGH, c);
		prop = new CParkProp(p);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<CParkProp> phaseOne() {
		return new PhaseReadSpots();
	}

	/** Phase to read the available spots */
	protected class PhaseReadSpots extends Phase<CParkProp> {

		/** Execute the phase */
		protected Phase<CParkProp> poll(
			CommMessage<CParkProp> mess) throws IOException
		{
			mess.add(prop);
			mess.queryProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		long stamp = prop.getTime();
		int per_sec = prop.getPeriod();
		controller.storeOccupancy(stamp, per_sec, START_PIN,
			prop.getScans(), MAX_SCANS);
		super.cleanup();
	}
}
