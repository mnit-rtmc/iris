/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2019  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dr500;

import java.io.IOException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to get average speed from DR500.
 *
 * @author Douglas Lau
 */
public class OpQuerySpeed extends OpDR500 {

	/** Starting pin for controller I/O */
	static private final int START_PIN = 1;

	/** Binning period (seconds) */
	private final int period;

	/** Time stamp of sample data */
	private long stamp;

	/** Average speed property */
	private final AvgSpeedProperty avg_speed = new AvgSpeedProperty();

	/** Create a new query speed operation */
	public OpQuerySpeed(ControllerImpl c, int p) {
		super(PriorityLevel.DATA_30_SEC, c);
		period = p;
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<DR500Property> phaseOne() {
		return new GetAvgSpeed();
	}

	/** Phase to get the average speed */
	private class GetAvgSpeed extends Phase<DR500Property> {

		/** Get the average speed */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			stamp = TimeSteward.currentTimeMillis();
			mess.add(avg_speed);
			mess.queryProps();
			return null;
		}
	}

	/** Get the average speed sample array */
	private int[] getSpeed() {
		Integer s = avg_speed.getSpeed();
		return (s != null) ? new int[] { s } : new int[0];
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		controller.storeSpeed(stamp, period, START_PIN, getSpeed());
		super.cleanup();
	}
}
