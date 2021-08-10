/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * Detector status property
 *
 * @author Douglas Lau
 */
public class DetectorStatusProp extends DetectorProp {

	/** Parse a time */
	static private long parseTime(String v) {
		if (v.length() == 8 &&
		    v.charAt(2) == ':' &&
		    v.charAt(5) == ':')
		{
			int hour = parseInt(v.substring(0, 2));
			int min = parseInt(v.substring(3, 5));
			int sec = parseInt(v.substring(6, 8));
			if (hour >= 0 && min >= 0 && sec >= 0) {
				long now = TimeSteward.currentTimeMillis();
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(now);
				cal.set(Calendar.HOUR_OF_DAY, hour);
				cal.set(Calendar.MINUTE, min);
				cal.set(Calendar.SECOND, sec);
				cal.set(Calendar.MILLISECOND, 0);
				// Is the stamp from before midnight?
				if (cal.getTimeInMillis() > now)
					cal.add(Calendar.DAY_OF_MONTH, -1);
				return cal.getTimeInMillis();
			}
		}
		return 0;
	}

	/** Duration (ms) */
	private int duration;

	/** Get the duration */
	public int getDuration() {
		return duration;
	}

	/** Headway (ms) */
	private int headway;

	/** Get the headway */
	public int getHeadway() {
		return headway;
	}

	/** Date/time stamp */
	private long stamp;

	/** Log vehicle event */
	public boolean logEvent(ControllerImpl ctrl) {
		int pin = lookupPin(ctrl);
		if (pin > 0 && stamp > 0) {
			DetectorImpl det = ctrl.getDetectorAtPin(pin);
			if (det != null) {
				det.logVehicle(duration, headway, stamp, 0, 0);
				return true;
			}
		}
		return false;
	}

	/** Create a new detector status property */
	public DetectorStatusProp(Counter c) {
		super(c, -1);
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		String msg = "DS," + message_id + '\n';
		tx_buf.put(msg.getBytes(UTF8));
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws IOException
	{
		// Clear the detector status
		detector_num = -1;
		super.decodeQuery(op, rx_buf);
	}

	/** Get the message code */
	@Override
	protected String code() {
		return "ds";
	}

	/** Get the number of response parameters */
	@Override
	protected int parameters() {
		return 6;
	}

	/** Parse parameters for a received message */
	@Override
	protected boolean parseParams(String[] param) {
		detector_num = parseInt(param[2]);
		duration = parseInt(param[3]);
		headway = parseInt(param[4]);
		stamp = parseTime(param[5]);
		return detector_num >= 0 && detector_num < 32;
	}
}
