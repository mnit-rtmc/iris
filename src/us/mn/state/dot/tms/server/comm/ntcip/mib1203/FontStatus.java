/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

/**
 * Enumeration of font status values.
 * Added in 1203 v2.
 *
 * @author Douglas Lau
 */
public enum FontStatus {
	undefined,
	notUsed,
	modifying,
	calculatingID,
	readyForUse,
	inUse,
	permanent,
	modifyReq,
	readyForUseReq,
	notUsedReq,
	unmanagedReq,
	unmanaged;

	/** Check if status is valid */
	public boolean isValid() {
		switch (this) {
			case readyForUse:
			case inUse:
			case permanent:
			case unmanaged:
				return true;
			default:
				return false;
		}
	}
}
