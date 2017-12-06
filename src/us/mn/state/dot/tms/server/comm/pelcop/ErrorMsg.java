/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelcop;

/**
 * Error messages to display on keyboard.
 *
 * @author Douglas Lau
 */
public enum ErrorMsg {
	NoTieLines(7),
	MonitorDenied(10),
	MonNotPresent(11),
	CameraDenied(12),
	CamNotPresent(13),
	GPIDenied(14),
	GPINotPresent(15),
	NoMacroSpace(16),
	MacNotPresent(17),
	AlmNotPresent(18),
	AlarmDenied(19),
	MacroDefined(39),
	LockDenied(40),
	UnlockDenied(41),
	OverrideDenied(42),
	NodeOffline(43),
	MonNodeOffline(44),
	CamNodeOffline(45),
	NIUOffline(46),
	NoAltCam(47),
	PrtyLockOpr(48),
	LinkCamOffline(49),
	COVOffline(57);

	/** Error message code */
	public final int code;

	/** Create an error message */
	private ErrorMsg(int e) {
		code = e;
	}
}
