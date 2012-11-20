/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

/**
 * Message response error code enumeration.
 *
 * @author Douglas Lau
 */
public enum ResponseCode {
	NO_ERRORS(0),
	PAYLOAD_SIZE(1),
	BODY_CRC(2),
	READ_ONLY(3),
	INTERVAL_DOES_NOT_EXIST(15),
	LANE_DOES_NOT_EXIST(16),
	FLASH_BUSY_ON_RETRIEVE(17),
	INVALID_PUSH_STATE(19),
	RTC_SET_ERROR(20),
	RTC_SYNC_ERROR(21),
	FLASH_ERASE_ERROR(22),
	FLASH_BUSY_ON_ERASE(23),
	INVALID_PROTOCOL_STATE(24),
	TOO_MANY_APPROACHES(25),
	TOO_MANY_LANES(26),
	AUTOMATIC_LANE(30),
	WRONG_LANE_COUNT(31),
	INVALID_BAUD_RATE(33),
	UNKNOWN_ERROR(-1);

	/** Error response code */
	private final int code;

	/** Create a response code */
	private ResponseCode(int c) {
		code = c;
	}

	/** Lookup a response code from value */
	static public ResponseCode fromCode(int c) {
		for(ResponseCode rc: ResponseCode.values()) {
			if(rc.code == c)
				return rc;
		}
		return UNKNOWN_ERROR;
	}
}
