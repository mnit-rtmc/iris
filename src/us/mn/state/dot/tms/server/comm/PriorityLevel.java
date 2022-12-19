/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

/**
 * Priority level for communication operations.  Levels are sorted from high
 * to low.
 *
 * @author Douglas Lau
 */
public enum PriorityLevel {
	CONFIGURE,   /* 0: system-level configuration queries */
	COMMAND,     /* 1: user initiated commands */
	POLL_HIGH,   /* 2: high-priority poll (short period) */
	SETTINGS,    /* 3: sending settings */
	POLL_LOW,    /* 4: low-priority poll (long period) */
	DIAGNOSTIC,  /* 5: diagnostics and testing */
	IDLE;        /* 6: idle/continuous queries */
}
