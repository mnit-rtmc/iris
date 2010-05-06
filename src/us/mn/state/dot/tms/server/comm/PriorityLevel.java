/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2010  Minnesota Department of Transportation
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
	URGENT,		/* 0 */
	COMMAND,	/* 1 */
	DATA_30_SEC,	/* 2 */
	DOWNLOAD,	/* 3 */
	DATA_5_MIN,	/* 4 */
	DEVICE_DATA,	/* 5 */
	DIAGNOSTIC;	/* 6 */
}
