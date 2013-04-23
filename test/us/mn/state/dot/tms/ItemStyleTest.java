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
package us.mn.state.dot.tms;

import java.util.Arrays;
import junit.framework.TestCase;

/**
 * ItemStyle unit tests.
 *
 * @author Doug Lau
 */
public class ItemStyleTest extends TestCase {

	public ItemStyleTest(String name) {
		super(name);
	}

	public void test() {
		long bits = ItemStyle.toBits(ItemStyle.ACTIVE, ItemStyle.ALL);
		assertTrue(Arrays.equals(ItemStyle.toStyles(bits),
			new ItemStyle[] { ItemStyle.ALL, ItemStyle.ACTIVE }));
		bits = ItemStyle.toBits(ItemStyle.DEPLOYED,ItemStyle.AVAILABLE);
		assertTrue(Arrays.equals(ItemStyle.toStyles(bits),
			new ItemStyle[] { ItemStyle.AVAILABLE,
			ItemStyle.DEPLOYED }));
	}
}
