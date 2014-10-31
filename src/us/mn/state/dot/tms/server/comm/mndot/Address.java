/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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
 * Address
 *
 * @author Douglas Lau
 */
public interface Address {

	/** Cabinet type location */
	int CABINET_TYPE = 0x00FE;

	/** 5-minute data buffer location */
	int DATA_BUFFER_5_MINUTE = 0x0300;

	/** 30-second data buffer location */
	int DATA_BUFFER_30_SECOND = 0x034B;

	/** Queue detector bitmap location */
	int QUEUE_BITMAP = 0x0129;

	/** Communication fail timeout location */
	int COMM_FAIL = 0x012C;

	/** Special function output location */
	int SPECIAL_FUNCTION_OUTPUTS = 0x012F;

	/** Bits to reset watchdog monitor */
	int WATCHDOG_BITS = 0x0E;

	/** Bits to reset detectors */
	int DETECTOR_RESET = 2;

	/** Ramp meter 1 timing table */
	int METER_1_TIMING_TABLE = 0x0140;

	/** Ramp meter 2 timing table */
	int METER_2_TIMING_TABLE = 0x0180;

	/** Red time offset */
	int OFF_RED_TIME = 0x08;

	/** PM timing table offset */
	int OFF_PM_TIMING_TABLE = 0x1B;

	/** Alarm special function inputs */
	int ALARM_INPUTS = 0x5005;

	/** Prom software version location */
	int PROM_VERSION = 0xFFF6;

	/** Ramp meter data location */
	int RAMP_METER_DATA = 0x010C;

	/** Meter data offset for ramp metering status */
	int OFF_STATUS = 0;

	/** Meter data offset for current rate */
	int OFF_CURRENT_RATE = 1;

	/** Meter data offset for 30-second green count */
	int OFF_GREEN_COUNT_30 = 2;

	/** Meter data offset for remote rate */
	int OFF_REMOTE_RATE = 3;

	/** Meter data offset for police panel &amp; verifies */
	int OFF_POLICE_PANEL = 4;

	/** Meter data offset for 5-minute green count */
	int OFF_GREEN_COUNT_5 = 5;

	/** Meter data offset for meter 1 data */
	int OFF_METER_1 = 0;

	/** Meter data offset for meter 2 data */
	int OFF_METER_2 = 6;

	/** 5-minute data offset for meter 1 green count */
	int OFF_GREEN_METER_1 = 72;

	/** 5-minute data offset for meter 2 green count */
	int OFF_GREEN_METER_2 = 73;
}
