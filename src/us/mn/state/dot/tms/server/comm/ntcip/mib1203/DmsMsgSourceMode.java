/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Integer;

/**
 * Ntcip DmsMsgSourceMode object
 *
 * @author Douglas Lau
 */
public class DmsMsgSourceMode extends SignControl implements ASN1Integer {

	/** Message source codes */
	static public final int UNDEFINED = 0;
	static public final int OTHER = 1;
	static public final int LOCAL = 2;
	static public final int EXTERNAL = 3;
	static public final int OTHER_COM1 = 4;
	static public final int OTHER_COM2 = 5;
	static public final int OTHER_COM3 = 6;
	static public final int OTHER_COM4 = 7;
	static public final int CENTRAL = 8;
	static public final int TIMEBASED_SCHEDULER = 9;
	static public final int POWER_RECOVER8Y = 10;
	static public final int RESET = 11;
	static public final int COMM_LOSS = 12;
	static public final int POWER_LOSS = 13;
	static public final int END_DURATION = 14;

	/** Source descriptions */
	static protected final String[] SOURCE = {
		"???", "other", "local", "external", "otherCom1", "otherCom2",
		"otherCom3", "otherCom4", "central", "timebasedScheduler",
		"powerRecovery", "reset", "commLoss", "powerLoss", "endDuration"
	};

	/** Create a new DmsMsgSourceMode object */
	public DmsMsgSourceMode() {
		super(7);
	}

	/** Get the object name */
	protected String getName() {
		return "dmsMsgSourceMode";
	}

	/** Actual message source */
	protected int source;

	/** Set the integer value */
	public void setInteger(int value) {
		if(value < 0 || value >= SOURCE.length)
			source = UNDEFINED;
		else
			source = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return source;
	}

	/** Get the object value */
	public String getValue() {
		return SOURCE[source];
	}
}
