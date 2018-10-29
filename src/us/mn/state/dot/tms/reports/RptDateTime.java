/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  SRF Consulting Group
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

package us.mn.state.dot.tms.reports;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Datetime conversion tools for Report generator.
 *
 * @author John L. Stanley - SRF Consulting
 *
 */
public class RptDateTime {

	private static final String dtFormat = "MM/dd/yyyy HH:mm:ss";
	private static final SimpleDateFormat dtFormatter = new SimpleDateFormat(dtFormat);

	/** Convert datetime string to long epoch value.
	 * 
	 * @param dt String datetime value
	 * @return long epoch value -or- zero if the string is null or blank.
	 * @throws ParseException
	 */
	public static long getLong(String dt) {
		if ((dt == null) || dt.isEmpty())
			return 0;
		try {
			Date d = dtFormatter.parse(dt);
			return d.getTime();
		} catch (ParseException e) {
			return 0;
		}
	}
	
	/** Convert long epoch datetime value to a readable string
	 * 
	 * @param dt datetime as a long epoch value
	 * @return String showing readable datetime -or- "" if dt is null or zero
	 */
	public static String getString(Long dt) {
		// return blank string for null or error values
		if ((dt == null) || (dt == 0))
			return "";
		return dtFormatter.format(new Date(dt));
	}
}
