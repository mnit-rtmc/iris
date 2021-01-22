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
package us.mn.state.dot.tms;

/**
 * Alert state enumeration.  The ordinal values correspond to the records in
 * the iris.alert_state look-up table.
 *
 * @author Douglas Lau
 */
public enum AlertState {
	PENDING,     // 0  pending approval
	APPROVED,    // 1  approved but not deployed
	INACTIVE,    // 2  deployed but not active on any DMS
	DEPLOYED,    // 3  deployed and active on one or more DMS
	EXPIRED,     // 4  past expiration time or replaced
	APPROVE_REQ, // 5  user approve or update request
	CANCEL_REQ;  // 6  user cancel request

	/** Cached values array */
	static private final AlertState[] VALUES = values();

	/** Get an alert state from an ordinal value */
	static public AlertState fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : PENDING;
	}
}
