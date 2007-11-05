/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.comm.mndot;

/**
 * Meter status enumeration
 *
 * @author Douglas Lau
 */
public class MeterStatus {

	/** Flash metering status from 170 */
	static public final int FLASH = 0;

	/** Manual metering status from 170 */
	static public final int MANUAL = 1;

	/** Central (remote) metering status from 170 */
	static public final int CENTRAL = 2;

	/** Time-of-day metering status from 170 */
	static public final int TOD = 3;

	/** Metering status descriptions */
	static protected final String[] DESCRIPTION = {
		"Flash", "Manual", "Metering", "Time-of-day"
	};

	/** Status code */
	protected final int status;

	/** Create new meter status */
	public MeterStatus(int s) {
		status = s;
	}

	/** Check if the status code is valid */
	public boolean isValid() {
		return status >= 0 && status < DESCRIPTION.length;
	}

	/** Check if the status is metering */
	public boolean isMetering() {
		return status != FLASH;
	}

	/** Check if the status is manual mode */
	public boolean isManual() {
		return status == MANUAL;
	}

	/** Get a string description of the meter status */
	public String toString() {
		if(isValid())
			return DESCRIPTION[status];
		else
			return "Unknown";
	}
}
