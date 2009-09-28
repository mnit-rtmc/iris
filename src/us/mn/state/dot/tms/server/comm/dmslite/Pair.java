/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

package us.mn.state.dot.tms.server.comm.dmslite;

import java.io.IOException;

/**
 * Pair class. A Pair Comparable is a LISP like pair, with a car and cdr.
 * @author      Michael Darter
 * @company     AHMCT
 * @created     03/12/08
 */
public class Pair
{
	// fields
	private final Comparable m_car;
	private final Comparable m_cdr;

	/**
	 * Constructor.
	 */
	Pair(Comparable car, Comparable cdr) {
		m_car = car;
		m_cdr = cdr;
	}

	/** return car */
	public Comparable car() {
		return m_car;
	}

	/** return cdr */
	public Comparable cdr() {
		return m_cdr;
	}

	/**
	 *  find a Pair in a list by comparing the car of each list 
	 *  element with the argument.
	 *  @returns the first item in the list that matches else null 
	 *	     if not found.
	 *  @throws IOException if the specified car is not found.
	 */
	public static Pair findCar(Pair[] list, Comparable argcar)
		throws IOException {
		for(Pair i : list)
			if(i.car().compareTo(argcar) == 0) {
				return i;
			}
		throw new IOException("Pair.findCar(" + argcar.toString()
			+ ") not found.");
	}

	/** test this class */
	public static boolean test() {
		boolean ok = true;

		ok = ok && ((Integer) (new Pair(
			new Integer(1), new Double(3.25)).car())).compareTo(
				new Integer(1)) == 0;
		ok = ok && ((Integer) (new Pair(new Integer(1), new Double(
			3.25)).car())).compareTo(new Integer(2)) < 0;
		ok = ok && ((Integer) (new Pair(
			new Integer(1), new Double(3.25)).car())).compareTo(
				new Integer(-3)) > 0;
		ok = ok && ((Double) (new Pair(
			new Integer(1), new Double(3.25)).cdr())).compareTo(
				new Double(3.25)) == 0;
		ok = ok && ((Double) (new Pair(new Integer(1),
			new Double(3.25)).cdr())).compareTo(new Double(4)) < 0;
		ok = ok && ((Double) (new Pair(
			new Integer(1), new Double(3.25)).cdr())).compareTo(
				new Double(.33)) > 0;

		// test findCar()
		{
			Pair[] p = new Pair[2];

			p[0] = new Pair("a", "aa");
			p[1] = new Pair("b", "bb");

			try {
				ok = ok && (Pair.findCar(p,
					"b").cdr().compareTo("bb") == 0);
			} catch (IOException ex) {
				ok = false;
			}

			try {
				ok = ok && (Pair.findCar(p, "bx") == null);
				ok = false;
			} catch (IOException ex) {
				ok = ok && true;
			}
		}
		return ok;
	}
}
