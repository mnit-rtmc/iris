/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query 30-second sample data
 *
 * @author Douglas Lau
 */
public class OpQuerySamples30Sec extends OpQuerySamples {

	/** Sample period (seconds) */
	static public final int SAMPLE_PERIOD_SEC = 30;

	/** Maximum number of scans in 30 seconds */
	static private final int MAX_SCANS = 1800;

	/** Create a new 30-second data operation */
	public OpQuerySamples30Sec(ControllerImpl c) {
		super(PriorityLevel.DATA_30_SEC, c);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseOne() {
		return new QuerySample30Sec();
	}

	/** Phase to query the 30-second sample data */
	protected class QuerySample30Sec extends Phase<MndotProperty> {

		/** Query 30-second sample data */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			byte[] r = new byte[72];
			MemoryProperty sample_mem = new MemoryProperty(
				Address.DATA_BUFFER_30_SECOND, r);
			mess.add(sample_mem);
			mess.queryProps();
			setStamp();
			processData(r);
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		controller.storeVolume(getStamp(), SAMPLE_PERIOD_SEC,
			FIRST_DETECTOR_PIN, volume);
		controller.storeOccupancy(getStamp(), SAMPLE_PERIOD_SEC,
			FIRST_DETECTOR_PIN, scans, MAX_SCANS);
		super.cleanup();
	}
}
