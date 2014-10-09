/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.manchester;

/**
 * A property to control camera aux.
 *
 * @author Douglas Lau
 */
public class AuxProperty extends ManchesterProperty {

	/** Requested aux number (1-6) */
	private final int aux;

	/** Create a new aux property */
	public AuxProperty(int a) {
		aux = a;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "aux: " + aux;
	}

	/** Get command bits */
	@Override
	protected byte commandBits() {
		switch (aux) {
		case 1:  return EX_AUX_1;
		case 2:  return EX_AUX_2;
		case 3:  return EX_AUX_3;
		case 4:  return EX_AUX_4;
		case 5:  return EX_AUX_5;
		default: return EX_AUX_6;
		}
	}

	/** Check if packet is extended function */
	@Override
	protected boolean isExtended() {
		return true;
	}
}
