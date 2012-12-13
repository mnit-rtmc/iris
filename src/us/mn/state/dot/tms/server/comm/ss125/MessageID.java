/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
 * SmartSensor HD message IDs.
 *
 * @author Douglas Lau
 */
public enum MessageID {
	UNKNOWN(-1),
	GENERAL_CONFIG(0x00),
	DATA_CONFIG(0x03),
	VERSION(0x04),
	FLASH_CONFIG(0x08),
	PUSH_ENABLE(0x0D),
	DATE_TIME(0x0E),
	APPROACH_INFO(0x11),
	CLASS_CONFIG(0x13),
	LANE_INFO(0x17),
	ALL_PUSH_ENABLE(0x1C),
	LANE_PUSH(0x62),
	EVENT_BULK(0x63),
	CLEAR_NV(0x64),
	EVENT_PUSH(0x65),
	ACTIVE_EVENTS(0x67),
	PRESENCE(0x68),
	PRESENCE_PUSH(0x69),
	CLEAR_EVENT_FIFO(0x6D),
	INTERVAL_NV(0x70),
	INTERVAL(0x71);

	/** Message ID */
	public final int id;

	/** Create a message ID */
	private MessageID(int i) {
		id = i;
	}

	/** Lookup a message ID from value */
	static public MessageID fromCode(int i) {
		for(MessageID mid: MessageID.values()) {
			if(mid.id == i)
				return mid;
		}
		return UNKNOWN;
	}
}
