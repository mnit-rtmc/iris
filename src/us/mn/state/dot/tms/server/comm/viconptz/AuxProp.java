/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.viconptz;

import java.io.IOException;

/**
 * Vicon property to control aux.
 *
 * @author Douglas Lau
 */
public class AuxProp extends ViconPTZProp {

	/** Aux number */
	private final int aux;

	/** Create a new aux property */
	public AuxProp(int d, int a) throws IOException {
		super(d);
		aux = a;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "aux: " + aux;
	}

	/** Get the aux bits */
	@Override
	protected byte auxBits() {
		return (byte) ((1 << (7 - aux)) & 0x7E);
	}
}
