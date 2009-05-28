/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2009  Minnesota Department of Transportation
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
 * Ntcip PixelTestActivation object
 *
 * @author Douglas Lau
 */
public class PixelTestActivation extends StatError implements ASN1Integer {

	/** Undefined code */
	static public final int UNDEFINED = 0;

	/** Other (useless) code */
	static public final int OTHER = 1;

	/** No pixel test active */
	static public final int NO_TEST = 2;

	/** Activate pixel test / test in progress */
	static public final int TEST = 3;

	/** Code to clear the pixel error table */
	static public final int CLEAR_TABLE = 4;

	/** Pixel test activation descriptions */
	static protected final String ACTIVATION[] = {
		"???", "Other", "No test", "Test", "Clear Table"
	};

	/** Create a new PixelTestActivation object */
	public PixelTestActivation() {
		super(2);
		oid[node++] = 4;
		oid[node++] = 0;
		activation = TEST;
	}

	/** Get the object name */
	protected String getName() {
		return "pixelTestActivation";
	}

	/** Pixel test activation */
	protected int activation;

	/** Set the integer value */
	public void setInteger(int value) {
		if(value < 0 || value >= ACTIVATION.length)
			activation = UNDEFINED;
		else
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
