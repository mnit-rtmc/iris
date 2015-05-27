/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2015  Minnesota Department of Transportation
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
 * Enumeration of illumination control types.
 * DmsIllumControl determines how the light output is controlled.  Note: when
 * switching to one of the manual modes, dmsIllumManLevel is automatically set
 * to the current brightness level.  This means that setting dmsIllumManLevel
 * must be done in a seperate set-request.
 *
 * @author Douglas Lau
 */
public enum DmsIllumControl {
	undefined,
	other,		// deprecated in v2
	photocell,
	timer,
	manual,		// deprecated in v2
	manualDirect,	// added in v2
	manualIndexed;	// added in v2
}
