/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.net.InetAddress;
import junit.framework.TestCase;

/** CIDRAddress tests
 *
 * @author Douglas Lau
 */
public class CIDRAddressTest extends TestCase {

	static private InetAddress addr(String a) throws Exception {
		return InetAddress.getByName(a);
	}

	public CIDRAddressTest(String name) {
		super(name);
	}

	public void test1() {
		try {
			CIDRAddress a = new CIDRAddress("192.168.1.0/24");
			assertTrue(a.matches(addr("192.168.1.1")));
			assertTrue(a.matches(addr("192.168.1.254")));
			assertFalse(a.matches(addr("192.168.2.1")));
			assertFalse(a.matches(addr("10.1.1.1")));
		}
		catch(Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void test2() {
		try {
			CIDRAddress a = new CIDRAddress("192.168.0.0/20");
			assertTrue(a.matches(addr("192.168.1.1")));
			assertTrue(a.matches(addr("192.168.1.254")));
			assertTrue(a.matches(addr("192.168.2.1")));
			assertTrue(a.matches(addr("192.168.15.254")));
			assertFalse(a.matches(addr("192.168.16.1")));
			assertFalse(a.matches(addr("10.1.1.1")));
		}
		catch(Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void test3() {
		try {
			CIDRAddress a = new CIDRAddress("192.168.1.1/32");
			assertTrue(a.matches(addr("192.168.1.1")));
			assertFalse(a.matches(addr("192.168.1.254")));
			assertFalse(a.matches(addr("192.168.2.1")));
			assertFalse(a.matches(addr("192.168.16.1")));
			assertFalse(a.matches(addr("10.1.1.1")));
		}
		catch(Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void test4() {
		try {
			CIDRAddress a = new CIDRAddress(
				"1080:0:0:0:8:800:200C:417A/112");
			assertTrue(a.matches(addr(
				"1080:0:0:0:8:800:200C:4280")));
			assertTrue(a.matches(addr(
				"1080:0:0:0:8:800:200C:EEEE")));
			assertFalse(a.matches(addr(
				"1080:0:0:0:8:800:200B:EEEE")));
			assertFalse(a.matches(addr("192.168.1.1")));
			assertFalse(a.matches(addr("192.168.1.254")));
			assertFalse(a.matches(addr("192.168.2.1")));
			assertFalse(a.matches(addr("192.168.16.1")));
			assertFalse(a.matches(addr("10.1.1.1")));
		}
		catch(Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
