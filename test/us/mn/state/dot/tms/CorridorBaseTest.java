/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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

import junit.framework.TestCase;
import us.mn.state.dot.tms.CorridorBase;

/** 
 * CorridorBase test cases
 *
 * @author Doug Lau
 */
public class CorridorBaseTest extends TestCase {
	public CorridorBaseTest(String name) {
		super(name);
	}
	public void test() {
		for(int m = 0; m < 10000; m++) {
			float miles = m + m / 10000f;
			float ep = CorridorBase.calculateEpsilon(miles);
			float m2 = miles + ep;
			assertTrue("miles == " + miles + ", m2 == " + m2,
				miles != m2);
		}
	}
}
