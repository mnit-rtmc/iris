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
 * Ntcip GlobalModuleTable node
 *
 * @author Douglas Lau
 */
abstract class GlobalModuleTable extends GlobalConfiguration {

	/** Module index */
	protected final int index;

	/** Create a new GlobalModuleTable item
	  * @param i Index in the module table */
	protected GlobalModuleTable(int i) {
		super(4);
		index = i;
		oid[node++] = 3;
		oid[node++] = 1;
		oid[node++] = getTableItem();
		oid[node++] = index;
	}

	/** Create an object description */
	public final String toString() {
		return getName() + "." + index + ": " + getValue();
	}

	/** Get the font table item (defined in subclasses) */
	abstract protected int getTableItem();
}
