/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.natch;

import java.util.Calendar;
import java.util.Map;
import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.TimingTable;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * Step to send ramp meter timing table
 *
 * @author Douglas Lau
 */
public class OpMeterTiming extends OpNatch {

	/** Message ID counter */
	private final Counter counter;

	/** Ramp meter */
	private final RampMeterImpl meter;

	/** Timing table start time */
	private final int start;

	/** Timing table stop time */
	private final int stop;

	/** Meter table entry number (0-3) */
	private final int entry;

	/** Meter timing property */
	private final MeterTimingProp prop;

	/** Create a new ramp meter timing step */
	public OpMeterTiming(Counter c, RampMeterImpl m, int e) {
		counter = c;
		meter = m;
		entry = e;
		prop = new MeterTimingProp(c, meter);
		Hashtags tags = new Hashtags(meter.getNotes());
		TimingTable table = new TimingTable(tags);
		start = table.lookupStart(entry);
		stop = table.lookupStop(entry);
	}

	/** Get the red time for a time of day */
	private int getRed(int min) {
		return RedTime.fromReleaseRate(getTarget(min));
	}

	/** Get the target release rate for a time of day */
	private int getTarget(int min) {
		switch (TimeActionHelper.getPeriod(min)) {
		case Calendar.AM:
			return meter.getAmTarget();
		case Calendar.PM:
			return meter.getPmTarget();
		default:
			return 2000;
		}
	}

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		int red = getRed(start);
		prop.setEntry(entry);
		if (stop > start && red > 0) {
			prop.setStart(start);
			prop.setStop(stop);
			prop.setRed(red);
		} else {
			prop.setStart(0);
			prop.setStop(0);
			prop.setRed(0);
		}
		prop.encodeStore(op, tx_buf);
		setPolling(false);
	}

	/** Get the property */
	@Override
	protected NatchProp getProp() {
		return prop;
	}

	/** Handle received property */
	@Override
	protected void handleReceived(Operation op, NatchProp pr) {
		setDone(true);
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		if (done) {
			return (entry < 3)
			      ? new OpMeterTiming(counter, meter, entry + 1)
			      : new OpMeterWatchdogReset(counter, meter);
		} else
			return this;
	}
}
