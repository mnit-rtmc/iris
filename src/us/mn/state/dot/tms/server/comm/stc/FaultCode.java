/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2023  Minnesota Department of Transportation
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
 * Fault codes for STC.
 *
 * @author Douglas Lau
 */
public enum FaultCode {
	FAL_UNKNOWN			(-1),
	FAL1_RUN_TIMEOUT		(0),
	FAL2_PHOTO_EYE			(1),
	FAL3_VOLTAGE_SAG		(2),
	FAL4_GATE_NO_LOAD		(3),
	ERR1_DIRECTION			(4),
	ERR2_DISCONNECTED_IES		(5),
	ERR4_RS_485_MS_COMM		(7),
	ERR6_RS_485_BOARD		(9),
	ALE1_GATE_FORCED_OPEN		(13),
	ALE2_GATE_DRIFT_CLOSED		(14),
	ALE3_EXCESSIVE_GATE_DRIFT	(15),
	ALE4_MOTOR_THERMAL_OVERLOAD	(16),
	ALE5_BOTH_LIMITS_TRIGGERED	(17),
	ALE6_NO_MOTION			(18),
	ALE7_ELD_FREQ_CHANGE		(19),	/* exit loop detector */
	ALE8_ELD_SHORTED		(20),
	ALE9_ELD_DISCONNECTED		(21),
	AL10_ELD_COMM			(22),
	AL11_ELD_MALFUNCTION		(23),
	AL12_ELD_5_MINUTES		(24),
	ALE7_IOLD_FREQ_CHANGE		(25),	/* inner obstruction loop det */
	ALE8_IOLD_SHORTED		(26),
	ALE9_IOLD_DISCONNECTED		(27),
	AL10_IOLD_COMM			(28),
	AL11_IOLD_MALFUNCTION		(29),
	AL12_IOLD_5_MINUTES		(30),
	ALE7_OOLD_FREQ_CHANGE		(31),	/* outer obstruction loop det */
	ALE8_OOLD_SHORTED		(32),
	ALE9_OOLD_DISCONNECTED		(33),
	AL10_OOLD_COMM			(34),
	AL11_OOLD_MALFUNCTION		(35),
	AL12_OOLD_5_MINUTES		(36),
	ALE7_SLD_FREQ_CHANGE		(37),	/* reset/shadow loop det */
	ALE8_SLD_SHORTED		(38),
	ALE9_SLD_DISCONNECTED		(39),
	AL10_SLD_COMM			(40),
	AL11_SLD_MALFUNCTION		(41),
	AL12_SLD_5_MINUTES		(42),
	AL13_STIFF_GATE_80_POWER	(43),
	AL14_STUCK_GATE_100_POWER	(44),
	AL15_NO_TARGET_DETECTED		(45),
	ERR3_ELD_FAILED			(58),
	ERR3_IOLD_FAILED		(59),
	ERR3_OOLD_FAILED		(60),
	ERR3_SLD_FAILED			(61),
	AL17_BAD_COIN_BATTERY		(66),
	ERR8_RPM_SENSOR			(81),
	ERR9_DISCONNECTED_BATT		(82),
	AL18_REPLACE_BATTERY		(83),
	AL19_FALSE_SLOWDOWN_SIGNAL	(143),
	FAL5_OPEN_LIMIT			(144),
	FAL5_CLOSE_LIMIT		(145),
	ER10_OPEN_SLOWDOWN_SWITCH	(146),
	ER10_CLOSE_SLOWDOWN_SWITCH	(147),
	AL20_INTERLOCK_BLOCKS_OPEN	(155),
	FA14_EXCESSIVE_STUCK_ALERTS	(166),
	AL21_VFD_DRIVE_TRIPPED		(167),
	ERR8_PWM_SENSOR			(168),
	AL22_INTERLOCK_CONNECTION_LOST	(170),
	FAL7_MISSING_OR_BROKEN_ARM	(171),
	AL24_EXTERNAL_RELAY_BOARD	(172),
	ER12_ARM_WEDGE_COMM		(177),
	ER13_HYINVERTER_COMM		(180),
	FAL6_HYINVERTER_INPUT_OVERLOAD	(181),
	FAL8_LOW_FLUID_LEVEL		(185),
	FAL2_PHOTO_EYE_1		(194),
	FAL2_PHOTO_EYE_2		(195),
	FAL2_PHOTO_EYE_3		(196),
	FAL2_PHOTO_EYE_1_SECONDARY	(202),
	FAL2_PHOTO_EYE_2_SECONDARY	(203),
	FAL2_PHOTO_EYE_3_SECONDARY	(204),
	ER14_UNABLE_2_LOAD_HY5B_ELD	(205),
	ER14_UNABLE_2_LOAD_HY5B_IOLD	(206),
	ER14_UNABLE_2_LOAD_HY5B_OOLD	(207),
	ER14_UNABLE_2_LOAD_HY5B_SLD	(208),
	AL26_LIMIT_NOT_REACHED		(213),
	AL27_ACCUMULATOR_PRESSURE_LOW	(215),
	AL28_DISCONNECTED_BATTERY	(217);

	/** Code value */
	public final int value;

	/** Create a fault code */
	private FaultCode(int v) {
		value = v;
	}

	/** Lookup a fault code from value */
	static public FaultCode fromValue(int v) {
		for (FaultCode fc: FaultCode.values()) {
			if (fc.value == v)
				return fc;
		}
		return null;
	}
}
