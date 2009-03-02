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
package us.mn.state.dot.tms.comm.ntcip;

/**
 * Ntcip FanTestActivation object. This object has been deprecated by
 * NTCIP 1203 v2.
 *
 * @author Douglas Lau
 */
public class FanTestActivation extends StatError implements ASN1Integer {

	/** Undefined code */
	static public final int UNDEFINED = 0;

	/** Other (useless) code */
	static public final int OTHER = 1;

	/** No fan test active */
	static public final int NO_TEST = 2;

	/** Activate fan test / test in progress */
	static public final int TEST = 3;

	/** Fan test activation descriptions */
	static protected final String ACTIVATION[] = {
		"???", "Other", "No test", "Test"
	};

	/** Create a new FanTestActivation object */
	public FanTestActivation() {
		super(2);
		oid[node++] = 9;
		oid[node++] = 0;
		activation = TEST;
	}

	/** Get the object name */
	protected String getName() {
		return "fanTestActivation";
	}

	/** Fan test activation */
	protected int activation;

	/** Set the integer value */
	public void setInteger(int value) {
		if(value < 0 || value >= ACTIVATION.length)
			value = UNDEFINED;
		activation = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return activation;
	}

	/** Get the object value */
	public String getValue() {
		return ACTIVATION[activation];
	}
}
