/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.stc;

/**
 * Operator status represents the state of STC gate operator.
 *
 * @author Douglas Lau
 */
public enum OperatorStatus {
	RESET,				/*  0  controller reset */
	LEARN_LIMIT_STOP,		/*  1    slidewinder learn mode */
	LEARN_LIMIT_OPEN,		/*  2    slidewinder learn mode */
	LEARN_LIMIT_CLOSE,		/*  3    slidewinder learn mode */
	STOP_NORMAL,			/*  4  normal stop */
	CHECK_PHOTO_EYE_OPEN,		/*  5    PEO check */
	PEP_2_OPEN,			/*  6    PEO check */
	WARN_B4_OPEN,			/*  7    warning before open */
	OPEN_NORMAL,			/*  8  normal open */
	REVERSE_2_CLOSE_PEO,		/*  9    PEO safety reverse close */
	WAIT_PHOTO_EYE_OPEN,		/* 10    wait for PEO clear */
	DELAY_PHOTO_EYE_OPEN,		/* 11    short delay after PEO clear */
	CHECK_PHOTO_EYE_CLOSE,		/* 12    PEC check */
	PEP_2_CLOSE,			/* 13    PEC check */
	WARN_B4_CLOSE,			/* 14    warning before close */
	CLOSE_NORMAL,			/* 15  normal close */
	WAIT_VEHICLE,			/* 16    wait for vehicle to clear */
	REVERSE_2_OPEN_PEC,		/* 17    PEC safety reverse open */
	WAIT_PHOTO_EYE_CLOSE,		/* 18    wait for PEC clear */
	DELAY_PHOTO_EYE_CLOSE,		/* 19    short delay after PEC clear */
	REVERSE_2_CLOSE,		/* 20    sensor safety reverse close */
	REVERSE_2_OPEN,			/* 21    sensor safety reverse open */
	STOP_SAFETY,			/* 22  stopped in SAFE mode */
	STOP_ENTRAPMENT,		/* 23  stopped in ENTRAPMENT mode */
	FAULT_RUN_TIMEOUT,		/* 24    stopped; FAULT_1 */
	FAULT_PHOTO_EYE,		/* 25    stopped; FAULT_2 */
	FAULT_VOLTAGE_SAG,		/* 26    stopped; FAULT_3 */
	FAULT_GATE_NO_LOAD,		/* 27    stopped; FAULT_4 */
	FAULT_OPEN_CLOSE_LIMIT,		/* 28    stopped; FAULT_5 */
	FAULT_EXCESSIVE_STUCK_ALERTS,	/* 29    stopped; FAULT_14 */
	FAULT_15,			/* 30    spare */
	ERROR_DIRECTION,		/* 31    stopped; ERROR_1 */
	ERROR_DISCONNECTED_IES,		/* 32    inherent entrapment sensor */
	ERROR_RS_485_BOARD,		/* 33    stopped; ERROR_6 */
	ERROR_RPM_SENSOR,		/* 34    stopped; ERROR_8 */
	ERROR_DISCONNECTED_BATT,	/* 35    stopped; ERROR_9 */
	ERROR_SLOWDOWN_SWITCH,		/* 36    stopped; ERROR_10 */
	ALERT_GATE_FORCED_OPEN,		/* 37    stopped; ALERT_1 */
	ALERT_GATE_DRIFT_CLOSED,	/* 38    stopped; ALERT_2 */
	ALERT_MOTOR_THERMAL_OVERLOAD,	/* 39    stopped; ALERT_4 */
	ALERT_BOTH_LIMITS_TRIGGERED,	/* 40    stopped; ALERT_5 */
	ALERT_NO_MOTION,		/* 41    stopped; ALERT_6 */
	ALERT_VFD_DRIVE_TRIPPED;	/* 42    stopped; ALERT_21 */

	/** Lookup a gate operator status from ordinal */
	static public OperatorStatus fromOrdinal(int o) {
		for(OperatorStatus os: OperatorStatus.values()) {
			if(os.ordinal() == o)
				return os;
		}
		return null;
	}

	/** Test if an operator status is "fault" (generic) */
	static public boolean isFault(OperatorStatus os) {
		switch(os) {
		case FAULT_RUN_TIMEOUT:
		case FAULT_PHOTO_EYE:
		case FAULT_VOLTAGE_SAG:
		case FAULT_GATE_NO_LOAD:
		case FAULT_OPEN_CLOSE_LIMIT:
		case FAULT_EXCESSIVE_STUCK_ALERTS:
		case FAULT_15:
		case ERROR_DIRECTION:
		case ERROR_DISCONNECTED_IES:
		case ERROR_RS_485_BOARD:
		case ERROR_RPM_SENSOR:
		case ERROR_DISCONNECTED_BATT:
		case ERROR_SLOWDOWN_SWITCH:
		case ALERT_GATE_FORCED_OPEN:
		case ALERT_GATE_DRIFT_CLOSED:
		case ALERT_MOTOR_THERMAL_OVERLOAD:
		case ALERT_BOTH_LIMITS_TRIGGERED:
		case ALERT_NO_MOTION:
		case ALERT_VFD_DRIVE_TRIPPED:
			return true;
		default:
			return false;
		}
	}
}
