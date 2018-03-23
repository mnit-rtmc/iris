/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2018  Minnesota Department of Transportation
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
	FAL1_RUN_TIMEOUT,		/* 24    stopped */
	FAL2_PHOTO_EYE,			/* 25    stopped */
	FAL3_VOLTAGE_SAG,		/* 26    stopped */
	FAL4_GATE_NO_LOAD,		/* 27    stopped */
	FAL5_OPEN_CLOSE_LIMIT,		/* 28    stopped */
	FAL7_MISSING_OR_BROKEN_ARM,	/* 29    stopped */
	FAL8_LOW_FLUID_LEVEL,		/* 30    stopped */
	FA14_EXCESSIVE_STUCK_ALERTS,	/* 31    stopped */
	FA15,				/* 32    stopped */
	ERR1_DIRECTION,			/* 33    stopped */
	ERR2_DISCONNECTED_IES,		/* 34    stopped */
	ERR6_RS_485_BOARD,		/* 35    stopped */
	ERR8_RPM_SENSOR,		/* 36    stopped */
	ERR9_DISCONNECTED_BATT,		/* 37    stopped */
	ER10_SLOWDOWN_SWITCH,		/* 38    stopped */
	ER12,				/* 39 */
	ER13,				/* 40 */
	ALE1_GATE_FORCED_OPEN,		/* 41    stopped */
	ALE2_GATE_DRIFT_CLOSED,		/* 42    stopped */
	ALE4_MOTOR_THERMAL_OVERLOAD,	/* 43    stopped */
	ALE5_BOTH_LIMITS_TRIGGERED,	/* 44    stopped */
	ALE6_NO_MOTION,			/* 45    stopped */
	AL21_VFD_DRIVE_TRIPPED,		/* 46    stopped */
	FACTORY_TEST,			/* 47 */
	LEARN_LIMIT_GEO,		/* 48   new UL325 */
	LEARN_LIMIT_GEC;		/* 49   new UL325 */

	/** Lookup a gate operator status from ordinal */
	static public OperatorStatus fromOrdinal(int o) {
		for (OperatorStatus os: OperatorStatus.values()) {
			if (os.ordinal() == o)
				return os;
		}
		return null;
	}

	/** Test if an operator status is "fault" (generic) */
	static public boolean isFault(OperatorStatus os) {
		switch (os) {
		case FAL1_RUN_TIMEOUT:
		case FAL2_PHOTO_EYE:
		case FAL3_VOLTAGE_SAG:
		case FAL4_GATE_NO_LOAD:
		case FAL5_OPEN_CLOSE_LIMIT:
		case FAL7_MISSING_OR_BROKEN_ARM:
		case FAL8_LOW_FLUID_LEVEL:
		case FA14_EXCESSIVE_STUCK_ALERTS:
		case FA15:
		case ERR1_DIRECTION:
		case ERR2_DISCONNECTED_IES:
		case ERR6_RS_485_BOARD:
		case ERR8_RPM_SENSOR:
		case ERR9_DISCONNECTED_BATT:
		case ER10_SLOWDOWN_SWITCH:
		case ER12:
		case ER13:
		case ALE1_GATE_FORCED_OPEN:
		case ALE2_GATE_DRIFT_CLOSED:
		case ALE4_MOTOR_THERMAL_OVERLOAD:
		case ALE5_BOTH_LIMITS_TRIGGERED:
		case ALE6_NO_MOTION:
		case AL21_VFD_DRIVE_TRIPPED:
			return true;
		default:
			return false;
		}
	}
}
