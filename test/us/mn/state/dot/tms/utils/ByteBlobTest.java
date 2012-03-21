/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
 * Copyright (C) 2012  Iteris Inc.
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

import junit.framework.TestCase;

/** 
 * Test cases for ByteBlob
 * @author Michael Darter
 */
public class ByteBlobTest extends TestCase {

	/** constructor */
	public ByteBlobTest(String name) {
		super(name);
	}

	/** test methods */
	static public void test() {
		final byte[] nullarg = null;
		final byte[] empty = new byte[0];
		final byte[] three = new byte[] {1, 2, 3};
		final byte[] threezero = new byte[] {1, 2, 3, 0, 0};
		final byte[] four = new byte[] {1, 2, 3, 4};
		final byte[] zzz = new byte[] {0, 0, 0};
		final byte[] five = new byte[] {1, 2, 3, 4, 5};
		final String nullString = null;
		final byte[] nullbyte = null;

		// 1 arg constructor
		{
			assertTrue(new ByteBlob(nullarg).equals(empty));
			assertTrue(1 == (new ByteBlob(new byte[] {1}).size()));
			assertTrue(new ByteBlob(new byte[] {1}).
				equals(new byte[]{1}));
		}

		// 2 arg constructor
		{
			// crazy args
			assertTrue(new ByteBlob(-1, null).isEmpty());
			assertTrue(new ByteBlob(-1, new byte[] {1}).isEmpty());
			// less than in array
			assertTrue(new ByteBlob(0, new byte[] {1}).isEmpty());
			assertTrue(new ByteBlob(3, five).equals(three));
			// more than in array
			assertTrue(new ByteBlob(5, three).equals(threezero));
		}

		// isEmpty
		assertFalse(new ByteBlob(five).isEmpty());
		assertTrue(new ByteBlob(empty).isEmpty());
		assertTrue(new ByteBlob(nullarg).isEmpty());
		
		// add
		assertTrue(new ByteBlob(four).add((byte)5).equals(five));
		assertTrue(new ByteBlob(four).add((int)5).equals(five));
		assertTrue(new ByteBlob(three).add(new byte[] {4, 5}).
			equals(five));

		// getInt
		assertTrue(new ByteBlob(five).getInt(-1) == 1);
		assertTrue(new ByteBlob(five).getInt(99) == 5);

		// getBits
		assertTrue(new ByteBlob(new byte[]{(byte)0xff}).getBits(0, 0) == 0);
		assertTrue(new ByteBlob(new byte[]{(byte)0xff}).getBits(0, 1) == 1);
		assertTrue(new ByteBlob(new byte[]{(byte)0xff}).getBits(0, 2) == 3);
		assertTrue(new ByteBlob(new byte[]{(byte)0xff}).getBits(0, 3) == 7);
		assertTrue(new ByteBlob(new byte[]{(byte)0xff}).getBits(0, 4) == 15);
		assertTrue(new ByteBlob(new byte[]{(byte)0xff}).getBits(0, 5) == 31);
		assertTrue(new ByteBlob(new byte[]{(byte)0xff}).getBits(0, 6) == 63);
		assertTrue(new ByteBlob(new byte[]{(byte)0xff}).getBits(0, 7) == 127);
		assertTrue(new ByteBlob(new byte[]{(byte)0xff}).getBits(0, 8) == 255); 
		assertTrue(new ByteBlob(new byte[]{(byte)0xff}).getBits(0, 9) == 255); 

		// clone
		assertTrue(new ByteBlob(five).clone().equals(five));
		assertTrue(new ByteBlob(empty).clone().equals(empty));

		// unsigned
		assertTrue(ByteBlob.unsigned((byte)6) == (int)6);
		assertTrue(ByteBlob.unsigned((byte)200) == (int)200);

		// calcIntChecksum
		assertTrue(828 == new ByteBlob(new byte[] {100, 101, 
			102, 103, 104, 105, 106, 107}).calcIntChecksum());

		// calcByteChecksum
		{
			byte[] p = new ByteBlob(new byte[] {100, 101, 102, 
				103, 104, 105, 106, 107}).calcByteChecksum();
			assertTrue(828 == p[0] * 256 + p[1]);

			byte[] x = new ByteBlob(new byte[] {(byte)57, 
				(byte)240, (byte)240, (byte)240, (byte)240, 
				(byte)240, (byte)240, (byte)240, (byte)0, 
				(byte)57, (byte)64}).calcByteChecksum();
			assertTrue(1858 == x[0] * 256 + x[1]);
		}

		// calcOneByteChecksum
		assertTrue(66 == new ByteBlob(new byte[] {(byte)57, 
				(byte)240, (byte)240, (byte)240, (byte)240, 
				(byte)240, (byte)240, (byte)240, (byte)0, 
				(byte)57, (byte)64}).calcOneByteChecksum());

		// test unsigned byte ability
		{
			ByteBlob bb2 = new ByteBlob(new byte[] {(byte)200, 
				(byte)201, (byte)202, (byte)203});
			assertTrue(200 == bb2.getInt(0));
			assertTrue(-56 == bb2.getByte(0));
		}

		// validate methods
		{
			ByteBlob bb2 = new ByteBlob(new byte[] {(byte)200, 
				(byte)201, (byte)202, (byte)203});
			assertTrue(!bb2.validateByte(0, (byte)10));
			assertTrue(bb2.validateByte(0, (byte)200));
			assertTrue(!bb2.validateByte(0, new byte[]
				{(byte)10, (byte)11, (byte)12}));
			assertTrue(bb2.validateByte(0, new byte[] 
				{(byte)10, (byte)11, (byte)200}));
		}

		// getTwoByteValue
		{
			ByteBlob bb2 = new ByteBlob(new byte[] {(byte)200, 
				(byte)201, (byte)202, (byte)100, (byte)101});
			assertTrue(25701 == bb2.getTwoByteValue(3));
			assertTrue(51401 == bb2.getTwoByteValue(0));
		}

		// getByteArray, getByteBlob
		{
			// crazy 1st arg
			assertTrue(new ByteBlob(five).getByteBlob(-3, 1).
				equals(new byte[] {1}));
			assertTrue(new ByteBlob(five).getByteBlob(55, 1).
				equals(empty));
			// crazy 2nd arg
			assertTrue(new ByteBlob(five).getByteBlob(0, 55).
				equals(five));
			assertTrue(new ByteBlob(five).getByteBlob(0, 0).
				equals(empty));
			// valid args
			assertTrue(new ByteBlob(five).getByteBlob(0, -1).
				equals(five));
			assertTrue(new ByteBlob(five).getByteBlob(0, 1).
				equals(new byte[] {1}));
			assertTrue(new ByteBlob(five).getByteBlob(0, 5).
				equals(five));
			assertTrue(new ByteBlob(five).getByteBlob(0, 3).
				equals(three));
			assertTrue(new ByteBlob(five).getByteBlob(2, 5).
				equals(new byte[] {3, 4, 5}));
			assertTrue(new ByteBlob(five).getByteBlob(4, 5).
				equals(new byte[] {5}));
		}

		// search at specified position
		{
			ByteBlob bb1 = new ByteBlob(new byte[] {(byte)100, 
				(byte)200, (byte)300, (byte)100, (byte)101});
			assertTrue(bb1.search(new byte[] {(byte)100}, 0));
			assertTrue(bb1.search(new byte[] {(byte)101}, 4));
			assertTrue(bb1.search(new byte[] {(byte)100, 
				(byte)200}, 0));
			assertTrue(bb1.search(new byte[] {(byte)100, 
				(byte)101}, 3));
			assertTrue(bb1.search(new byte[] {(byte)100, 
				(byte)200, (byte)300}, 0));
			assertTrue(bb1.search(new byte[] {(byte)100, 
				(byte)200, (byte)300}, 0));
			assertTrue(bb1.search(new byte[] {}, 0));
			assertTrue(bb1.search(null, 5));
			assertTrue(!bb1.search(new byte[] {(byte)101}, 34));
			assertTrue(!bb1.search(new byte[] {(byte)101}, 5));
			assertTrue(!bb1.search(new byte[] 
				{100, 101, 102, 103}, 3));
		}

		// search for leader
		{
			ByteBlob bb1 = new ByteBlob(new byte[] {(byte)100, 
				(byte)200, (byte)300, (byte)100, (byte)101});
			assertTrue(0 == bb1.search(new byte[] {(byte)100}));
			assertTrue(-1 == bb1.search(new byte[] {(byte)10}));
			assertTrue(3 == bb1.search(new byte[] {(byte)100, 
				(byte)101}));
			assertTrue(0 == bb1.search(new byte[] {(byte)100, 
				(byte)200, (byte)300, (byte)100, (byte)101}));
			assertTrue(-1 == bb1.search(new byte[] {}));
			assertTrue(-1 == bb1.search(null));
		}

		// startsWith: string arg
		assertTrue(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			startsWith(nullString));
		assertTrue(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			startsWith(""));
		assertTrue(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			startsWith("abc"));
		assertFalse(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			startsWith("abcdef"));
		assertFalse(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			startsWith("xabcdef"));
		assertFalse(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			startsWith("bcde"));
		// startsWith: byte arg
		assertTrue(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			startsWith(nullbyte));
		assertTrue(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			startsWith(new byte[] {97, 98, 99}));

		// startsWithIgnoreLeadCrLf
		assertTrue(new ByteBlob(new byte[] {(byte)13, 
			(byte)10, (byte)99, (byte)100, (byte)101}).
			startsWithIgnoreLeadCrLf(null));
		assertTrue(new ByteBlob(new byte[] {(byte)13, 
			(byte)10, (byte)99, (byte)100, (byte)101}).
			startsWithIgnoreLeadCrLf(""));
		assertTrue(new ByteBlob(new byte[] {(byte)13, 
			(byte)10, (byte)99, (byte)100, (byte)101}).
			startsWithIgnoreLeadCrLf("cd"));
		assertTrue(new ByteBlob(new byte[] {(byte)13, 
			(byte)10, (byte)99, (byte)100, (byte)101}).
			startsWithIgnoreLeadCrLf("cd"));

		// endsWith
		assertTrue(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			endsWith(null));
		assertTrue(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			endsWith(""));
		assertTrue(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			endsWith("de"));
		assertFalse(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			endsWith("xef"));
		assertFalse(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			endsWith("abc"));

		// contains
		assertFalse(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			contains(null));
		assertFalse(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			contains(""));
		assertTrue(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			contains("bcde"));
		assertFalse(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			contains("bcx"));
		assertFalse(new ByteBlob(new byte[] {(byte)97, 
			(byte)98, (byte)99, (byte)100, (byte)101}).
			contains("xxx"));

		// size
		assertTrue(new ByteBlob(five).size() == 5);
		assertTrue(new ByteBlob(five).size(-1) == 5);
		assertTrue(new ByteBlob(five).size(0) == 5);
		assertTrue(new ByteBlob(five).size(4) == 1);
		assertTrue(new ByteBlob(five).size(5) == 0);
		assertTrue(new ByteBlob(five).size(9) == 0);

		// setSize
		assertTrue(new ByteBlob(nullbyte).setSize(3).equals(zzz));
		assertTrue(new ByteBlob(five).setSize(-1).isEmpty());
		assertTrue(new ByteBlob(five).setSize(0).isEmpty());
		assertTrue(new ByteBlob(five).setSize(3).equals(three));
		assertTrue(new ByteBlob(three).setSize(5).equals(threezero));

		// getFourByteValue
		assertTrue(new ByteBlob(five).getFourByteValue(0) == 16909060);
	}
}
