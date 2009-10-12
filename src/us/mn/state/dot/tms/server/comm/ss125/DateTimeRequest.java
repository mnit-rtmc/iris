/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Date / Time Request.
 *
 * @author Douglas Lau
 */
public class DateTimeRequest extends Request {

	/** Date / time request ID */
	static protected final byte MSG_ID = 0x0E;

	/** Format the body of a GET request */
	byte[] formatBodyGet() throws IOException {
		byte[] body = new byte[3];
		body[0] = MSG_ID;
		body[1] = SUB_ID_DONT_CARE;
		body[2] = REQ_READ;
		return body;
	}

	/** Format the body of a SET request */
	byte[] formatBodySet() throws IOException {
		byte[] body = new byte[11];
		body[0] = MSG_ID;
		body[1] = SUB_ID_DONT_CARE;
		body[2] = REQ_WRITE;
		formatDate(body, 3);
		return body;
	}

	/** Parse the payload of a GET response */
	void parsePayload(byte[] body) throws IOException {
		if(body.length != 11)
			throw new ParsingException("BODY LENGTH");
		parseDate(parse32(body, 3), parse32(body, 7));
	}

	/** Date / time stamp */
	protected final Date stamp = new Date();

	/** Get the date / time stamp */
	public Date getStamp() {
		return new Date(stamp.getTime());
	}

	/** Format a date / time stamp */
	protected void formatDate(byte[] body, int pos) {
		stamp.setTime(System.currentTimeMillis());
		TimeZone utc = TimeZone.getTimeZone("GMT");
		Calendar cal = Calendar.getInstance(utc);
		cal.setTime(stamp);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		int ms = cal.get(Calendar.MILLISECOND);
		int date = (year << 9) | (month << 5) | day;
		int time = (hour << 22) | (minute << 16) | (second << 10) | ms;
		format32(date, body, 3);
		format32(time, body, 7);
	}

	/** Parse a date / time stamp */
	protected void parseDate(int date, int time) {
		int year = (date >> 9) & 0x0FFF;
		int month = (date >> 5) & 0x0F;
		int day = date & 0x1F;
		int hour = (time >> 22) & 0x1F;
		int minute = (time >> 16) & 0x2F;
		int second = (time >> 10) & 0x2F;
		int ms = time & 0x3FF;
		TimeZone utc = TimeZone.getTimeZone("GMT");
		Calendar cal = Calendar.getInstance(utc);
		cal.set(year, month - 1, day, hour, minute, second);
		cal.set(Calendar.MILLISECOND, ms);
		stamp.setTime(cal.getTimeInMillis());
	}
}
