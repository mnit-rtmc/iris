/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2002  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.comm.ntcip;

/**
 * Ntcip FontTable node
 *
 * @author Douglas Lau
 */
abstract class FontTable extends FontDefinition {

	/** Font index */
	protected final int font;

	/** Create a new FontTable item
	 * @param f Index in the font table */
	protected FontTable(int f) {
		super(4);
		font = f;
		oid[node++] = 2;
		oid[node++] = 1;
		oid[node++] = getTableItem();
		oid[node] = font;
	}

	/** Create an object description */
	public final String toString() {
		return getName() + "." + font + ": " + getValue();
	}

	/** Get the font table item (defined in subclasses) */
	abstract protected int getTableItem();
}
