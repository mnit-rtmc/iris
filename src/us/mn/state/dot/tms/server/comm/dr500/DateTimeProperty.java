/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Date / Time Property.
 *
 * @author Douglas Lau
 */
public class DateTimeProperty extends DR500Property {

	/** Date / time stamp */
	private final Date stamp = new Date();

	/** Format a date / time stamp */
	private byte[] formatDate() {
		stamp.setTime(TimeSteward.currentTimeMillis());
		TimeZone utc = TimeZone.getTimeZone("GMT");
		Calendar cal = Calendar.getInstance(utc);
		cal.setTime(stamp);
		byte[] body = new byte[8];
		body[0] = (byte) MsgCode.TIME_SET.code;
		body[1] = 0;
		body[2] = (byte) cal.get(Calendar.SECOND);
		body[3] = (byte) cal.get(Calendar.MINUTE);
		body[4] = (byte) cal.get(Calendar.HOUR_OF_DAY);
		body[5] = (byte) cal.get(Calendar.DAY_OF_MONTH);
		body[6] = (byte) cal.get(Calendar.MONTH);
		body[7] = (byte)(cal.get(Calendar.YEAR) - 1900);
		return body;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		encodeRequest(os, formatDate());
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		int status = parseStatus(decodeResponse(is));
		if (status != 0)
			throw new ParsingException("STATUS:" + status);
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("date/time:");
		sb.append(stamp.toString());
		return sb.toString();
	}
}
