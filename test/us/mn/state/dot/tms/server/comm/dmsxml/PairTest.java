/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.dmsxml;

import junit.framework.TestCase;
import java.io.IOException;
import java.util.Arrays;

/**
 * Pair test cases
 * @author Michael Darter
 */
public class PairTest extends TestCase {

	/** constructor */
	public PairTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {

		boolean ok = true;

		assertTrue(((Integer)(new Pair(new Integer(1),
			new Double(3.25)).car())).compareTo(
			new Integer(1)) == 0);
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
		assertTrue(ok);

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
		assertTrue(ok);
	}
}
