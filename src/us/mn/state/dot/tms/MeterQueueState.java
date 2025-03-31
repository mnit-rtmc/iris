/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2025  Minnesota Department of Transportation
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

/**
 * Ramp meter queue state
 *
 * @author Douglas Lau
 */
public enum MeterQueueState {

	/** Queue unknown state */
	UNKNOWN(null),

	/** Queue empty state */
	EMPTY(RampMeter.QUEUE_EMPTY),

	/** Queue exists state */
	EXISTS(RampMeter.QUEUE_EXISTS),

	/** Queue full state */
	FULL(RampMeter.QUEUE_FULL);

	/** Create a new meter queue state */
	private MeterQueueState(String d) {
		description = d;
	}

	/** Get the string representation */
	@Override
	public String toString() {
		return description;
	}

	/** Description of the queue state */
	public final String description;
}
