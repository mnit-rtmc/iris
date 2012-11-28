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
package us.mn.state.dot.tms.server.comm.g4;

/**
 * A qualifier code determines a RTMS G4 message type.
 *
 * @author Douglas Lau
 */
public enum QualCode {
	UNKNOWN(-1),
	VOLUME(0x10),		/* Statistical volume data */
	OCCUPANCY(0x11),	/* Statistical occupancy data */
	SPEED(0x12),		/* Statistical speed data */
	GAP(0x13),		/* Statistical gap data */
	C1(0x14),		/* Statistical vehicle class C1 volumes */
	C2(0x15),		/* Statistical vehicle class C2 volumes */
	C3(0x16),		/* Statistical vehicle class C3 volumes */
	C4(0x17),		/* Statistical vehicle class C4 volumes */
	C5(0x18),		/* Statistical vehicle class C5 volumes */
	NAK(0x1A),		/* Negative acknowledgement response */
	ACK(0x1C),		/* Acknowledgement response */
	HEADWAY(0x1E),		/* Statistical headway data */
	SPEED_85(0x1F),		/* Statistical speed 85% data */
	INFORMATION(0x40),	/* Sensor information; response to INFO_QUERY */
	SETUP(0x41),		/* Setup configuration (change or response) */
	RTC(0x49),		/* RTC (change or response) */
	TEST_POLL(0x4C),	/* Request a RTMS self-test */
	MEMORY_DOWNLOAD(0x4F),	/* Memory download request */
	SYNCHRONIZE(0x4E),	/* Synchronize message period request */
	MEMORY_READ(0x50),	/* Read memory request */
	MEMORY_CLEAR(0x51),	/* Clear memory request */
	ABORT_DOWNLOAD(0x52),	/* Abort memory download */
	STAT_POLL(0x53),	/* Poll statistical data */
	CLEAR_BUFFER(0x54),	/* Clear buffer request */
	CLASSIFICATION(0x57),	/* Vehicle classificaton (change or response) */
	VEHICLE_EVENT(0x5E),	/* Sent for each vehicle in event mode */
	NORMAL_MODE(0x5F),	/* Sent every 100ms while in normal mode */
	MEMORY_TIME(0x68),	/* Memory download by time request */
	STAT_HEADER(0x80),	/* Statistical data header */
	STAT_FOOTER(0x81),	/* Statistical data footer */
	INFO_QUERY(0xC0),	/* Query sensor information */
	SETUP_QUERY(0xC1),	/* Query setup configuration */
	TEST_RESULT(0xCC),	/* Result of RTMS self-test (TEST_POLL) */
	MEMORY(0xD0),		/* Read memory address response */
	CLASS_QUERY(0xD7),	/* Query vehicle classification config */
	RTC_QUERY(0xF9);	/* Query RTC */

	/** Qualifier code */
	public final int code;

	/** Create a qualifier code */
	private QualCode(int c) {
		code = c;
	}

	/** Lookup a qualifier code from value */
	static public QualCode fromCode(int c) {
		for(QualCode qc: QualCode.values()) {
			if(qc.code == c)
				return qc;
		}
		return UNKNOWN;
	}
}
