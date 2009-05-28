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
package us.mn.state.dot.tms.server.comm.ntcip;

/**
 * Ntcip CharacterTable object
 *
 * @author Douglas Lau
 */
abstract class CharacterTable extends FontDefinition {

	/** Font index */
	protected final int font;

	/** Character index */
	protected final int index;

	/** Create a new CharacterTable item
	 * @param f font index
	 * @param i Index in the character table */
	protected CharacterTable(int f, int i) {
		super(5);
		font = f;
		index = i;
		oid[node++] = 4;
		oid[node++] = 1;
		oid[node++] = getTableItem();
		oid[node++] = font;
		oid[node++] = index;
	}

	/** Create an object description */
	public final String toString() {
		return getName() + "." + font + "." + index + ": " + getValue();
	}

	/** Get the font table item (defined in subclasses) */
	abstract protected int getTableItem();
}
