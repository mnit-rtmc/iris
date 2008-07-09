/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.rmi.RemoteException;

/**
 * 170 Controller
 *
 * @author Douglas Lau
 */
public interface Controller170 extends Controller {

	/** Minutes before communication is failed */
	public int COMM_FAIL_THRESHOLD = 3;

	/** I/O pin for first traffic device */
	int DEVICE_1_PIN = 2;

	/** I/O pin for second ramp meter */
	int METER_2_PIN = 3;

	/** Total number of detector inputs on a 170 controller */
	public int DETECTOR_INPUTS = 24;

	/** I/O pin offset for 170 controller detector inputs */
	public int DETECTOR_PIN_OFFSET = 38;

	/** Reserved cabinet type string */
	public String RESERVED = "RESERVED";

	/** Cabinet type names */
	public String CABINET[] = {
		"336", "334Z", "334D", "334Z-94", "DRUM", "334DZ", "334",
		"334Z-99", RESERVED, "S334Z", "PREHISTORIC", "334Z-00",
		RESERVED, "334Z-05", RESERVED, "334ZP"
	};

	/** 336 style cabinet type */
	public int CAB_336 = 0;

	/** 334Z style cabinet type */
	public int CAB_334Z = 1;

	/** 334D style cabinet type */
	public int CAB_334D = 2;

	/** 334Z-94 style cabinet type */
	public int CAB_334Z_94 = 3;

	/** Drum style cabinet type */
	public int CAB_DRUM = 4;

	/** 334DZ style cabinet type (modified 334D) */
	public int CAB_334DZ = 5;

	/** 334 style cabinet type */
	public int CAB_334 = 6;

	/** 334Z-99 style cabinet type */
	public int CAB_334Z_99 = 7;

	/** S334Z style cabinet type */
	public int CAB_S334Z = 9;

	/** Prehistoric style cabinet type */
	public int CAB_PREHISTORIC = 10;

	/** 334Z-00 style cabinet type */
	public int CAB_334Z_00 = 11;

	/** 334Z-05 style cabinet type */
	public int CAB_334Z_05 = 13;

	/** 334ZP style cabinet type (modified 334Z) */
	public int CAB_334ZP = 15;

	/** Set the cabinet type */
	public void setCabinet( short cabinet ) throws TMSException,
		RemoteException;

	/** Get the cabinet type */
	public short getCabinet() throws RemoteException;
}
