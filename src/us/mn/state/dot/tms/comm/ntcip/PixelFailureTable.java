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
package us.mn.state.dot.tms.comm.ntcip;

/**
 * Ntcip PixelFailureTable node
 *
 * @author Douglas Lau
 */
abstract class PixelFailureTable extends StatError {

	/** Row index */
	protected final int row;

	/** Create a new PixelFailureTable item
	 * @param r Row in the pixel failure table */
	protected PixelFailureTable(int r) {
		super(5);
		row = r;
		oid[node++] = 3;
		oid[node++] = 1;
		oid[node++] = getTableItem();
		oid[node++] = 2;	// what does 2 indicate here?
		oid[node] = row;
	}

	/** Create an object description */
	public final String toString() {
		return getName() + ".2." + row + ": " + getValue();
	}

	/** Get the pixel failure table item (defined in subclasses) */
	abstract protected int getTableItem();
}
