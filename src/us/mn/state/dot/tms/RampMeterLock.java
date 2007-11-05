/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.io.Serializable;

/**
 * MeterLock holds information associated with a metering rate lock.
 *
 * @author Erik Engstrom
 */
public class RampMeterLock implements Serializable {

	/** Name of the user who created the lock */
	protected final String user;

	/** Reason the user locked the metering rate */
	protected String reason;

	/** Create a new meter lock */
	public RampMeterLock(String u, String reason) {
		this.user = u;
		this.reason = reason;
	}

	/** Get the username of the user who locked the meter */
	public String getUser() {
		return user;
	}

	/** Get the reason for this metering lock */
	public String getReason() {
		return reason;
	}
}
