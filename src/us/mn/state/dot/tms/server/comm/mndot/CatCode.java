/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

/**
 * Message category codes.
 *
 * @author Douglas Lau
 */
public enum CatCode {
	SHUT_UP,		// 0
	LEVEL_1_RESTART,	// 1
	SYNCHRONIZE_CLOCK,	// 2
	QUERY_RECORD_COUNT,	// 3
	SEND_NEXT_RECORD,	// 4
	DELETE_OLDEST_RECORD,	// 5
	WRITE_MEMORY,		// 6
	READ_MEMORY;		// 7
}
